/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client.cell;

import com.jme.bounding.BoundingVolume;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.avatar.ViewCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;

/**
 *
 * @author paulby
 */
public class CellCacheBasicImpl implements CellCache, CellCacheConnection.CellCacheMessageListener {

    private Map<CellID, Cell> cells = Collections.synchronizedMap(new HashMap());
    private Set<Cell> rootCells = Collections.synchronizedSet(new HashSet());
    
    private static Logger logger = Logger.getLogger(CellCacheBasicImpl.class.getName());
    
    private ViewCell viewCell = null;

    
    /** the session this cache is associated with */
    private WonderlandSession session;
    
    /** the connection for sending cell cache information */
    private CellCacheConnection cellCacheConnection;
    
    /** the connection for sending cell information */
    private CellChannelConnection cellChannelConnection;
    
    /**
     * Create a new cache implementation
     * @param session the WonderlandSession the cache is associated with
     * @param cellCacheConnection the connection for sending cell cache
     * information
     * @param cellChannelConnection the connectiong for sending cell channel
     * messages
     */
    public CellCacheBasicImpl(WonderlandSession session,
                              CellCacheConnection cellCacheConnection,
                              CellChannelConnection cellChannelConnection)
    {
        this.session = session;
        this.cellCacheConnection = cellCacheConnection;
        this.cellChannelConnection = cellChannelConnection;
    }
    
    public Cell getCell(CellID cellId) {
        return cells.get(cellId);
    }
    
    /**
     * Return the set of cells in this cache
     * @return
     */
    public Cell[] getCells() {
        return (Cell[]) cells.values().toArray(new Cell[cells.size()]);
    }

    public void loadCell(CellID cellId, 
                         String className, 
                         BoundingVolume localBounds, 
                         CellID parentCellID, 
                         CellTransform cellTransform, 
                         CellSetup setup,
                         String cellName) {
        Cell cell = instantiateCell(className, cellId);
        if (cell==null)
            return;     // Instantiation failed, error has already been logged
        
        cell.setLocalBounds(localBounds);
        cell.setTransform(cellTransform);
        cell.setName(cellName);
        Cell parent = cells.get(parentCellID);
        System.out.println("Loading Cell "+className+" "+cellTransform.getTranslation(null));
        if (parent!=null) {
            try {
                parent.addChild(cell);
            } catch (MultipleParentException ex) {
                Logger.getLogger(CellCacheBasicImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            logger.warning("loadCell - Cell parent is null");
        }

        cells.put(cellId, cell);
        
        // record the set of root cells
        if (cell instanceof RootCell) {
            rootCells.add(cell);
        }
        
        if (setup!=null)
            cell.setupCell(setup);
        
        // if the cell has a channel, notify it of the CellChannelConnection
        if (cell instanceof ChannelCell) {
            ((ChannelCell) cell).setCellChannelConnection(cellChannelConnection);
        }
    }

    /**
     * Unload the cell from memory, sets the Cells status to DISK
     * @param cellId
     */
    public void unloadCell(CellID cellId) {
        Cell cell = cells.remove(cellId);
        cell.setStatus(CellStatus.DISK);
    }

    public void deleteCell(CellID cellId) {
        // TODO - remove local resources from client asset cache as long
        // as they are not shared
        Cell cell = cells.remove(cellId);
    }

    public void moveCell(CellID cellId, CellTransform cellTransform) {
        Cell cell = cells.get(cellId);
        if (cell==null) {
            // TODO this is probably ok, need to check
            logger.warning("Got move for non-local cell");
            return;
        }

        if (!(cell instanceof MovableCell)) {
            cell.setTransform(cellTransform);
        }
    }
    
    private Cell instantiateCell(String className, CellID cellId) {
        Cell cell;
        
        try {
            Class clazz = Class.forName(className);
            Constructor constructor = clazz.getConstructor(CellID.class);
            cell = (Cell) constructor.newInstance(cellId);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Problem instantiating cell "+className, e);
            return null;
        }
        
        return cell;
    }

    /**
     * Set the view cell for this cache
     * 
     * @param viewCellID the id of the view cell
     */
    public void viewSetup(CellID viewCellID) {
        viewCell = (ViewCell)cells.get(viewCellID);
    }

    /**
     * {@inheritDoc}
     */
    public WonderlandSession getSession() {
        return session;
    }
}
