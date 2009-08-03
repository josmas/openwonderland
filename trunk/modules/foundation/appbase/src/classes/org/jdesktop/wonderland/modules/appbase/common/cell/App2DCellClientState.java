/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.appbase.common.cell;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * The cell client state information used by generic 2D apps.
 *
 * @author deronj
 */
@ExperimentalAPI
public class App2DCellClientState extends CellClientState {

    /** The default pixel scale. */
    public static final float DEFAULT_PIXEL_SCALE = 0.01f;
    /** The pixel scale. */
    private Vector2f pixelScale = new Vector2f(DEFAULT_PIXEL_SCALE, DEFAULT_PIXEL_SCALE);

    /**
     * Create a new instance of App2DCellClientState with the default pixel scale.
     */
    public App2DCellClientState() {
        super();
    }

    /**
     * Create a new instance of App2DCellClientState with the given pixel scale.
     */
    public App2DCellClientState(Vector2f pixelScale) {
        super();
        this.pixelScale = pixelScale;
    }

    /**
     * Specify a new pixelScale.
     *
     * @param pixelScale The new pixel scale.
     */
    public void setPixelScale(Vector2f pixelScale) {
        this.pixelScale = new Vector2f(pixelScale);
    }

    /**
     * Return the pixel scale.
     */
    public Vector2f getPixelScale() {
        return new Vector2f(pixelScale);
    }
}

