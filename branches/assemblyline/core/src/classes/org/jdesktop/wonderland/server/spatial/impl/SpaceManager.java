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
import org.jdesktop.wonderland.server.cell.*;
import com.jme.math.Vector3f;
import java.io.Serializable;

/**
 *
 * @author paulby
 */
interface SpaceManager {

    /**
     * Return the spaces that intersect this volume, if the space does not exist, create it
     * @param position
     * @return
     * 
     */
    Iterable<Space> getEnclosingSpace(BoundingVolume volume);

    /**
     * Return the space that encloses this point
     * @param point
     * @return
     */
    
    /**
     * Initialize the space system
     */
    void initialize();
}
