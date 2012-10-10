/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.hudx;

import javax.swing.JComponent;
import org.jdesktop.wonderland.client.SPI.HUDWindowSPI;
import org.jdesktop.wonderland.client.annotations.HUDWindow;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;

/**
 *
 * @author Ryan
 */
@HUDWindow
public class SimpleHUDWindow implements HUDWindowSPI {
    
    private SimpleController controller;
    
    public SimpleHUDWindow() {
        controller = new SimpleController();
        
    }
    
    public Layout getPosition() {
        return Layout.CENTER;
    }

    public String getWindowTitle() {
        return "Simple";
    }

    public JComponent getComponent() {
        return controller.view.getJPanel();
    }

    public void initialize() {

    }

    public void cleanup() {
        controller.cleanup();
    }

    public boolean openOnStartup() {
        return false;
    }
    
}
