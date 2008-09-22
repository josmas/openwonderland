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
import com.jme.math.Vector3f;
import com.sun.sgs.app.ManagedReference;
import java.util.HashSet;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.Math3DUtils;

/**
 * Provides a mechanism for listener notification when the a view cell
 * enters/exits a set of bounds for a cell. 
 * 
 * The bounds must be ordered from largest to smallest, thus localBounds[i]
 * must enclose localBounds[i+1]. The listeners will be notified as the View
 * enters each subsequent bounding volume and then notified the view exits each
 * volume.
 * 
 * For example given a set of Bounding Spheres with the same center and radii of
 * 10, 5, 2. As the ViewCell moves from outside to the center of the spheres the
 * listeners will be called with
 * 
 * enter, 10
 * enter, 5 
 * enter, 2
 * 
 * then as the user moves away from the center the following sequence of exits
 * will be called
 * 
 * exit, 2
 * exit, 5
 * exit, 10
 * 
 * 
 * @author paulby
 */
@ExperimentalAPI
public class ProximityComponentMO extends CellComponentMO {

    private BoundingVolume[] localProxBounds = null;
    private BoundingVolume[] worldProxBounds = null;
    private ViewTransformListener viewTransformListener=null;
    private HashSet<ProximityListenerMO> proximityListeners = null;
    
    /**
     * Set a list of bounds for which the system will track view enter/exit for
     * this cell. When the view enters/exits one of these bounds the listener
     * will be called with the index of the bounds in the supplied array.
     * 
     * The bounds must be ordered from largest to smallest, thus localBounds[i]
     * must enclose localBounds[i+1]
     * 
     * @param cell the cell
     * @param localProximityBounds the proximity bounds in cell local coordinates
     */
    public ProximityComponentMO(CellMO cell, BoundingVolume[] localProximityBounds) {
        super(cell);
        setProximityBounds(localProximityBounds);
        
//        cell.addTransformChangeListener(new TransformChangeListenerMO() {
//            public void transformChanged(Cell cell) {
//                updateWorldBounds();
//            }
//        });
    }
    
    private void updateWorldBounds() {
        if (localProxBounds==null)
            return;

        // Update the world proximity bounds
//        CellTransform l2vw = cell.getLocalToWorldTransform();
//        int i=0;
//        synchronized(this) {
//            for(BoundingVolume lb : localProxBounds) {
//                worldProxBounds[i] = lb.clone(worldProxBounds[i]);
//                l2vw.transform(worldProxBounds[i]);
//                i++;
//            }
//        }        
    }
    
    /**
     * Set a list of bounds for which the system will track view enter/exit for
     * this cell. When the view enters/exits one of these bounds the listener
     * will be called with the index of the bounds in the supplied array.
     * 
     * The bounds must be ordered from largest to smallest, thus localBounds[i]
     * must enclose localBounds[i+1]. An IllegalArgumentException will be thrown
     * if this is not the case.
     * 
     * @param bounds
     */
    public void setProximityBounds(BoundingVolume[] localBounds) {
//        synchronized(this) {
//            this.localProxBounds = new BoundingVolume[localBounds.length];
//            this.worldProxBounds = new BoundingVolume[localBounds.length];
//            int i=0;
//            for(BoundingVolume b : localBounds) {
//                this.localProxBounds[i] = b.clone(null);
//                worldProxBounds[i] = b.clone(null);
//                
//                if (i>0 && !Math3DUtils.encloses(localProxBounds[i-1], localProxBounds[i]))
//                        throw new IllegalArgumentException("Proximity Bounds incorrectly ordered");
//                i++;
//            }
//        }
    }
    
    public void addProximityListener(ProximityListenerMO listener) {
        synchronized(this) {
            if (proximityListeners==null)
                proximityListeners = new HashSet();

            proximityListeners.add(listener);
        }
    }
    
//    @Override
//    public void setLive(boolean live) {
//        super.setLive(live);
//        
//        if (live) {
//                if (viewTransformListener==null)
//                    viewTransformListener = new ViewTransformListener();
//                updateWorldBounds();
//                cell.getCellCache().getViewCell().addTransformChangeListener(viewTransformListener);
//        } else {
//                if (viewTransformListener!=null)
//                    cell.getCellCache().getViewCell().removeTransformChangeListener(viewTransformListener);
//        }
//    }
//    
//    private void notifyProximityListeners(boolean entered, BoundingVolume enterVolume, int enterVolumeIndex) {
//        synchronized(this) {
//            for(ProximityListenerMO listener : proximityListeners) {
//                listener.viewEnterExit(entered, cell, enterVolume, enterVolumeIndex);
//            }
//        }
//    }
    
    /**
     * Listen for view moves and check the view against our proximity bounds
     */
    class ViewTransformListener implements TransformChangeListenerSrv {

        private BoundingVolume currentlyIn = null;
        private int currentlyInIndex = 0;
        
        public void transformChanged(ManagedReference<CellMO> cell, CellTransform localTransform, CellTransform local2WorldTransform) {
//            if (proximityListeners==null)
//                return;
//            
//            Vector3f worldTransform = local2WorldTransform.getTranslation(null);
//            
//            // View Cell has moved
//            synchronized(worldProxBounds) {
//                BoundingVolume nowIn = null;
//                int nowInIndex=-1;      // -1 = not in any bounding volume
//                int i=0;
//                while(i<worldProxBounds.length) {
//                    if (worldProxBounds[i].contains(worldTransform)) {
//                        nowIn = worldProxBounds[i];
//                        nowInIndex = i;
//                    } else {
//                        i=worldProxBounds.length; // Exit the while
//                    }
//                    i++;
//                }
//                
//                if (currentlyInIndex!=nowInIndex) {
//                    if (nowInIndex<currentlyInIndex) {
//                        // EXIT
//                        notifyProximityListeners(false, currentlyIn, currentlyInIndex);
//                    } else {
//                        // ENTER
//                        notifyProximityListeners(true, nowIn, nowInIndex);
//                    }
//                    currentlyIn = nowIn;
//                    currentlyInIndex = nowInIndex;
//                }
//            }
        }
        
    }
}
