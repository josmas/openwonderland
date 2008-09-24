// TODO: register mtgame controller update
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

import java.awt.Graphics2D;
import com.jmex.awt.swingui.ImageGraphics;
import com.jme.math.Vector2f;
import com.jme.renderer.ColorRGBA;
import java.awt.Color;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A rectangular, pixel-based drawing surface onto which 2D graphics
 * can be drawn. This is used by WindowGraphics2D. This drawing surface which is implemented 
 * using an JME ImageGraphics. Because JME ImageGraphics is a subclass of AWT Graphics2D you can use Java2D 
 * Graphics2D methods to draw on this drawing surface by using Graphics2D returned by getGraphics. 
 *
 * This class provides to subclasses the protected members (penX, penY) which the subclass can use to indicate the 
 * current position at which drawing operations take place (the "pen"). An XOR cursor is drawn on the 
 * image to visually indicate this position.
 *
 * @author paulby, deronj
 */

@ExperimentalAPI
public class DrawingSurface {
    
    /* The ImageGraphics onto which drawing for this surface is rendered */
    protected ImageGraphics imageGraphics;

    /* The X location of the current cursor position (in image coordinates) */
    protected int penX;

    /* The Y location of the current cursor position (in image coordinates) */
    protected int penY;

    /* The color in which to draw the cursor */
    protected Color cursorColor = Color.RED;
    
    /** The width of the surface (in pixels) */
    protected int surfaceWidth;

    /** The height of the surface (in pixels) */
    protected int surfaceHeight;
    
    /** 
     * Create an instance of DrawingSurface.
     * <br>
     * Note: You must do a setSize before using a surface created in this way.
     */
    public DrawingSurface () {}

    /** 
     * Create an instance of DrawingSurface.
     * 
     * @param width The width of the surface in pixels.
     * @param height The height of the surface in pixels.
     */
    public DrawingSurface (int width, int height) {
	setSize(width, height);
    }
    
    /**
     * Resize the surface. 
     *
     * @param width The new width of the surface in pixels.
     * @param height The new height of the surface in pixels.
     */
    public void setSize(int width, int height) {

	imageGraphics = ImageGraphics.createInstance(width, height, 0);
        surfaceWidth = width;
        surfaceHeight = height;

	// Erase new surface to all white
        imageGraphics.setBackground(Color.WHITE);
        imageGraphics.setColor(Color.WHITE);
        imageGraphics.fillRect(0, 0, width,height);

	initSurface(imageGraphics);
    }
    
    
    /**
     * Set the clip of the given Graphics2D to render the entire image of this surface.
     *
     * @param g The Graphics2D whose clip is to be set.
     * @param clipEnabled TODO: I have no idea what this does. Currently seems to be a no-op.
     */
    protected void setClip(Graphics2D g, boolean clipEnabled) {
        // TODO: why do both clauses do the exact same thing?
        if (clipEnabled)
            g.setClip(0, 0, surfaceWidth, surfaceHeight);
        else
            g.setClip(0, 0, surfaceWidth, surfaceHeight);
    }
    
    /**
     * Draw the cursor onto the surface at the current pen position.
     */
    protected void renderCursor() {
        imageGraphics.setXORMode(cursorColor);
        imageGraphics.translate(penX, penY);
        imageGraphics.drawLine(-5, 0, 5, 0);
        imageGraphics.drawLine(0,-5, 0, 5);
        imageGraphics.translate(penX, penY);
    }
    
    /**
     * Given an (x,y) coordinate, where both x and y are in the range [-1,1]
     * relative to the center of the image, calculate the corresponding
     * pixel location in the image.
     *
     * @return The corresponding pixel location in the image.
     */
    protected Vector2f computePoint(float x, float y) {
        int pX = surfaceWidth / 2 + (int)((x * surfaceWidth) / 2);
        int pY = surfaceHeight / 2 + (int)((y * surfaceHeight) / 2);
        
        return new Vector2f(pX, pY);
    }
    
    /**
     * Called on surface initialization after the surface has been
     * initially erased. The subclass can draw whatever it wants on 
     * the surface. However, care should be taken to draw in a place
     * that doesn't get overdrawn by future paints.
     *
     * @param g The Graphics2D that must be used during this call 
     * to draw to the surface. Use g instead of getGraphics().
     */
    protected void initSurface (Graphics2D g) {}

    /**
     * Returns a Graphics2D to draw on the surface.
     */
    public Graphics2D getGraphics () {
        return imageGraphics;
    }

    /**
     * Returns the width of the surface.
     */
    public int getWidth () {
	return surfaceWidth;
    }

    /**
     * Returns the height of the surface.
     */
    public int getHeight () {
	return surfaceHeight;
    }
}
