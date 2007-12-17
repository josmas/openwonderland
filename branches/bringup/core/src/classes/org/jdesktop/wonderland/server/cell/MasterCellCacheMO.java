/**
 * Project Wonderland
 *
 * $RCSfile: MasterCellCacheGLO.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.14 $
 * $Date: 2007/11/07 15:46:52 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.TaskManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.jdesktop.wonderland.common.CellID;
import org.jdesktop.wonderland.ExperimentalAPI;

/**
 * Contains references to all UserCellCacheGLO's
 *
 * @author paulby
 */
@ExperimentalAPI
public class MasterCellCacheMO implements ManagedObject, Serializable {
    
    private static final String BINDING_NAME = "MASTER_CELL_CACHE";
    
    private ArrayList<ManagedReference> userCaches = new ArrayList<ManagedReference>();
    private ManagedReference worldRef;
    
    static final boolean useTasks = true; // Use sgs task for cache revalidation

    /** Creates a new instance of MasterTileCacheGLO */
    public MasterCellCacheMO() {
        AppContext.getTaskManager().schedulePeriodicTask(new MasterCellCacheTask(), 1000, 500);

        AppContext.getDataManager().setBinding(MasterCellCacheMO.BINDING_NAME, this);
        WorldRootCellGLO w = new WorldRootCellGLO();

        AppContext.getDataManager().setBinding(w.getGLOName(), w);
        worldRef = AppContext.getDataManager().createReference(w);
        
        w.buildWorld();
        
        openChannel();

    }
    
    public static MasterCellCacheMO getMasterCellCache() {
        DataManager dm = AppContext.getDataManager();
        return dm.getBinding(BINDING_NAME, MasterCellCacheMO.class);
    }

    /**
     * Add the UserCellCache to the set of tracked caches
     */
    void addUserCellCache(UserCellCacheGLO userCache) {
        AppContext.getDataManager().markForUpdate(this);
        userCaches.add(AppContext.getDataManager().createReference(userCache));
    }
    
    /**
     * Remove the UserCEllCache to the set of tracked caches
     */
    void removeUserCellCache(UserCellCacheGLO userCache) {
        AppContext.getDataManager().markForUpdate(this);
        userCaches.remove(AppContext.getDataManager().createReference(userCache));
    }
    
    /**
     * Revalidate all tile caches
     */
    public void revalidate() {
        MasterCellCacheTask.getMasterCellCacheTask().setRevalidationRequired(true);
    }

    /** 
      * Actually do the revalidation, called by the MasterCellCacheTask
      */
    void revalidateImpl() {
        TaskManager taskMgr = AppContext.getTaskManager();
        AppContext.getDataManager().markForUpdate(this);
        
        for(ManagedReference cache : userCaches)  {
            if (useTasks) {
                taskMgr.scheduleTask(UserCellCacheTask.createRevalidateTask(cache));
            } else {
                UserCellCacheGLO cacheGLO = cache.getForUpdate(UserCellCacheGLO.class);
                cacheGLO.revalidate();
            }
        }
    }
    
    /**
     * Open all tile channels
     */
    public void openChannel() {
        openChannel(worldRef);
    }
    
    /**
     * Open the channel on this cell and all it's children
     */
    private void openChannel(ManagedReference cellRef) {
        cellRef.get(CellGLO.class).openChannel();
        
        Collection<ManagedReference> children = cellRef.getForUpdate(CellGLO.class).getChildren();
        if (children!=null) {
            for(ManagedReference child : children) {
                openChannel(child);
            }
        }
    }
    
    /**
     * A tile has moved or it's bounds have changed in some way.
     *
     * Update it's position in the tile hierarchy and notify all interested clients
     * of the changes
     */
    public void cellMoved(MoveableCellGLO cell) {
        // TODO relocate the cell in the cell tree, notify clients of reparenting
       
        TaskManager taskMgr = AppContext.getTaskManager();
        
        for(ManagedReference cache : userCaches)  {
            taskMgr.scheduleTask(UserCellCacheTask.createCellMovedTask(cache, cell));
        }

    }
    
    /**
     * Add the cell into the cell hierarchy
     */
    public void addCell(ManagedReference cellRef) {
        AppContext.getDataManager().markForUpdate(this);
        // Temporary, TODO distinguish between Stationary and moveable cells,
        // place the avatar cells into the hierarchy at the correct place
        worldRef.getForUpdate(CellGLO.class).addChildCell(cellRef);
        revalidate();
    }
    
    /**
     * Remove the cell from the world
     */
    public void removeCell(ManagedReference cellRef) {
        AppContext.getDataManager().markForUpdate(this);
        CellGLO cell = cellRef.get(CellGLO.class);
        worldRef.getForUpdate(CellGLO.class).removeChildCell(cellRef);
        revalidate();
    }
    
    /**
     * Delete the cell, after removing it from the world
     */
    public void deleteCell(ManagedReference cellRef) {
        removeCell(cellRef);
        AppContext.getDataManager().removeObject(cellRef.get(CellGLO.class));
    }
    
    /**
     * Find a cell in the hierarchy
     * @param cellId the id of the cell to find
     * @return the cell with the given Id, or null if no cell exists with
     * that id
     */
    public CellGLO findCell(CellID cellId) {        
        return AppContext.getDataManager().getBinding(CellGLO.getGLOName(cellId), CellGLO.class);
    }
}
