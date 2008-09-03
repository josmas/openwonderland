/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.server.cell;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.Collection;
import org.jdesktop.wonderland.server.TimeManager;

/**
 *
 * @author paulby
 */
public abstract class SpaceMO implements ManagedObject, Serializable {

    // All the static cells in this space
    private CellListMO staticCellList;
    
    // All the dynamic cells in this space
    private CellListMO dynamicCellList;
    
    protected BoundingVolume worldBounds;
    protected Vector3f position;
    private SpaceID spaceID;
    
    SpaceMO(BoundingVolume bounds, Vector3f position, SpaceID spaceID) {
        this.position = new Vector3f(position);
        this.worldBounds = bounds.clone(null);
        this.worldBounds.setCenter(position);
        this.spaceID = spaceID;
        dynamicCellList = new CellListMO();
        staticCellList = new CellListMO();
    }
    
    public SpaceID getSpaceID() {
        return spaceID;
    }
    
    /**
     * Add the cell to this space. Called from CellMO.addToSpace
     * 
     * @param cell
     */
    void addCell(CellMO cell) {
//        System.out.println("Space "+getName()+"  adding Cell "+cell.getName());
        CellListMO cellList;
        if (cell.getComponent(MovableComponentMO.class)==null) {
            cellList = staticCellList;
        } else {
            cellList = dynamicCellList;
        }
        
        CellDescription cellDesc = cellList.addCell(cell);
        
        // Update the transform time stamp so this cell appears to have changed
        // Forcing it to be picked up by any ViewCache revalidations
        cellDesc.setTransform(cell.getLocalTransform(null), TimeManager.getWonderlandTime());
        
//        System.out.println("Cell "+cell.getName()+" entering space "+position);
    }
    
    /**
     * Remove the cell from this space. Called from CellMO.removeFromSpace
     * 
     * @param cell
     */
    void removeCell(CellMO cell) {
        CellListMO cellList;
        if (cell.isMovable()) {
            cellList = dynamicCellList;
        } else {
            cellList = staticCellList;
        }
        
        cellList.removeCell(cell);        
//        System.out.println("Cell "+cell.getName()+" left space "+position);
    }
    
    void notifyCellTransformChanged(CellMO cell, long timestamp) {
        if (cell.isMovable()) {
            dynamicCellList.notifyCellTransformChanged(cell, timestamp);
        } else {
            staticCellList.notifyCellTransformChanged(cell, timestamp);
        }
    }
    
    void notifyCellDetached(CellMO cell, long timestamp) {
        if (cell.isMovable()) {
            dynamicCellList.removeCell(cell);
        } else {
            staticCellList.removeCell(cell);
        }
        
    }
    
    public BoundingVolume getWorldBounds(BoundingVolume b) {
        return worldBounds.clone(b);
    }
    
    
    public CellListMO getDynamicCells(Collection<ManagedReference<SpaceMO>> spaces, BoundingVolume bounds, CellListMO results, CacheStats stats) {        
        return getDynamicCells(spaces, bounds, results, stats, 0L);
    }
    
    public CellListMO getDynamicCells(Collection<ManagedReference<SpaceMO>> spaces, BoundingVolume bounds, CellListMO results, CacheStats stats, long changedSince) {
        
        if (results==null)
            results = new CellListMO();
        int cellCount = 0;
        for(ManagedReference<SpaceMO> spaceRef : spaces) {
            cellCount += spaceRef.get().getDynamicCells(results, bounds, stats, changedSince);
        }

//        System.out.println("Checked "+spaces.size()+" spaces and "+cellCount+" cells");
        
        return results;
    }
    
    public CellListMO getStaticCells(Collection<ManagedReference<SpaceMO>> spaces, BoundingVolume bounds, CellListMO results, CacheStats stats) {
        
        if (results==null)
            results = new CellListMO();
        
        int cellCount = 0;
//        System.out.println("Neighbours ");
        for(ManagedReference<SpaceMO> spaceRef : spaces) {
//            System.out.print(spaceRef.get().getSpaceID()+" ");
            cellCount += spaceRef.get().getStaticCells(results, bounds, stats);
        }
        System.out.println();

//        System.out.println("Checked "+spaces.size()+" spaces and "+cellCount+" cells");
        
        return results;
    }
    
    /**
     * Add all modifiable cells in this space whos bounds intersect with the parameter
     * bounds to the list. Return the number of items added to the list during this call
     * 
     * @param list
     * @param bounds
     */
    private int getDynamicCells(CellListMO list, BoundingVolume bounds, CacheStats stats, long changedSince) {
        if (dynamicCellList.getChangeTimestamp()>changedSince-TimeManager.getTimeDrift()) {
            // List has changed recently, so check contents
//        System.err.println("Checking list "+dynamicCellList.size());
            for(CellDescription cellDesc : dynamicCellList.getCells()) {
    //            System.err.println(cellDesc.getCellID()+"  "+cellDesc.getTransformTimestamp()+">"+(changedSince-TimeManager.getTimeDrift()));
                if (cellDesc.getTransformTimestamp()>changedSince-TimeManager.getTimeDrift() && CellManagerMO.getCell(cellDesc.getCellID()).getWorldBounds().intersects(bounds)) {
                    list.addCell(cellDesc);
                    if (stats!=null) {
                        stats.logCellIntersect(this, cellDesc);
                    }
    //                System.out.println("intersect with "+cellDesc.getCellID());
                }
            }
            return dynamicCellList.size();
        } else {
            // List has not changed, nothing to do....
            return 0;
        }
        
    }
    
    /**
     * Add all stationary cells in this space whos bounds intersect with the parameter
     * bounds to the list. Return the number of items added to the list during this call
     * 
     * @param list
     * @param bounds
     */
    private int getStaticCells(CellListMO list, BoundingVolume bounds, CacheStats stats) {
//        return getCells(list, bounds, stationaryCellListRef.get());
        return getCells(list, bounds, staticCellList, stats);
    }
    
    private int getCells(CellListMO list, BoundingVolume bounds, CellListMO localList, CacheStats stats) {
        for(CellDescription cellDesc : localList.getCells()) {
            // Check if list already contains cellDesc to avoid DS datastore get
            if (!list.contains(cellDesc) && CellManagerMO.getCell(cellDesc.getCellID()).getWorldBounds().intersects(bounds)) {
                list.addCell(cellDesc);
//                System.out.println("intersect with "+cellDesc.getCellID());
                if (stats!=null) {
                    stats.logCellIntersect(this, cellDesc);
                }
            }
        }
        return localList.size();
    }
    
    /**
     * Return the spaces within the bounding volume. This space must be within
     * the volume otherwise null will be returned. Use CellManagerMO.getSpaces(...)
     * when you don't have a space from which to start the search.
     * 
     * @param v
     * @return
     */
    
    abstract Collection<ManagedReference<SpaceMO>> getSpaces(BoundingVolume v);
    
    /**
     * Return all spaces that are adjacent to this space
     * @return
     */
    abstract Collection<ManagedReference<SpaceMO>> getAdjacentSpaces();
    
}
