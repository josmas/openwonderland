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
package org.jdesktop.wonderland.modules.darkstar.server;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.LoginUI;
import org.jdesktop.wonderland.client.login.PluginFilter;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager.NoAuthLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.UserPasswordLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.WebURLLoginControl;
import org.jdesktop.wonderland.front.admin.ServerInfo;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.RunManager.RunnerListener;
import org.jdesktop.wonderland.runner.Runner;
import org.jdesktop.wonderland.runner.Runner.RunnerStatusListener;
import org.jdesktop.wonderland.runner.Runner.Status;

/**
 * Singleton for managing login to the Darkstar server from the web server.
 * @author jkaplan
 */
public class DarkstarWebLogin implements RunnerListener, RunnerStatusListener {
    private static final Logger logger =
            Logger.getLogger(DarkstarWebLogin.class.getName());

    private static final Map<DarkstarRunnerImpl, ServerSessionManager> sessions =
            new LinkedHashMap<DarkstarRunnerImpl, ServerSessionManager>();

    private static final Set<DarkstarServerListener> listeners =
            new CopyOnWriteArraySet<DarkstarServerListener>();

    /**
     * Get an instance of the DarkstarWebLogin class
     * @return a shared instance of Darkstar web login
     */
    public static DarkstarWebLogin getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Protected singleton constructor -- use getInstance() instead.
     */
    protected DarkstarWebLogin() {
        LoginManager.setLoginUI(new DarkstarWebLoginUI());
        LoginManager.setPluginFilter(new PluginFilter.NoPluginFilter());

        // listen for runners
        RunManager.getInstance().addRunnerListener(this);

        // if any runners already exist, add them
        Collection<DarkstarRunnerImpl> runners =
                RunManager.getInstance().getAll(DarkstarRunnerImpl.class);
        for (DarkstarRunnerImpl dr : runners) {
            runnerAdded(dr);
        }
    }

    /**
     * Add a listener that will be notified when Darkstar servers start and
     * stop. On addition, the listener will be immediately notified of all
     * existing servers.
     * @param listener the listener to add
     */
    public void addDarkstarServerListener(DarkstarServerListener listener) {
        listeners.add(listener);

        for (Entry<DarkstarRunnerImpl, ServerSessionManager> e : sessions.entrySet()) {
            listener.serverStarted(e.getKey(), e.getValue());
        }
    }

    /**
     * Remove a listener that will be notified of server stops and starts.
     * @param listener the listener to remove
     */
    public void removeDarkstarServerListener(DarkstarServerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notification that a new runner has been added
     * @param runner the runner that was added
     */
    public void runnerAdded(Runner runner) {
        if (!(runner instanceof DarkstarRunnerImpl)) {
            return;
        }

        Status status = runner.addStatusListener(this);
        if (status == Status.RUNNING) {
            statusChanged(runner, status);
        }
    }

    /**
     * Notification that a runner has been removed
     * @param runner the runner that was removed
     */
    public void runnerRemoved(Runner runner) {
        if (!(runner instanceof DarkstarRunnerImpl)) {
            return;
        }

        runner.removeStatusListener(this);
        statusChanged(runner, Status.NOT_RUNNING);
    }

    /**
     * Handle when a runner starts up or shuts down.
     * @param runner the runner that changed status
     * @param status the new status
     */
    public void statusChanged(final Runner runner,
                              final Status status)
    {
        new Thread(new Runnable() {
            public void run() {
                switch (status) {
                    case RUNNING:
                        fireServerStarted((DarkstarRunnerImpl) runner);
                        break;
                    case NOT_RUNNING:
                        fireServerStopped((DarkstarRunnerImpl) runner);
                        break;
                }
            }
        }).start();
    }

    /**
     * Notify listeners of a server starting
     * @param runner the runner that started
     */
    protected void fireServerStarted(DarkstarRunnerImpl runner) {

        try {
            // XXX TODO: Make server-specific
            ServerSessionManager sessionManager =
                LoginManager.getSessionManager(ServerInfo.getServerURL());

            // notify listeners
            for (DarkstarServerListener l : listeners) {
                l.serverStarted(runner, sessionManager);
            }
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error getting session manager", ioe);
        }
    }

    /**
     * Notify listeners of a server stopping
     * @param runner the runner that stopped
     */
    protected void fireServerStopped(DarkstarRunnerImpl runner) {
        for (DarkstarServerListener l : listeners) {
            l.serverStopped(runner);
        }
    }

    /**
     * Listener to notify of server connects and disconnects
     */
    public interface DarkstarServerListener {
        /**
         * Notification that the server has started up
         * @param runner the DarkstarRunnerImpl that started up
         * @param sessionManager a server sesssion manager that can be
         * used to connect to this server
         */
        public void serverStarted(DarkstarRunner runner,
                                  ServerSessionManager sessionManager);

        /**
         * Notification that the server has shut down
         * @param runner the DarkstarRunnerImpl that shut down
         */
        public void serverStopped(DarkstarRunner runner);
    }

    /**
     * Internal class for handling login to the Darkstar server
     */
    private class DarkstarWebLoginUI implements LoginUI {
        public void requestLogin(NoAuthLoginControl control) {
            try {
                control.authenticate("webserver", "Wonderland web server");
            } catch (LoginFailureException lfe) {
                logger.log(Level.WARNING, "Error connecting to " +
                           control.getServerURL(), lfe);
                control.cancel();
            }
        }

        public void requestLogin(UserPasswordLoginControl control) {
        }

        public void requestLogin(WebURLLoginControl control) {
        }
    }

    /**
     * Holder for the singleton instance
     */
    private static final class SingletonHolder {
        private static final DarkstarWebLogin INSTANCE = new DarkstarWebLogin();
    }

}
