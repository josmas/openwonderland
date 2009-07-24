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
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
        CellResourceManager crm = AppContext.getManager(CellResourceManager.class);
        SecurityManager security = AppContext.getManager(SecurityManager.class);
        ResourceMap rm = new ResourceMap();

        // cells we have permission for (since they don't have a resource)
        Map<CellID, CellDescription> granted = new HashMap<CellID, CellDescription>();

        // cells we need to check for permission
        Map<CellID, CellDescription> check = new HashMap<CellID, CellDescription>();

        // get the resource for each cell and add it to the appropriate map
        for (CellDescription cell : cells) {
            Resource resource = crm.getCellResource(cell.getCellID());
            if (resource == null) {
                // don't need to check this cell
                granted.put(cell.getCellID(), cell);
            } else {
                Resource r = new CellIDResource(cell.getCellID(), resource);
                rm.put(r.getId(), new ActionMap(r, new ViewAction()));

                // do check this cell
                check.put(cell.getCellID(), cell);
            }
        }

        // see if we need to check any of the cells
        if (check.size() > 0) {
            // we do need to do this securely -- start a task
            SecureTask checkLoad = new LoadCellsTask(check, granted, this);
            security.doSecure(rm, checkLoad);
        } else {
            // just send the messages directly
            sendLoadMessages(cells);
        }
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
        CellResourceManager crm = AppContext.getManager(CellResourceManager.class);
        SecurityManager security = AppContext.getManager(SecurityManager.class);
        ResourceMap rm = new ResourceMap();

        // cells we need to check for permission
        Map<CellID, CellDescription> check = new HashMap<CellID, CellDescription>();

        // get the resource for each cell and add it to the appropriate map
        for (CellDescription cell : cells) {
            Resource resource = crm.getCellResource(cell.getCellID());
            if (resource != null) {
                Resource r = new CellIDResource(cell.getCellID(), resource);
                rm.put(r.getId(), new ActionMap(r, new ViewAction()));

                // do check this cell
                check.put(cell.getCellID(), cell);
            } 
        }

        // see if we need to check any of the cells
        if (check.size() > 0) {
            // we do need to do this securely -- start a task
            SecureTask checkCells = new RevalidateCellsTask(check, this);
            security.doSecure(rm, checkCells);
        }
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

    private static final class LoadCellsTask implements SecureTask, Serializable {
        private Map<CellID, CellDescription> check;
        private Map<CellID, CellDescription> granted;
        private ManagedReference<ViewCellCacheMO> viewCellCacheRef;

        public LoadCellsTask(Map<CellID, CellDescription> check,
                             Map<CellID, CellDescription> granted,
                             ViewCellCacheMO viewCellCache)
        {
            this.check = check;
            this.granted = granted;

            viewCellCacheRef = AppContext.getDataManager().createReference(viewCellCache);
        }

        public void run(ResourceMap grants) {
            // go through and move any cells that have been ok'd into the
            // granted list
            for (ActionMap am : grants.values()) {
                // the resource is OK'dif the view action is granted
                if (am.size() == 1) {
                    CellID id = ((CellIDResource) am.getResource()).getCellID();
                    CellDescription desc = check.get(id);
                    granted.put(id, desc);
                }
            }

            // now send a load message with all the granted cells
            ViewCellCacheMO cache = viewCellCacheRef.getForUpdate();
            cache.sendLoadMessages(granted.values());
        }
    }

    private static final class RevalidateCellsTask implements SecureTask, Serializable {
        private Map<CellID, CellDescription> check;
        private ManagedReference<ViewCellCacheMO> viewCellCacheRef;

        public RevalidateCellsTask(Map<CellID, CellDescription> check,
                                  ViewCellCacheMO viewCellCache)
        {
            this.check = check;
            viewCellCacheRef = AppContext.getDataManager().createReference(viewCellCache);
        }

        public void run(ResourceMap grants) {
            List<CellDescription> load = new LinkedList<CellDescription>();
            List<CellDescription> unload = new LinkedList<CellDescription>();
            ViewCellCacheMO cache = viewCellCacheRef.get();

            // go through and look at each cell to see if its granted or denied
            for (ActionMap am : grants.values()) {
                CellID id = ((CellIDResource) am.getResource()).getCellID();
                CellDescription desc = check.get(id);

                // the resource is OK'd if the view action is granted
                if (am.size() == 1 && !cache.isLoaded(id)) {
                    load.add(desc);
                } else if (am.size() == 0 && cache.isLoaded(id)) {
                    unload.add(desc);
                }
            }

            // now send any messages
            cache.sendLoadMessages(load);
            cache.sendUnloadMessages(unload);
        }
    }
}
        
