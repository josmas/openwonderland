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
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.setup.BasicCellMOHelper;
import org.jdesktop.wonderland.server.setup.BasicCellMOSetup;

/**
 * Server side representation of a cell
 * 
 * @author paulby
 */
@ExperimentalAPI
public class CellMO implements ManagedObject, Serializable {

    private ManagedReference<CellMO> parentRef=null;
    private ArrayList<ManagedReference<CellMO>> childCellRefs = null;
    private CellTransform transform = null;
    protected CellID cellID;
    private BoundingVolume localBounds;
    
    // a check if there is a bounds change that has not been committed.  If
    // there are uncommitted bounds changes, certain operations (like 
    // getting the computed VW bounds) are not valid
    private transient boolean boundsChanged = false;
    
    private String name=null;
    
    private boolean live = false;
    
    protected ManagedReference<Channel> cellChannelRef = null;
    
    protected static Logger logger = Logger.getLogger(CellMO.class.getName());
    
    private short priority;
    
    // ManagedReferences of ClientSessions
    protected HashSet<ManagedReference<ClientSession>> clientSessionRefs = null;
    
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
        boundsChanged = true;
        
        if (live) {
            BoundsManager.get().cellBoundsChanged(cellID, bounds);
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
            throw new IllegalStateException("Cell is not live");
        
        if (boundsChanged)
            throw new IllegalStateException("LocalBounds have been changed, "
                    + "cached bounds not valid until the transaction commits");
        
        return BoundsManager.get().getCachedVWBounds(cellID);
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
            throw new IllegalStateException("Cell is not live");
        
        if (boundsChanged)
            throw new IllegalStateException("LocalBounds have been changed, "
                    + "cached bounds not valid until the transaction commits");
        
        return BoundsManager.get().getComputedWorldBounds(cellID);   
    }
   
    /**
     * Get the local to VWorld transform of this cells origin. This call
     * can only be made on live cells, an IllegalStateException will be thrown
     * if the cell is not live.
     * 
     * TODO - should we create our own exception type ?
     * 
     * @return
     */
    CellTransform getLocalToVWorld() {
        if (!live)
            throw new IllegalStateException("Unsupported Operation");
        
        if (boundsChanged)
            throw new IllegalStateException("LocalBounds have been changed, "
                    + "cached bounds not valid until the transaction commits");
        
        return BoundsManager.get().getLocalToVWorld(cellID);
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
            childCellRefs = new ArrayList<ManagedReference<CellMO>>();
        
        child.setParent(AppContext.getDataManager().createReference(this));
        
        childCellRefs.add(AppContext.getDataManager().createReference(child));
        
        if (live) {
           child.setLive(true);
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
     * Return the collection of children references for this cell. 
     * If this cell has no children an empty collection is returned.
     * Users of this call should not make changes to the collection directly
     * 
     * @return a collection of references to the children of this cell.
     */
    public Collection<ManagedReference<CellMO>> getAllChildrenRefs() {
        if (childCellRefs==null)
            return new ArrayList<ManagedReference<CellMO>>();
        
        return childCellRefs;
    }
        
    /**
     *  Return the cell which is the parentRef of this cell, null if this not
     * attached to a parentRef
     */
    public CellMO getParent() {
        if (parentRef==null)
            return null;
        return parentRef.get();                
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
            BoundsManager.get().cellTransformChanged(cellID, transform);
        }
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
     * Notify the client that the contents of the cell have changed
     */
    public void contentChanged() {
        logger.severe("CellMO.contentChanged NOT IMPLEMENTED");
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
     * world root and are present in the world, non-live cells are not
     * @param live
     */
    void setLive(boolean live) {
        this.live = live;
        if (live) {
            openChannel();
            BoundsManager.get().createBounds(this);
            if (getParent()!=null) { // Root cell has a null parent
//                    System.out.println("setLive "+getCellID()+" "+getParent().getCellID());
                BoundsManager.get().cellChildrenChanged(getParent().getCellID(), cellID, true);
            }
        } else {
            closeChannel();
            BoundsManager.get().cellChildrenChanged(getParent().getCellID(), cellID, false);
            BoundsManager.get().removeBounds(this);
        }
        
        for(ManagedReference<CellMO> ref : getAllChildrenRefs()) {
            CellMO child = ref.get();
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
    protected void defaultOpenChannel() {
        ChannelManager cm = AppContext.getChannelManager();
        Channel cellChannel = cm.createChannel(Delivery.RELIABLE);
        
        DataManager dm = AppContext.getDataManager();
        cellChannelRef = dm.createReference(cellChannel);
    }
    
    /**
     * Cells that have a channel should overload this method to actually open the
     * channel. The convenience method defaultOpenChannel can be used to open the channel
     * with a default channel name. Called when the cell is made live.
     */
    protected void openChannel() {
    }
    
    /**
     * Close the cells channel. Called when the cell is no longer live.
     */
    protected void closeChannel() {
        
    }
    
    /**
     * Add a client session with the specified capabilities to this cell. 
     * Called by the ViewCellCacheMO as part of makeing a cell active
     * 
     * @param session
     * @param capabilities
     * @return
     */
    protected CellSessionProperties addSession(ClientSession session, 
                                            ClientCapabilities capabilities) {
        addUserToCellChannel(session);
        
        return new CellSessionProperties();
    }
    
    /**
     * Called to notify the cell that some aspect of the client sessions capabilities
     * have changed. This call is made from the ViewCellCacheOperations exectue
     * method returned by addSession.
     * 
     * @param session
     * @param capabilities
     * @return
     */
    protected CellSessionProperties changeSession(ClientSession session, 
                                               ClientCapabilities capabilities) {
        return new CellSessionProperties();
        
    }
    
    /**
     * Remove this cell from the specified session
     * 
     * @param session
     */
    protected void removeSession(ClientSession session) {
        removeUserFromCellChannel(session);
    }

    /**
     * Add user to the cells channel, if there is no channel simply return
     * @param userID
     */
    private void addUserToCellChannel(ClientSession session) {
        if (cellChannelRef == null)
            return;
            
        cellChannelRef.get().join(session);
    }
    
    /**
     * Remove user from the cells channel
     * @param userID
     */
    private void removeUserFromCellChannel(ClientSession session) {
        if (cellChannelRef == null)
            return;
            
        cellChannelRef.get().leave(session);        
    }
     
    /**
     * Handle messages sent to this cell.
     * @param sender a sender that can be used to send messages back to 
     * the client that originated this message.
     * @param session the session that sent the message
     * @param message the message to handle
     */
    protected void messageReceived(WonderlandClientSender sender,
                                   ClientSession session, 
                                   CellMessage message)
    {
        throw new RuntimeException("Not Implemented");
    }
    
    /**
     * Returns the fully qualified name of the class that represents
     * this cell on the client
     */
    protected String getClientCellClassName(ClientCapabilities capabilities) {
//        throw new RuntimeException("Not Implemented");
        return "dummy";
    }
    
    /**
     * Get the setupdata for this cell. Subclasses should overload to
     * return their specific setup object.
     */
    protected CellSetup getClientSetupData(ClientCapabilities capabilities) {
        return null;
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

    /**
     * Return the priorty of the cell. A cells priority dictates the order
     * in which it is loaded by a client. Priortity 0 cells are loaded first, 
     * followed by subsequent priority levels. Priority is only a hint to the 
     * client, it has no effect on the server
     * 
     * The default priority is 5
     * 
     * @return
     */
    public short getPriority() {
        return priority;
    }

    /**
     * Set the cell priority
     * 
     * The default priority is 5
     * 
     * @param priority
     */
    public void setPriority(short priority) {
        this.priority = priority;
    }
    
}

