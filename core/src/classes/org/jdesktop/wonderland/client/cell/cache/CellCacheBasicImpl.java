/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.client.cell.cache;

import org.jdesktop.wonderland.client.connections.CellCacheConnection;
import com.jme.bounding.BoundingVolume;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.connections.CellChannelConnection;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.CellStatistics;
import org.jdesktop.wonderland.client.cell.CellStatistics.TimeCellStat;
import org.jdesktop.wonderland.client.cell.EnvironmentCell;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.cell.TransformChangeListener.ChangeSource;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.connections.CellCacheMessageListener;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * A basic implementation of core cell cache features. This is a convenience
 * class designed to be called from more complete cache implementations.
 *
 * @author paulby
 */
public class CellCacheBasicImpl implements CellCache, CellCacheMessageListener {

    private static final String STATUS_MANAGER_PROP =
            CellCacheBasicImpl.class.getSimpleName() + ".StatusManager";
    private static final String STATUS_MANAGER_DEFAULT =
            ParallelCellStatusManager.class.getName();
    private Map<CellID, Cell> cells = Collections.synchronizedMap(new HashMap());
    private Set<Cell> rootCells = Collections.synchronizedSet(new HashSet());
    protected static Logger logger = Logger.getLogger(CellCacheBasicImpl.class.getName());
    ViewCell viewCell = null;
    /**
     * the classloader to use when instantiating classes
     */
    ClassLoader classLoader;
    /**
     * the session this cache is associated with
     */
    WonderlandSession session;
    /**
     * the connection for sending cell cache information
     */
    CellCacheConnection cellCacheConnection;
    /**
     * the connection for sending cell information
     */
    CellChannelConnection cellChannelConnection;
    /**
     * listeners
     */
    private final Set<CellCacheListener> listeners =
            new CopyOnWriteArraySet<CellCacheListener>();
    /**
     * statistics
     */
    private final CellStatistics stats = new CellStatistics();
    /**
     * status manager
     */
    final CellStatusManager statusManager;
    /**
     * cell being processed on the current thread
     */
    private static final ThreadLocal<Cell> currentCell = new ThreadLocal<Cell>();
    private boolean cellsAreStillLoading = true;

    /**
     * Create a new cache implementation
     *
     * @param session the WonderlandSession the cache is associated with
     * @param cellCacheConnection the connection for sending cell cache
     * information
     * @param cellChannelConnection the connectiong for sending cell channel
     * messages
     */
    public CellCacheBasicImpl(WonderlandSession session,
            ClassLoader classLoader,
            CellCacheConnection cellCacheConnection,
            CellChannelConnection cellChannelConnection) {
        this.session = session;
        this.classLoader = classLoader;
        this.cellCacheConnection = cellCacheConnection;

        this.cellChannelConnection = cellChannelConnection;
        this.cellChannelConnection.setCellCache(this);

        this.statusManager = createStatusManager();

        ClientContext.registerCellCache(this, session);
        if (this.classLoader == null) {
            this.classLoader = getClass().getClassLoader();
        }

        logger.warning("Create cell cache");
    }

    public void eventObserved(String property, Object value) {
    
        
        if(property.equals("load-cell")) {
            CellHierarchyMessage msg = (CellHierarchyMessage)value;
            loadCell(msg.getCellID(),
                     msg.getCellClassName(),
                     msg.getLocalBounds(),
                     msg.getParentID(),
                     msg.getCellTransform(),
                     msg.getSetupData(),
                     msg.getCellName());
        } else if(property.equals("configure-cell")) {
            CellHierarchyMessage msg = (CellHierarchyMessage)value;
            configureCell(msg.getCellID(),
                          msg.getSetupData(),
                          msg.getCellName());
        } else if(property.equals("unload-cell")) {
            CellHierarchyMessage msg = (CellHierarchyMessage)value;
            unloadCell(msg.getCellID());
            
        } else if(property.equals("delete-cell")) {
            CellHierarchyMessage msg = (CellHierarchyMessage)value;
            deleteCell(msg.getCellID());
        } else if(property.equals("loading-finished")) {
            
            cellsAreStillLoading = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Cell getCell(CellID cellId) {
        return cells.get(cellId);
    }

    /**
     * {@inheritDoc}
     */
    public EnvironmentCell getEnvironmentCell() {
        return (EnvironmentCell) getCell(CellID.getEnvironmentCellID());
    }

    /**
     * Return the set of cells in this cache
     *
     * @return
     */
    public Cell[] getCells() {
        return (Cell[]) cells.values().toArray(new Cell[cells.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public Cell loadCell(CellID cellId,
            String className,
            BoundingVolume localBounds,
            CellID parentCellID,
            CellTransform cellTransform,
            CellClientState setup,
            String cellName) {

        long startTime = System.currentTimeMillis();

        try {
            if (cells.containsKey(cellId)) {
                logger.severe("Attempt to create cell that already exists " + cellId);
                return null;
            }

            logger.info("creating cell " + className + " " + cellId);
            Cell cell = instantiateCell(className, cellId);
            if (cell == null) {
                return null;     // Instantiation failed, error has already been logged
            }
            // cell we are currently working on. Be sure to reset in the
            // finally block below
            currentCell.set(cell);

            cell.setName(cellName);
            Cell parent = cells.get(parentCellID);
            if (parent != null) {
                try {
                    parent.addChild(cell);
                } catch (MultipleParentException ex) {
                    logger.log(Level.SEVERE, "Failed to load cell", ex);
                }
            } else if (parentCellID != null) {
                logger.warning("Failed to find parent " + parentCellID
                        + " for child " + cellId);
            }

            cell.setLocalBounds(localBounds);
            cell.setLocalTransform(cellTransform, TransformChangeListener.ChangeSource.SERVER_ADJUST);
            //        System.out.println("Loading Cell "+className+" "+cellTransform.getTranslation(null));

            cells.put(cellId, cell);

            // record the set of root cells
            logger.fine("LOADING CELL " + cell.getName());
            if (parent == null && !cellId.equals(CellID.getEnvironmentCellID())) {
                logger.fine("LOADING ROOT CELL " + cell.getName());
                rootCells.add(cell);
            }

            // TODO this will change, the state will applied when the cell
            // becomes ACTIVE
            if (setup != null) {
                cell.setClientState(setup);
            } else {
                logger.warning("Cell has null setup " + className + "  " + cell);
            }

            // Force the cell to create the JME renderer entity
            // Current assumption is that the cell is about to be VISIBLE, so we want the renderer asap
//            createCellRenderer(cell);

            // notify listeners
            fireCellLoaded(cell);

            if (viewCell != null) {
                // No point in makeing cells active if we don't have a view
                // The changeCellStatus actually changes the status on another thread
                // so we don't perform geometry load operations on the DS listener thread
                changeCellStatus(cell, CellStatus.VISIBLE);
            } else if (cell instanceof ViewCell
                    || cellId.equals(CellID.getEnvironmentCellID())) {
//                changeCellStatus(cell, CellStatus.ACTIVE);
                if(cell instanceof ViewCell) {
                    handleViewCellStatus(cell);
                } else {
                    changeCellStatus(cell, CellStatus.ACTIVE);
                }
            }

            // record loading time statistic
            long time = System.currentTimeMillis() - startTime;
            TimeCellStat loadStat =
                    new TimeCellStat("LoadTime", "Cell Load Time");
            loadStat.setValue(time);
            getStatistics().add(cell, loadStat);

            return cell;
        } catch (Exception e) {
            // notify listeners
            fireCellLoadFailed(cellId, className, parentCellID, e);
            logger.log(Level.SEVERE, "Failed to loadCell", e);
            return null;
        } finally {
            currentCell.remove();
        }
    }

    
    
    private void handleViewCellStatus(Cell cell) {
        //we want to try and load the view cell last...
        
        //...so let's wait until the other objects have started loading.
        
        logger.warning("WAITING FOR ENVIRONMENT TO LOAD BEFORE LOADING AVATAR.");
        while(cellsAreStillLoading) {
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(CellCacheBasicImpl.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                logger.warning("\n*******************************\n"
                            + "* ENVIRONMENT LOADING FINISHED*\n"
                            + "*******************************");
            }
        }
       
        //ok we're done, go ahead and load
        changeCellStatus(cell, CellStatus.ACTIVE);
        
    }
    /**
     * {@inheritDoc}
     */
    public void configureCell(CellID cellID, CellClientState clientState, String cellName) {
        // First fetch the cell from the cache given the unique ID. If there
        // is none, post a message to the log
        Cell cell = cells.get(cellID);
        if (cell == null) {
            logger.warning("Received a CONFIGURE_CELL message to a non-"
                    + "existent cell with id " + cellID);
            return;
        }

        try {
            // set the cell we are currently operating on
            currentCell.set(cell);

            // If the name of the cell has changed, then change it
            if (cellName != null && cellName.equals(cell.getName()) == false) {
                cell.setName(cellName);
            }

            // If there is a non-null client state object, then set it
            if (clientState != null) {
                cell.setClientState(clientState);
            }
        } finally {
            currentCell.remove();
        }
    }

    /**
     * Create a the cell renderer for this cache.
     *
     * @param cell the cell to create a renderer for
     */
    protected CellRenderer createCellRenderer(Cell cell) {
        try {
            return cell.getCellRenderer(ClientContext.getRendererType());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get Cell Renderer for cell " + cell.getClass().getName(), e);
        }
        return null;
    }

    /**
     * Unload the cell from memory, sets the Cells status to DISK
     *
     * @param cellId
     */
    public void unloadCell(CellID cellId) {
        Cell cell = cells.remove(cellId);
        if (cell != null) {
            logger.fine("UNLOADING CELL " + cell.getName());
            try {
                currentCell.set(cell);

                // notify listeners
                fireCellUnloaded(cell);

                // OWL issue #37: make sure to update the status on a separate
                // thread to prevent deadlocks waiting for update messages and
                // races with loading the cell
                changeCellStatus(cell, CellStatus.DISK);

                if (cell.getParent() == null) {
                    logger.fine("UNLOADING ROOT CELL " + cell.getName());
                    rootCells.remove(cell);
                } else {
                    cell.getParent().removeChild(cell);
                }
            } finally {
                currentCell.remove();
            }
        } else {
            logger.log(Level.WARNING, "Unloading unknown cell " + cellId);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteCell(CellID cellId) {
        // TODO - remove local resources from client asset cache as long
        // as they are not shared
        unloadCell(cellId);
    }

    /**
     * TODO - this is not used yet, we are reparenting by removing and adding
     * nodes as a test to ensure there are no threading issues
     *
     * {@inheritDoc}
     */
    public void changeParent(CellID cellID, CellID parentCellID, CellTransform cellTransform) {
        // Find the current parent of the Cell and remove the Cell. If the Cell
        // does not exist in the cache, then do nothing. It could perhaps be
        // created on the client at a later date.
        logger.warning("Changing the parent of Cell with ID " + cellID);
        logger.warning("The ID of the new parent " + parentCellID);
        logger.warning("The new transform is " + cellTransform.toString());

        Cell cell = cells.get(cellID);
        if (cell == null) {
            logger.warning("Unable to find Cell in Cache with ID " + cellID);
            return;
        }

        try {
            currentCell.set(cell);

            // First, remove the Cell from its parent, if the parent is not null. If
            // the parent is null, this means it is a "root" Cell and we need to
            // remove it from the list of root Cells.
            Cell parentCell = cell.getParent();
            if (parentCell != null) {
                // The removeChild() method will remove from the parent but also
                // null the parent reference in the child Cell.
                logger.warning("Removing the Cell from old parent " + parentCell.getCellID());
                parentCell.removeChild(cell);
            } else {
                logger.warning("Removing the Cell from the root");
                rootCells.remove(cell);
            }

            // Find the new parent Cell. If the parent Cell ID is -1 (which will
            // result in no parent Cell being found, it means we wish to add this
            // to the root.
            Cell newParentCell = cells.get(parentCellID);
            if (newParentCell != null) {
                try {
                    logger.warning("Adding the Cell to new parent " + newParentCell.getCellID());
                    newParentCell.addChild(cell);
                } catch (MultipleParentException excp) {
                    logger.log(Level.WARNING, "Multiple parents are set for Cell"
                            + " with ID " + cellID, excp);
                }
            } else if (!cellID.equals(CellID.getEnvironmentCellID())) {
                logger.warning("Adding the Cell to the root");
                rootCells.add(cell);
            }

            // Update the Cell transform with the new local transform
            cell.setLocalTransform(cellTransform, ChangeSource.REMOTE);
        } finally {
            currentCell.remove();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Cell> getRootCells() {
        return new LinkedList(rootCells);
    }

    private Cell instantiateCell(String className, CellID cellId) {
        Cell cell;

        try {
            Class clazz = Class.forName(className, true, classLoader);
            Constructor constructor = clazz.getConstructor(CellID.class, CellCache.class);
            cell = (Cell) constructor.newInstance(cellId, this);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem instantiating cell " + className, e);
            return null;
        }

        return cell;
    }

    /**
     * {@inheritDoc}
     */
    public void setViewCell(ViewCell viewCell) {
        this.viewCell = viewCell;

        // Activate all current cells
        synchronized (cells) {
            // issue #850 -- make sure to notify parents before their
            // children
            for (Cell cell : cells.values()) {
                if (cell.getParent() != null) {
                    // ignore child cells
                    continue;
                }

                // change the status of this parent and all its children
                changeCellTreeStatus(cell, CellStatus.VISIBLE);
            }
        }
    }

    /**
     * Notify a tree of cells to change their status
     *
     * @param cell the root cell to notify
     * @param status the status to set
     */
    private void changeCellTreeStatus(Cell root, CellStatus status) {
        if (root.getStatus().ordinal() < status.ordinal()) {
            changeCellStatus(root, status);
        }

        for (Cell child : root.getChildren()) {
            changeCellTreeStatus(child, status);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ViewCell getViewCell() {
        return viewCell;
    }

    /**
     * {@inheritDoc}
     */
    public WonderlandSession getSession() {
        return session;
    }

    public CellChannelConnection getCellChannelConnection() {
        return cellChannelConnection;
    }

    /**
     * {@inheritDoc}
     */
    public void addCellCacheListener(CellCacheListener listener) {
        listeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeCellCacheListener(CellCacheListener listener) {
        listeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    public CellStatistics getStatistics() {
        return stats;
    }

    /**
     * Notify listeners that a cell is loaded
     *
     * @param cell the cell that was loaded
     */
    protected void fireCellLoaded(Cell cell) {
        for (CellCacheListener listener : listeners) {
            listener.cellLoaded(cell.getCellID(), cell);
        }
    }

    /**
     * Notify listeners that a cell laod failed
     *
     * @param cellID the id of the cell that failed to load
     * @param className the class of the cell that failed to load
     * @param parentCellID the id of the cell's parent
     * @param cause the reason for failure
     */
    protected void fireCellLoadFailed(CellID cellID, String className,
            CellID parentCellID, Throwable cause) {
        for (CellCacheListener listener : listeners) {
            listener.cellLoadFailed(cellID, className, parentCellID, cause);
        }
    }

    /**
     * Notify listeners that a cell is unloaded
     *
     * @param cell the cell that was unloaded
     */
    protected void fireCellUnloaded(Cell cell) {
        for (CellCacheListener listener : listeners) {
            listener.cellUnloaded(cell.getCellID(), cell);
        }
    }

    /**
     * Cell status changes can take a while so should not be performed on the
     * Darkstar listener thread that calls the cache methods. Therefore schedule
     * a task actually apply the status change to the cell.
     *
     * @param cell
     * @param status
     */
    private void changeCellStatus(Cell cell, CellStatus status) {
        statusManager.setCellStatus(cell, status);
    }

    /**
     * Get the currently active cell for this thread. If no cell is active, this
     * method return null.
     *
     * @return the currently active cell for this thread, if any
     */
    public static Cell getCurrentActiveCell() {
        return currentCell.get();
    }

    /**
     * Create a cell status manager
     */
    private CellStatusManager createStatusManager() {
        String className = System.getProperty(STATUS_MANAGER_PROP,
                STATUS_MANAGER_DEFAULT);
        try {
            Class<CellStatusManager> clazz = (Class<CellStatusManager>) Class.forName(className);

            Constructor<CellStatusManager> ctor = clazz.getConstructor(CellCacheBasicImpl.class);
            return ctor.newInstance(this);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException(nsme);
        } catch (InstantiationException ie) {
            throw new RuntimeException(ie);
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        } catch (InvocationTargetException te) {
            throw new RuntimeException(te);
        }
    }

    public ThreadLocal<Cell> getCurrentCell() {
        return currentCell;
    }
}
