/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.menus;

import java.awt.event.ActionEvent;
import org.jdesktop.wonderland.client.utils.Observer;

/**
 *
 * @author Ryan
 */
public abstract class BaseMenuItemController implements Observer {
    
    private MenuItemView view;
    
    public BaseMenuItemController(String text) {
        view = new MenuItemView(text);
        
        
    }

    public void eventObserved(String property, Object value) {
        if(property.equals("action-performed")) {
            handleActionPerformed((ActionEvent)value);
        }
    }

    protected abstract void handleActionPerformed(ActionEvent actionEvent);
        
}
