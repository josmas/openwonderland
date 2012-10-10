/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JagWire
 */
public class ClassInstantiator<T> {

    public ClassInstantiator() {
    }

    public Class getClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    public Map.Entry<Class, T> instantiate(String className) {

        
        T o = null;
        Class clazz = null;
        try {
            clazz = getClass(className);
            Constructor constructor = clazz.getConstructor();
            Object obj = constructor.newInstance();
            
            o = (T) obj;
            
            
            
            
        } catch (InstantiationException ex) {
            Logger.getLogger(ClassInstantiator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ClassInstantiator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ClassInstantiator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvocationTargetException ex) {
            Logger.getLogger(ClassInstantiator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(ClassInstantiator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (SecurityException ex) {
            Logger.getLogger(ClassInstantiator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClassInstantiator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            return couple(clazz, o);
        }
    }

    public Map.Entry<Class, T> couple(final Class key, final T value) {
        return new Map.Entry<Class, T>() {
            public Class getKey() {
                return key;
            }

            public T getValue() {
                return value;
            }

            public Object setValue(Object v) {
                return new Object();
            }
        };
    }
}
