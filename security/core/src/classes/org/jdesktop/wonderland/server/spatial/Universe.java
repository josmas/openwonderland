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
package org.jdesktop.wonderland.server.spatial;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Matrix4f;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 *
 * @author paulby
 */
public interface Universe {

    /**
     * Add a SpatialCell (and all it's children) to the universe
     * @param cell
     */
    public void addSpatialCell(SpatialCell cell);

    public SpatialCell createSpatialCell(CellID id, boolean view);

}
