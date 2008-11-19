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
package org.jdesktop.wonderland.client.login;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

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

    private static final HashMap<WonderlandSession, ServerSessionManager> sessionMap = new HashMap();

    /** the primary manager */
    private static ServerSessionManager primaryLoginManager;

    /** whether to load plugins */
    private static boolean loadPlugins = true;

    /**
     * Set the LoginUI to call back to during login attempts.
     * @param ui the user interface to login with
     */
    public synchronized static void setLoginUI(LoginUI ui) {
        LoginManager.ui = ui;
    }

    synchronized static LoginUI getLoginUI() {
        return ui;
    }

    /**
     * Get the login manager for a particular server URL
     * @param serverURL the serverURL to get a login manager for
     * @return the login manager
     * @throws IOException if there is an error connecting to the given
     * server URL
     */
    public static ServerSessionManager getInstance(String serverURL)
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
     * Get all login managers
     * @return a list of all known login manager
     */
    public static Collection<ServerSessionManager> getAll() {
        return managers.values();
    }

    /**
     * Get the login manager that is responsible for a particular session.
     * @param session the session to find a login manager for.
     * @return the LoginManager associated with the given session, or null
     * if no login manager is associated with the given session.
     */
    public static ServerSessionManager find(WonderlandSession session) {
        synchronized (sessionMap) {
            return sessionMap.get(session);
        }
    }

    /**
     * Get the primary login manager
     * @return the primary login manager, if one has been set
     */
    public synchronized static ServerSessionManager getPrimary() {
        return primaryLoginManager;
    }

    /**
     * Get the primary login manager
     * @return the primary login manager, if one has been set
     */
    public synchronized static void setPrimary(ServerSessionManager primary) {
        LoginManager.primaryLoginManager = primary;
    }

    /**
     * Get whether or not plugins are being loaded.
     * @return true if plugins are being loaded, or false if not
     */
    public synchronized static boolean getLoadPlugins() {
        return LoginManager.loadPlugins;
    }

    /**
     * Set whether or not to load plugins
     * @param loadPlugins true to load plugins, or false to skip them
     */
    public synchronized static void setLoadPlugins(boolean loadPlugins) {
        LoginManager.loadPlugins = loadPlugins;
    }

    /**
     * Called by ServerSessionManager to add a session to a server. LoginManager
     * keeps track of the server/session relationship to avoid deadlock issues
     * with find.
     *
     * @param session
     * @param loginInfo
     */
    synchronized static void addSession(WonderlandSession session, ServerSessionManager loginInfo) {
        synchronized(sessionMap) {
            sessionMap.put(session, loginInfo);
        }
    }
}
