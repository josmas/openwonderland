/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.client.login;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Handle logins for the Wonderland system. Keeps track of the relationship
 * between WonderlandSessions and the ServerSessionManager which owns the
 * server connection.
 *
 * @author jkaplan
 */
public class LoginManager {

    /** the UI to prompt the user for login information */
    private static LoginUI ui;

    /** the managers we have created, mapped by server URL */
    private static final Map<String, ServerSessionManager> managers =
            Collections.synchronizedMap(new HashMap<String, ServerSessionManager>());

    /** listeners to notify when the primary login manager changes */
    private static final Set<PrimaryServerListener> listeners =
            new CopyOnWriteArraySet<PrimaryServerListener>();

    /** the primary manager */
    private static ServerSessionManager primaryLoginManager;

    /** the default plugin filter to use */
    private static PluginFilter defaultPluginFilter = 
            new PluginFilter.DefaultPluginFilter();

    /**
     * Set the LoginUI to call back to during login attempts.
     * @param ui the user interface to login with
     */
    public synchronized static void setLoginUI(LoginUI ui) {
        LoginManager.ui = ui;
    }

    /**
     * Get the LoginUI to call back during login attempts
     * @return the login UI
     */
    synchronized static LoginUI getLoginUI() {
        return ui;
    }

    /**
     * Set the default plugin filter for all session managers created.
     * @param filter the plugin filter to use
     */
    public synchronized static void setPluginFilter(PluginFilter filter) {
        LoginManager.defaultPluginFilter = filter;
    }

    /**
     * Get the default plugin filter for all session managers created.
     * @return the plugin filter to use
     */
    synchronized static PluginFilter getPluginFilter() {
        return defaultPluginFilter;
    }

    /**
     * Get the session manager for a particular server URL
     * @param serverURL the serverURL to get a session manager for
     * @return the session manager
     * @throws IOException if there is an error connecting to the given
     * server URL
     */
    public static ServerSessionManager getSessionManager(String serverURL)
        throws IOException
    {
        synchronized (managers) {
            ServerSessionManager manager = managers.get(serverURL);
            if (manager == null) {
                manager = new ServerSessionManager(serverURL);
                managers.put(serverURL, manager);
            }

            return manager;
        }
    }

    /**
     * Get all session managers
     * @return a list of all known session managers
     */
    public static Collection<ServerSessionManager> getAll() {
        return managers.values();
    }

    /**
     * Get the primary session manager
     * @return the primary session manager, if one has been set
     */
    public synchronized static ServerSessionManager getPrimary() {
        return primaryLoginManager;
    }

    /**
     * Set the primary session manager
     * @param primary the primary session manager, or null if the client no
     * longer has a primary server
     */
    public synchronized static void setPrimary(ServerSessionManager primary) {
        LoginManager.primaryLoginManager = primary;

        // notify listeners
        for (PrimaryServerListener l : listeners) {
            l.primaryServer(primary);
        }
    }

    /**
     * Add a primary server listener
     * @param listener the listener to add
     */
    public synchronized static void addPrimaryServerListener(PrimaryServerListener listener) {
        listeners.add(listener);
        if (LoginManager.primaryLoginManager != null) {
            listener.primaryServer(LoginManager.primaryLoginManager);
        }
    }

    /**
     * Remove a primary server listener
     * @param listener the listener to remove
     */
    public static void removePrimaryServerListener(PrimaryServerListener listener) {
        listeners.remove(listener);
    }
}
