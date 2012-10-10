/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JagWire
 */
public  class ClassLookup<T> {
    
    private Map<Class, T> instances;
    
    public ClassLookup() {
        instances = new HashMap<Class, T>();
    }
    
    public ClassLookup(Set<String> classNames) {
        ClassInstantiator instantiator = new ClassInstantiator<T>();
        
        for(String name: classNames) {
            
            Map.Entry<Class, T> e = instantiator.instantiate(name);
            
            if(e == null) {
                continue;
            }
            
            instances.put(e.getKey(), e.getValue());
        }
    }
    
    
    public T getInstance(Class clazz) {
        
        if(instances.containsKey(clazz)) {
            return instances.get(clazz);
        } else {
            return null;
        }
    }
    
    public Collection<T> getInstances() {
        return instances.values();
    }
    
}
