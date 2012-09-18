/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.jme.input.bindings;

import java.util.Map;
import javax.swing.KeyStroke;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.jme.input.bindings.controllers.KeyController;
import org.jdesktop.wonderland.client.jme.input.bindings.controllers.MouseButtonController;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ActionSPI;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ControllerSPI;

/**
 *
 * @author Ryan
 */
public class Binder {
    private Map<Class, ControllerSPI> controllers = null;
    private ActionBindingContext context = null;
    
//    public Binder(Map<Class, ControllerSPI> controllers) {
//        this.controllers = controllers;
//    }

    public Binder(ActionBindingContext context, Map<Class, ControllerSPI> controllers) {
        this.context = context;
        this.controllers = controllers;
    }
    
    /**
     * Sets a modified keybinding for a designated action
     * @param action the action to be bound
     * @param key the key to trigger the action
     * @param modifier the modifier that personalizes the key
     */
    public void bind(KeyStroke key, ActionSPI value) {
        KeyController controller = (KeyController)controllers.get(KeyEvent3D.class);
        
        controller.registerActionSequence(key, value.getName());
        
    }
    
    /**
     * Sets a modified mouse button binding for a designated action
     * @param action the action to be bound
     * @param id the button to trigger the action
     * @param modifier the modifier that personalizes the mouse button
     */
    public void bind(ActionSPI action, ButtonId id, ModifiersGroup.Modifier modifier) {
        MouseButtonController controller = (MouseButtonController)controllers
                                                .get(MouseButtonEvent3D.class);
        
        ActionWrapperObject awo = new ActionWrapperObject(action,
                                                         modifier,
                                                         controller);
        Couple<Integer, ActionWrapperObject> c = new Couple(id, awo);
        controller.registerActionSequence(c);
    }
    
    /**
     * Removes a modified keybinding from triggering a designated action
     * @param action the action to be unbound
     * @param key the key that triggers the action
     * @param modifier the modifier that personalizes the key
     */
    public void unbind(KeyStroke key) {
//        KeyController controller = (KeyController)controllers.get(KeyEvent3D.class);
        ControllerSPI controller = controllers.get(KeyEvent3D.class);
        
        controller.unbindObject(key);
    }
    
    
    /**
     * Removes a modified mouse button binding for a designated action
     * @param action the action to be unbound
     * @param id the button that triggers the action
     * @param modifier the modifier that personalizes the mouse button
     */
    public void unbind(ActionSPI action, ButtonId id, ModifiersGroup.Modifier modifier) {
        MouseButtonController controller = (MouseButtonController)controllers
                                                .get(MouseButtonEvent3D.class);
        
        ActionWrapperObject awo = new ActionWrapperObject(action,
                                                         modifier,
                                                         controller);
        Couple<Integer, ActionWrapperObject> c = new Couple(id, awo);
        controller.unregisterActionSequence(c);
    }

    public void bindKeyStroke(KeyStroke keyStroke, String actionName) {
//        KeyController controller = (KeyController)controllers.get(KeyEvent3D.class);
        ControllerSPI c = controllers.get(KeyEvent3D.class);
        c.bindObject(keyStroke, actionName);
//        controller.registerActionSequence(keyStroke, actionName);
    }

    public void bindAction(String actionName, ActionSPI action) {
        context.getRegistry().registerAction(action, actionName);
    }
    
}
