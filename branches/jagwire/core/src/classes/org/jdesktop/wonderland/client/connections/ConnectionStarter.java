/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.connections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.input.bindings.Couple;
import org.jdesktop.wonderland.client.utils.Lookup;

/**
 *
 * @author Ryan
 */
public class ConnectionStarter {

    private Map<Class, BaseConnection> connections = null;
    private final List<Couple<Class, Properties>> orderedClasses;
    private final Lookup<BaseConnection> lookup;

    private static final Logger logger = Logger.getLogger(ConnectionStarter.class.getName());
    
    public ConnectionStarter(Lookup<BaseConnection> connectionLookup, List<Couple<Class, Properties>> orderedClasses) {
        this.lookup = connectionLookup;
        connections = new HashMap<Class, BaseConnection>(connectionLookup.getAllInMap());
        this.orderedClasses = orderedClasses;
    }

    public void start(WonderlandSession session) throws ConnectionFailureException {
        for (Couple<Class, Properties> couple : orderedClasses) {

            if (connections.containsKey(couple.getFirst())) {
                connections.get(couple.getFirst()).connect(session, couple.getSecond());
                connections.remove(couple.getFirst());
            } else {
                
//                BaseConnection connection = instantiateClass(couple.getFirst());
//                if(connection != null) {
//                    connection.connect(session, couple.getSecond());
//                } else {
                    logger.warning("COULD NOT CONNECT: "+couple.getFirst());
//                }
            }
        }

        for (BaseConnection connection : connections.values()) {
            connection.connect(session);
        }

    }
    

}
