package org.jdesktop.wonderland.client.jme.input.bindings;


import java.awt.event.InputEvent;
import java.util.*;
import org.jdesktop.wonderland.client.jme.input.InputEvent3D;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ActionSPI;


/**
 *
 * @author Ryan
 */
public class ModifiersGroup {
    
    
    private Map<Modifier, ActionSPI> actions;
    
    public ModifiersGroup() { 
        actions = 
                Collections.synchronizedMap(new EnumMap<Modifier, ActionSPI>(Modifier.class));
    }

    public Map<Modifier, ActionSPI> getActions() {
        return actions;
    }
    
    
    
    public void registerAction(Modifier modifier, ActionSPI a) {
        
        actions.put(modifier, a);
        
    }
    
    public void registerAction(ActionSPI a) {
        actions.put(Modifier.NONE, a);
    }
    
    
    
    public ActionSPI getAction(Modifier modifier) {
        return actions.get(modifier);
    }
    
    public ActionSPI getAction() {
        return actions.get(Modifier.NONE);
    }
    
    public void forwardEvent(InputEvent3D event) {
        actions.get(getModifierFromEvent(event)).performAction(event);
    }
    
    public void removeAction(Modifier modifier) {
        synchronized(actions) {
            actions.remove(modifier);
        }
    }
    
    private Modifier getModifierFromEvent(InputEvent3D event) {
        if(event == null) 
            return Modifier.NONE;
 
        InputEvent awtEvent = event.getAwtEvent();
        
        if(awtEvent.isAltDown()) {
            if(awtEvent.isShiftDown()) {
                if(awtEvent.isControlDown()) {
                    //ALT+SHIFT+CTRL
                    return Modifier.ALT_SHIFT_CTRL;
                } else {
                    //ALT+SHIFT
                    return Modifier.ALT_SHIFT;
                }
            } else if(awtEvent.isControlDown()) {
                 //ALT+CTRL
                return Modifier.ALT_CTRL;
            } else {
              //ALT  
                return Modifier.ALT;
            } 
        } else if (awtEvent.isShiftDown()) {
            if(awtEvent.isControlDown()) {
                //SHIFT+CTRL
                return Modifier.SHIFT_CTRL;
            } else {
                //SHIFT
                return Modifier.SHIFT;
            }
        } else if (awtEvent.isControlDown()) {
            //CTRL
            return Modifier.CTRL;
        } else {
            //NONE
            return Modifier.NONE;
        }
        
        
    }

    public Set<Couple<Modifier, ActionSPI>> getActionSet() {
        Set<Couple<Modifier, ActionSPI>> actionSet = new HashSet();
        
        for(Map.Entry<Modifier, ActionSPI> e: actions.entrySet()) {
            Couple<Modifier, ActionSPI> c = new Couple();
            c.setFirst(e.getKey());
            c.setSecond(e.getValue());
            
            actionSet.add(c);
        }
        
        
        return actionSet;
        
    }
    
    public static enum Modifier {
        ALT,
        SHIFT,
        CTRL,
        ALT_SHIFT,
        ALT_CTRL,
        ALT_SHIFT_CTRL,
        SHIFT_CTRL,
        NONE
       
    }
    
}
