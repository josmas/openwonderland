/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.kernel.ComponentRegistry;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.security.Action;
import org.jdesktop.wonderland.server.cell.view.ViewCellMO;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.security.ViewAction;
import org.jdesktop.wonderland.server.security.ActionMap;
import org.jdesktop.wonderland.server.security.Resource;
import org.jdesktop.wonderland.server.security.ResourceMap;
import org.jdesktop.wonderland.server.security.SecurityManager;
import org.jdesktop.wonderland.server.security.SecureTask;

/**
 * Container for the cell cache for a viewcell.
 *
 * This is a nieve implementation that does not contain View Frustum culling,
 * culling is performed only on relationship to viewcell's position.
 *
 * @author paulby
 */
@InternalAPI
public abstract class ViewCellCacheMO implements ManagedObject, Serializable {
    /** a logger */
    private final static Logger logger =
            Logger.getLogger(ViewCellCacheMO.class.getName());

    /** a reference to the view that owns this cache */
    private ManagedReference<? extends ViewCellMO> viewRef;

    /** the set of all cells that have been loaded by this cache */
    // If this Set becomes large we may want to move it into it's own managed object
    // so we don't pay the penalty of serialization when processing avatar moves
    private Set<CellID> loaded = new HashSet<CellID>();
    
    /**
     * Creates a new instance of ViewCellCacheMO associated with the given
     * view.
     * @param view the view associated with this cache
     */
    public ViewCellCacheMO(ViewCellMO view) {
        DataManager dm = AppContext.getDataManager();
        viewRef = dm.createReference(view);
    }

    /**
     * Gets the ViewCellMO associated with this ViewCellCacheMO.
     */
    public ViewCellMO getViewCell() {
        return viewRef.get();
    }

    /**
     * Called by the UniverseService to request this cache generate load
     * messages for the given cells.  Note that this list contains all the
     * possible cells to load, regardless of any security on those cells. It
     * is up to the cache to enforce security on this list, and only send
     * updates to clients that this cell is allowed to see.
     * @param cells the set of cells to load
     */
    public void generateLoadMessagesService(Collection<CellDescription> cells) {
        // check if this user has permission to view the cells in this
        // collection, and then generate load messages for any that we
        // do have permission for
        ResourceMap rm = new ResourceMap();
       
        // a map of all the cells. The cells that we don't have permission
        // to see wil be removed by the LoadCellsTask below.
        // OWL issue #95: this map must maintain the ordering of the original
        // set in order to guarantee that parent cells are loaded before their
        // children.
        Map<CellID, CellDescription> cellsMap =
                new LinkedHashMap<CellID, CellDescription>();

        // convert to a mutable list for processing
        List<CellDescription> processList = new ArrayList<CellDescription>(cells);
        
        // schedule a task to process
        AppContext.getTaskManager().scheduleTask(new CellResourcesTask(
                processList, cellsMap, rm, new LoadCellsTask(this)));  
    }

    /**
     * Update our cache because the given cells may have changed. Just like
     * <code>generateLoadMessagesService()</code>, this method is responsible
     * for enforcing security on the list of cells.
     * @param cells the cells to revalidate
     */
    public void revalidateCellsService(Collection<CellDescription> cells) {
        // check if this user has permission to view the cells in this
        // collection, and then generate load messages for any that we
        // do have permission for
        ResourceMap rm = new ResourceMap();

        // cells we need to check for permission
        // OWL issue #95: this map must maintain the ordering of the original
        // set in order to guarantee that parent cells are loaded before their
        // children. We include all cells on this map to ensure we will see
        // cells where security has been removed.
        Map<CellID, CellDescription> cellsMap =
                new LinkedHashMap<CellID, CellDescription>();

        // convert to a mutable list for processing
        List<CellDescription> processList = new ArrayList<CellDescription>(cells);
        
        // schedule a task to process
        AppContext.getTaskManager().scheduleTask(new CellResourcesTask(
                processList, cellsMap, rm, new RevalidateCellsTask(this)));
    }

    /**
     * Called by the UniverseService to request this cache generate unload
     * messages for the given cells.  No security is required for unload
     * messages.
     * @param cells the set of cells to unload
     */
    public void generateUnloadMessagesService(Collection<CellDescription> cells) {
        sendUnloadMessages(cells);
    }

    /**
     * Method to actually send the cell load messages to the associated client.
     * This method is called when the view associated with this cache moves
     * into range of the given cells.
     * <p>
     * This method is called after any security checks are performed, so the
     * set of cells are all cells that the client has permission to access.
     * <p>
     * Subclasses can override this method to perform their own handling of
     * messages about loading the given cells.
     * @param cells the cells to generate load messages for.
     */
    protected abstract void sendLoadMessages(Collection<CellDescription> cells);

    /**
     * Method to actually send the cell unload messages to the associated client.
     * This method is called when the view associated with this cache moves
     * out of range of the given cells.
     * <p>
     * This method is called after any security checks are performed, so the
     * set of cells are all cells that the client has permission to access.
     * <p>
     * Subclasses can override this method to perform their own handling of
     * messages about unloading the given cells.
     * @param cells the cells to generate unload messages for.
     */
    protected abstract void sendUnloadMessages(Collection<CellDescription> unload);

    /**
     * Return true if this cache has loaded the given cell, or false if not.
     * @param cellID the cell to check
     * @return true of the given cell is loaded, or false if not
     */
    protected boolean isLoaded(CellID cellID) {
        return loaded.contains(cellID);
    }

    /**
     * Mark the given cell as loaded
     * @param cellID the id of the loaded cell
     * @return true if the cell id was added, or false if it was already in the
     * set
     */
    protected boolean setLoaded(CellID cellID) {
        return loaded.add(cellID);
    }

    /**
     * Mark the given cell as unloaded
     * @param cellID the id of the unloaded cell
     * @return true if the cell id was removed, or false if it wasn't in the set
     */
    protected boolean setUnloaded(CellID cellID) {
        return loaded.remove(cellID);
    }

    /**
     * Get the set of loaded cells
     * @return a set of loaded cells
     */
    protected Set<CellID> getLoadedCells() {
        return loaded;
    }
    
    private static class CellIDResource implements Resource, Serializable {
        private CellID cellID;
        private Resource wrapped;

        public CellIDResource(CellID cellID, Resource wrapped) {
            this.cellID = cellID;
            this.wrapped = wrapped;
        }

        public CellID getCellID() {
            return cellID;
        }

        public String getId() {
            return wrapped.getId();
        }

        public Result request(WonderlandIdentity identity, Action action) {
            return wrapped.request(identity, action);
        }

        public boolean request(WonderlandIdentity identity, Action action,
                               ComponentRegistry registry)
        {
            return wrapped.request(identity, action, registry);
        }
    }

    private interface CellResourceTask extends SecureTask {
        public void setCells(Map<CellID, CellDescription> cells);
    }
    
    private static class CellResourcesTask implements Task, Serializable {
        private static final int COUNT = 10;
        
        private final List<CellDescription> processList;
        private final Map<CellID, CellDescription> cells;
        private final ResourceMap rm;
        private final CellResourceTask task;
        
        public CellResourcesTask(List<CellDescription> processList, 
                                 Map<CellID, CellDescription> cells,
                                 ResourceMap rm,
                                 CellResourceTask task)
        {
            this.processList = processList;
            this.cells = cells;
            this.rm = rm;
            this.task = task;
        }

        public void run() throws Exception {
            int items = Math.min(COUNT, processList.size());
            for (int i = 0; i < items; i++) {
                CellDescription cell = processList.remove(0);
                processCell(cell);
            }
            
            // figure out the next task
            Task next;
            if (!processList.isEmpty()) {
                // still ids to process
                next = new CellResourcesTask(processList, cells, rm, task);
            } else {
                // all done -- perform task
                next = new CellResourceTaskRunner(cells, rm, task);
            }
            
            AppContext.getTaskManager().scheduleTask(next);
        }
        
        private void processCell(CellDescription cell) {
            // add to the map of cells
            cells.put(cell.getCellID(), cell);

            CellResourceManager crm = AppContext.getManager(CellResourceManager.class);
            Resource resource = crm.getCellResource(cell.getCellID());
            if (resource != null) {
                // add the resource to the security check
                Resource r = new CellIDResource(cell.getCellID(), resource);
                rm.put(r.getId(), new ActionMap(r, new ViewAction()));
            } 
        }
    }
    
    private static class CellResourceTaskRunner implements Task, Serializable {
        private final Map<CellID, CellDescription> cells;
        private final ResourceMap rm;
        private final CellResourceTask task;
        
        public CellResourceTaskRunner(Map<CellID, CellDescription> cells,
                                      ResourceMap rm, CellResourceTask task)
        {
            this.cells = cells;
            this.rm = rm;
            this.task = task;
        }
        
        public void run() throws Exception {
            task.setCells(cells);
            
            SecurityManager security = AppContext.getManager(SecurityManager.class);
            security.doSecure(rm, task);
        }
    }
    
    private static final class LoadCellsTask implements CellResourceTask, Serializable {
        private final ManagedReference<ViewCellCacheMO> viewCellCacheRef;
        private Map<CellID, CellDescription> cells;
        
        public LoadCellsTask(ViewCellCacheMO viewCellCache)
        {
            viewCellCacheRef = AppContext.getDataManager().createReference(viewCellCache);
        }
        
        public void setCells(Map<CellID, CellDescription> cells) {
            this.cells = cells;
        }

        public void run(ResourceMap grants) {
            // go through and move any cells that have been ok'd into the
            // granted list
            for (ActionMap am : grants.values()) {
                // the resource is OK'd if the view action is granted. If
                // the action map is empty, it means permission was denied.
                if (am.isEmpty()) {
                    CellID id = ((CellIDResource) am.getResource()).getCellID();
                    cells.remove(id);
                }
            }

            // now send a load message with all the granted cells.
            // OWL issue #95: these are sent in the order of the original
            // list, so order is preserved
            ViewCellCacheMO cache = viewCellCacheRef.getForUpdate();
            cache.sendLoadMessages(cells.values());
        }
    }

    private static final class RevalidateCellsTask implements CellResourceTask, Serializable {
        private final ManagedReference<ViewCellCacheMO> viewCellCacheRef;
        private Map<CellID, CellDescription> cells;

        public RevalidateCellsTask(ViewCellCacheMO viewCellCache)
        {
            viewCellCacheRef = AppContext.getDataManager().createReference(viewCellCache);
        }

        public void setCells(Map<CellID, CellDescription> cells) {
            this.cells = cells;
        }
        
        public void run(ResourceMap grants) {
            Map<CellID, CellDescription> unloadCells =
                    new LinkedHashMap<CellID, CellDescription>(cells);
            ViewCellCacheMO cache = viewCellCacheRef.get();

            // go through and look at each cell to see if its granted or denied
            for (ActionMap am : grants.values()) {
                CellID id = ((CellIDResource) am.getResource()).getCellID();
                
                // the resource is OK'd if the view action is granted
                if (am.size() == 0) {
                    // cell is removed -- keep on the unload list only
                    cells.remove(id);
                } else {
                    // cell is added -- keep on the load list only
                    unloadCells.remove(id);
                } 
            }

            // go through the load list and remove any cells that are
            // already in the cache.
            for (Iterator<CellID> loadCells = cells.keySet().iterator();
                 loadCells.hasNext();)
            {
                CellID loadID = loadCells.next();

                // remove this id if it is already loaded
                if (cache.isLoaded(loadID)) {
                    loadCells.remove();
                }

                // remove this id from the unload list. This will take
                // care of any cells that don't have a security resource
                // and therefore were not checked above
                unloadCells.remove(loadID);
            }

            // now go through the unload list and remove any cells that
            // are already unloaded
            for (Iterator<CellID> unloads = unloadCells.keySet().iterator();
                 unloads.hasNext();)
            {
                CellID unloadID = unloads.next();

                // remove this id if it is already unloaded
                if (!cache.isLoaded(unloadID)) {
                    unloads.remove();
                }
            }

            // now send any messages if there has been a change. 
            // OWL issue #95: These are sent based on the original lists, so
            // order is preserved
            cache.sendLoadMessages(cells.values());
            cache.sendUnloadMessages(unloadCells.values());
        }
    }
}
        
