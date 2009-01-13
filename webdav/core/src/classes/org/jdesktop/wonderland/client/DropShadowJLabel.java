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
package org.jdesktop.wonderland.client;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import javax.swing.JLabel;
import org.jdesktop.layout.Baseline;

/**
 * JLabel with a drop-shadow
 * @author jkaplan
 */
public class DropShadowJLabel extends JLabel {
    
    private float offsetLeft = -3;
    private float offsetTop = 3;
    private Color shadowColor = Color.BLACK;
    private float shadowOpacity = 0.5f;
    
    public DropShadowJLabel() {
        super();
    }
    
    /** Creates a new instance of DropShadowJLabel */
    public DropShadowJLabel(String text) {
        super (text);
    }
    
    /**
     * Paint the label
     */
    public void paintComponent(Graphics g) {
        // create drop shadow
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        FontRenderContext frc = g2.getFontRenderContext();
        GlyphVector gv = getFont().createGlyphVector(frc, getText());
        
        // get the original text placement
        Insets insets = getInsets();
        float textX = insets.left;
        float textY = Baseline.getBaseline(this, getWidth(), getHeight());
        float shadowX = textX + getOffsetLeft();
        float shadowY = textY + getOffsetTop();
        
        // draw the shadow
        g2.setColor(getShadowColor());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getShadowOpacity()));
        g2.drawGlyphVector(gv, shadowX, shadowY);
        
        // draw the text
        g2.setColor(getForeground());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.drawGlyphVector(gv, textX, textY);
    }

    public float getOffsetLeft() {
        return offsetLeft;
    }

    public void setOffsetLeft(float offsetLeft) {
        this.offsetLeft = offsetLeft;
    }

    public float getOffsetTop() {
        return offsetTop;
    }

    public void setOffsetTop(float offsetTop) {
        this.offsetTop = offsetTop;
    }

    public Color getShadowColor() {
        return shadowColor;
    }

    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
    }

    public float getShadowOpacity() {
        return shadowOpacity;
    }

    public void setShadowOpacity(float shadowOpacity) {
        this.shadowOpacity = shadowOpacity;
    }
}
