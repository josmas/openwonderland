/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.menus;

import java.awt.event.ActionEvent;

/**
 *
 * @author Ryan
 */
public class SimpleMenuItemController extends BaseMenuItemController {

    public SimpleMenuItemController(String caption) {
        super(caption);
    }
    
    @Override
    protected void handleActionPerformed(ActionEvent actionEvent) {
        //do something crazy
    }
    
}
