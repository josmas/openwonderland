package org.jdesktop.wonderland.client.jme.input.bindings.controllers;

import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.KeyStroke;
import org.jdesktop.wonderland.client.jme.input.InputEvent3D;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.bindings.ActionBindingContext;
import org.jdesktop.wonderland.client.jme.input.bindings.ActionWrapperObject;
import org.jdesktop.wonderland.client.jme.input.bindings.Couple;
import org.jdesktop.wonderland.client.jme.input.bindings.ModifiersGroup;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ActionSPI;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.Controller;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.KeyAction;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.KeyActionSPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;

/**
 *
 * @author Ryan
 */
//@Controller
public class KeyController {// extends AbstractBaseController {

    private Map<KeyStroke, String> sequences;
    private ActionBindingContext context = null;

    public KeyController() {
        super();
    }

//    @Override
    public Class consumesEventClass() {
        return KeyEvent3D.class;
    }

//    @Override
    public <T extends InputEvent3D> void dispatchEvent(T event) {

        KeyEvent keyEvent = (KeyEvent) event.getAwtEvent();

        if (containsKey(keyEvent)) {
            String actionName = sequences.get(keyStrokeForEvent(keyEvent));
            
            
            ActionSPI action = context.getActionByName(actionName);
            
            action.performAction(event);
        }
    }

    /**
     * Checks internal sequences map for a keystroke associated with the given
     * KeyEvent
     *
     * @param event to generate a keystroke from
     * @return true if the sequences map contains the keystroke, false otherwise
     */
    private boolean containsKey(KeyEvent event) {
        KeyStroke key = keyStrokeForEvent(event);
        if (sequences.containsKey(key)) {
            return true;
        }

        return false;
    }

    /**
     * Helper function to generate a KeyStroke object from a KeyEvent
     */
    private KeyStroke keyStrokeForEvent(KeyEvent event) {
        return KeyStroke.getKeyStrokeForEvent(event);
    }

    private String charToString(char c) {
        return new String(new char[]{c});
    }

//    @Override
    public void loadActions(ActionBindingContext context) {
        this.context = context;

        sequences = new HashMap<KeyStroke, String>();


        //scan for our KeyActionSPI implementations
        ScannedClassLoader loader = LoginManager.getPrimary().getClassloader();
        Iterator<KeyActionSPI> keyActions =
                loader.getInstances(KeyAction.class, KeyActionSPI.class);

        while (keyActions.hasNext()) {
            KeyActionSPI keyAction = keyActions.next();


            //register action sequence            
            registerActionSequence(keyAction.getKeyStroke(), keyAction.getName());

            //add actions to registry 
            context.getRegistry().registerAction(keyAction, keyAction.getName());
        }


    }

//    public void registerActionSequence(KeyStroke key, ActionSPI value) {
//        if (!sequences.containsKey(key)) {
//            sequences.put(key, value);
//        }
//
//    }

    public void unregisterActionSequence(KeyStroke key) {
        synchronized (sequences) {
            if (sequences.containsKey(key)) {
                sequences.remove(key);
            }
        }
    }

//    @Override
    public Set<Couple<? extends Object, ActionWrapperObject>> getActions() {
        Set<Couple<? extends Object, ActionWrapperObject>> returnables = new HashSet<Couple<? extends Object, ActionWrapperObject>>();
//
//        for (Map.Entry<String, ModifiersGroup> e : sequences.entrySet()) {
//            returnables.addAll(createCouples(e));
//
//        }

        return returnables;
    }

//    private Set<Couple<String, ActionWrapperObject>> createCouples(Map.Entry<String, ModifiersGroup> e) {
//
//        Set<Couple<String, ActionWrapperObject>> cs = new HashSet();
//        ModifiersGroup modifiers = e.getValue();
//
//        for (Couple<ModifiersGroup.Modifier, ActionSPI> item : modifiers.getActionSet()) {
//            Couple<String, ActionWrapperObject> c = new Couple();
//            c.setFirst(e.getKey()); //keycode
//
//            ActionWrapperObject awo = new ActionWrapperObject(item.getSecond(), //action object
//                    item.getFirst(), //modifier
//                    this); //controller
//            c.setSecond(awo);
//            cs.add(c);
//
//
//        }
//
//        return cs;
//    }
    public void setContext(ActionBindingContext context) {
        this.context = context;
    }

    public void registerActionSequence(KeyStroke keyStroke, String actionName) {
        synchronized(sequences) {
            sequences.put(keyStroke, actionName);
        }
    }
}
