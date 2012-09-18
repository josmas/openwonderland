/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.jme.input.bindings;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ryan
 */
public enum ContextFactory {
    INSTANCE;
    
    private Map<String, ActionBindingContext> contexts = new HashMap<String, ActionBindingContext>();
    
    
    public ActionBindingContext getContext(String contextName) {
        synchronized(contexts) {
            if(contexts.containsKey(contextName)) {
                return contexts.get(contextName);
            }
            
            ActionBindingContext context = new ActionBindingContext();
            contexts.put(contextName, context);
            return context;
        }
    }
            
}
