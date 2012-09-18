package org.jdesktop.wonderland.client.jme.input.bindings;


import org.jdesktop.wonderland.client.jme.input.bindings.spi.ActionSPI;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ControllerSPI;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ryan
 */
public class ActionWrapperObject {

    private ActionSPI action;
    private ModifiersGroup.Modifier modifier;
    private ControllerSPI controller;

    
    public ActionWrapperObject(ActionSPI action,
                               ModifiersGroup.Modifier modifier,
                               ControllerSPI controller) {
        this.action = action;
        this.modifier = modifier;
        this.controller = controller;

    }

    public ActionSPI getAction() {
        return action;
    }

    public ControllerSPI getController() {
        return controller;
    }

//    public String getKeyCode() {
//        return keyCode;
//    }

    public ModifiersGroup.Modifier getModifier() {
        return modifier;
    }
    
    
}
