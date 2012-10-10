/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.connections.ConnectionStarter;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;

/**
 *
 * @author Ryan
 */
public class DiscreteLookup<S> implements Lookup<S> {

    private Map<Class, S> references = new HashMap<Class, S>();
    private static final Logger logger = Logger.getLogger(DiscreteLookup.class.getName());
    
    
    public DiscreteLookup(Iterator<S> classes) {
        
        while(classes.hasNext()) {
            S clazz = classes.next();
            logger.warning("LOOKUP FOUND CLASS: "+clazz.toString());
            references.put(clazz.getClass(), clazz);
        }
        
    }
    
    public S get(Class<? extends S> clazz) {
        if(references.containsKey(clazz)) {
            return references.get(clazz);
        }
        
        return null;
    }

    public Collection<S> getAll() {
        return references.values();
    }

    public <X> X getExplicit(Class<X> clazz) {
        if(references.containsKey(clazz)) {
            return (X)references.get(clazz);
        }
        
        return null;
    }

    public Map<Class, S> getAllInMap() {
        return new HashMap<Class, S>(references);
    }
    
    public void addClass(Class c) {
        S obj = instantiateClass(c);
        if(obj != null) {
            references.put(c, obj);
        }
    }
    
    private S instantiateClass(Class c) {
        S clazz = null;
        try {
            Constructor construct = c.getConstructor();
            clazz = (S)construct.newInstance();
            
            logger.warning("INSTANTIATING CLASS: "+c.toString());
        } catch (InstantiationException ex) {
            Logger.getLogger(ConnectionStarter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ConnectionStarter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ConnectionStarter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(ConnectionStarter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(ConnectionStarter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ConnectionStarter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return clazz;
        }
    }
    
    
}
