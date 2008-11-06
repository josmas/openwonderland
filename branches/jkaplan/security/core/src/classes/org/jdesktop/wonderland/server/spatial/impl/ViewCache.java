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

import com.jme.bounding.BoundingVolume;
import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.DataService;
import com.sun.sgs.service.TransactionProxy;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.CellDescription;
import org.jdesktop.wonderland.server.cell.ViewCellCacheMO;

/**
 *
 * @author paulby
 */
class ViewCache {

    private SpaceManager spaceManager;
    private SpatialCellImpl viewCell;
    private HashMap<SpatialCellImpl, Integer> rootCells = new HashMap(); // The rootCells visible in this cache

    private HashSet<Space> spaces = new HashSet();              // Spaces that intersect this caches world bounds

    private Vector3f lastSpaceValidationPoint = null;   // The view cells location on the last space revalidation

    private static final float REVAL_DISTANCE_SQUARED = (float) Math.pow(SpaceManagerGridImpl.SPACE_SIZE/4, 2);
//    private static final float REVAL_DISTANCE_SQUARED = 1f;

    private LinkedList<CacheUpdate> pendingCacheUpdates = new LinkedList();

    private ScheduledExecutorService spaceLeftProcessor = Executors.newSingleThreadScheduledExecutor();

    private Identity identity;
    private CacheProcessor cacheProcessor;
    private BigInteger cellCacheId;
    private DataService dataService;

    public ViewCache(SpatialCellImpl cell, SpaceManager spaceManager, Identity identity, BigInteger cellCacheId) {
        this.viewCell = cell;
        this.spaceManager = spaceManager;
        this.identity = identity;
        this.cellCacheId = cellCacheId;
        cacheProcessor = new CacheProcessor();
        cacheProcessor.start();
        dataService = UniverseImpl.getUniverse().getDataService();
    }

    private void viewCellMoved(CellTransform worldTransform) {
        if (lastSpaceValidationPoint==null ||
            lastSpaceValidationPoint.distanceSquared(worldTransform.getTranslation(null))>REVAL_DISTANCE_SQUARED) {

            revalidateSpaces();
            lastSpaceValidationPoint = worldTransform.getTranslation(null);
        }

    }

    void login() {
        cellMoved(viewCell, viewCell.getWorldTransform());
    }

    void logout() {

        cacheProcessor.quit();

        synchronized(spaces) {
            for(Space sp : spaces)
                sp.removeViewCache(this);
            spaces.clear();
        }
        
        System.err.println("-----------------> LOGOUT");
    }

    void cellMoved(SpatialCellImpl cell, CellTransform worldTransform) {
        if (cell==viewCell) {
            // Process view movement immediately
            viewCellMoved(worldTransform);
        } else {
            synchronized(pendingCacheUpdates) {
                pendingCacheUpdates.add(new CacheUpdate(cell, worldTransform));
            }
        }
    }

    /**
     * Called to add a root cell when the root cell is added to a space with which
     * this cache is already registered
     * @param rootCell
     */
    void rootCellAdded(SpatialCellImpl rootCell) {
        viewCell.acquireRootReadLock();

        synchronized(pendingCacheUpdates) {
            pendingCacheUpdates.add(new CacheUpdate(rootCell, true));
        }

        viewCell.releaseRootReadLock();
    }

    void rootCellRemoved(SpatialCellImpl rootCell) {
        viewCell.acquireRootReadLock();

        synchronized(pendingCacheUpdates) {
            pendingCacheUpdates.add(new CacheUpdate(rootCell, false));
        }

        viewCell.releaseRootReadLock();
    }
    /**
     * Update the set of spaces which intersect with this caches world bounds
     */
    private void revalidateSpaces() {
        viewCell.acquireRootReadLock();

        HashSet<Space> oldSpaces = (HashSet<Space>) spaces.clone();

        Iterable<Space> newSpaces = spaceManager.getEnclosingSpace(viewCell.getWorldBounds());
        for(Space sp : newSpaces) {
            if (spaces.add(sp)) {
                // Entered a new space
                synchronized(pendingCacheUpdates) {
                    pendingCacheUpdates.add(new CacheUpdate(sp, true));
                }
            }
            oldSpaces.remove(sp);
            sp.addViewCache(this);
        }

//        System.out.println("Old spaces "+oldSpaces.size());
        for(Space sp : oldSpaces) {
            sp.removeViewCache(this);
            spaces.remove(sp);
            // We don't remove the space cells immediately in case the user
            // is moving along the border of the space

            // TODO this is flawed, we need to track pending removes and make changes
            // if the user moves so that the cell is visible again.
//            spaceLeftProcessor.schedule(new CacheUpdate(sp, false), 30, TimeUnit.SECONDS);
            // In the meantime update the cache immediately
            synchronized(pendingCacheUpdates) {
                pendingCacheUpdates.add(new CacheUpdate(sp,false));
            }
        }

        System.out.print("ViewCell moved, current spaces ");
        for(Space sp : spaces) {
            System.out.print(sp.getName()+", ");
        }
        System.out.println();

        viewCell.releaseRootReadLock();
    }

    class CacheProcessor extends Thread {
        boolean quit = false;

        public void run() {
            Collection<CacheUpdate> updateList;

            while(!quit) {
                synchronized(pendingCacheUpdates) {
                    updateList = (Collection<CacheUpdate>) pendingCacheUpdates.clone();
                    pendingCacheUpdates.clear();
                }

                for(CacheUpdate update : updateList) {
                    update.run();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
            }
        }

        public void quit() {
            quit = true;
            cacheProcessor.interrupt();
        }
    }


    class CacheUpdate implements Runnable {

        private SpatialCellImpl cell;
        private CellTransform worldTransform;
        private Space space;

        private static final int VIEW_MOVED = 0;
        private static final int CELL_MOVED = 1;
        private static final int EXIT_SPACE = 2;
        private static final int ENTER_SPACE = 3;
        private static final int ROOT_ADDED = 4;
        private static final int ROOT_REMOVED = 5;

        private int jobType;

        /**
         * A cacheUpdate caused by a cell updating it's worldTransform
         * @param cell
         * @param worldTransform
         */
        public CacheUpdate(SpatialCellImpl cell, CellTransform worldTransform) {
            this.cell = cell;
            this.worldTransform = worldTransform;
            if (cell==viewCell)
                jobType = VIEW_MOVED;
            else
                jobType = CELL_MOVED;
        }

        /**
         * Enter or exit a space
         * @param space
         * @param enter
         */
        public CacheUpdate(Space space, boolean enter) {
            if (enter) {
                jobType = ENTER_SPACE;
            } else {
                jobType = EXIT_SPACE;
            }
            this.space = space;
        }

        /**
         * RootCell added/removed
         * @param rootCell
         * @param add
         */
        public CacheUpdate(SpatialCellImpl rootCell, boolean add) {
            jobType = (add) ? ROOT_ADDED : ROOT_REMOVED;
            this.cell = rootCell;
        }

        public void run() {
            Collection<SpatialCellImpl> spaceRoots;

            switch(jobType) {
                case VIEW_MOVED:
                    viewCellMoved(worldTransform);
                    break;
                case CELL_MOVED:
                    // Check for cache enter/exit
                    break;
                case EXIT_SPACE:
                    ArrayList<CellDescription> oldCells = new ArrayList();
                    spaceRoots = space.getRootCells();

                    synchronized(rootCells) {
                        for(SpatialCellImpl root : spaceRoots) {
                            removeRootCellImpl(root, oldCells);
                        }
                    }

                    UniverseImpl.getUniverse().scheduleTransaction(new ViewCacheUpdateTask((Collection<CellDescription>) oldCells.clone(), false), identity);
                    break;
                case ENTER_SPACE:
                    ArrayList<CellDescription> newCells = new ArrayList();
                    spaceRoots = space.getRootCells();

                    System.err.println("EnteringSpace "+space.getName()+"  roots "+spaceRoots.size());

                    synchronized(rootCells) {
                        for(SpatialCellImpl root : spaceRoots) {
                            addRootCellImpl(root, newCells);
                        }
                    }

                    UniverseImpl.getUniverse().scheduleTransaction(new ViewCacheUpdateTask((Collection<CellDescription>) newCells, true), identity);
                    break;
                case ROOT_ADDED:
                    addRootCell(cell);
                    break;
                case ROOT_REMOVED:
                    removeRootCell(cell);
                    break;
            }
        }

        /**
         * Called to add a root cell when the cell is added to a space with which
         * this cache is already registered
         * @param rootCell
         */
        private void addRootCell(SpatialCellImpl rootCell) {
            ArrayList<CellDescription> newCells = new ArrayList();
            synchronized(rootCells) {
                addRootCellImpl(rootCell, newCells);
            }

            System.out.println("RootCell Added "+rootCell.getCellID()+"  "+newCells.size());
            UniverseImpl.getUniverse().scheduleTransaction(new ViewCacheUpdateTask((Collection<CellDescription>) newCells, true), identity);
        }

        /**
         * Called to add a root cell when the cell is added to a space with which
         * this cache is already registered
         * @param rootCell
         */
        private void removeRootCell(SpatialCellImpl rootCell) {
            ArrayList<CellDescription> newCells = new ArrayList();
            synchronized(rootCells) {
                removeRootCellImpl(rootCell, newCells);
            }

            UniverseImpl.getUniverse().scheduleTransaction(new ViewCacheUpdateTask((Collection<CellDescription>) newCells, false), identity);
        }

        /**
         * Callers must be synchronized on rootCells
         * @param root
         * @param newCells
         */
        private void addRootCellImpl(SpatialCellImpl root, ArrayList<CellDescription> newCells) {
            Integer refCount = rootCells.get(root);
            if (refCount==null) {
                root.acquireRootReadLock();

                newCells.add(new CellDesc(root.getCellID()));
                addChildCells(newCells, root);

                root.releaseRootReadLock();
                refCount=1;
            } else
                refCount++;
            rootCells.put(cell, refCount);
        }

        /**
         * Callers must be synchronized on rootCells
         * @param root
         * @param newCells
         */
        private void removeRootCellImpl(SpatialCellImpl root, ArrayList<CellDescription> oldCells) {
            Integer refCount = rootCells.get(cell);

            if (refCount==null)
                return;

            if (refCount==1) {
                root.acquireRootReadLock();

                oldCells.add(new CellDesc(root.getCellID()));
                addChildCells(oldCells, root);

                root.releaseRootReadLock();
                rootCells.remove(cell);
            } else {
                refCount--;
                rootCells.put(cell, refCount);
            }
        }

        private void addChildCells(ArrayList<CellDescription> cells, SpatialCellImpl parent) {
            if (parent.getChildren()==null)
                return;
            
            for(SpatialCellImpl child : parent.getChildren()) {
                cells.add(new CellDesc(child.getCellID()));
                addChildCells(cells, child);
            }
        }

    }

    class ViewCacheUpdateTask implements KernelRunnable {

        private Collection<CellDescription> cells;
        private boolean loadCells;

        public ViewCacheUpdateTask(Collection<CellDescription> newCells, boolean loadCells) {
            cells = newCells;
            this.loadCells = loadCells;
        }

        public String getBaseTaskType() {
            return KernelRunnable.class.getName();
        }

        public void run() throws Exception {
            ViewCellCacheMO cacheMO = (ViewCellCacheMO) dataService.createReferenceForId(cellCacheId).get();
            if (loadCells)
                cacheMO.generateLoadMessagesService(cells);
            else
                cacheMO.generateUnloadMessagesService(cells);
            
//            System.err.println("--------> DS UpdateTask "+viewCell.getCellID()+"  loading "+loadCells);
//            for(CellDescription c : cells)
//                System.err.print(c.getCellID()+", ");
//            System.err.println();
        }

    }

    class CellDesc implements CellDescription {

        private CellID cellID;

        public CellDesc(CellID cellID) {
            this.cellID = cellID;
        }

        public CellID getCellID() {
            return cellID;
        }

        public long getContentsTimestamp() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getTransformTimestamp() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public BoundingVolume getLocalBounds() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public BoundingVolume getWorldBounds() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setWorldBounds(BoundingVolume worldBounds) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public CellTransform getLocalTransform() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setLocalTransform(CellTransform localTransform, long timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public short getPriority() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Class getCellClass() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isMovable() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
