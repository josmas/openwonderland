/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.SPI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author JagWire
 */
public interface MenuItemSPI extends ActionListener {
    
    /**
     * Provides array of groups that are allowed to use this Menu Item
     * @return an array of strings
     */
    public String[] getPermittedGroups();
    
    /**
     * The menu to add this item to.
     * 
     * @return 
     */
    public String getMenu();
    
    /**
     * The name to be shown within the menu.
     * @return 
     */
    public String getDisplayName();
    
    /**
     * 
     * @inherit 
     */
    public void actionPerformed(ActionEvent event);
    
}
