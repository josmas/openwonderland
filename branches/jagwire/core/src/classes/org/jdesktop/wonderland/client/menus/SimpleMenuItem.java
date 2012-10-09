/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.menus;

import java.awt.event.ActionEvent;
import org.jdesktop.wonderland.client.SPI.MenuItemSPI;
import org.jdesktop.wonderland.client.annotations.MenuItem;

/**
 *
 * @author Ryan
 */
@MenuItem
public class SimpleMenuItem implements MenuItemSPI {

    public SimpleMenuItemController controller;
    
    public SimpleMenuItem() {
        controller = new SimpleMenuItemController(getDisplayName());
    }
    
    public String[] getPermittedGroups() {
        return new String[] { };
    }

    public String getMenu() {
        return "Tools";
    }

    public String getDisplayName() {
        return "Simple Menu Item";
    }

    public BaseMenuItemController getController() {
        return controller;
    }
    
}
