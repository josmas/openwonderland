/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.jme.input.bindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.input.InputEvent3D;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ControllerSPI;

/**
 *
 * @author Ryan
 */
public class Dispatcher {
    private Map<Class, ControllerSPI> controllers = null;
    private Class[] clazzes = null;// new Class[] { };
    public Dispatcher(Map<Class, ControllerSPI> controllers) {
        this.controllers = controllers;
        
        //install global listener
        
        List<ControllerSPI> spis = new ArrayList<ControllerSPI>(controllers.values());
        clazzes = new Class[spis.size()];
        for(int i = 0; i < spis.size(); i++) {
            clazzes[i] = spis.get(i).consumesEventClass();
        }
        
        
        InputManager.inputManager().addGlobalEventListener(new ActionEventClassListener());
    }
    
    private void dispatchEventToController(InputEvent3D event, Class clazz) {
        synchronized(controllers) {
            controllers.get(clazz).dispatchEvent(event);
        }
    }
    
    class ActionEventClassListener extends EventClassListener {
        @Override
        public Class[] eventClassesToConsume() {
           
            return clazzes;
        }
        
        @Override
        public void commitEvent(Event event) {
            if(event instanceof InputEvent3D) {
                dispatchEventToController((InputEvent3D)event, event.getClass());
            }
        }

    }
}
