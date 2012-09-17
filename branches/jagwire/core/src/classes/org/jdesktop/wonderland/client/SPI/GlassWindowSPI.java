/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.SPI;

import java.awt.Image;
import java.awt.Point;

/**
 *
 * @author Ryan
 */
public interface GlassWindowSPI {

    /**
     * Get the image to appear on the glass pane.
     * 
     * @return 
     */
    public Image getImage();
    
    
    /**
     * Get the position for which the window will appear
     * 
     * @return 
     */
    public Point getPosition();
}
