/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.client.comms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A manager for creating new instances of WonderlandSession.  This class
 * also provides methods to manage session lifecycle listeners.
 * @author jkaplan
 */
@ExperimentalAPI
public class WonderlandSessionManager {
    /** sessions we have created, mapped my serverInfo */
    private Map<WonderlandServerInfo, WonderlandSession> sessions =
        new HashMap<WonderlandServerInfo, WonderlandSession>();
    
    /** client lifecycle listeners */
    private Set<SessionLifecycleListener> lifecycleListeners =
            new CopyOnWriteArraySet<SessionLifecycleListener>();
    
    
    /**
     * Get the WonderlandSession to connect to server identified by the given
     * server info object.  
     * @param serverInfo the server to connect to
     */
    public WonderlandSession getSession(WonderlandServerInfo serverInfo) {
        WonderlandSession session;
        boolean newSession = false;
        
        synchronized (WonderlandSessionManager.class) {
            session = sessions.get(serverInfo);
            
            // see if we need to create a new session
            if (session == null) {
                newSession = true;
                session = new WonderlandSessionImpl(serverInfo);
                sessions.put(serverInfo, session);
            }
        }
       
        // notify listeners if we created a new session
        if (newSession) {
            fireNewSession(session);
        }
        
        return session;
    }
    
    /**
     * Add a lifecycle listener.  This will receive messages for all
     * clients that are created or change status
     * @param listener the listener to add
     */
    public void addLifecycleListener(SessionLifecycleListener listener) {
        lifecycleListeners.add(listener);
    }
    
    /**
     * Remove a lifecycle listener.
     * @param listener the listener to remove
     */
    public void removeLifecycleListener(SessionLifecycleListener listener) {
        lifecycleListeners.remove(listener);
    }
    
    /**
     * Notify any registered lifecycle listeners that a new session was created
     * @param session the client that was created
     */
    private void fireNewSession(WonderlandSession session) {
        for (SessionLifecycleListener listener : lifecycleListeners) {
            listener.sessionCreated(session);
        }
    }
}
