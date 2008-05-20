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
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.MultipleParentException;

/**
 *
 * @author paulby
 */
public class SpaceCellMO extends CellMO {

    // The set of spaces that are 'close' to this space
    private LinkedList<ManagedReference<SpaceCellMO>> proximitySpaces = new LinkedList<ManagedReference<SpaceCellMO>>();    
    
    // All the static cells in this space
    private ManagedReference<CellListMO> staticCellListRef;
    
    // All the movable cells in this space
    private ManagedReference<CellListMO> movableCellListRef;
    
    // The set of both static and movable cell lists for all spaces in close proximity to this space
    private LinkedList<ManagedReference<CellListMO>> proximityCellLists = new LinkedList<ManagedReference<CellListMO>>();
    
    public SpaceCellMO(BoundingVolume bounds, CellTransform transform) {
        super(bounds, transform);
        movableCellListRef = AppContext.getDataManager().createReference(new CellListMO());
        staticCellListRef = AppContext.getDataManager().createReference(new CellListMO());
    }
    
    /**
     * Add the cell to this space. Called from CellMO.addToSpace
     * 
     * @param cell
     */
    void addCell(CellMO cell) {
//        System.out.println("Space "+getName()+"  adding Cell "+cell.getName());
        CellListMO cellList;
        if (cell.getComponent(MovableComponentMO.class)!=null) {
            cellList = movableCellListRef.getForUpdate();
        } else {
            cellList = staticCellListRef.getForUpdate();
        }
        
        cellList.addCell(cell);
    }
    
    /**
     * Remove the cell from this space. Called from CellMO.removeFromSpace
     * 
     * @param cell
     */
    void removeCell(CellMO cell) {
        System.out.println("Space "+getName()+"  removing Cell "+cell.getName());
        CellListMO cellList;
        if (cell.getComponent(MovableComponentMO.class)!=null) {
            cellList = movableCellListRef.getForUpdate();
        } else {
            cellList = staticCellListRef.getForUpdate();
        }
        
        cellList.removeCell(cell);        
    }
    
    /**
     * Return the reference to the list of movable cells in this space
     * @return
     */
    ManagedReference<CellListMO> getMovableCellListRef() {
        return movableCellListRef;
    }
    
    /**
     * Return the reference to the list of static cells in this space
     * @return
     */
    ManagedReference<CellListMO> getStaticCellListRef() {
        return staticCellListRef;
    }
        
    
    /**
     * Add the provided space to the set of spaces that are in the proximity
     * of this space
     * 
     * @param space
     */
    void addProximitySpace(SpaceCellMO space) {
        if (this==space)
            return;
        
        proximitySpaces.add(AppContext.getDataManager().createReference(space));
        proximityCellLists.add(space.getMovableCellListRef());
        proximityCellLists.add(space.getStaticCellListRef());
    }
    
    
    /**
     * Remove the provided space to the set of spaces that are in the proximity
     * of this space
     * 
     * @param space
     */
    void removeProximitySpace(SpaceCellMO space) {
        proximitySpaces.remove(AppContext.getDataManager().createReference(space));
        proximityCellLists.remove(space.getMovableCellListRef());
        proximityCellLists.remove(space.getStaticCellListRef());
    }
    
    Collection<ManagedReference<SpaceCellMO>> getProximitySpaces() {
        return (Collection<ManagedReference<SpaceCellMO>>) proximitySpaces.clone();
    }
    
    /**
     * Return an collection of all the lists of cells which are in the proximity of
     * this space. The return collection will not contain null elements.
     * 
     * @return
     */
    Collection<CellListMO> getProximityCellLists() {
        ArrayList<CellListMO> ret = new ArrayList();
        CellListMO list;
        list = staticCellListRef.get();
        if (list!=null) {
            ret.add(list);
        }
        list = movableCellListRef.get();
        if (list!=null) {
            ret.add(list);
        }
        
        for(ManagedReference<CellListMO> listRef : proximityCellLists) {
            ret.add(listRef.get());
        }
        
//        System.out.println("Space "+getName()+"  proximityCells "+ret.size() + " list Size "+proximityCellLists.size());
        
        return ret;
    }
    
    @Override
    protected String getClientCellClassName(ClientSession clientSession, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.client.cell.SpaceCell";
    }

    @Override
    void setLive(boolean live) {
        super.setLive(live);
        assignSpace(this, live);
        addToSpace(this); // This space should be returned as a in this space.
    }
    
    @Override
    public void addChild(CellMO child) throws MultipleParentException {
        super.addChild(child);
        if (isLive()) {
            child.addToSpace(this);
            assignSpace(child, true);
        }
    }
    
    private void assignSpace(CellMO cell, boolean live) {
        Collection<ManagedReference<CellMO>> childRefs = cell.getAllChildrenRefs();
        if (childRefs==null)
            return;
        for(ManagedReference<CellMO> childRef : childRefs) {
            CellMO child = childRef.get();
            
            if (live) {
                System.err.println("Adding cell to space "+child.getName());
                child.addToSpace(this);
            } else {
                child.removeFromSpace(this);
            }
            assignSpace(child, live);
        }
    }
}
