/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.SPI;

import javax.swing.JComponent;
import org.jdesktop.wonderland.client.hud.CompassLayout;

/**
 *
 * @author JagWire
 */
public interface HUDWindowSPI {
  
    /**
     * Get the position on screen for the window to appear.
     * 
     * @return 
     */
    public CompassLayout.Layout getPosition();
    
    /**
     * Get the title for the window.
     * 
     * @return 
     */
    public String getWindowTitle();
    
    /**
     * Get the Swing component which will appear in the window
     * @return 
     */
    public JComponent getComponent();
    
    /**
     * Initialize the window for appearance on HUD
     */
    public void initialize();
    
    /**
     * Cleanup the window on client logout from server.
     */
    public void cleanup();
}
