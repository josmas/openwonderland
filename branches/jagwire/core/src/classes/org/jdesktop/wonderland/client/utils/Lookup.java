/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.utils;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Ryan
 */
public interface Lookup<S> {
    public S get(Class<? extends S> clazz);
    
    public Collection<S> getAll();
    
    public Map<Class, S> getAllInMap();
    
    public void addClass(Class c);
}
