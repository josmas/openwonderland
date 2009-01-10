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
package org.jdesktop.wonderland.client.cell;

import com.jme.bounding.BoundingVolume;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.common.cell.config.CellConfig;

/**
 * The client side representation of a cell. Cells are created via the 
 * CellCache and should not be instantiated directly by the user on the client.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class Cell {
    private BoundingVolume cachedVWBounds;
    private BoundingVolume computedWorldBounds;
    private BoundingVolume localBounds;
    private Cell parent;
    private ArrayList<Cell> children = null;
    private CellTransform localTransform;
    private CellTransform local2VW = new CellTransform(null, null);
    private CellTransform worldTransform = new CellTransform(null, null);
    private CellID cellID;
    private String name=null;
    private CellStatus currentStatus = CellStatus.DISK;
    private CellCache cellCache;
    
    private HashMap<Class, CellComponent> components = new HashMap<Class, CellComponent>();
    
    /**
     * An enum representing the various render types supported by Wonderland.
     * A Cell represents the state, the renderer the visual representation of
     * that state.
     * 
     */
    public enum RendererType {

        /**
         * A 3D renderer for the JME client
         */
        RENDERER_JME,
        /**
         * A 2D rendering (not yet implemented)
         */
        RENDERER_2D,
        /**
         * No Renderer
         */
        NONE,
        /**
         * Low end 3D rendering, cell phone renderer etc, TBD
         */
    };
    
    private HashMap<RendererType, CellRenderer> cellRenderers = new HashMap();
    
    /**
     * The logger for Cell (and possibly it's subclasses)
     */
    protected static Logger logger = Logger.getLogger(Cell.class.getName());
    
    private HashSet<TransformChangeListener> transformChangeListeners = null;
    
    /**
     * Instantiate a new cell
     * @param cellID the cells unique ID
     * @param cellCache the cell cache which instantiated, and owns, this cell
     */
    public Cell(CellID cellID, CellCache cellCache) {
        this.cellID = cellID;
        this.cellCache = cellCache;
        
        logger.fine("Cell: Creating new Cell ID=" + cellID);
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
     * Return the list of children for this cell, or an empty list if there
     * are no children
     * @return
     */
    public List<Cell> getChildren() {
        if (children==null)
            return new ArrayList<Cell>(0);
        
        synchronized(children) {
            return (List<Cell>) children.clone();
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
     * Remove the specified cell from the set of children of this cell.
     * Returns silently if the supplied cell is not a child of this cell.
     * 
     * TODO Test me
     * 
     * @param child
     */
    public void removeChild(Cell child) {
        if (children==null)
            return;
        
        synchronized(children) {
            if (children.remove(child)) {
                child.setParent(null);
            }            
        }
    }
    
    /**
     * Return this cells instance of the specified component class if defined. Otherwise
     * return null.
     * 
     * @param <T> The class of the component being queried
     * @return the cells component of the requested class (or null)
     */
    public <T extends CellComponent> T getComponent(Class<T> cellComponentClass) {
        return (T) components.get(cellComponentClass);
    }
    
    /**
     * Add a component to this cell. Only a single instance of each component
     * class can be added to a cell. Adding duplicate components will result in
     * an IllegalArgumentException.
     * 
     * When a component is added component.setStatus is called automatically with
     * the current status of this cell.
     * 
     * @param component the componnet to be added
     */
    public void addComponent(CellComponent component) {
        addComponent(component, component.getClass());
    }

    /**
     * Add a component to this cell, with the specified componentClass. This allows for specialized
     * subclasses to be registered with a higher level interface/class.
     * Only a single instance of each component
     * class can be added to a cell. Adding duplicate components will result in
     * an IllegalArgumentException.
     *
     * When a component is added component.setStatus is called automatically with
     * the current status of this cell.
     *
     * @param component the componnet to be added
     */
    public void addComponent(CellComponent component, Class componentClass) {
        CellComponent previous = components.put(componentClass,component);
        if (previous!=null)
            throw new IllegalArgumentException("Adding duplicate component of class "+component.getClass().getName()); 
        synchronized(currentStatus) {
            component.setStatus(currentStatus);
        }
    }
    
    /**
     * Remove the cell component of the specified class, the components
     * setStatus method will be called with CellStatus.DISK to trigger cleanup
     * of any component state.
     * 
     * TODO Test me
     *  
     * @param componentClass
     */
    public void removeComponent(Class<? extends CellComponent> componentClass) {
        CellComponent component = components.remove(componentClass);
        component.setStatus(CellStatus.DISK);
    }
    
    /**
     * Return a collection of all the components in this cell.
     * The collection is a clone of the internal data structure, so this is a
     * snapshot of the component set.
     * 
     * @return
     */
    public Collection<CellComponent> getComponents() {
        return new ArrayList<CellComponent>(components.values());
    }
    
    /**
     * Set the parent of this cell, called from addChild and removeChild
     * @param parent
     */
    void setParent(Cell parent) {
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
        
        synchronized(children) {
            return children.size();
        }
    }
    
    /**
     * Return the transform for this cell
     * @return
     */
    public CellTransform getLocalTransform() {
        if (localTransform==null)
            return null;
        return (CellTransform) localTransform.clone(null);
    }
    
    /**
     * Set the transform for this cell.
     * 
     * Users should not call this method directly, rather MovableComponent should
     * be used, which will keep the client and server in sync.
     * 
     * @param localTransform
     */
    void setLocalTransform(CellTransform localTransform, TransformChangeListener.ChangeSource source) {
        // Don't process the same transform twice
        if (this.localTransform!=null && this.localTransform.equals(localTransform))
            return;

        if (localTransform==null) {
            this.localTransform=null;
            // Get parent worldTransform
            Cell current=getParent();
            while(current!=null) {
                CellTransform parentWorldTransform = current.getWorldTransform();
                if (parentWorldTransform!=null) {
                    setWorldTransform(parentWorldTransform, source);  // this method also calls notifyTransformChangeListeners
                    current = null;
                } else
                    current = current.getParent();
            }
        } else {
            this.localTransform = (CellTransform) localTransform.clone(null);
            if (parent!=null) {
                worldTransform = (CellTransform) localTransform.clone(null);
                worldTransform = worldTransform.mul(parent.getWorldTransform());
                cachedVWBounds = localBounds.clone(cachedVWBounds);
                worldTransform.transform(cachedVWBounds);                

                local2VW = null;
            } else if (parent==null) { // ROOT
                worldTransform = (CellTransform)localTransform.clone(null);
                local2VW = null;
                
                cachedVWBounds = localBounds.clone(cachedVWBounds);               
                worldTransform.transform(cachedVWBounds);                
            }
            
            notifyTransformChangeListeners(source);
        }
        
        if (cachedVWBounds==null) {
            logger.warning("********** NULL cachedVWBounds "+getName() +"  "+localBounds+"  "+localTransform);
            Thread.dumpStack();
        }
                
        for(Cell child : getChildren())
            transformTreeUpdate(this, child, source);      

        // Notify Renderers that the cell has moved
        for(CellRenderer rend : cellRenderers.values())
            rend.cellTransformUpdate(worldTransform);

    }
        
    /**
     * Return the local to Virtual World transform for this cell.
     * @return cells local to VWorld transform
     */
    public CellTransform getLocalToWorldTransform() {
        if (local2VW==null) {
            local2VW = worldTransform.clone(null);
            local2VW.invert();
        } 
        return (CellTransform) local2VW.clone(null);
    }

    /**
     * Return the world transform of the cell.
     *
     * @return the world transform of this cell.
     */
    public CellTransform getWorldTransform() {
        return (CellTransform)worldTransform.clone(null);
    }
    
    
    /**
     * Set the localToVWorld transform for this cell
     * @param localToVWorld
     */
    void setWorldTransform(CellTransform worldTransform, TransformChangeListener.ChangeSource source) {
        worldTransform = (CellTransform) worldTransform.clone(null);
        cachedVWBounds = localBounds.clone(cachedVWBounds);
        worldTransform.transform(cachedVWBounds);
        local2VW = null; // force local2VW to be recalculated
        
        notifyTransformChangeListeners(source);
    }
    
    /**
     * Compute the local to vworld of the cell, this for test purposes only
     * @param parent
     * @return
     */
//    private CellTransform computeLocal2VWorld(Cell cell) {
//        LinkedList<CellTransform> transformStack = new LinkedList<CellTransform>();
//        
//        // Get the root
//        Cell current=cell;
//        while(current.getParent()!=null) {
//            transformStack.addFirst(current.localTransform);
//            current = current.getParent();
//        }
//        CellTransform ret = new CellTransform(null, null);
//        for(CellTransform t : transformStack) {
//            if (t!=null)
//                ret.mul(t);
//        }
//        
//        return ret;
//    }

    /**
     * Update local2VWorld and bounds of child and all its children recursively 
     * to reflect changes in a parent
     * 
     * @param parent
     * @param child
     * @return the combined bounds of the child and all it's children
     */
    private BoundingVolume transformTreeUpdate(Cell parent, Cell child, TransformChangeListener.ChangeSource source) {
        CellTransform parentWorldTransform = parent.getWorldTransform();

        CellTransform childTransform = child.getLocalTransform();
        
        if (childTransform!=null) {
            childTransform.mul(parentWorldTransform);
            child.setWorldTransform(childTransform, source);
        } else {
            child.setWorldTransform(parentWorldTransform, source);
        }
        
        BoundingVolume ret = child.getWorldBounds();
        
        Iterator<Cell> it = child.getChildren().iterator();
        while(it.hasNext()) {
            ret.mergeLocal(transformTreeUpdate(child, it.next(), source));
        }
        
        child.setWorldBounds(ret);
                
        return null;
    }
    
    /**
     * Returns the world bounds, this is the local bounds transformed into VW 
     * coordinates. These bounds do not include the subgraph bounds. This call 
     * is only valid for live cells.
     * 
     * @return world bounds
     */
    public BoundingVolume getWorldBounds() {
        return cachedVWBounds;
    }

    /**
     * Set the World Bounds for this cell
     * @param cachedVWBounds
     */
    private void setWorldBounds(BoundingVolume cachedVWBounds) {
        this.cachedVWBounds = cachedVWBounds;
    }
    
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

    /**
     * Get the local bounds for this cell. Local bounds are in the cells
     * coordinate system
     * 
     * @return local bounds for this cell
     */
    public BoundingVolume getLocalBounds() {
        return localBounds.clone(null);
    }

    /**
     * Set the local bounds for this cell
     * @param localBounds
     */
    public void setLocalBounds(BoundingVolume localBounds) {
        this.localBounds = localBounds;
    }

    /**
     * Return the cell cache which instantiated and owns this cell.
     * @return the cell cache which instantiated this cell.
     */
    public CellCache getCellCache() {
        return cellCache;
    }
    
    /**
     * Returns the status of this cell
     * Cell states
     *
     * DISK - Cell is on disk with no memory footprint
     * BOUNDS - Cell object is in memory with bounds initialized, NO geometry is loaded
     * INACTIVE - All cell data is in memory
     * ACTIVE - Cell is within the avatars proximity bounds
     * VISIBLE - Cell is in the view frustum
     *
     * @return returns CellStatus
     */
    public CellStatus getStatus() {
        return this.currentStatus;
    }
    
    /**
     * Set the status of this cell
     *
     *
     * Cell states
     *
     * DISK - Cell is on disk with no memory footprint
     * BOUNDS - Cell object is in memory with bounds initialized, NO geometry is loaded
     * INACTIVE - All cell data is in memory
     * ACTIVE - Cell is within the avatars proximity bounds
     * VISIBLE - Cell is in the view frustum
     * 
     * The system guarantees that if a change is made between non adjacent status, say from BOUNDS to VISIBLE
     * that setStatus will automatically be called for the intermediate values.
     * 
     * If you overload this method in your own class you must call super.setStatus(...) as the first operation
     * in your method.
     *
     * @param status the cell status
     * @return true if the status was changed, false if the new and previous status are the same
     */
    public boolean setStatus(CellStatus status) {
        synchronized(currentStatus) {
            if (currentStatus==status)
                return false;

            int ord = status.ordinal();
            int currentOrd = currentStatus.ordinal();
            if (ord>currentOrd+1 || ord<currentOrd-1) {
                int t = currentOrd;
                int dir = (ord>currentOrd ? 1 : -1);
                setStatus(CellStatus.values()[t+dir]);
            }

            currentStatus = status;

            for(CellComponent component : components.values())
                component.setStatus(status);

            for(CellRenderer renderer : cellRenderers.values()) {
                renderer.setStatus(status);
            }

            switch(status) {
                case DISK :
                    if (transformChangeListeners!=null) {
                        transformChangeListeners.clear();
                        transformChangeListeners = null;
                    }

                    if (components!=null) {
                        components.clear();
                    }
                    break;
            }

            CellManager.getCellManager().notifyCellStatusChange(this, status);
        }
        return true;
    }
    
    /**
     * Called when the cell is initially created and any time there is a 
     * major configuration change. The cell will already be attached to it's parent
     * before the initial call of this method
     * 
     * @param configData the configuration data for the cell
     */
    public void configure(CellConfig configData) {

        System.err.println("configure cell "+getCellID()+"  "+getClass());
        // Install the CellComponents
        for(String compClassname : configData.getClientComponentClasses()) {
            try {
                Class compClazz = Class.forName(compClassname);
                if (!components.containsKey(compClazz)) {
                    logger.warning("Installing component "+compClassname);
                    Constructor<CellComponent> constructor = compClazz.getConstructor(Cell.class);
                    CellComponent comp = constructor.newInstance(this);
                    addComponent(comp, comp.getLookupClass());
                }
            } catch (InstantiationException ex) {
                logger.log(Level.SEVERE, "Instantiation exception for class "+compClassname+"  in cell "+getClass().getName(), ex);
            } catch (IllegalAccessException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    /**
     * Create the renderer for this cell
     * @param rendererType The type of renderer required
     * @return the renderer for the specified type if available, or null
     */
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        Logger.getAnonymousLogger().warning(this.getClass().getName()+" createCellRenderer returning null");
        return null;
    }
    
    /**
     * Return the renderer of the given type for this cell. If a renderer of the
     * requested type is not available null will be returned
     * @param rendererType the type of the render to return
     * @return the renderer, or null if no renderer of the specified type is available
     */
    public CellRenderer getCellRenderer(RendererType rendererType) {
        CellRenderer ret = cellRenderers.get(rendererType);
        if (ret==null) {
            ret = createCellRenderer(rendererType);
            if (ret!=null) {
                cellRenderers.put(rendererType, ret);
                ret.setStatus(currentStatus);
            }
        }
        
        return ret;
    }
    
    /**
     * Add a TransformChangeListener to this cell. The listener will be
     * called for any changes to the cells transform
     * 
     * @param listener to add
     */
    public void addTransformChangeListener(TransformChangeListener listener) {
        if (transformChangeListeners==null)
            transformChangeListeners = new HashSet();
        transformChangeListeners.add(listener);
    }
    
    /**
     * Remove the specified listener.
     * @param listener to be removed
     */
    public void removeTransformChangeListener(TransformChangeListener listener) {
        transformChangeListeners.remove(listener);
    }
    
    private void notifyTransformChangeListeners(TransformChangeListener.ChangeSource source) {
        if (transformChangeListeners==null)
            return;
        
        for(TransformChangeListener listener : transformChangeListeners)
            listener.transformChanged(this, source);
    }
}
