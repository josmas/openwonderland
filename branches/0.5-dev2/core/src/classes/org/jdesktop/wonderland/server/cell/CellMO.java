/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.server.cell;

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.cell.setup.CellComponentSetup;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.setup.BasicCellSetupHelper;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;
import org.jdesktop.wonderland.server.spatial.UniverseManager;
import org.jdesktop.wonderland.server.spatial.UniverseManagerFactory;

/**
 * Superclass for all server side representation of a cell
 * 
 * @author paulby
 */
@ExperimentalAPI
public abstract class CellMO implements ManagedObject, Serializable, BeanSetupMO {

    private ManagedReference<CellMO> parentRef=null;
    private ArrayList<ManagedReference<CellMO>> childCellRefs = null;
    private CellTransform localTransform = null;
    protected CellID cellID;
    private BoundingVolume localBounds;
    private CellID parentCellID;
    
    // a check if there is a bounds change that has not been committed.  If
    // there are uncommitted bounds changes, certain operations (like 
    // getting the computed VW bounds) are not valid
//    private transient boolean boundsChanged = false;
    
    private String name=null;
    
    private boolean live = false;
    
    protected ManagedReference<Channel> cellChannelRef = null;
    
    protected static Logger logger = Logger.getLogger(CellMO.class.getName());
    
    private short priority;
    
    // ManagedReferences of ClientSessions
    protected HashSet<ManagedReference<ClientSession>> clientSessionRefs = null;
    
    private HashMap<Class, ManagedReference<CellComponentMO>> components = new HashMap();
    
    private CellTransform worldTransform = new CellTransform(new Quaternion(), new Vector3f(), new Vector3f());
    private BoundingVolume vwBounds=null;        // Bounds in VW coordinates
    private boolean isMovable=false;            // Is this cell movable
    private boolean isParentMovable = false;    // Is a parent of this cell movable
    private HashSet<TransformChangeListenerSrv> transformChangeListeners=null;
    
    /** Default constructor, used when the cell is created via WFS */
    public CellMO() {
        this.localBounds = null;
        this.localTransform = null;
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
        this.localTransform = transform;
        setLocalBounds(localBounds);

    }
    
    /**
     * Set the bounds of the cell in cell local coordinates
     * @param bounds
     */
    public void setLocalBounds(BoundingVolume bounds) {
        localBounds = bounds.clone(null);
        if (live) {
            throw new RuntimeException("SetBounds on live cells is not implemented yet");
//            UniverseManager.getUniverseManager().setLocalBounds(bounds);
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
        
        return UniverseManagerFactory.getUniverseManager().getWorldBounds(this, null);
    }
   
    /**
     * Get the world transform of this cells origin. This call
     * can only be made on live cells, an IllegalStateException will be thrown
     * if the cell is not live.
     * 
     * TODO - should we create our own exception type ?
     * 
     * @param result the CellTransform to populate with the result and return, 
     * can be null in which case a new CellTransform will be returned.
     * @return
     */
    public CellTransform getWorldTransform(CellTransform result) {
        if (!live)
            throw new IllegalStateException("Unsupported Operation, only valid for a live Cell "+this.getClass().getName());
        
        return UniverseManagerFactory.getUniverseManager().getWorldTransform(this, result);
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
        
        child.setParent(this);
        
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
                UniverseManagerFactory.getUniverseManager().removeChild(this, child);
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
     * Return the cellID of the parent.  This method was added for debugging
     * and is used by SpaceMO to check that the lists are ordered correctly.
     * 
     * TODO remove
     * 
     * @return
     */
    CellID getParentCellID() {
        return parentCellID;
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
     * @param parent the parent cell
     * @throws org.jdesktop.wonderland.common.cell.MultipleParentException
     */
    void setParent(CellMO parent) throws MultipleParentException {
        if (parent!=null && parentRef!=null)
            throw new MultipleParentException();
        
        if (parent==null) {
            this.parentRef = null;
            this.parentCellID = CellID.getInvalidCellID();
        } else {
            this.parentRef = AppContext.getDataManager().createReference(parent);
            this.parentCellID = parent.getCellID();
        }
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
        
        this.localTransform = (CellTransform) transform.clone(null);

        if (live)
            UniverseManagerFactory.getUniverseManager().setLocalTransform(this, localTransform);
    }
    

    /**
     * Return the cells transform
     * 
     * @return return a clone of the transform
     */
    public CellTransform getLocalTransform(CellTransform result) {
        return (CellTransform) localTransform.clone(result);
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
            if (localBounds==null) {
                logger.severe("CELL HAS NULL BOUNDS, defaulting to unit sphere");
                localBounds = new BoundingSphere(1f, new Vector3f());
            }

            addToUniverse(UniverseManagerFactory.getUniverseManager());
        } else {
            removeFromUniverse(UniverseManagerFactory.getUniverseManager());
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
     * Add this cell to the universe
     */
    void addToUniverse(UniverseManager universe) {
        universe.createCell(this);
        System.err.println("CREATING SPATIAL CELL " + getCellID().toString() + " " + this.getClass().getName());

        if (transformChangeListeners != null) {
            for (TransformChangeListenerSrv listener : transformChangeListeners) {
                universe.addTransformChangeListener(this, listener);
            }
        }

        if (parentRef != null) {
            universe.addChild(parentRef.getForUpdate(), this);
        }
    }

    /**
     * Remove this cell from the universe
     */
    void removeFromUniverse(UniverseManager universe) {
        universe.removeCell(this);
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
     * @param clientID the ID of the client that is being added
     * @param capabilities
     * @return
     */
    protected CellSessionProperties addClient(WonderlandClientID clientID,
                                            ClientCapabilities capabilities) {
        ChannelComponentMO chan = getComponent(ChannelComponentMO.class);
        if (chan!=null) {
            chan.addUserToCellChannel(clientID);
        }
        
        return new CellSessionProperties(getViewCellCacheRevalidationListener(), 
                getClientCellClassName(clientID, capabilities),
                getCellConfig(clientID, capabilities));
    }
    
    /**
     * Called to notify the cell that some aspect of the client sessions capabilities
     * have changed. This call is made from the ViewCellCacheOperations exectue
     * method returned by addSession.
     * 
     * @param clientID
     * @param capabilities
     * @return
     */
    protected CellSessionProperties changeClient(WonderlandClientID clientID,
                                               ClientCapabilities capabilities) {
        return new CellSessionProperties(getViewCellCacheRevalidationListener(), 
                getClientCellClassName(clientID, capabilities),
                getCellConfig(clientID, capabilities));
        
    }
    
    /**
     * Remove this cell from the specified session, only applicable to cells
     * with a ChannelComponent. This modifies the ChannelComponent for this cell
     * (if it exists) but does not modify the CellMO itself.
     * 
     * @param clientID
     */
    protected void removeSession(WonderlandClientID clientID) {
        ChannelComponentMO chan = getComponent(ChannelComponentMO.class);
        if (chan!=null) {
            chan.removeUserFromCellChannel(clientID);
        }
    }

    /**
     * Returns the fully qualified name of the class that represents
     * this cell on the client
     */
    protected abstract String getClientCellClassName(WonderlandClientID clientID,
                                                     ClientCapabilities capabilities);
    
    /**
     * Get the cellconfig for this cell. Subclasses should overload to
     * return their specific setup object.
     */
    protected CellConfig getCellConfig(WonderlandClientID clientID,
                                       ClientCapabilities capabilities)
    {
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
    public void setupCell(BasicCellSetup setup) {
        // Set up the transform (origin, rotation, scaling) and cell bounds
        setLocalTransform(BasicCellSetupHelper.getCellTransform(setup));
        setLocalBounds(BasicCellSetupHelper.getCellBounds(setup));
        
        // For all components in the setup class, create the component classes
        // and setup them up and add to the cell.
        for (CellComponentSetup compSetup : setup.getCellComponentSetups()) {
            String className = compSetup.getServerComponentClassName();
            try {
                Class clazz = Class.forName(className);
                Constructor<CellComponentMO> constructor = clazz.getConstructor(CellMO.class);
                CellComponentMO comp = constructor.newInstance(this);
                comp.setupCellComponent(compSetup);
                this.addComponent(comp);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Reconfigure the cell with the given properties.  This just
     * calls <code>setupCell()</code>.
     * @param setup the properties to setup with
     */
    public void reconfigureCell(BasicCellSetup setup) {
        // just call setupCell, since there is nothing to do differently
        // if this is a change
        setupCell(setup);
    }

    /**
     * Returns the setup information currently configured on the cell. If the
     * setup argument is non-null, fill in that object and return it. If the
     * setup argument is null, create a new setup object.
     * 
     * @param setup The setup object, if null, creates one.
     * @return The current setup information
     */
    public BasicCellSetup getCellSetup(BasicCellSetup setup) {
        // In the case of CellMO, if the 'setup' parameter is null, it means
        // it was not created by the super class. In which case, this class
        // should just return null
        if (setup == null) {
            return null;
        }
        
        // Fill in the details about the origin, rotation, and scaling
        setup.setBounds(BasicCellSetupHelper.getSetupBounds(localBounds));
        setup.setOrigin(BasicCellSetupHelper.getSetupOrigin(localTransform));
        setup.setRotation(BasicCellSetupHelper.getSetupRotation(localTransform));
        setup.setScaling(BasicCellSetupHelper.getSetupScaling(localTransform));

        // add setups for each component
        List<CellComponentSetup> setups = new LinkedList<CellComponentSetup>();
        for (ManagedReference<CellComponentMO> componentRef : components.values()) {
            CellComponentMO component = componentRef.get();
            CellComponentSetup compSetup = component.getCellComponentSetup(null);
            if (compSetup != null) {
                setups.add(compSetup);
            }
        }
        setup.setCellComponentSetups(setups.toArray(new CellComponentSetup[0]));

        return setup;
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
        addComponent(component, component.getClass());
    }

    public void addComponent(CellComponentMO component, Class componentClass) {
        ManagedReference<CellComponentMO> previous = components.put(componentClass, 
                AppContext.getDataManager().createReference(component));
        if (previous!=null)
            throw new IllegalArgumentException("Adding duplicate component of class "+component.getClass().getName()); 
    }
    
    
    /**
     * Add a TransformChangeListener to this cell. The listener will be
     * called for any changes to the cells transform. The listener can either
     * be a Serialized object, or an instance of ManagedReference. Both types
     * are handled correctly.
     * 
     * Listeners should generally execute quickly, if they take a long time
     * it is recommended that the listener schedules a new task to service
     * the callback.
     * 
     * @param listener to add
     */
    public void addTransformChangeListener(TransformChangeListenerSrv listener) {
        if (transformChangeListeners==null)
            transformChangeListeners = new HashSet();
        transformChangeListeners.add(listener);

        if (isLive())
            UniverseManagerFactory.getUniverseManager().addTransformChangeListener(this, listener);

    }
    
    /**
     * Remove the specified listener.
     * @param listener to be removed
     */
    public void removeTransformChangeListener(TransformChangeListenerSrv listener) {
        transformChangeListeners.remove(listener);
        if (isLive())
            UniverseManagerFactory.getUniverseManager().removeTransformChangeListener(this, listener);
    }
    
}
