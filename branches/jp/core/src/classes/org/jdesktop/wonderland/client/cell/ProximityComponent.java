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
import com.jme.math.Vector3f;
import java.util.HashMap;
import java.util.HashSet;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.Math3DUtils;

/**
 * Provides a mechanism for listener notification when the local view cell
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
public class ProximityComponent extends CellComponent {

    private ViewTransformListener viewTransformListener=null;
    private CellTransformListener  cellTransformListener=null;
    
    private HashSet<ListenerRecord> listenerRecords = new HashSet();
    
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
    public ProximityComponent(Cell cell) {
        super(cell);
    }
    
    public void addProximityListener(ProximityListener listener, BoundingVolume[] localBounds) {
        synchronized(listenerRecords) {
           ListenerRecord lr = new ListenerRecord(listener, localBounds);
           listenerRecords.add(lr);
           if (status!=null && status.ordinal()>=CellStatus.ACTIVE.ordinal())
               lr.updateWorldBounds();
        }
    }
    
    public void removeProximityListener(ProximityListener listener) {
        synchronized(listenerRecords) {
            listenerRecords.remove(new ListenerRecord(listener, null));
        }
    }
    
    @Override
    public void setStatus(CellStatus status) {
        synchronized(listenerRecords) {
            super.setStatus(status);

            switch(status) {
                case ACTIVE :
                    if (viewTransformListener==null) {
                        viewTransformListener = new ViewTransformListener();
                        cellTransformListener = new CellTransformListener();
                    }

                    for(ListenerRecord l : listenerRecords)
                        l.updateWorldBounds();

                    cell.getCellCache().getViewCell().addTransformChangeListener(viewTransformListener);
                    cell.addTransformChangeListener(cellTransformListener);
                    break;
                case DISK :
                    if (viewTransformListener!=null) {
                        cell.getCellCache().getViewCell().removeTransformChangeListener(viewTransformListener);
                        cell.removeTransformChangeListener(cellTransformListener);
                    }
                    break;
            }
        }
    }
    
    /**
     * Listen for view moves and check the view against our proximity bounds
     */
    class ViewTransformListener implements TransformChangeListener {

        
        public void transformChanged(Cell cell) {
            
            synchronized(listenerRecords) {
                for(ListenerRecord l : listenerRecords) {
                    l.transformChanged(cell);
                }
            }
        }
        
    }
    
    /**
     * Listen for the cell to which this component is attached moving. When
     * notified update the bounds
     */
    class CellTransformListener implements TransformChangeListener {

        public void transformChanged(Cell cell) {
            synchronized(listenerRecords) {
                for(ListenerRecord l : listenerRecords) {
                    l.updateWorldBounds();
                    
                    // Reevalute view position and send enter/exit events as necessary
                    l.transformChanged(cell.getCellCache().getViewCell());
                }
            }
        }
        
    }
    
    class ListenerRecord {
        ProximityListener proximityListener;
        BoundingVolume[] localProxBounds;
        BoundingVolume[] worldProxBounds;
        private BoundingVolume currentlyIn = null;
        private int currentlyInIndex = -1;
        
        public ListenerRecord(ProximityListener proximityListener, BoundingVolume[] localBounds) {
            this.proximityListener = proximityListener;
            setProximityBounds(localBounds);
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
        void setProximityBounds(BoundingVolume[] localBounds) {
            this.localProxBounds = new BoundingVolume[localBounds.length];
            this.worldProxBounds = new BoundingVolume[localBounds.length];
            int i=0;
            for(BoundingVolume b : localBounds) {
                this.localProxBounds[i] = b.clone(null);
                worldProxBounds[i] = b.clone(null);

                if (i>0 && !Math3DUtils.encloses(localProxBounds[i-1], localProxBounds[i]))
                        throw new IllegalArgumentException("Proximity Bounds incorrectly ordered");
                i++;
            }
        }
        
        private void updateWorldBounds() {
            if (localProxBounds==null)
                return;

            // Update the world proximity bounds
            CellTransform l2vw = cell.getLocalToWorldTransform();
            int i=0;
            synchronized(worldProxBounds) {
                for(BoundingVolume lb : localProxBounds) {
                    worldProxBounds[i] = lb.clone(worldProxBounds[i]);
                    l2vw.transform(worldProxBounds[i]);
                    i++;
                }
            }        
        }
        
        public void transformChanged(Cell cell) {
            Vector3f worldTransform = cell.getLocalToWorldTransform().getTranslation(null);
            
            // View Cell has moved
            synchronized(worldProxBounds) {
                BoundingVolume nowIn = null;
                int nowInIndex=-1;      // -1 = not in any bounding volume
                int i=0;
                while(i<worldProxBounds.length) {
                    if (worldProxBounds[i].contains(worldTransform)) {
                        nowIn = worldProxBounds[i];
                        nowInIndex = i;
                    } else {
                        i=worldProxBounds.length; // Exit the while
                    }
                    i++;
                }
                
                if (currentlyInIndex!=nowInIndex) {
                    if (nowInIndex<currentlyInIndex) {
                        // EXIT
                        proximityListener.viewEnterExit(false, cell, currentlyIn, currentlyInIndex);
                    } else {
                        // ENTER
                        proximityListener.viewEnterExit(true, cell, nowIn, nowInIndex);
                    }
                    currentlyIn = nowIn;
                    currentlyInIndex = nowInIndex;
                }
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ListenerRecord))
                return false;
            
            return ((ListenerRecord)o).proximityListener==proximityListener;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.proximityListener != null ? this.proximityListener.hashCode() : 0);
            return hash;
        }
    }
}
