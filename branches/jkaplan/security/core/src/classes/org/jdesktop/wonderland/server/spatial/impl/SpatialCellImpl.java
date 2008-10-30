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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.server.spatial.SpatialCell;

/**
 *
 * @author paulby
 */
public class SpatialCellImpl implements SpatialCell {

    private BoundingVolume worldBounds = new BoundingSphere();
    private BoundingVolume localBounds = null;
    private Matrix4f localTransform = null;
    private Matrix4f worldTransform = null;
    private SpatialCellImpl rootNode = null;
    private SpatialCellImpl parent = null;
    private CellID cellID;

    private ArrayList<SpatialCellImpl> children = null;

    private ReentrantReadWriteLock readWriteLock = null;

    private WorldBoundsChangeListener boundsChangeListener = null;
    private TransformChangeListener worldTransformChangeListener=null;

    private HashSet<Space> spaces = null;

    private HashMap<ViewCache, Integer> viewCache = null;

    public SpatialCellImpl(CellID id) {
//        System.out.println("Creating SpatialCell "+id);
        this.cellID = id;
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
    public Matrix4f getLocalTransform() {
        return localTransform;
    }

    public void setLocalTransform(Matrix4f transform) {
        acquireRootWriteLock();
        this.localTransform = transform;
        
        if (rootNode!=null) {
            updateWorldTransform();
        }
        releaseRootWriteLock();
    }

    public Matrix4f getWorldTransform() {
        return worldTransform;
    }

    public void addChild(SpatialCell child) {
        if (((SpatialCellImpl)child).parent!=null)
            throw new RuntimeException("Multiple parent exception");
        
        acquireRootWriteLock();

        if (children==null) {
            children = new ArrayList();
        }
        
        children.add((SpatialCellImpl) child);
        ((SpatialCellImpl)child).setParent(this);

        if (rootNode!=null) {
            worldBounds.mergeLocal(((SpatialCellImpl)child).updateWorldTransform());
        }
        releaseRootWriteLock();
    }


    /**
     * Update the world transform of this node and all it's children iterating
     * down the graph, then coming back up set the world bounds correctly
     * Return the world bounds
     * @return
     */
    private BoundingVolume updateWorldTransform() {
        Matrix4f oldWorld;
        if (worldTransform==null)
            oldWorld=null;
        else
            oldWorld = worldTransform.clone();

        if (parent!=null) {
            Matrix4f parentWorld = parent.worldTransform;
            worldTransform = parentWorld.mult(localTransform);
        } else {
            worldTransform = localTransform.clone();
        }

        if (!worldTransform.equals(oldWorld)) {         // TODO should be epsilonEquals
            if (worldTransformChangeListener!=null)     // For view cells
                worldTransformChangeListener.transformChanged(this);
            notifyViewCaches(worldTransform);
        }

        computeWorldBounds();

        if (children!=null) {
            for(SpatialCellImpl s : children) {
                worldBounds.mergeLocal(s.updateWorldTransform());
            }
            if (boundsChangeListener!=null)
                boundsChangeListener.worldBoundsChanged(this);
        }

        System.err.println("Cell "+cellID+"  "+worldTransform.toTranslationVector());

        return worldBounds;
    }
    
    /**
     * Compute the world bounds for this node from the local bounds and world transform
     */
    private void computeWorldBounds() {
        // TODO support scale
        worldBounds = localBounds.clone(worldBounds);
        worldBounds.transform(worldTransform.toRotationQuat(), 
                              worldTransform.toTranslationVector(), 
                              new Vector3f(1f,1f,1f), worldBounds);
    }

    Iterable getChildren() {
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
    void setRoot(SpatialCell root) {
        if (root==this) {
            readWriteLock = new ReentrantReadWriteLock(true);
            viewCache = new HashMap();

            // This node is the root of a graph, so set the world transform
            worldTransform = localTransform.clone();
            computeWorldBounds();

            if (children!=null) {
                for(SpatialCellImpl s : children)
                    worldBounds.mergeLocal(s.updateWorldTransform());
            }
        }

        // Now set the rootNode in all children
        this.rootNode = (SpatialCellImpl) root;
        acquireRootWriteLock();
        if (children!=null) {
            for(SpatialCellImpl s : children)
                s.setRoot(root);
        }
        releaseRootWriteLock();
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

    void acquireRootReadLock() {
        if (rootNode!=null)
            rootNode.readWriteLock.readLock().lock();
    }
    
    void releaseRootReadLock() {
        if (rootNode!=null)
            rootNode.readWriteLock.readLock().unlock();
    }

//    void setWorldBoundsChangeListener(WorldBoundsChangeListener listener) {
//        boundsChangeListener = listener;
//    }
//
//    void setWorldTransformChangeListener(TransformChangeListener listener) {
//        worldTransformChangeListener = listener;
//    }

//    void addSpace(Space space) {
//        synchronized(spaces) {
//            spaces.add(space);
//        }
//    }
//
//    void removeSpace(Space space) {
//        synchronized(spaces) {
//            spaces.remove(space);
//        }
//    }

    void addViewCache(Collection<ViewCache> caches) {
        synchronized(viewCache) {
            for(ViewCache c : caches) {
                Integer i = viewCache.get(c);
                if (i==null) {
                    i = new Integer(1);
                } else {
                    i++;
                }
                viewCache.put(c, i);
            }
        }
    }

    void removeViewCache(Collection<ViewCache> caches) {
        synchronized(viewCache) {
            for(ViewCache c : caches) {
                Integer i = viewCache.get(c);
                if (i==null) {
                    throw new RuntimeException("ERROR, cache not in set");
                } else {
                    i--;
                }
                
                if (i==0)
                    viewCache.remove(c);
                else
                    viewCache.put(c, i);
            }
        }
    }



    private void notifyViewCaches(Matrix4f worldTransform) {
        // Called from updateWorldBounds so we have a write lock on the graph
        SpatialCellImpl root = (SpatialCellImpl) getRoot();
        if (root==null)
            return;

        Iterable<ViewCache> caches = root.viewCache.keySet();
        for(ViewCache cache : caches) {
            cache.cellMoved(this, worldTransform);
        }
    }

    public interface WorldBoundsChangeListener {
        public void worldBoundsChanged(SpatialCellImpl cell);
    }

    public interface TransformChangeListener {
        public void transformChanged(SpatialCellImpl cell);
    }
}
