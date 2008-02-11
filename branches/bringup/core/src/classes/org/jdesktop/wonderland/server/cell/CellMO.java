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

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSessionId;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.comms.WonderlandChannelNames;
import org.jdesktop.wonderland.server.UserPerformanceMonitor;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.WonderlandMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientChannel;
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
    private CellTransform transform = null;
    private CellTransform localToVWorld = null;
    private CellID cellID;
    private BoundingVolume localBounds;
    
    private String name=null;
    
    private boolean live = false;
    
    private Channel cellChannel = null;
    private String channelName =null;
    
    private long transformVersion = Long.MIN_VALUE;
    
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
     * @param localBounds the bounds of the new cell, must not be null
     * @param transform the transform for this cell
     */
    public CellMO(BoundingVolume localBounds, CellTransform transform) {
        assert(localBounds!=null);
        
        cellID = WonderlandContext.getCellManager().createCellID(this);
        this.transform = transform;
        setLocalBounds(localBounds);
    }
    
    /**
     * Set the bounds of the cell in cell local coordinates
     * @param bounds
     */
    public void setLocalBounds(BoundingVolume bounds) {
        localBounds = bounds.clone(null);
        
        if (live) {
            BoundsHandler.get().setLocalBounds(cellID, bounds);
        }
    }
    
    /**
     *  Return (a clone) of the cells bounds in cell local coordinates
     * @return the bounds in local coordinates
     */
    public BoundingVolume getLocalBounds() {
        return (BoundingVolume) localBounds.clone(null);     
    }
    
    /**
     * Returns the local bounds transformed into VW coordinates. These bounds
     * do not include the subgraph bounds. This call is only valid for live
     * cells
     * 
     * @return
     */
    BoundingVolume getCachedVWBounds() {
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
     * This call is only valid for live
     * cells
     * 
     * @return the bounds in world coordinates
     */
    public BoundingVolume getComputedWorldBounds() {
        if (!live)
            throw new RuntimeException("Cell is not live");
        
        return BoundsHandler.get().getComputedWorldBounds(cellID);   
    }
    
    /**
     * Set the computed world bounds of this cell
     * 
     * @param bounds
     */
    void setComputedWorldBounds(BoundingVolume bounds) {
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
     * @return the number of children
     */
    public int getNumChildren() {
        if (childCellRefs==null)
            return 0;
        
        return childCellRefs.size();
    }
    
    /**
     * Return an Iterator over the children references for this cell. 
     * @return a collection of references to the children of this cell.
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
    public void setTransform(CellTransform transform) {
        this.transform = (CellTransform) transform.clone();  
        
        if (live) {
            BoundsHandler.get().cellTransformChanged(cellID, transform);
            transformVersion++;
        }
    }
    
    public long getTransformVersion() {
        return transformVersion;
    }
    
    /**
     * Return the cells transform
     * 
     * @return return a clone of the transform
     */
    public CellTransform getTransform() {
        return (CellTransform) transform.clone();
    }
    
    /**
     * Set the local to VWorld transform of this cells origin
     * @param transform
     */
    void setLocalToVWorld(CellTransform transform) {
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
    CellTransform getLocalToVWorld() {
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
//                    System.out.println("setLive "+getCellID()+" "+getParent().getCellID());
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
     * @return the cell's name
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
     * Convenience method to open the channel for this cell. Creates a
     * channelName based on the cellID.
     */
    protected void openCellChannel() {
        channelName = WonderlandChannelNames.CELL_PREFIX+"."+cellID.toString();
        cellChannel = AppContext.getChannelManager().createChannel(channelName, null, Delivery.RELIABLE); 
        
    }
    
   /**
     * Cells that have a channel should overload this method to actually open the
    * channel. The convenience method openCellChannel can be used to open the channel
    * with a default channel name.
     */
    protected void openChannel() {
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
     * @return the name of the cell's channel
     */
    public String getCellChannelName() {
        return channelName;
    }
    
    /**
     * Handle messages sent to this cell.
     * @param channel a channel that can be used to send messages back to 
     * the client that originated this message.  Messages will automatically
     * be sent to the correct WonderlandClient.
     * @param sessionId the sessionId that sent the message
     * @param message the message to handle
     */
    protected void messageReceived(WonderlandClientChannel channel,
                                   ClientSessionId sessionId, 
                                   CellMessage message) 
    {
        throw new RuntimeException("Not Implemented");
    }
    
    /**
     * Returns the fully qualified name of the class that represents
     * this cell on the client
     */
    public String getClientCellClassName() {
//        throw new RuntimeException("Not Implemented");
        return "dummy";
    }
    
    /**
     * Get the setupdata for this cell. Subclasses should overload to
     * return their specific setup object.
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
    public Collection<CellMirror> getVisibleCells(BoundingVolume bounds, UserPerformanceMonitor monitor) {
        if (!live)
            throw new RuntimeException("Cell is not live");
        
        return BoundsHandler.get().getVisibleCells(cellID, bounds, monitor);
    }
    
    /**
     * Set up the cell from the given properties
     * @param setup the properties to setup with
     */
    public void setupCell(BasicCellMOSetup<?> setup) {
        setTransform(BasicCellMOHelper.getCellTransform(setup));
        setLocalBounds(BasicCellMOHelper.getCellBounds(setup));
    }
    
    /**
     * Reconfigure the cell with the given properties.  This just
     * calls <code>setupCell()</code>.
     * @param setup the properties to setup with
     */
    public void reconfigureCell(BasicCellMOSetup<?> setup) {
        // just call setupCell, since there is nothing to do differently
        // if this is a change
        setupCell(setup);
    }
    
}

