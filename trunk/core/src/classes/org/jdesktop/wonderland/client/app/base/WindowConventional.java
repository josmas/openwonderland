/**
 * Project Wonderland
 *
 * $RCSfile: AppApertureRect.java,v $
 *
 * Copyright (c) 2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.3 $
 * $Date: 2007/11/05 23:21:55 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.client.app.base;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A window object for a 2D conventional type of application. This kind of window provides main two ways
 * to update its image contents: <code>displayPixels</code> and <code>copyArea</code>. 
 * <br><br>
 * The window provides the concept of a "border width." Some conventional window systems (namely, X11)
 * support a non-zero gap of borderWidth number of pixels thickness ringing the window. No drawing is 
 * performed in this area. The input coordinates to <code>displayPixels</code> and <code>copyArea</code> 
 * are in border-less window coordinates (that is, they don't include the border). (This is opposed to 
 * window image coordinates which include the border. But these coordinates are only used internally). 
 * If the conventional window system used by a subclass doesn't support the concept of border width then 
 * the <code>borderWidth argument</code> passed to the constructor should always be 0.
 * <br><br>
 * WARNING: This interface is not thread safe. External synchronization must be performed if the 
 * <code>displayPixels</code> and <code>copyArea</code> methods of the same window are to be accessed 
 * simultaneously by multiple threads.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class WindowConventional extends WindowGraphics2D {

    /** The app to which the window belongs */
    protected AppConventional appConventional;

    /** The border width of the window */
    protected int borderWidth;

    /** 
     * Create a new instance of WindowConventional.
     *
     * @param app The app to which the window belongs.
     * @param width The width of the window (in pixels). This does NOT include the borderWidth.
     * @param height The height of the window (in pixels). This does NOT include the borderWidth.
     * @param topLevel Whether the window is top left (that is, is decorated).
     * @param borderWidth The border width of the window.
     * @param pixelScale The size of the window pixels in world coordinates.
     * @throws InstantiationException if the the window cannot be created.
     */
    public WindowConventional (App app, int width, int height, boolean topLevel, int borderWidth, Vector2f pixelScale)
	throws InstantiationException
    {
	super(app, width, height, topLevel, pixelScale, 
	      new DrawingSurface(width + 2 * borderWidth, height + 2 * borderWidth));
	this.borderWidth = borderWidth;
	appConventional = (AppConventional) app;
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {
	super.cleanup();
    }


    /**
     * Resize the window. Note that window contents will be lost when the window is resized.
     * The visual representations of the window are updated accordingly.
     *
     * @param width The new width of the window. This does NOT include the borderWidth.
     * @param height The new height of the window. This does NOT include the borderWidth
     */
    public void setSize (int width, int height) {
	super.setSize(width + 2 * borderWidth, height + 2 * borderWidth);
    }

    /**
     * Specify a new border width.
     * The visual representations of the window are updated accordingly.
     *
     * @param borderWidth The new border width.
     */
    public void setBorderWidth (int borderWidth) {
	width -= 2 * borderWidth;
	height -= 2 * borderWidth;
	this.borderWidth = borderWidth;
	setSize(width, height);
    }

    /** 
     * Returns the border width of the window.
     */
    public int getBorderWidth () {
	return borderWidth;
    }

    /**
     * {@inheritDoc}
     */
    public void setVisible (boolean visible) {
	super.setVisible(visible);

	if (visible && appConventional.getInitInBestView()) {
	    moveToBestView();
	    appConventional.setInitInBestView(false);
	}

	// TODO: ensureIsAboveFloor();
    }

    /** TODO: promote to parent! */
    public void moveToBestView () {
	// TODO
    }

    /**
     * Insert the given pixels into the window's image into a subrectangle starting at (x, y) 
     * (in borderless coordinates) and having dimensions w x h.
     * 
     * @param x The X coordinate of the top-lel corner of the image subrectangle which is to be changed.
     * @param y The Y coordinate of the top left corner of the image subrectangle which is to be changed.
     * @param w The width of the image subrectangle which is to be changed.
     * @param h The height of the image subrectangle which is to be changed.
     * @param pixels An array which contains the pixels. It must be of length w x h.
     */
    public void displayPixels (int x, int y, int w, int h, ByteBuffer pixels) {
	//TODO	((ViewWorldConventional)(viewWorld)).displayPixels(x, y, w, h, pixels);
    }

    /**
     * Copy the contents of a source subrectangle of the window's image to a destination subrectangle.
     * The source subrectangle starts at (srcX, srcY) (in borderless coordinates) and has dimensions
     * width x height. The destination subrectangle starts at (dstX, dstY) (in borderless coordinates) 
     * and has dimensions width x height. The source and destination rectangles can overlap.
     *
     * @param srcX The X coordinate of the top left corner of the source subrectangle.
     * @param srcY The Y coordinate of the top left corner of the source subrectangle.
     * @param width The width of both the source and destination subrectangles.
     * @param height The height of both the source and destination subrectangles.
     * @param dstX The X coordinate of the top left corner of the destination subrectangle.
     * @param dstY The Y coordinate of the top left corner of the destination subrectangle.
     */
    public void copyArea (int srcX, int srcY, int width, int height, int dstX, int dstY) {
	// TODO getSurface.getGraphics().copyArea(srcX, srcY, width, height, dstX, dstY);
    }
}
