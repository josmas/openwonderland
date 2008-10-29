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
package org.jdesktop.wonderland.modules.appbase.client.gui.guidefault;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.Window2DViewWorldConventional;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A world view which is used by conventional app windows. It provides the additional capability 
 * of manipulating the window's texture with displayPixels and copyArea methods.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class ViewWorldConventionalDefault extends ViewWorldDefault implements Window2DViewWorldConventional {

    private static final Logger logger = Logger.getLogger(ViewWorldConventionalDefault.class.getName());

    /** The window. */
    private Window2D window;

    /**
     * Create a new instance of ViewWorldConventionalDefault.
     */
    public ViewWorldConventionalDefault (Window2D window) {
	super(window);
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
	/*
	Rectangle rect = new Rectangle(x + borderWidth, y + borderWidth, w, h);
	clipRectangle(rect);

	// TODO: imageComp.updateData(imageUpdater, rect.x, rect.y, rect.width, rect.height);
	*/
    }
    
    /** 
     * Clip the given rectangle to the boundary of this image.
     *
     * @param r The rectangle whose attributes are to be clipped.
     */
    /*
    private void clipRectangle (Rectangle r) {
	int texWidth = getTextureWidth();
	int texHeight = getTextureHeight();
	
	if (r.x >= texWidth || r.y >= texHeight) {
	    r.setSize(0, 0);
	    return;
	}

	int xmax = r.x + r.width - 1;
	int ymax = r.y + r.height - 1;
	if (r.x < 0) r.x = 0;
	if (r.y < 0) r.y = 0;
	if (xmax >= texWidth) {
	    xmax = texWidth - 1;
	}
	if (ymax >= texHeight) {
	    ymax = texHeight - 1;
	}
	r.width = xmax - r.x + 1;
	r.height = ymax - r.y + 1;
    }
*/
    
    /**
     * Called inside the updateData critical section to deposit the given pixels into the image at the given location.
     *
     * @param imageComp The image component to be updated.
     * @param dstX The X coordinate of the top left corner of the image subrectangle which is to be updated.
     * @param dstY The Y coordinate of the top left corner of the image subrectangle which is to be updated.
     * @param width The width of the image subrectangle which is to be updated.
     * @param height The height of the image subrectangle which is to be updated.
     * @param pixels An array which contains the pixels. It must be of length width x height.
     */
/*
 private void performDisplayPixels (ImageComponent2D imageComp, int dstX, int dstY, int width, int height, 
				       int[] pixels) {

	BufferedImage bi = imageComp.getImage();
	int dstWidth = imageComp.getWidth();
	int dstHeight = imageComp.getHeight();
	WritableRaster ras = bi.getRaster();
	DataBufferInt dataBuf = (DataBufferInt) ras.getDataBuffer();
	int[] dstPixels = dataBuf.getData();

	logger.warning("performDisplayPixels");
	logger.warning("dstX = " + dstX);
	logger.warning("dstY = " + dstY);
	logger.warning("width = " + width);
	logger.warning("height = " + height);
	logger.warning("dstWidth = " + dstWidth);
	logger.warning("dstHeight = " + dstHeight);

	int srcIdx = 0;
	int dstIdx = dstY * dstWidth + dstX;
	int dstNextLineIdx = dstIdx;

	for (int y = 0; y < height; y++) {
	    dstNextLineIdx += dstWidth;

	    for (int i = 0; i < width; i++) {
		if (dstPixels == null) {
		    logger.severe("bailing out because dstPixels is null");
		    return;
		}
		if (pixels == null) {
		    logger.severe("bailing out because pixels is null");
		    return;
		}
		dstPixels[dstIdx++] = pixels[srcIdx++];
	    }

	    dstIdx = dstNextLineIdx;
	}
    }
*/
        
    /**
     * Called inside the updateData critical section to copy pixels from one part of the image to another.
     *
     *
     * @param srcX The X coordinate of the top left corner of the source subrectangle.
     * @param srcX The Y coordinate of the top left corner of the source subrectangle.
     * @param width The width of both the source and destination subrectangles.
     * @param height The height of both the source and destination subrectangles.
     * @param dstX The X coordinate of the top left corner of the destination subrectangle.
     * @param dstX The Y coordinate of the top left corner of the destination subrectangle.
     */
/*
 private void performCopyArea (ImageComponent2D imageComp, int srcX, int srcY, int width, int height, 
				  int dstX, int dstY) {

	//logger.warning("FSI.copyArea, srcXY = " + srcX + ", " + srcY +
	//	       ", wh = "  + width + ", " + height +
	//	       ", dstXY = " + dstX + ", " + dstY);
		       
	BufferedImage bi = imageComp.getImage();
	WritableRaster ras = bi.getRaster();
	DataBufferInt dataBuf = (DataBufferInt) ras.getDataBuffer();
	int[] srcPixels = dataBuf.getData();
	int[] pixels = new int[width * height];

	extractPixelIntBuf(pixels, srcPixels, srcX, srcY, width, height, imageComp.getWidth());

	performDisplayPixels(imageComp, dstX, dstY, width, height, pixels);
    }
*/
    
    /**
     * Copies a rectangle of pixels from the given source array to the given destination array.
     * The top left corner of the rectangle is at (x, y) and the dimensions of the rectangle are
     * width x height.
     *
     * @param dstPixels The destination array. The length of this must be width x height.
     * @param srcPixels The source array.
     * @param x The X coordinate of the top left corner of the pixel rectangle.
     * @param y The Y coordinate of the top left corner of the pixel rectangle.
     * @param w The width of the pixel rectangle.
     * @param h The height of the pixel rectangle.
     * @param srcLineWidth The number of pixels per line of the source array.
     */
    protected static void extractPixelIntBuf (int[] dstPixels, int[] srcPixels, 
					      int x, int y, int w, int h,
					      int srcLineWidth /*in pixels*/) {
	int dstIdx = 0;
	int srcIdx = y * srcLineWidth + x;
	int srcNextLineIdx = srcIdx;

       	for (int i = 0; i < h; i++) {
	    srcNextLineIdx += srcLineWidth;
	    for (int k = 0; k < w; k++) {
		dstPixels[dstIdx++] = srcPixels[srcIdx++];
	    }	    
	    srcIdx = srcNextLineIdx;
	}
    }
}
