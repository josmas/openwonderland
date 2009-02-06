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
package org.jdesktop.wonderland.modules.appbase.client;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An app which creates windows that can be drawn on by a Graphics2D.
 *
 * @author deronj
 */
@ExperimentalAPI
public class AppGraphics2D extends App2D {

    /**
     * Create a new instance of AppGraphics2D.
     *
     * @param appType The type of 2D graphics app to create.
     * @param controlArb The control arbiter to use. null means that all users can control at the same time.
     * @param pixelScale The size of the window pixels in world coordinates.
     */
    public AppGraphics2D(AppType appType, ControlArb controlArb, Vector2f pixelScale) {
        super(appType, controlArb, pixelScale);
    }

    /** 
     * Create a window which can be drawn to using an AWT Graphics2D.
     *
     * @param width The width (in pixels) of the window.
     * @param height The height (in pixels of the window.
     * @param topLevel Whether the window is top-level (that is, whether the window is decorated with a frame).
     */
    public WindowGraphics2D createWindow(int width, int height, boolean topLevel)
            throws InstantiationException {
        return new WindowGraphics2D(this, width, height, topLevel, pixelScale,
                new DrawingSurfaceBufferedImage(width, height));
    }
}
