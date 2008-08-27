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
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.setup.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.TimeManager;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.setup.BasicCellMOHelper;
import org.jdesktop.wonderland.server.setup.BasicCellMOSetup;

/**
 * Superclass for all server side representation of a cell
 * 
 * @author paulby
 */
@ExperimentalAPI
public abstract class CellMO implements ManagedObject, Serializable {

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
    
    private HashSet<SpaceInfo> inSpaces = new HashSet();
    
    private HashMap<Class, ManagedReference<CellComponentMO>> components = new HashMap();
    
    private CellTransform local2VWorld = new CellTransform(new Quaternion(), new Vector3f(), new Vector3f());
    private BoundingVolume vwBounds=null;        // Bounds in VW coordinates
    private boolean isStatic=false;
    private HashSet<ManagedReference<TransformChangeListenerMO>> transformChangeListeners=null;
    
    /** Default constructor, used when the cell is created via WFS */
    public CellMO() {
        this.localBounds = null;
        this.transform = null;
        this.cellID = WonderlandContext.getCellManager().createCellID(this);
    }
    
    /**
     * Create a CellMO with the specified localBounds and transform.
     * If either parameter is null an IllegalArgumentException will be thrown.
     * @param localBounds the bounds of the new cell, must not be null
     * @param transform the transform for this cell, must not be null
     */
    public CellMO(BoundingVolume localBounds, CellTransform transform) {
        if (localBounds==null)
            throw new IllegalArgumentException("localBounds must not be null");
        if (transform==null)
            throw new IllegalArgumentException("transform must not be null");
        
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
        
//        if (live) {
//            BoundsManager.get().cellBoundsChanged(cellID, bounds);
//        }
        if (live) {
            calcWorldBounds();
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
    public BoundingVolume getWorldBounds() {
        if (!live)
            throw new IllegalStateException("Cell is not live");
        
//        if (boundsChanged) {
//            CellTransform t = computeLocalToVWorld(this);
////            logger.warning("LocalBounds have been changed, "
////                    + "cached bounds not valid until the transaction commits ");
//            BoundingVolume ret = getLocalBounds();
//                    
//            ret = ret.transform(t.getRotation(null), t.getTranslation(null), t.getScaling(null));
//            return ret;
//        }
//        
//        return BoundsManager.get().getCachedVWBounds(cellID);
        
        if (vwBounds==null) {
            logger.severe("NULL BOUNDS "+getName());
            return null;
        }
        
        return vwBounds.clone(null);
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
    public CellTransform getLocalToWorld() {
        if (!live)
            throw new IllegalStateException("Unsupported Operation, only valid for a live Cell");
        
//        if (boundsChanged) {
//            logger.warning("LocalBounds have been changed, "
//                    + "cached bounds not valid until the transaction commits, so computing on the fly...");
//            return computeLocalToVWorld(this);
//        }
//        
//        return BoundsManager.get().getLocalToVWorld(cellID);
        return (CellTransform) local2VWorld.clone();
    }
    
    private CellTransform computeLocalToWorld(CellMO currentCell) {
        if (currentCell instanceof RootCellMO)
            return currentCell.getLocalTransform();
        
        CellTransform ret = currentCell.computeLocalToWorld(currentCell.getParent());
        ret.mul(currentCell.getLocalTransform());
        return ret;
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
        
        notifySpacesDetach(TimeManager.getWonderlandTime());
        
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
     * This method is usually called during cell construction or from
     * reconfigureCell. If you want a cell that moves regularly around the
     * world use MovableComponent.
     * 
     * @param transform
     */
    protected void setLocalTransform(CellTransform transform) {
        if (isStatic() && isLive()) {
            throw new RuntimeException("Modifying Static Cell");
        } 
        
        this.transform = (CellTransform) transform.clone();  
        
//        if (live) {
//            BoundsManager.get().cellTransformChanged(cellID, transform);
//        }
        
        if (live) {
            processTransformChange();
            notifySpacesTransformChanged(transform, TimeManager.getWonderlandTime());
        }
    }
    
    /**
     * Notify children that the transform of the parent node has changed
     * @param parent
     */
    private void processTransformChange() {
        calcLocal2World();
        calcWorldBounds();
            
        Collection<ManagedReference<CellMO>> childrenRef = getAllChildrenRefs();
        for(ManagedReference<CellMO> childRef : childrenRef) {
            childRef.getForUpdate().processTransformChange();
        }
        
        notifyTransformChangeListeners();
    }
    
    /**
     * Calculate the vw bounds
     */
    private void calcWorldBounds() {
        assert(live);
        vwBounds = localBounds.clone(vwBounds);
        local2VWorld.transform(vwBounds);        
    }
    
    /**
     * Calculate the local2VWorld transform
     */
    private void calcLocal2World() {
        assert(live);
        local2VWorld = (CellTransform) transform.clone();
        if (parentRef!=null) {
             local2VWorld.mul(parentRef.get().getLocalTransform());  
        }
    }
    
    /**
     * Return the cells transform
     * 
     * @return return a clone of the transform
     */
    public CellTransform getLocalTransform() {
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
        if (this.live==live)
            return;
        
        this.live = live;
        
        if (live) {
//            BoundsManager.get().createBounds(this);
            if (getParent()!=null) { // Root cell has a null parent
//                    System.out.println("setLive "+getCellID()+" "+getParent().getCellID());
//                BoundsManager.get().cellChildrenChanged(getParent().getCellID(), cellID, true);
            }
            calcLocal2World();
            calcWorldBounds();
        } else {
//            BoundsManager.get().cellChildrenChanged(getParent().getCellID(), cellID, false);
//            BoundsManager.get().removeBounds(this);
        }
        
        // Notify all components of new live state
        Collection<ManagedReference<CellComponentMO>> compList = components.values();
        for(ManagedReference<CellComponentMO> c : compList) {
            c.get().setLive(live);
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
     * Add a client session with the specified capabilities to this cell. 
     * Called by the ViewCellCacheMO as part of makeing a cell active, only 
     * applicable to cells with a ChannelComponent.
     * 
     * @param session
     * @param capabilities
     * @return
     */
    protected CellSessionProperties addSession(ClientSession session, 
                                            ClientCapabilities capabilities) {
        ChannelComponentMO chan = getComponent(ChannelComponentMO.class);
        if (chan!=null) {
            chan.addUserToCellChannel(session);
        }
        
        return new CellSessionProperties(getViewCellCacheRevalidationListener(), 
                getClientCellClassName(session, capabilities),
                getClientSetupData(session, capabilities));
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
        return new CellSessionProperties(getViewCellCacheRevalidationListener(), 
                getClientCellClassName(session, capabilities),
                getClientSetupData(session, capabilities));
        
    }
    
    /**
     * Remove this cell from the specified session, only applicable to cells
     * with a ChannelComponent. This modifies the ChannelComponent for this cell
     * (if it exists) but does not modify the CellMO itself.
     * 
     * @param session
     */
    protected void removeSession(ClientSession session) {
        ChannelComponentMO chan = getComponent(ChannelComponentMO.class);
        if (chan!=null) {
            chan.removeUserFromCellChannel(session);
        }
    }

    /**
     * Returns the fully qualified name of the class that represents
     * this cell on the client
     */
    protected abstract String getClientCellClassName(ClientSession clientSession,ClientCapabilities capabilities);
    
    /**
     * Get the setupdata for this cell. Subclasses should overload to
     * return their specific setup object.
     */
    protected CellSetup getClientSetupData(ClientSession clientSession, ClientCapabilities capabilities) {
        return null;
    }
    
    /**
     * Returns the ViewCacheOperation, or null
     * @return
     */
    protected ViewCellCacheRevalidationListener getViewCellCacheRevalidationListener() {
        return null;
    }
    
    
    /**
     * Set up the cell from the given properties
     * @param setup the properties to setup with
     */
    public void setupCell(BasicCellMOSetup<?> setup) {
        setLocalTransform(BasicCellMOHelper.getCellTransform(setup));
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
     * Set the cell priority. The priority must be >=0 otherwise an 
     * IllegalArgumentException will be thrown.
     * 
     * The default priority is 5
     * 
     * @param priority
     */
    public void setPriority(short priority) {
        if (priority<0)
            throw new IllegalArgumentException("priorty must be >= 0");
        
        this.priority = priority;
    }
    
    /**
     * If this cell supports the capabilities of cellComponent then
     * return an instance of cellComponent associated with this cell. Otherwise
     * return null.
     * 
     * @see MovableCellComponent
     * @param cellComponent
     * @return
     */
    public <T extends CellComponentMO> T getComponent(Class<T> cellComponentClass) {
        assert(CellComponentMO.class.isAssignableFrom(cellComponentClass));
        ManagedReference<CellComponentMO> comp = components.get(cellComponentClass);
        if (comp==null)
            return null;
        return (T) comp.get();
    }
    
    /**
     * Add a component to this cell. Only a single instance of each component
     * class can be added to a cell. Adding duplicate components will result in
     * an IllegalArgumentException 
     * 
     * @param component
     */
    public void addComponent(CellComponentMO component) {
        ManagedReference<CellComponentMO> previous = components.put(component.getClass(), 
                AppContext.getDataManager().createReference(component));
        if (previous!=null)
            throw new IllegalArgumentException("Adding duplicate component of class "+component.getClass().getName()); 
    }
    
    /**
     * Return the leaf spaces which this cell is in.
     * A cell is in a space when the origin of the cell is contained
     * within the bounds of the space, and that space is a leaf space, ie is has 
     * no child spaces.
     * 
     * The returned collection is a shallow clone of the underlying data so 
     * modifications to the underlying data or return data can take place without
     * causing ConcurrentModificationExceptions.
     * 
     * @return
     */
    public Collection<SpaceInfo> inSpaces() {
        return (Collection<SpaceInfo>) inSpaces.clone();
    }
    
    /**
     * Return the number of spaces this cell is currently in
     * 
     * @return
     */
    public int numInSpaces() {
        return inSpaces.size();
    }
    
    /**
     * Static cells do not change in any way, so their state is not checked
     * periodically. If a cell changes in any way (moves, content changes, etc)
     * then isStatic will be false;
     * 
     * @return
     */
    public boolean isStatic() {
        return isStatic;
    }
    
    /**
     * Set the isStatic property of this cell
     * @param isStatic
     */
    public void setStatic(boolean isStatic) {
        if (isLive()) 
            throw new RuntimeException("Changing staic state of live cells is not currently supported");
        
        this.isStatic = isStatic;
    }
    
    /**
     * Add this cell to the space.
     * The space must be live otherwise an IllegalArgumentException will be thrown.
     * @param space
     */
    void addToSpace(SpaceMO space) {
        inSpaces.add(new SpaceInfo(space));
        space.addCell(this);
    }
    
    void removeFromSpace(SpaceMO space) {
        inSpaces.remove(new SpaceInfo(space));
        space.removeCell(this);
    }
    
    /**
     * Update the transform timestamp in the cell descriptions
     * @param timestamp
     */
    private void notifySpacesTransformChanged(CellTransform transform, long timestamp) {
        boolean spaceTransition = false;
        
        ArrayList<SpaceMO> removeList = null;
        
        for(SpaceInfo spaceInfo : inSpaces) {
            if (spaceInfo.getSpaceBounds().intersects(vwBounds)) {
                // Cell still in space
                spaceInfo.getSpaceRef().getForUpdate().notifyCellTransformChanged(this, timestamp);
            } else {
                if (removeList==null)
                     removeList = new ArrayList();
                
                // Cell left space
                removeList.add(spaceInfo.getSpaceRef().getForUpdate());
                spaceTransition = true;
            }
        }
        
        
        if (spaceTransition) {
            for(SpaceMO remove : removeList)
                removeFromSpace(remove);
            
            // This is really too expensive to do every transform change, so we only do this when we leave a space
            // Besides we should avoid space overlap where possible
            SpaceMO[] spaces = WonderlandContext.getCellManager().getEnclosingSpace(transform.getTranslation(null));
            ArrayList<SpaceMO> addList = new ArrayList();
            for(SpaceMO space : spaces) {
                if (!inSpaces.contains(this)) {
                    addList.add(space);
                }
            }
            
            for(SpaceMO space : addList) {
                addToSpace(space);
            }
        }
    }
    
    private void notifySpacesDetach(long timestamp) {
        for(SpaceInfo spaceInfo : inSpaces) {
            spaceInfo.getSpaceRef().getForUpdate().notifyCellDetached(this, timestamp);
        }
    }
    
    /**
     * Add a TransformChangeListener to this cell. The listener will be
     * called for any changes to the cells transform
     * 
     * @param listener to add
     */
    public void addTransformChangeListener(TransformChangeListenerMO listener) {
        if (transformChangeListeners==null)
            transformChangeListeners = new HashSet();
        transformChangeListeners.add(AppContext.getDataManager().createReference(listener));
    }
    
    /**
     * Remove the specified listener.
     * @param listener to be removed
     */
    public void removeTransformChangeListener(TransformChangeListenerMO listener) {
        transformChangeListeners.remove(listener);
    }
    
    private void notifyTransformChangeListeners() {
        if (transformChangeListeners==null)
            return;
        
        // Dispatch listener notifications in new tasks
        final ManagedReference<CellMO> thisRef = AppContext.getDataManager().createReference(this);
        final CellTransform newLocal = (CellTransform) transform.clone();
        final CellTransform newL2VW = (CellTransform) local2VWorld.clone();
        
        for(final ManagedReference<TransformChangeListenerMO> listenerRef : transformChangeListeners) {
            AppContext.getTaskManager().scheduleTask(new TransformChangeNotifierTask(listenerRef, thisRef, newLocal, local2VWorld));
        }
    }
    
    public static class SpaceInfo implements Serializable {
        private ManagedReference<SpaceMO> space;
        private BoundingVolume spaceBounds;
        private SpaceID spaceID;
        
        public SpaceInfo(SpaceMO space) {
            this.space = AppContext.getDataManager().createReference(space);
            spaceBounds = null;
            spaceID = space.getSpaceID();
        }
        
        public BoundingVolume getSpaceBounds() {
            if (spaceBounds==null)
                spaceBounds = space.get().getWorldBounds(null);
            return spaceBounds;
        }
        
        /**
         * Return the managed reference for the space
         * @return
         */
        public ManagedReference<SpaceMO> getSpaceRef() {
            return space;
        }
        
        public SpaceID getSpaceID() {
            return spaceID;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.space != null ? this.space.hashCode() : 0);
            return hash;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof SpaceMO && ((SpaceMO)o).getSpaceID().equals(spaceID))
                return true;
            
            if (!(o instanceof SpaceInfo))
                return false;
            return ((SpaceInfo)o).space.equals(space);
        }
        
    }
}

