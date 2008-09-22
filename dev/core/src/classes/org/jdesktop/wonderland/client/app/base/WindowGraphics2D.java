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
package org.jdesktop.wonderland.client.app.base;

import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.math.Vector2f;
import com.jme.util.TextureManager;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A window that provides a drawing surface (a buffered image) that can be drawn on by a Graphics2D. 
 *
 * @author deronj
 */

@ExperimentalAPI
public class WindowGraphics2D extends Window2D {

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
	this.surface = surface;
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

    // TODO: for now just copy the entire window contents into a new image and replace it in the texture
    protected void repaint() {
	Texture texture = getTexture();
	Image image = texture.getImage();
	int width = image.getWidth();
	int height = image.getHeight();

	// Paint this window onto a new buffered image
	// TODO: this is a klunky interface -- usually there is a BufferedInterface in the drawing surface.
	// Why create a new one?
        /* Debug
	BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	paint((Graphics2D)bi.getGraphics());
        */
        
	// For debug
	System.err.println("WindowGraphics2D: load test image");
	java.awt.Image bi = Toolkit.getDefaultToolkit().getImage(
		              "/home/dj/jme/cvs/jme/src/jmetest/data/images/Monkey.jpg");

	/* For debug
	BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	for (int y = 0; y < height; y++) {
	    for (int x = 0; x < width; x++) {
		bi.setRGB(x, y, 0x00ff0000);
	    }
	}
	*/

	// Copy the BufferedImage to a new JME image
	image = TextureManager.loadImage(bi, false);


	// Attach the new image to the texture
	texture.setImage(image);

	// For debug
      	texture.setApply(Texture.ApplyMode.Modulate);
    }

    /**
     * {@inheritDoc}
     */
    public void setVisible (boolean visible) {
	super.setVisible(visible);
	repaint();
    }
}
