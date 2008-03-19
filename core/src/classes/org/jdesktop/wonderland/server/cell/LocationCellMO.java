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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;

/**
 * A cell which provides detailed location information. The
 * location is a point within the bounds of the cell, but not
 * necessarily the center of the bounds.
 * 
 * Used for objects which make frequent small moves in which case
 * we don't update the bounds for every move (Avatars are a good example)
 * 
 * @author paulby
 */
public class LocationCellMO extends MoveableCellMO {

    private Vector3f locationVW;
    private Quaternion orienationVW;
    
    /**
     * Get the location and velocity in virtual world coordinates
     * 
     * @param location
     * @param velocity
     */
    public void getVWLocation(Vector3f locationVW, 
                              Quaternion orientationVW) {
        locationVW.set(locationVW);
        orientationVW.set(orienationVW);
    }
    
    public void setVWLocation(Vector3f locationVW, 
                              Quaternion orientationVW) {
        this.locationVW = locationVW;
        this.orienationVW = orientationVW;
    }
}
