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
    private Cell parent;
    private ArrayList<Cell> children = null;
    private CellTransform localTransform;
    private CellTransform local2VW;
    private CellID cellID;
    
    public Cell(CellID cellID) {
        this.cellID = cellID;
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
    
    /** TODO This is public for test purposes only, make package scope
     * 
     * @param cachedVWBounds
     */
    public void setCachedVWBounds(BoundingVolume cachedVWBounds) {
        this.cachedVWBounds = cachedVWBounds;
    }
         
    /**
     * Return the transform for this cell
     * @return
     */
    public CellTransform getTransform() {
        return localTransform;
    }
    
    /**
     * Return the local to Virtual World transform for this cell.
     * @return
     */
    public CellTransform getLocal2VWorld() {
        return local2VW;
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
     * Return a computed bounds for this cell in World coordinates that 
     * encapsulates the bounds of this cell and all it's children.
     * 
     * The bounds returned by this call are computed periodically so changes
     * to the local bounds of this node or any of it's children may not be 
     * immediately reflected in this bounds.
     * 
     * @return
     */
    public BoundingVolume getComputedWorldBounds() {
        return computedWorldBounds;
    }

    public void setComputedWorldBounds(BoundingVolume computedWorldBounds) {
        this.computedWorldBounds = computedWorldBounds;
    }
}
