package org.jdesktop.wonderland.client.jme.input.bindings.controllers;

import java.awt.event.MouseEvent;
import java.util.Map.Entry;
import java.util.*;
import org.jdesktop.wonderland.client.jme.input.InputEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.bindings.ActionBindingContext;
import org.jdesktop.wonderland.client.jme.input.bindings.ActionWrapperObject;
import org.jdesktop.wonderland.client.jme.input.bindings.Couple;
import org.jdesktop.wonderland.client.jme.input.bindings.ModifiersGroup;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ActionSPI;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.MouseButtonAction;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.MouseButtonActionSPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ryan
 */
public class MouseButtonController extends AbstractBaseController {

    private Map<Integer, ModifiersGroup> sequences;
    
//    public void registerActionSequence(ActionWrapperObject awo) {
//        sequences.put(awo.getKeyCode(), new ModifiersGroup());
//        sequences.get(awo.getKeyCode()).registerAction(awo.getModifier(), awo.getAction());
//    }
    private ActionBindingContext context;
    
    
    @Override
    public Class consumesEventClass() {
        return MouseButtonEvent3D.class;
    } 

    @Override
    public <T extends InputEvent3D> void dispatchEvent(T event) {
        MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D)event;
        MouseEvent awtEvent = (MouseEvent)event.getAwtEvent();
        int button = awtEvent.getButton();
        
        sequences.get(button).forwardEvent(event);
        
    }


    @Override
    public void loadActions(ActionBindingContext context) {
        this.context = context;
        
        sequences = new HashMap<Integer, ModifiersGroup>();
        
        
        ScannedClassLoader loader = LoginManager.getPrimary().getClassloader();
        
        Iterator<MouseButtonActionSPI> actions = 
                loader.getInstances(MouseButtonAction.class,
                                    MouseButtonActionSPI.class);
        
        while(actions.hasNext()) {
            MouseButtonActionSPI mouseAction = actions.next();
        
            //construct AWO
            ActionWrapperObject awo = new ActionWrapperObject(mouseAction,
                                                              mouseAction.getModifier(),
                                                              this);
            //construct Couple
            Couple<Integer, ActionWrapperObject> sequence =
                    new Couple<Integer, ActionWrapperObject>();
            sequence.setFirst(mouseAction.getButton());
            sequence.setSecond(awo);
                        
            //register action sequence
            registerActionSequence(sequence);
            
            //add actions to registry
            this.context.getRegistry().registerAction(awo, mouseAction.getName());
            
            
        }
        
        
        
        
                
        
        
    }

    @Override
    public Set<Couple<? extends Object, ActionWrapperObject>> getActions() { 
        Set<Couple<? extends Object, ActionWrapperObject>> returnables = new HashSet<Couple<? extends Object, ActionWrapperObject>>();
        
        for(Map.Entry<Integer, ModifiersGroup> e: sequences.entrySet()) {
            returnables.addAll(createCouples(e));
        }
        
        return returnables;                    
    
    
    }
    public void registerActionSequence(Couple<Integer, ActionWrapperObject> linkedAction) {

        if(!sequences.containsKey(linkedAction.getFirst())) {
            sequences.put(linkedAction.getFirst(), new ModifiersGroup());
        }
        
        ActionWrapperObject awo = linkedAction.getSecond();
        sequences.get(linkedAction.getFirst()).registerAction(awo.getModifier(),
                                                               awo.getAction());
    }
    
    public void unregisterActionSequence(Couple<Integer, ActionWrapperObject> linkedAction) {
        synchronized(sequences) {
            ActionWrapperObject awo = linkedAction.getSecond();
            
            if(sequences.containsKey(linkedAction.getFirst())) {
                sequences.get(linkedAction.getFirst()).removeAction(awo.getModifier());
            }
        }
    }

    private Collection<? extends Couple<Integer, ActionWrapperObject>> createCouples(Entry<Integer, ModifiersGroup> e) {
        Set<Couple<Integer, ActionWrapperObject>> cs = new HashSet();
        
        ModifiersGroup modifiers = e.getValue();
        
        for(Couple<ModifiersGroup.Modifier, ActionSPI> item: modifiers.getActionSet()) {
            Couple<Integer, ActionWrapperObject> c = new Couple();
            
            c.setFirst(e.getKey());
            ActionWrapperObject awo = new ActionWrapperObject(item.getSecond(),
                                                              item.getFirst(),
                                                              this);
            c.setSecond(awo);
            cs.add(c);
        }
        return cs;
    }

    public void setContext(ActionBindingContext context) {
        this.context = context;
    }

    public void bindObject(Object in, String name) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void unbindObject(Object key) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    
}
