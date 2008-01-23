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

import org.jdesktop.wonderland.server.cell.bounds.ServiceBoundsHandler;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSessionId;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedReference;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.SerializationHelper;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.comms.WonderlandChannelNames;
import org.jdesktop.wonderland.server.UserPerformanceMonitor;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.WonderlandMO;
import org.jdesktop.wonderland.server.setup.BasicCellMOHelper;
import org.jdesktop.wonderland.server.setup.BasicCellMOSetup;

/**
 * Server side representation of a cell
 * 
 * @author paulby
 */
@ExperimentalAPI
public class CellMO extends WonderlandMO {

    private ManagedReference parentRef=null;
    private ArrayList<ManagedReference> childCellRefs = null;
    private Matrix4d transform = null;
    private Matrix4d localToVWorld = null;
    private CellID cellID;
    private transient Bounds localBounds;
    
    private String name=null;
    
    private boolean live = false;
    
    private Channel cellChannel = null;
    private String channelName =null;
    
    private long version;
    
    protected static Logger logger = Logger.getLogger(CellMO.class.getName());
    
    /**
     * Create a CellMO with a localBounds of an empty sphere and a transform of
     * null
     */
    public CellMO() {
        this(new BoundingSphere(),null);
    }
    
    /**
     * Create a CellMO with the specified localBounds and transform.
     * @param localBounds, must not be null
     * @param transform
     */
    public CellMO(Bounds localBounds, Matrix4d transform) {
        assert(localBounds!=null);
        
        cellID = WonderlandContext.getMasterCellCache().createCellID(this);
        this.transform = transform;
        setLocalBounds(localBounds);
    }
    
    /**
     * Set the bounds of the cell in cell local coordinates
     * @param bounds
     */
    public void setLocalBounds(Bounds bounds) {
        localBounds = (Bounds) bounds.clone();
        
        if (live) {
            BoundsHandler.get().setLocalBounds(cellID, bounds);
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
        
        return BoundsHandler.get().getCachedVWBounds(cellID);
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
        return BoundsHandler.get().getComputedWorldBounds(cellID);   
    }
    
    /**
     * Set the computed world bounds of this cell
     * 
     * @param bounds
     */
    void setComputedWorldBounds(Bounds bounds) {
        BoundsHandler.get().setComputedWorldBounds(cellID, bounds);
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
        if (childCellRefs==null)
            childCellRefs = new ArrayList<ManagedReference>();
        
        child.setParent(AppContext.getDataManager().createReference(this));
        
        childCellRefs.add(AppContext.getDataManager().createReference(child));
        
        if (live) {
           child.setLive(true);
           BoundsHandler.get().cellChildrenChanged(cellID, child.getCellID(), true);
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
        
        if (childCellRefs.remove(childRef)) {
            try {
                child.setParent(null);
                if (live) {
                    child.setLive(false);
                    BoundsHandler.get().cellChildrenChanged(cellID, child.getCellID(), false);
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
        if (childCellRefs==null)
            return 0;
        
        return childCellRefs.size();
    }
    
    /**
     * Return an Iterator over the children references for this cell. 
     * @return
     */
    public Collection<ManagedReference> getAllChildrenRefs() {
        if (childCellRefs==null)
            return new ArrayList<ManagedReference>();
        
        return (Collection<ManagedReference>) childCellRefs.clone();
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
     * Detach this cell from its parent
     */
    public void detach() {
        CellMO parent = getParent();
        if (parent==null)
            return;
        
        parent.removeChild(this);
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
            BoundsHandler.get().cellTransformChanged(cellID, transform);
        }
    }
    
    /**
     * Return the cells transform
     * 
     * @return return a clone of the transform
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
        BoundsHandler.get().setLocalToVworld(cellID, transform);
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
     * world root, non-live cells are not
     * @param live
     */
    void setLive(boolean live) {
        this.live = live;
        if (live) {
            try {
                BoundsHandler.get().createBounds(this);
                if (getParent()!=null) { // Root cell has a null parent
                    System.out.println("setLive "+getCellID()+" "+getParent().getCellID());
                    BoundsHandler.get().addChild(getParent(), this);
                }
            } catch (MultipleParentException ex) {
                Logger.getLogger(CellMO.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            BoundsHandler.get().removeChild(getParent(), this);
            BoundsHandler.get().removeBounds(this);
        }
        
        for(ManagedReference ref : getAllChildrenRefs()) {
            CellMO child = ref.get(CellMO.class);
            child.setLive(live);
        }
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
    
    /**
     * Open the channel for this cell.
     */
    protected void openCellChannel() {
        channelName = WonderlandChannelNames.CELL_PREFIX+"."+cellID.toString();
        cellChannel = AppContext.getChannelManager().createChannel(channelName, null, Delivery.RELIABLE); 
        
    }
    
   /**
     * Open the cell channels, TODO make abstract. This is called when a CellMO
    * is created, any cells which required channels should overload this method
    * and open the required channels.
     */
    protected void openChannels() {
    }
    
    
    /**
     * Add user to all the cells channels, if there is no channel simply return
     * @param userID
     */
    public void addUserToCellChannel(ClientSessionId userID) {
        if (cellChannel==null)
            return;
            
        cellChannel.join(userID.getClientSession(), null);
    }
    
    /**
     * Remove user from all cell channels
     * @param userID
     */
    public void removeUserFromCellChannel(ClientSessionId userID) {
        if (cellChannel==null)
            return;
            
        cellChannel.leave(userID.getClientSession());        
    }
    
    /**
     * Return the name of this cells channel, or null if this cell has no channel
     * @return
     */
    public String getCellChannelName() {
        return channelName;
    }
    
    /**
     * Returns the fully qualified name of the class that represents
     * this cell on the client
     */
    public String getClientCellClassName() {
        throw new RuntimeException("Not Implemented");
    }
    
    /**
     * Get the setupdata for this cell
     */
    public CellSetup getSetupData() {
        return null;
    }
    
    /**
     * Returns a list of Cells that intersect with the supplied bounds. The
     * bounds are referenced in world coordinates; Returns a list of references to visible cells.
     *
     * This call can only be made on live Cells
     * 
     * @param bounds The viewing bounds, in world coordinates
     * @param monitor The performance monitor
     * @return A list of visible cells
     */
    public Collection<CellID> getVisibleCells(Bounds bounds, UserPerformanceMonitor monitor) {
        if (!live)
            throw new RuntimeException("Cell is not live");
        
        return BoundsHandler.get().getVisibleCells(cellID, bounds, monitor);
    }
    
    /**
     * Get the current version number for this CellMO. This is used by
     * the UserCellCacheGLO to determine if a user's copy of this cell
     * is up to date.  If the version is higher than the user's version,
     * the AvatarCellCacheMO will send a reconfigure message to the associated
     * client.
     * @return this cells version number
     */
    public long getVersion() {
        return version;
    }
    
    /**
     * Increment the cell's version number
     * @return the new version number
     */
    public long incrementVersion() {
        return version++;
    }
    
    /**
     * Handle serialization of Bounds
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        SerializationHelper.writeBoundsObject(localBounds, out);
    }
    
    /**
     * Handle de-serialization of Bounds
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        localBounds = SerializationHelper.readBoundsObject(in);
    }

    /**
     * Set up the cell from the given properties
     * @param properties the properties to setup with
     */
    public void setupCell(BasicCellMOSetup<?> setup) {
        setTransform(BasicCellMOHelper.getCellOrigin(setup));
        setLocalBounds(BasicCellMOHelper.getCellBounds(setup));
    }
    
    /**
     * Reconfigure the cell with the given properties.  This just
     * calls <code>setupCell()</code>.
     * @param properties the properties to setup with
     */
    public void reconfigureCell(BasicCellMOSetup<?> setup) {
        // just call setupCell, since there is nothing to do differently
        // if this is a change
        setupCell(setup);
    }
    
}

