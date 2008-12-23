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

import com.jme.image.Texture;
import com.jme.util.TextureManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Convert a text string into a texture.
 *
 * @author deronj
 */

@ExperimentalAPI
class TextTextureGenerator {

    private static final Logger logger = Logger.getLogger(TextTextureGenerator.class.getName());

    // This is supposed to be the font which is universally available 
    // across JREs on all platforms
    private static final Font textFont = new Font("Serif", Font.BOLD, 30);

    // TODO: this determines the foreground color of the text.
    // This should probably be an argument to the constructor
    private static final Color text2DColor = new Color(0f, 0f, 0f, 1f);

    private static final int widthMargin = 1;
    private static final int heightMargin = 1;
    private static final FontMetrics fontMetrics;
    
    private Texture texture;
    private float width;
    private float widthRatio;
    private float heightRatio;
    
    static {
	BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2d = (Graphics2D)bi.getGraphics();
	g2d.setFont(textFont);
	fontMetrics = g2d.getFontMetrics();
	g2d.dispose();
    }
    
    public TextTextureGenerator(String text, int texWidth, int texHeight) { 
        texture = createTexture(text, texWidth, texHeight);
    }
    
    public Texture getTexture() {
        return texture;
    }
    
    private Texture createTexture (String text, int texWidth, int texHeight) {
	BufferedImage bi = createTextureImage(text, texWidth, texHeight);
	return TextureManager.loadTexture(bi, Texture.MinificationFilter.Trilinear, 
                    Texture.MagnificationFilter.Bilinear, false);
    }
    
    private BufferedImage createTextureImage(String text, int texWidth, int texHeight) {
	if (text == null) {
	    text = " "; 
	}

	//logger.warning("texWH = " + texWidth + " " + texHeight);
	int biWidth = getRoundUptoPow2(texWidth);
	int biHeight = getRoundUptoPow2(texHeight);
	//logger.warning("biWH = " + biWidth + " " + biHeight);

	int descent = fontMetrics.getDescent();
	float x = widthMargin;
	float y = texHeight - heightMargin - descent;

	BufferedImage bi 
	    = new BufferedImage(biWidth, biHeight, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2d = (Graphics2D)bi.getGraphics();

	// For debug
	for (int i = 0; i < biHeight; i++) {
	    for (int k = 0; k < biWidth; k++) {
		bi.setRGB(k, i, 0xffffffff);
	    }
	}

	g2d.setColor(new Color(1f, 1f, 1f, 0f));
	g2d.fillRect((int)0, (int)0, biWidth, biHeight);

	// Scale font uniformly to fit texture height while drawing
	int fontHeight = fontMetrics.getHeight();
	double hScale = (double)(texHeight - 2 * heightMargin) / (double)fontHeight;
	x /= hScale;
	y /= hScale;

	g2d.setRenderingHints(
	    new RenderingHints(
		RenderingHints.KEY_TEXT_ANTIALIASING,
		RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
                
	g2d.setFont(textFont);
	g2d.setColor(text2DColor);

	texWidth -= 2 * widthMargin;
	logger.warning("text (unclipped) = " + text);
	text = clipText(text, hScale, texWidth, g2d);
	logger.warning("text (final clipped) = " + text);
	if (text != null && text.length() > 0) {
	    y += (texHeight - fontHeight) / 2;
	    g2d.drawString(text, x, y);
	    g2d.dispose();
	}

	return bi;
    }
    
    private int getRoundUptoPow2(int n) {
	if (n <= 1) {
	    return 1;
	}

	int pow = 2;
	for ( ; pow < n; pow *= 2);

	return pow;
    }

    /**
     * Returns a shorted version of the given text string so
     * that an integral number of characters fit into the 
     * texture width.
     */
    private String clipText (String text, double wScale, int texWidth, Graphics2D g2d) {
	String clippedText = text;
	Rectangle2D rect = fontMetrics.getStringBounds(clippedText, g2d);
	int clippedLen = text.length() - 1;

	// User names are typically short enough that a linear 
	// search is quick
	while ((int)(rect.getWidth() * wScale) > texWidth && clippedLen > 0) {
	    clippedText = text.substring(0, clippedLen - 1);
	    if (clippedText == null || clippedText.length() <= 0) {
		return null;
	    }
	    rect = fontMetrics.getStringBounds(clippedText, g2d);
	    clippedLen--;
	}

	return clippedText;
    }
}

