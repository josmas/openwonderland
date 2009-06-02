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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.sas.provider;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.ServerUnavailableException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.WonderlandURLStreamHandlerFactory;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.LoginUI;
import org.jdesktop.wonderland.client.login.PluginFilter;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager.NoAuthLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.UserPasswordLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.WebURLLoginControl;
import org.jdesktop.wonderland.client.login.SessionCreator;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The SAS provider client.
 *
 * @author deronj
 */
@ExperimentalAPI
public class SasProvider {
    private static final Logger logger =
            Logger.getLogger(SasProvider.class.getName());

    /** The user name with which the provider will log in. */
    private String userName;
    /** The full name with which the provider will log in. */
    private String fullName;
    /** The password with which the provider will log in. */
    private String password;
    /** The server to which the provider will log in. */
    private String serverUrl;
    /** The execution site dependent listener for messages from the SAS server to provider. */
    private SasProviderConnectionListener listener;

    /**
     * Create a new instance of SasProvider, given login information.
     */
    public SasProvider (String userName, String fullName, String password, String serverUrl,
                        SasProviderConnectionListener listener) {

        this.userName = userName;
        this.fullName = fullName;
        this.password = password;
        this.serverUrl = serverUrl;
        this.listener = listener;

        // set up URL handlers for Wonderland types
        URL.setURLStreamHandlerFactory(new WonderlandURLStreamHandlerFactory());

        // Prevent the login manager from loading usual Wonderland user client jars
        LoginManager.setPluginFilter(new PluginFilter.NoPluginFilter());

        MyLoginUI loginUI = new MyLoginUI();
        LoginManager.setLoginUI(loginUI);

        ServerSessionManager lm;
        try {
            lm = LoginManager.getSessionManager(serverUrl);
        } catch (IOException ioe) {
            RuntimeException re = new RuntimeException("Cannot get login manager instance");
            re.initCause(ioe);
            throw re;
        }

        // create a new session
        SasProviderSession curSession = null;
        try {
            
            // keep trying to log in until we succeed.  Pause 5 seconds between
            // login attempts
            boolean loggedIn = false;
            boolean notified = false;
            do {
                try {
                    curSession = lm.createSession(loginUI);
                    loggedIn = true;
                } catch (ServerUnavailableException sue) {
                    if (!notified) {
                        logger.log(Level.WARNING, "[SasProvider] Darkstar " +
                                   "server not available.  Retrying every 5 " +
                                   "seconds.");
                        notified = true;
                    }
                    Thread.sleep(5000);
                }
            } while (!loggedIn);

            if (notified) {
                logger.log(Level.WARNING, "[SasProvider] connected to " +
                           "Darkstar server.");
            }
        } catch (LoginFailureException lfe) {
            RuntimeException re = new RuntimeException("Error connecting to server.");
            re.initCause(lfe);
            throw re;
        } catch (InterruptedException ie) {
            // thread interrupted
            RuntimeException re = new RuntimeException("Error connecting to server.");
            re.initCause(ie);
            throw re;
        }

        // make sure we logged in successfully
        if (curSession == null) {
            throw new RuntimeException("Unable to create session.");
        }

        curSession.addSessionStatusListener(new SessionStatusListener() {
                public void sessionStatusChanged(WonderlandSession session, WonderlandSession.Status status) {
                    if (status==WonderlandSession.Status.DISCONNECTED) {
                        // TODO: Do nothing for now
                    }
                }
            });

        LoginManager.setPrimary(lm);
        lm.setPrimarySession(curSession);
    }

    /**
     *  Provides the login information from the constructor to the login manager.
     */
    private class MyLoginUI implements LoginUI, SessionCreator<SasProviderSession> {

        // The LoginManager calls this during the login process
        public void requestLogin(final NoAuthLoginControl control) {
            try {
                control.authenticate(userName, fullName);
                return;
            } catch (LoginFailureException lfe) {
                RuntimeException re = new RuntimeException("Cannot authenticate user " + userName);
                re.initCause(lfe);
                throw re;
            }
        }

        public void requestLogin(UserPasswordLoginControl control) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void requestLogin(WebURLLoginControl control) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        // The LoginManager calls this to create the SAS provider session
        public SasProviderSession createSession(ServerSessionManager sessionManager,
                                                WonderlandServerInfo server,
                                                ClassLoader loader)
        {
            return new SasProviderSession(sessionManager, server, loader, listener);
        }
    }
}
