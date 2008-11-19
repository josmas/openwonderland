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
package org.jdesktop.wonderland.server.spatial.impl;

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.kernel.KernelRunnable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;

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

    private boolean isRoot = false;

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
        this.localBounds = localBounds;

        if (rootNode!=null) {
            throw new RuntimeException("Updating bounds of live cell not supported yet");
        }
        releaseRootWriteLock();
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
        this.localTransform = transform;
        
        if (rootNode!=null) {
            updateWorldTransform(identity);
        }
        releaseRootWriteLock();
    }

    public CellTransform getWorldTransform() {
        return worldTransform;
    }

    public void addChild(SpatialCell child, Identity identity) {
        if (((SpatialCellImpl)child).parent!=null)
            throw new RuntimeException("Multiple parent exception");
        
        acquireRootWriteLock();

        if (children==null) {
            children = new ArrayList();
        }
        
        children.add((SpatialCellImpl) child);
        ((SpatialCellImpl)child).setParent(this);

        if (rootNode!=null) {
            worldBounds.mergeLocal(((SpatialCellImpl)child).updateWorldTransform(identity));
        }
        releaseRootWriteLock();
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
     * Update the world transform of this node and all it's children iterating
     * down the graph, then coming back up set the world bounds correctly
     * Return the world bounds
     * @return
     */
    private BoundingVolume updateWorldTransform(Identity identity) {
        CellTransform oldWorld;
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
            notifyViewCaches(worldTransform);
            notifyTransformChangeListeners(identity);
        }

        computeWorldBounds();

        if (children!=null) {
            for(SpatialCellImpl s : children) {
                worldBounds.mergeLocal(s.updateWorldTransform(identity));
            }
            if (boundsChangeListener!=null)
                boundsChangeListener.worldBoundsChanged(this);
        }

//        System.err.println("Cell "+cellID+"  "+worldTransform.toTranslationVector());

        return worldBounds;
    }
    
    /**
     * Compute the world bounds for this node from the local bounds and world transform
     */
    private void computeWorldBounds() {
        worldBounds = localBounds.clone(worldBounds);
        worldTransform.transform(worldBounds);

        if (isRoot) {
            // Root cell
            // Check which spaces the bounds intersect with
            Iterable<Space> it = UniverseImpl.getUniverse().getSpaceManager().getEnclosingSpace(worldBounds);
            for(Space s : it) {
                if (!spaces.contains(s)) {
                    s.addRootSpatialCell(this);
                    spaces.add(s);
                }
            }
        }
    }

    Iterable<SpatialCellImpl> getChildren() {
        return children;
    }

    public void removeChild(SpatialCell child) {
        acquireRootWriteLock();
        children.remove(child);
        releaseRootWriteLock();
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
        // Now set the rootNode in all children
        this.rootNode = (SpatialCellImpl) root;

        if (root==this) {
            readWriteLock = new ReentrantReadWriteLock(true);
            viewCache = new HashMap();
            isRoot = true;
            spaces = new HashSet();
            acquireRootWriteLock();

            // This node is the root of a graph, so set the world transform
            worldTransform = localTransform.clone(null);
            computeWorldBounds();

            if (children!=null) {
                for(SpatialCellImpl s : children)
                    worldBounds.mergeLocal(s.updateWorldTransform(identity));
            }
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

        if (root==this) {
            releaseRootWriteLock();
        }
    }

    void setParent(SpatialCellImpl parent) {
        this.parent = parent;
    }

    SpatialCellImpl getParent() {
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
                
                if (s.size()==0)
                    viewCache.remove(c);
            }
        }
    }



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

    public void destroy() {
        acquireRootWriteLock();

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

        releaseRootWriteLock();
    }

    public interface WorldBoundsChangeListener {
        public void worldBoundsChanged(SpatialCellImpl cell);
    }

    public interface TransformChangeListener {
        public void transformChanged(SpatialCellImpl cell);
    }


    class TransformChangeNotificationTask implements KernelRunnable {

        private TransformChangeListenerSrv[] listeners;
        private CellID cellID;
        private CellTransform localTransform;
        private CellTransform worldTransform;

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
