/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.utils;

import org.jdesktop.wonderland.common.utils.ScannedClassLoader;

/**
 * T = annotation type - @Something
 * P = service provider - @SomethingSPI
 * 
 * @author Ryan
 */
public class ClassLookupFactory {
    private final ScannedClassLoader loader;
    private final Class annotation;
    
    public ClassLookupFactory(ScannedClassLoader loader, Class annotation) {
        this.loader = loader;
        this.annotation = annotation;
        
    }
    
    /**
     * 
     * @param <C> type of Service Provider Interface
     * @param clazz class of Service Provider Interface
     * @return 
     */
    public <C>Lookup<C> getLookup(Class<C> clazz) {
       return new DiscreteLookup<C>(loader.getInstances(annotation, clazz));
        
    }
    
}
