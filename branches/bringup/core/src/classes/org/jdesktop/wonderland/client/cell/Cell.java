/**
 * Project Wonderland
 *
 * $RCSfile:$
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
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.client.cell;

import com.jme.bounding.BoundingVolume;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;

/**
 * The client side representation of a cell.
 * 
 * @author paulby
 */
public class Cell {
    private BoundingVolume cachedVWBounds;
    private BoundingVolume computedWorldBounds;
    private BoundingVolume localBounds;
    private Cell parent;
    private ArrayList<Cell> children = null;
    private CellTransform localTransform;
    private CellTransform local2VW = new CellTransform(null, null);
    private CellID cellID;
    private String name=null;
    
    public Cell(CellID cellID) {
        this.cellID = cellID;
    }
    
    /**
     * Return the unique id of this cell
     * @return the cell id
     */
    public CellID getCellID() {
        return cellID;
    }
    
    /**
     * Return the cells parent, or null if it have no parent
     * @return
     */
    public Cell getParent() {
        return parent;
    }
    
    /**
     * Return the set of children for this cell, or an empty set if there
     * are no children
     * @return
     */
    public Collection<Cell> getChildren() {
        if (children==null)
            return new ArrayList<Cell>(0);
        
        synchronized(children) {
            return (Collection<Cell>) children.clone();
        }
    }
    
    /**
     * Add the child to the set of children of this cell. Throws a MultipleParentException
     * if child is already a child to another cell
     * @param child to add
     * @throws org.jdesktop.wonderland.common.cell.MultipleParentException
     */
    public void addChild(Cell child) throws MultipleParentException {
        if (child.getParent()!=null) {
            throw new MultipleParentException();
        }
        
        if (children==null) {
            children=new ArrayList<Cell>();
        }
        
        synchronized(children) {
            children.add(child);
            child.setParent(this);
        }
    }
    
    /**
     * Set the parent of this cell, called from addChild
     * @param parent
     */
    void setParent(Cell parent) {
        assert(this.parent==null);
        this.parent = parent;
    }
    
    /**
     * Return the number of children
     * 
     * @return
     */
    public int getNumChildren() {
        if (children==null)
            return 0;
        return children.size();
    }
    
    /**
     * Return the transform for this cell
     * @return
     */
    public CellTransform getTransform() {
        return (CellTransform) localTransform.clone();
    }
    
    /**
     * Set the transform for this cell
     * @param localTransform
     */
    public void setTransform(CellTransform localTransform) {
        this.localTransform = (CellTransform) localTransform.clone();
        if (parent!=null) {
            local2VW = local2VW.mul(parent.getLocalToVWorld());
        }
        
        for(Cell child : getChildren())
            transformTreeUpdate(this, child);
    }
    
    /**
     * Return the local to Virtual World transform for this cell.
     * @return
     */
    public CellTransform getLocalToVWorld() {
        return computeLocal2VWorld(this);
    }
    
    void setLocalToVWorld(CellTransform localToVWorld) {
        local2VW = (CellTransform) localToVWorld.clone();
        localBounds.clone(cachedVWBounds);
        local2VW.transform(cachedVWBounds);
    }
    
    /**
     * Compute the local to vworld of the cell, this for test purposes only
     * @param parent
     * @return
     */
    private CellTransform computeLocal2VWorld(Cell cell) {
        LinkedList<CellTransform> transformStack = new LinkedList<CellTransform>();
        
        // Get the root
        Cell current=cell;
        while(current.getParent()!=null) {
            transformStack.addFirst(current.localTransform);
            current = current.getParent();
        }
        CellTransform ret = new CellTransform(null, null);
        for(CellTransform t : transformStack) {
            if (t!=null)
                ret.mul(t);
        }
        
        return ret;
    }

    /**
     * Update local2VWorld and bounds of child and all its children to
     * reflect changes in a parent
     * 
     * @param parent
     * @param child
     * @return the combined bounds of the child and all it's children
     */
    private BoundingVolume transformTreeUpdate(Cell parent, Cell child) {
        CellTransform parentL2VW = parent.getLocalToVWorld();
        
        CellTransform childTransform = child.getTransform();
        
        if (childTransform!=null) {
            childTransform.mul(parentL2VW);
            child.setLocalToVWorld(childTransform);
        } else {
            child.setLocalToVWorld(parentL2VW);
        }
        
        BoundingVolume ret = child.getCachedVWBounds();
        
        Iterator<Cell> it = child.getChildren().iterator();
        while(it.hasNext()) {
            ret.mergeLocal(transformTreeUpdate(child, it.next()));
        }
        
//        child.setComputedWorldBounds(ret);
        
        return null;
    }
    
    /**
     * Returns the local bounds transformed into VW coordinates. These bounds
     * do not include the subgraph bounds. This call is only valid for live
     * cells
     * 
     * @return
     */
    public BoundingVolume getCachedVWBounds() {
        return cachedVWBounds;
    }

    /**
     * TODO should not be public
     * @param cachedVWBounds
     */
    public void setCachedVWBounds(BoundingVolume cachedVWBounds) {
        this.cachedVWBounds = cachedVWBounds;
    }
    
    /**
     * Return a computed bounds for this cell in World coordinates that 
     * encapsulates the bounds of this cell and all it's children.
     * 
     * The bounds returned by this call are computed periodically so changes
     * to the local bounds of this node or any of it's children may not be 
     * immediately reflected in this bounds.
     * 
     * @return
     */
//    public BoundingVolume getComputedWorldBounds() {
//        return computedWorldBounds;
//    }
//
//    public void setComputedWorldBounds(BoundingVolume computedWorldBounds) {
//        this.computedWorldBounds = computedWorldBounds;
//    }

    /**
     * Return the name for this cell (defaults to cellID)
     * @return
     */
    public String getName() {
        if (name==null)
            return cellID.toString();
        return name;
    }

    /**
     * Set a name for the cell
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public BoundingVolume getLocalBounds() {
        return localBounds.clone(null);
    }

    public void setLocalBounds(BoundingVolume localBounds) {
        this.localBounds = localBounds;
    }


}
