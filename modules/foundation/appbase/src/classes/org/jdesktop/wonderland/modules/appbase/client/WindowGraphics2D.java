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
package org.jdesktop.wonderland.modules.appbase.client;

import com.jme.image.Texture;
import com.jme.math.Vector2f;
import com.jmex.awt.swingui.ImageGraphics;
import java.awt.Graphics2D;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A window that provides a drawing surface (a buffered image) that can be drawn on by a Graphics2D. 
 *
 * @author deronj
 */

@ExperimentalAPI
public class WindowGraphics2D extends Window2D {

    private static final Logger logger = Logger.getLogger(WindowGraphics2D.class.getName());

    /** The surface the client on which subclasses should draw */
    private DrawingSurface surface;

    /**
     * Create a Window2D instance and its "World" view.
     *
     * @param app The application to which this window belongs
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param topLevel Whether the window is top-level (e.g is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     * @param surface The drawing surface on which the creator will draw
     * @throws InstantiationException if the windows world view cannot be created.
     */
    public WindowGraphics2D (App app, int width, int height, boolean topLevel,
			     Vector2f pixelScale, DrawingSurface surface) 
	throws InstantiationException 
    {
	super(app, width, height, topLevel, pixelScale);
	updateTexture();

	// Arrange for the surface contents to be continuously copied into this window's texture.
	this.surface = surface;
	surface.setWindow(this);
	surface.setTexture(texture);
	surface.setUpdateEnable(true);
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {
	if (surface != null) {
	    surface.cleanup();
	    surface = null;
	}
	super.cleanup();
    }

    /**
     * Initialize the contents of the surface.
     */
    protected void initializeSurface () {
	if (surface != null) {
	    surface.initializeSurface();
	}
    }

    /**
     * Returns the drawing surface of this windows. This allows subclasses to access the drawing surface.
     */
    protected DrawingSurface getSurface () {
	return surface;
    }

    /**
     * Called to paint contents of the window. Subclasses should override to paint
     * their own custom contents.
     *
     * @param g The Graphics2D to use for drawing.
     */
    protected void paint(Graphics2D g) {}

    protected void repaint() {
	// Don't need to do anything. The copying of the 
    }

    /**
     * {@inheritDoc}
     */
    public void setVisible (boolean visible) {
	super.setVisible(visible);
	repaint();
    }
}
