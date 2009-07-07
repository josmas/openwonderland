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
package org.jdesktop.wonderland.server.spatial.impl;

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.kernel.KernelRunnable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ProximityListenerRecord;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;
import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;
import org.jdesktop.wonderland.server.spatial.ViewUpdateListener;

/**
 *
 * @author paulby
 */
public class SpatialCellImpl implements SpatialCell {

    private BoundingVolume worldBounds = new BoundingSphere();
    private BoundingVolume localBounds = null;
    private CellTransform localTransform = null;
    private CellTransform worldTransform = null;
    private SpatialCellImpl rootNode = null;
    private SpatialCellImpl parent = null;
    private CellID cellID;
    private BigInteger dsID;        // FOR DEBUG, the DS ID of the cell

    private ArrayList<SpatialCellImpl> children = null;

    private ReentrantReadWriteLock readWriteLock = null;

    private WorldBoundsChangeListener boundsChangeListener = null;
    private TransformChangeListener worldTransformChangeListener=null;

    private HashSet<Space> spaces = null;

    private HashMap<ViewCache, HashSet<Space>> viewCache = null;

    private ArrayList<TransformChangeListenerSrv> transformChangeListeners = null;

    private CopyOnWriteArraySet<ViewUpdateListener> viewUpdateListeners = null;

    private boolean isRoot = false;
    private HashSet<ProximityListenerSrv> proximityListeners = null;

    public SpatialCellImpl(CellID id, BigInteger dsID) {
//        System.out.println("Creating SpatialCell "+id);
        this.cellID = id;
        this.dsID = dsID;
    }

    /**
     * Users should call acquireRootReadLock while they are using the results of this call
     * @return
     */
    public BoundingVolume getLocalBounds() {
        return localBounds;
    }

    public void setLocalBounds(BoundingVolume localBounds) {
        acquireRootWriteLock();
        try {
            this.localBounds = localBounds;

            if (rootNode!=null) {
                throw new RuntimeException("Updating bounds of live cell not supported yet");
            }
        } finally {
            releaseRootWriteLock();
        }
    }

    /**
     * Users should call acquireRootReadLock while they are using the results of this call
     * @return
     */
    public BoundingVolume getWorldBounds() {
        return worldBounds;
    }

    /**
     * Users should call acquireRootReadLock while they are using the results of this call
     * @return
     */
    public CellTransform getLocalTransform() {
        return localTransform;
    }

    public void setLocalTransform(CellTransform transform, Identity identity) {
        acquireRootWriteLock();

        try {
            this.localTransform = transform;

            if (rootNode!=null) {
                updateWorldTransform(identity);
            }
        } finally {
            releaseRootWriteLock();
        }
    }

    public CellTransform getWorldTransform() {
        return worldTransform;
    }

    public void addChild(SpatialCell child, Identity identity) {
        if (((SpatialCellImpl)child).parent!=null) {
            throw new RuntimeException("Multiple parent exception, current parent "+((SpatialCellImpl)child).parent.cellID);
        }
        
        acquireRootWriteLock();
        try {
            if (children==null) {
                children = new ArrayList();
            }

            children.add((SpatialCellImpl) child);
            ((SpatialCellImpl)child).setParent(this);

            if (rootNode!=null) {
                worldBounds.mergeLocal(((SpatialCellImpl)child).updateWorldTransform(identity));
            }
            notifyCacheChildAddedOrRemoved(this, (SpatialCellImpl)child, true);
            revalidate(); // Security revalidation, optimize this
        } finally {
            releaseRootWriteLock();
        }
    }

    void addTransformChangeListener(TransformChangeListenerSrv listener) {
        if (transformChangeListeners==null)
            transformChangeListeners = new ArrayList();

        synchronized(transformChangeListeners) {
            transformChangeListeners.add(listener);
        }
    }

    void removeTransformChangeListener(TransformChangeListenerSrv listener) {
        if (transformChangeListeners==null)
            return;

        synchronized(transformChangeListeners) {
            transformChangeListeners.remove(listener);
        }
    }

    private void notifyTransformChangeListeners(Identity identity) {
        if (transformChangeListeners==null)
            return;

        synchronized(transformChangeListeners) {
            UniverseImpl.getUniverse().scheduleTransaction(new TransformChangeNotificationTask(transformChangeListeners, cellID, localTransform, worldTransform), identity);
        }
    }

    /**
     * Listener for changes to any view to which this cell is close
     * @param viewUpdateListener
     */
    public void addViewUpdateListener(ViewUpdateListener viewUpdateListener) {
        if (viewUpdateListeners==null)
            viewUpdateListeners = new CopyOnWriteArraySet();

        synchronized(viewUpdateListeners) {
            viewUpdateListeners.add(viewUpdateListener);
        }

        if (rootNode!=null) {
            // Only root cells track caches

            // Add the listener to all the view caches
            acquireRootReadLock();
            HashMap<ViewCache, HashSet<Space>> rootViewCaches = ((SpatialCellImpl)getRoot()).viewCache;
            if (rootViewCaches!=null) {
                for(ViewCache cache : rootViewCaches.keySet()) {
                    cache.addViewUpdateListener(cellID, viewUpdateListener);
                }
            }
            releaseRootReadLock();
        }
    }

    /**
     * TODO Implement
     * @param listener
     */
    public void removeViewUpdateListener(ViewUpdateListener viewUpdateListener) {
        if (viewUpdateListeners==null)
            return;

        synchronized(viewUpdateListeners) {
            viewUpdateListeners.remove(viewUpdateListener);
        }

        if (rootNode!=null) {
            // Only root cells track caches

            // Add the listener to all the view caches
            acquireRootReadLock();
            HashMap<ViewCache, HashSet<Space>> rootViewCaches = ((SpatialCellImpl)getRoot()).viewCache;
            if (rootViewCaches!=null) {
                for(ViewCache cache : rootViewCaches.keySet()) {
                    cache.removeViewUpdateListener(cellID, viewUpdateListener);
                }
            }
            releaseRootReadLock();
        }
    }

    Iterator<ViewUpdateListener> getViewUpdateListeners() {
        if (viewUpdateListeners==null)
            return null;

        return viewUpdateListeners.iterator();
    }

    /**
     * Update the world transform of this node and all it's children iterating
     * down the graph, then coming back up set the world bounds correctly
     * Return the world bounds
     * @return
     */
    private BoundingVolume updateWorldTransform(Identity identity) {
        CellTransform oldWorld;
        boolean transformChanged = false;
        if (worldTransform==null)
            oldWorld=null;
        else
            oldWorld = worldTransform.clone(null);

        if (parent!=null) {
            CellTransform parentWorld = parent.worldTransform;
            worldTransform = parentWorld.mul(localTransform);
        } else {
            worldTransform = localTransform.clone(null);
        }

        if (!worldTransform.equals(oldWorld)) {         // TODO should be epsilonEquals
            if (worldTransformChangeListener!=null)     // For view cells
                worldTransformChangeListener.transformChanged(this);
            transformChanged = true;
        }

        computeWorldBounds();

        if (children!=null) {
            for(SpatialCellImpl s : children) {
                worldBounds.mergeLocal(s.updateWorldTransform(identity));
            }
        }

        if (transformChanged) {
            notifyViewCaches(worldTransform);
            notifyTransformChangeListeners(identity);
        }
        
        return worldBounds;
    }
    
    /**
     * Compute the world bounds for this node from the local bounds and world transform
     */
    private void computeWorldBounds() {
        worldBounds = localBounds.clone(worldBounds);
        worldTransform.transform(worldBounds);

        if (isRoot) {
            HashSet<Space> oldSpaces = (HashSet<Space>) spaces.clone();

            // Root cell
            // Check which spaces the bounds intersect with
            Iterable<Space> it = UniverseImpl.getUniverse().getSpaceManager().getEnclosingSpace(worldBounds);
            for(Space s : it) {
                if (!spaces.contains(s)) {
                    s.addRootSpatialCell(this);
                    spaces.add(s);
                } else {
                    oldSpaces.remove(s);
                }
            }

            // Remove this cell from spaces it no longer intersects with
            for(Space s : oldSpaces) {
//                System.err.println("Removing cell from space "+s.getName());
                s.removeRootSpatialCell(this);
                spaces.remove(s);
            }

//            StringBuffer buf = new StringBuffer("Cell "+getCellID()+" in spaces ");
//            for(Space sp : spaces) {
//                buf.append(sp.getName()+" ");
//            }
//            System.err.println(buf.toString());
        }
    }

    Iterable<SpatialCellImpl> getChildren() {
        return children;
    }

    public void removeChild(SpatialCell child) {
        if (children==null)
            return;

        acquireRootWriteLock();
        try {
            System.err.println("SpatialCellImpl.removeChild() "+child);
            children.remove(child);
            ((SpatialCellImpl)child).setParent(null);
            notifyCacheChildAddedOrRemoved(this, (SpatialCellImpl)child, false);
        } finally {
            releaseRootWriteLock();
        }
    }

    public void setAttribute(Object attr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CellID getCellID() {
        return cellID;
    }

    /**
     * Return the root node of the graph that contains this node
     * @return
     */
    SpatialCell getRoot() {
        return rootNode;
    }

    /**
     * Set the root for this node and all it's children
     * @param root
     */
    void setRoot(SpatialCell root, Identity identity) {
        this.rootNode = (SpatialCellImpl) root;

        try {
            if (root==this) {
                readWriteLock = new ReentrantReadWriteLock(true);
                viewCache = new HashMap();
                isRoot = true;
                spaces = new HashSet();
                acquireRootWriteLock();

                // This node is the root of a graph, so set the world transform & bounds
                updateWorldTransform(identity);
            }

            if (isRoot) {
                if (root==null) {
                    // Root is being removed
                    for(Space s : spaces) {
                        s.removeRootSpatialCell(this);
                    }
                    spaces.clear();
                }
            }

            if (children!=null) {
                for(SpatialCellImpl s : children)
                    s.setRoot(root, identity);
            }
        } finally {
            if (root==this) {
                releaseRootWriteLock();
            }
        }
    }

    void setParent(SpatialCellImpl parent) {
        this.parent = parent;
    }

    SpatialCell getParent() {
        return parent;
    }

    private void acquireRootWriteLock() {
        if (rootNode!=null)
            rootNode.readWriteLock.writeLock().lock();
    }
    
    private void releaseRootWriteLock() {
        if (rootNode!=null)
            rootNode.readWriteLock.writeLock().unlock();;
    }

    public void acquireRootReadLock() {
        if (rootNode!=null)
            rootNode.readWriteLock.readLock().lock();
    }
    
    public void releaseRootReadLock() {
        if (rootNode!=null)
            rootNode.readWriteLock.readLock().unlock();
    }

    /**
     * Only called for root cells
     * The cell has entered a space, record all the view caches that
     * are now interested in this cell.
     *
     * A view cache can span a number of spaces, so the viewCache structure
     * maintains the set of spaces per ViewCache (effectively a reference count)
     *
     * @param caches
     * @param space
     */
    void addViewCache(Collection<ViewCache> caches, Space space) {
        synchronized(viewCache) {
            for(ViewCache c : caches) {
                HashSet<Space> s = viewCache.get(c);
                if (s==null) {
                    s = new HashSet();
                    s.add(space);
                    viewCache.put(c, s);
                } else {
                    s.add(space);
                }

            }
        }

        // Notify all cells in the graph that caches have
        // been added. TODO this could be optimized to only notify
        // children that have expressed an interest
        try {
            acquireRootReadLock();
            viewCachesAddedOrRemoved(caches, true, this);
        } finally {
            releaseRootReadLock();
        }
    }

    void removeViewCache(Collection<ViewCache> caches, Space space) {
        synchronized(viewCache) {
            for(ViewCache c : caches) {
                HashSet<Space> s = viewCache.get(c);
                if (s==null) {
                    throw new RuntimeException("ERROR, cache not in set");
                } else {
                    s.remove(space);
                }
                
                if (s.size()==0) {
                    viewCache.remove(c);
                }
            }
        }

        // Notify all cells in the graph that caches have
        // been added. TODO this could be optimized to only notify
        // children that have expressed an interest
        try {
            acquireRootReadLock();
            viewCachesAddedOrRemoved(caches, false, this);
        } finally {
            releaseRootReadLock();
        }
    }

    void viewCachesAddedOrRemoved(Collection<ViewCache> caches, boolean added, SpatialCellImpl cell) {
        if (viewUpdateListeners!=null) {
            for(ViewUpdateListener viewUpdateListener : viewUpdateListeners) {
                for(ViewCache c : caches) {
                    if (added) {
                        c.addViewUpdateListener(cellID, viewUpdateListener);
                    } else {
                        c.removeViewUpdateListener(cellID, viewUpdateListener);
                    }
                }
            }
        }

        if (children!=null) {
            for(SpatialCellImpl child : children) {
                child.viewCachesAddedOrRemoved(caches, added, child);
            }
        }
    }


    /**
     * Notify the ViewCaches that the worldTransform of this root cell
     * has changed
     * @param worldTransform
     */
    private void notifyViewCaches(CellTransform worldTransform) {
        // Called from updateWorldBounds so we have a write lock on the graph
        SpatialCellImpl root = (SpatialCellImpl) getRoot();
        if (root==null)
            return;

        Iterable<ViewCache> caches = root.viewCache.keySet();
        for(ViewCache cache : caches) {
            cache.cellMoved(this, worldTransform);
        }
    }

    /**
     * Notify the view caches that this cell needs to be revalidated
     */
    public void revalidate() {
        // Called from updateWorldBounds so we have a write lock on the graph
        SpatialCellImpl root = (SpatialCellImpl) getRoot();
        if (root==null)
            return;

        System.err.println("REVALIDATING CELL");
        Iterable<ViewCache> caches = root.viewCache.keySet();
        for(ViewCache cache : caches) {
            System.err.println("  notifying cache "+cache);
            cache.cellRevalidated(this);
        }
    }

    private void notifyCacheChildAddedOrRemoved(SpatialCellImpl parent, SpatialCellImpl child, boolean added) {
        SpatialCellImpl root = (SpatialCellImpl) getRoot();
        if (root==null)
            return;

        System.err.println("notifyCacheChildAddedOrRemoved "+added+"  "+root.viewCache.size());

        Iterable<ViewCache> caches = root.viewCache.keySet();
        for(ViewCache cache : caches) {
            if (added)
                cache.childCellAdded(child);
            else
                cache.childCellRemoved(parent, child);
        }

    }

    public void destroy() {
        acquireRootWriteLock();

        try {
            SpatialCellImpl root = (SpatialCellImpl) getRoot();
            if (root==null)
                return;

            Iterable<ViewCache> caches = root.viewCache.keySet();
            for(ViewCache cache : caches) {
                cache.cellDestroyed(this);
            }

            if (isRoot) {
                // This is a root node
                for(Space space : spaces) {
                    space.removeRootSpatialCell(this);
                }
            }
        } finally {
            releaseRootWriteLock();
        }
    }

    public interface WorldBoundsChangeListener {
        public void worldBoundsChanged(SpatialCell cell);
    }

    public interface TransformChangeListener {
        public void transformChanged(SpatialCell cell);
    }


    class TransformChangeNotificationTask implements KernelRunnable {

        private TransformChangeListenerSrv[] listeners;
        private CellID cellID;
        private CellTransform localTransform;
        private CellTransform worldTransform;
        private BoundingVolume worldBounds;

        public TransformChangeNotificationTask(Collection<TransformChangeListenerSrv> transformListeners, CellID cellID, CellTransform localTransform, CellTransform worldTransform) {
            listeners = transformListeners.toArray(new TransformChangeListenerSrv[transformListeners.size()]);
            this.cellID = cellID;
            this.localTransform = localTransform.clone(null);
            this.worldTransform = worldTransform.clone(null);
        }

        public String getBaseTaskType() {
            return KernelRunnable.class.getName();
        }

        public void run() throws Exception {
            ManagedReference<CellMO> cellRef = AppContext.getDataManager().createReference(CellManagerMO.getCell(cellID));
            for(int i=0; i<listeners.length; i++) {
                TransformChangeListenerSrv listener = listeners[i];
                if (listener instanceof ManagedReference)
                    ((ManagedReference<TransformChangeListenerSrv>)listener).get().transformChanged(cellRef, localTransform, worldTransform);
                else
                    listener.transformChanged(cellRef, localTransform, worldTransform);
            }
        }

    }
}
