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
package org.jdesktop.wonderland.common.cell;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.Math3DUtils;

/**
 *
 * Utility class to help implement proximity listeners on both client and server
 *
 * @author paulby
 */
public class ProximityListenerRecord {
    protected ProximityListenerWrapper proximityListener;
    private BoundingVolume[] localProxBounds;
    private BoundingVolume[] worldProxBounds;
    private BoundingVolume currentlyIn = null;
    private int currentlyInIndex = -1;

    public ProximityListenerRecord(ProximityListenerWrapper proximityListener, BoundingVolume[] localBounds) {
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

    /**
     * The cell world bounds have been updated, so update our internal
     * structures
     */
    public void updateWorldBounds(CellTransform worldTransform) {
        if (localProxBounds==null)
            return;

        // Update the world proximity bounds
        int i=0;
        synchronized(worldProxBounds) {
            for(BoundingVolume lb : localProxBounds) {
                worldProxBounds[i] = lb.clone(worldProxBounds[i]);
                worldTransform.transform(worldProxBounds[i]);
                i++;
            }
        }
    }

    /**
     * The view cells transform has changed so update our internal structures
     * @param cell
     */
    public void viewCellMoved(CellID viewCellID, CellTransform worldTransform) {
        Vector3f worldTranslation = worldTransform.getTranslation(null);

        // View Cell has moved
        synchronized(worldProxBounds) {
            BoundingVolume nowIn = null;
            int nowInIndex=-1;      // -1 = not in any bounding volume
            int i=0;
            while(i<worldProxBounds.length) {
                if (worldProxBounds[i].contains(worldTranslation)) {
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
                    proximityListener.viewEnterExit(false, currentlyIn, currentlyInIndex, viewCellID);
                } else {
                    // ENTER
                    proximityListener.viewEnterExit(true, nowIn, nowInIndex, viewCellID);
                }
                currentlyIn = nowIn;
                currentlyInIndex = nowInIndex;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProximityListenerRecord))
            return false;

        return ((ProximityListenerRecord)o).proximityListener==proximityListener;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.proximityListener != null ? this.proximityListener.hashCode() : 0);
        return hash;
    }

    /**
     * Wrapper for the listener, client and server listeners have a slightly
     * different interface
     */
    public interface ProximityListenerWrapper {
        public void viewEnterExit(boolean enter, BoundingVolume proximityVolume, int proximityIndex, CellID viewCellID );
    }
}
