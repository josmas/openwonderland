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

import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author paulby
 */
class ViewCache {

    private SpaceManager spaceManager;
    private SpatialCellImpl viewCell;
    private HashSet<SpatialCellImpl> rootCells = new HashSet(); // The rootCells visible in this cache
    private HashSet<Space> spaces = new HashSet();              // Spaces that intersect this caches world bounds

    private Vector3f lastSpaceValidationPoint = null;   // The view cells location on the last space revalidation

    private static final float REVAL_DISTANCE_SQUARED = (float) Math.pow(SpaceManagerGridImpl.SPACE_SIZE/4, 2);

    private LinkedList<CacheUpdate> pendingCacheUpdates = new LinkedList();

    private ScheduledExecutorService spaceLeftProcessor = Executors.newSingleThreadScheduledExecutor();


    public ViewCache(SpatialCellImpl cell, SpaceManager spaceManager) {
        this.viewCell = cell;
        this.spaceManager = spaceManager;
    }

    private void viewCellMoved(Matrix4f worldTransform) {
        if (lastSpaceValidationPoint==null ||
            lastSpaceValidationPoint.distanceSquared(worldTransform.toTranslationVector())>REVAL_DISTANCE_SQUARED) {

            revalidateSpaces();
            lastSpaceValidationPoint = worldTransform.toTranslationVector();
        }

    }

    void cellMoved(SpatialCellImpl cell, Matrix4f worldTransform) {
        if (cell==viewCell) {
            // Process view movement immediately
            viewCellMoved(worldTransform);
        } else {
            System.out.println("Cell move "+worldTransform);
            synchronized(pendingCacheUpdates) {
                pendingCacheUpdates.add(new CacheUpdate(cell, worldTransform));
            }
        }
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
            spaceLeftProcessor.schedule(new CacheUpdate(sp, false), 30, TimeUnit.SECONDS);
        }

        System.out.print("ViewCell moved, current spaces ");
        for(Space sp : spaces) {
            System.out.print(sp.getName()+", ");
        }
        System.out.println();

        viewCell.releaseRootReadLock();
    }


    class CacheUpdate implements Runnable {

        private SpatialCellImpl cell;
        private Matrix4f worldTransform;
        private Space space;

        private static final int VIEW_MOVED = 0;
        private static final int CELL_MOVED = 1;
        private static final int EXIT_SPACE = 2;
        private static final int ENTER_SPACE = 3;

        private int jobType;

        /**
         * A cacheUpdate caused by a cell updating it's worldTransform
         * @param cell
         * @param worldTransform
         */
        public CacheUpdate(SpatialCellImpl cell, Matrix4f worldTransform) {
            this.cell = cell;
            this.worldTransform = worldTransform;
            if (cell==viewCell)
                jobType = VIEW_MOVED;
            else
                jobType = CELL_MOVED;
        }

        public CacheUpdate(Space space, boolean enter) {
            if (enter) {
                jobType = ENTER_SPACE;
            } else {
                jobType = EXIT_SPACE;
            }
            this.space = space;
        }

        public void run() {
            switch(jobType) {
                case VIEW_MOVED:
                    viewCellMoved(worldTransform);
                    break;
                case CELL_MOVED:
                    // Check for cache enter/exit
                    break;
                case EXIT_SPACE:
                    break;
                case ENTER_SPACE:
                    ArrayList<SpatialCellImpl> newRoots = new ArrayList();
                    Collection<SpatialCellImpl> spaceRoots = space.getRootCells();

                    for(SpatialCellImpl root : spaceRoots) {
                        if (rootCells.add(root)) {
                            newRoots.add(root);
                        }
                    }

                    // TODO notify client of new root cells

                    break;
            }
        }

    }

}
