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
    public ProximityComponentMO(CellMO cell) {
        super(cell);
    }
    
    
    public void addProximityListener(ProximityListenerMO listener, BoundingVolume[] localBounds) {
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
    

}
