/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.hudx.SPI;

import javax.swing.JPanel;
import org.jdesktop.wonderland.client.utils.Observer;

/**
 *
 * @author Ryan
 */

public interface ViewSPI {

    public void addObserver(Observer observer);
    
    public void removeObserver(Observer observer);
    
    public JPanel getJPanel();
}
