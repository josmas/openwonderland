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

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.Bounds;
import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix4d;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.SerializationHelper;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.WonderlandMO;

/**
 * Server side representation of a cell
 * 
 * @author paulby
 */
@ExperimentalAPI
public class CellMO extends WonderlandMO {

    private ManagedReference parentRef=null;
    private ArrayList<ManagedReference> childrenRef = null;
    private Matrix4d transform = null;
    private Matrix4d localToVWorld = null;
    private transient Bounds localBounds = null;
    private transient Bounds computedWorldBounds = null;
    private CellID cellID;
    
    private String name=null;
    
    private boolean live = false;
    
    public CellMO() {
        cellID = WonderlandContext.getCellManager().createCellID(this);
        transform = new Matrix4d();
        transform.setIdentity();
    }
    
    /**
     * Set the bounds of the cell in cell local coordinates
     * @param bounds
     */
    public void setLocalBounds(Bounds bounds) {
        this.localBounds = bounds;
        
        if (live) {
            WonderlandContext.getCellManager().cellLocalBoundsChanged(this);
        }
    }
    
    /**
     *  Return (a clone) of the cells bounds in cell local coordinates
     * @return
     */
    public Bounds getLocalBounds() {
        return (Bounds) localBounds.clone();        
    }
    
    /**
     * Returns the local bounds transformed into VW coordinates. These bounds
     * do not include the subgraph bounds. This call is only valid for live
     * cells
     * 
     * @return
     */
    Bounds getCachedVWBounds() {
        if (!live)
            throw new RuntimeException("Cell is not live");
        
        Bounds ret = (Bounds) localBounds.clone();
        Transform3D t3d = new Transform3D(localToVWorld);
        ret.transform(t3d);
        
        return ret;
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
    public Bounds getComputedWorldBounds() {
        return computedWorldBounds;        
    }
    
    /**
     * Set the computed world bounds of this cell
     * 
     * @param bounds
     */
    void setComputedWorldBounds(Bounds bounds) {
        computedWorldBounds = bounds;
    }
    
    /**
     *  Add a child cell to list of children contained within this cell.
     *  A cell can only be attached to a single parent cell at any given time,
     *  attempting to add a cell to multiple parents will result in a
     *  MultipleParentException being thrown.
     * 
     * @param child
     * @throws org.jdesktop.wonderland.common.cell.MultipleParentException
     */
    public void addChild(CellMO child) throws MultipleParentException {
        if (childrenRef==null)
            childrenRef = new ArrayList<ManagedReference>();
        
        child.setParent(AppContext.getDataManager().createReference(this));
        
        childrenRef.add(AppContext.getDataManager().createReference(child));
        
        if (live) {
            WonderlandContext.getCellManager().cellChildrenChanged(this, child, true);
        }
    }
    
    /**
     * Remove the child from the list of children of this cell.
     * 
     * @param child to remove
     * @return true if the child was removed, false if the cell was not a child of
     * this cell.
     */
    public boolean removeChild(CellMO child) {
        ManagedReference childRef = AppContext.getDataManager().createReference(child);
        
        if (childrenRef.remove(childRef)) {
            try {
                child.setParent(null);
                if (live) {
                    WonderlandContext.getCellManager().cellChildrenChanged(this, child, false);
                }
                return true;
            } catch (MultipleParentException ex) {
                // This should never happen
                Logger.getLogger(CellMO.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        
        // Not a child of this cell
        return false;
    }
    
    /**
     * Return the number of children of this cell
     * @return
     */
    public int getNumChildren() {
        if (childrenRef==null)
            return 0;
        
        return childrenRef.size();
    }
    
    /**
     * Return an Iterator over the children references for this cell. 
     * @return
     */
    public Iterator<ManagedReference> getAllChildrenRefs() {
        if (childrenRef==null)
            return new ArrayList<ManagedReference>().iterator();
        
        return childrenRef.iterator();
    }
        
    /**
     *  Return the cell which is the parentRef of this cell, null if this not
     * attached to a parentRef
     */
    public CellMO getParent() {
        if (parentRef==null)
            return null;
        return parentRef.get(CellMO.class);                
    }
    
    /**
     * Set the parent of this cell. Package private because the parent is
     * controlled through add and remove child.
     * 
     * @param parentRef
     * @throws org.jdesktop.wonderland.common.cell.MultipleParentException
     */
    void setParent(ManagedReference newParentRef) throws MultipleParentException {
        if (newParentRef!=null && parentRef!=null)
            throw new MultipleParentException();
        
        this.parentRef = newParentRef;
    }
    
    /**
     * Set the transform for this cell. This will define the localOrigin of
     * the cell on the client. This transform is combined with all parentRef 
     * transforms to define the location of the cell in 3 space. 
     * 
     * Changing the transform repositions the cell which is a fairly expensive
     * operation as it changes the computed bounds of this cell and potentially
     * all it's parent cells.
     * 
     * @param transform
     */
    public void setTransform(Matrix4d transform) {
        this.transform = (Matrix4d) transform.clone();  
        
        if (live) {
            WonderlandContext.getCellManager().cellTransformChanged(this);
        }
    }
    
    /**
     * Return the cells transform (a clone)
     * 
     * @return
     */
    public Matrix4d getTransform() {
        return (Matrix4d) transform.clone();
    }
    
    /**
     * Set the local to VWorld transform of this cells origin
     * @param transform
     */
    void setLocalToVWorld(Matrix4d transform) {
        this.localToVWorld = transform;
    }
    
    /**
     * Get the local to VWorld transform of this cells origin. This call
     * can only be made on live cells, a RuntimeException will be thrown
     * if the cell is not live.
     * 
     * TODO - should we create our own exception type ?
     * 
     * @return
     */
    Matrix4d getLocalToVWorld() {
        if (!live)
            throw new RuntimeException("Unsupported Operation");
        
        return localToVWorld;
    }
    
    /**
     * Return the cellID for this cell
     * 
     * @return cellID
     */
    public CellID getCellID() {
        return cellID;
    }
    
    /**
     * Get the live state of this cell. live cells are connected to the
     * world root, inlive cells are not
     */
    public boolean isLive() {
        return live;
    }
    
    /**
     * Set the live state of this cell. Live cells are connected to the
     * world root, inlive cells are not
     * @param live
     */
    void setLive(boolean live) {
        this.live = live;
    }
    
    /**
     * Handle serialization of Bounds
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        SerializationHelper.writeBoundsObject(localBounds, out);
        SerializationHelper.writeBoundsObject(computedWorldBounds, out);
    }
    
    /**
     * Handle de-serialization of Bounds
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        localBounds = SerializationHelper.readBoundsObject(in);
        computedWorldBounds = SerializationHelper.readBoundsObject(in);
    }

    /**
     * Get the name of the cell, by default the name is the cell id.
     * 
     * @return
     */
    public String getName() {
        if (name==null)
            return cellID.toString();
        
        return name;
    }

    /**
     * Set the name of the cell. The name is simply for developer reference.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
