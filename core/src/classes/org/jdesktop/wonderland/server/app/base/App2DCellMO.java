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
package org.jdesktop.wonderland.server.app.base;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An abstract server-side app.base cell for 2D apps. 
 * Intended to be subclassed by server-side 2D app cells.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class App2DCellMO extends AppCellMO { 

    /** The pixel scale. */
    protected Vector2f pixelScale;

    /** Default constructor, used when the cell is created via WFS */
    public App2DCellMO () {
	super();
    }
    
    /**
     * Creates a new instance of <code>App2DCellMO</code> with the specified localBounds and transform
     * and the default pixel scale. If either parameter is null an IllegalArgumentException will be thrown.
     *
     * @param localBounds the bounds of the new cell, must not be null.
     * @param transform the transform for this cell, must not be null.
     */
    public App2DCellMO (BoundingVolume localBounds, CellTransform transform) {
        super(localBounds, transform);
    }

    /**
     * Creates a new instance of <code>App2DCellMO</code> with the specified localBounds, transform
     * and pixel scale. If either parameter is null an IllegalArgumentException will be thrown.
     *
     * @param localBounds the bounds of the new cell, must not be null.
     * @param transform the transform for this cell, must not be null.
     * @param pixelScale The size of the application pixels in world coordinates.
     */
    public App2DCellMO (BoundingVolume bounds, CellTransform transform, Vector2f pixelScale){
        super(bounds, transform);
	this.pixelScale = pixelScale;
    }
}
