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
package org.jdesktop.wonderland.modules.appbase.common;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.modules.appbase.common.AppCellConfig;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The cell config information used by generic 2D apps.
 *
 * @author deronj
 */

@ExperimentalAPI
public class App2DCellConfig extends AppCellConfig {

    /** The default pixel scale for both X and Y */
    public static final float DEFAULT_PIXEL_SCALE = 0.01f;

    /** 
     * The internal representation of the pixel scale. The pixel scale for X
     * is pixelScale[0] and the pixel scale for Y is pixelScale[1].
     */
    private float pixelScale[] = { DEFAULT_PIXEL_SCALE, DEFAULT_PIXEL_SCALE };

    /**
     * Create a new instance of App2DCellConfig with the default pixel scale.
     */
    public App2DCellConfig () {
	this(new Vector2f(DEFAULT_PIXEL_SCALE, DEFAULT_PIXEL_SCALE));
    }

    /**
     * Create a new instance of App2DCellConfig.
     *
     * @param pixelScale The pixel scale to use.
     */
    public App2DCellConfig (Vector2f pixelScale) {
	setPixelScale(pixelScale);
    }

    /**
     * Create a new instance of App2DCellConfig.
     *
     * @param pixelScaleX The X pixel scale to use.
     * @param pixelScaleY The Y pixel scale to use.
     */
    public App2DCellConfig (float pixelScaleX, float pixelScaleY) {
	setPixelScale(pixelScaleX, pixelScaleY);
    }

    /**
     * Specify a new pixelScale.
     *
     * @param pixelScale The new pixel scale.
     */
    public void setPixelScale (Vector2f pixelScale) {
	if (pixelScale == null) {
	    pixelScale = new Vector2f(DEFAULT_PIXEL_SCALE, DEFAULT_PIXEL_SCALE);
	} else {
	    this.pixelScale = new float[] {pixelScale.x, pixelScale.y};
	}
    }

    /**
     * Specify a new pixel scale.
     *
     * @param pixelScaleX The new X pixel scale.
     * @param pixelScaleY The new Y pixel scale.
     */
    public void setPixelScale (float pixelScaleX, float pixelScaleY) {
	pixelScale = new float[] {pixelScaleX, pixelScaleY};
    }

    /**
     * Return the pixel scale.
     */
    public Vector2f getPixelScale () {
	return new Vector2f(pixelScale[0], pixelScale[1]);
    }

    /**
     * Specify a new X pixel scale.
     *
     * @param x The new X pixel scale.
     */
    public void setPixelScaleX (float x) {
	pixelScale[0] = x;
    }

    /**
     * Returns the X pixel scale.
     */
    public float getPixelScaleX () {
	return pixelScale[0];
    }

    /**
     * Specify a new Y pixel scale.
     *
     * @param y The new Y pixel scale.
     */
    public void setPixelScaleY (float y) {
	pixelScale[1] = y;
    }

    /**
     * Returns the Y pixel scale.
     */
    public float getPixelScaleY () {
	return pixelScale[1];
    }

    /**
     * Returns the default pixel scale for both X and Y.
     */
    public static Vector2f getDefaultPixelScale () {
	return new Vector2f(DEFAULT_PIXEL_SCALE, DEFAULT_PIXEL_SCALE);
    }
}

