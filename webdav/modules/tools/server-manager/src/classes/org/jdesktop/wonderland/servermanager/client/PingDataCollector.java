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
package org.jdesktop.wonderland.servermanager.client;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSessionImpl;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager.NoAuthLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.UserPasswordLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.WebURLLoginControl;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.LoginUI;
import org.jdesktop.wonderland.client.login.PluginFilter;
import org.jdesktop.wonderland.client.login.SessionCreator;
import org.jdesktop.wonderland.front.admin.ServerInfo;
import org.jdesktop.wonderland.modules.darkstar.server.DarkstarRunner;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.RunManager.RunnerListener;
import org.jdesktop.wonderland.runner.Runner;
import org.jdesktop.wonderland.runner.Runner.RunnerStatusListener;
import org.jdesktop.wonderland.runner.Runner.Status;

/**
 * Montitor the server, and periodically collect data
 * @author jkaplan
 */
public class PingDataCollector 
        implements PingListener, RunnerListener,
                   RunnerStatusListener, SessionStatusListener
{
    private Logger logger =
            Logger.getLogger(PingDataCollector.class.getName());
    
    public static final String KEY = "_ping_data_collector";
    
    // the list of data we have collected
    private List<PingData> data = Collections.synchronizedList(
                                        new LinkedList<PingData>());
    
    // a map from Darkstar runners to the session associated with that
    // runner
    private Map<DarkstarRunner, ServerManagerSession> sessions =
            Collections.synchronizedMap(
                new HashMap<DarkstarRunner, ServerManagerSession>());
    
    public PingDataCollector() {
        LoginManager.setLoginUI(new ServerManagerLoginUI());
        LoginManager.setPluginFilter(new PluginFilter.NoPluginFilter());

        // listen for runners
        RunManager.getInstance().addRunnerListener(this);

        // if any runners already exist, add them
        Collection<DarkstarRunner> runners =
                RunManager.getInstance().getAll(DarkstarRunner.class);
        for (DarkstarRunner dr : runners) {
            runnerAdded(dr);
        }
    }

    /**
     * Get the total amount of data we've collected
     * @return the number of pingdata objects we've stored
     */
    public int getDataSize() {
        return data.size();
    }
    
    /**
     * Get all the data we've collected
     * @returh the data we collected
     */
    public List<PingData> getData() {
        return getData(0);
    }
    
    /**
     * Get all data after a given timestamp
     * @param after the timestamp to get after
     * @return all data after the given timestamp
     */
    public List<PingData> getData(long after) {
        return getData(0, Integer.MAX_VALUE);
    }
    
    /**
     * Get all data after a given timestamp, up to a maximum of
     * the given number of data points.
     * @param after the timestamp to get data after
     * @param count the maximum number of data points to get
     */
    public List<PingData> getData(long after, int count) {
        int firstIdx = 0;
        if (after > 0) {
            firstIdx = findFirstAfter(data, after);
        }
        
        int remaining = data.size() - firstIdx;
        int lastIdx = firstIdx + Math.min(remaining, count);
    
        // make sure these are valid values
        if (firstIdx < 0 || lastIdx < 0) {
            return Collections.emptyList();
        }
        
        return data.subList(firstIdx, lastIdx);
    }
    
    
    /**
     * Find the first data element after the given value. This assumes the
     * list is in ascending order of timestamp
     * @param data the list of data to search
     * @param after the timestamp to search after
     * @return the first index bigger than the given time, or null if
     * no indices are bigger than the given
     */
    protected int findFirstAfter(List<PingData> data, long after) {
        int count = 0;
        synchronized (data) {
            for (PingData d : data) {
                if (d.getSampleDate() > after) {
                    break;
                }
                
                count++;
            }
        }
        
        // check for not found
        if (count >= data.size()) {
            count = -1;
        }
        
        return count;
    }
    
    /**
     * Handle when a runner starts up or shuts down.
     * @param runner the runner that changed status
     * @param status the new status
     */
    public void statusChanged(final Runner runner, final Status status) {
        if (status == Status.RUNNING) {
            new Thread(new Runnable() {
                public void run() {
                    connectTo((DarkstarRunner) runner);
                }
            }).start();
        } else {
            disconnectFrom((DarkstarRunner) runner);
        }
    }

    /**
     * Notification that a session's status has changed
     * @param session the session with the changed status
     * @param status the session's status
     */
    public void sessionStatusChanged(WonderlandSession session, 
                                     WonderlandSession.Status status) 
    {
        if (status == WonderlandSession.Status.DISCONNECTED) {
            // find the runner associated with this session
            DarkstarRunner dr = null;
            synchronized (sessions) {
                for (Entry<DarkstarRunner, ServerManagerSession> e : 
                     sessions.entrySet()) 
                {
                    if (e.getValue().equals(session)) {
                        dr = e.getKey();
                        break;
                    }
                }
            }
            
            if (dr != null) {
                disconnectFrom(dr);
            }
        }
    }
    
    /**
     * Handle an incoing ping message
     * @param data the ping data
     */
    public void pingReceived(PingData cur) {
        data.add(cur);
    }
    
    /**
     * Shut down the collector and remove all registered listeners
     */
    public synchronized void shutdown() {
        // remove runner listener
        RunManager.getInstance().removeRunnerListener(this);

        // remove status listeners
        Collection<DarkstarRunner> runners = 
                RunManager.getInstance().getAll(DarkstarRunner.class);
        for (DarkstarRunner dr : runners) {
            dr.removeStatusListener(this);
        }
        
        // shutdown sessions
        for (ServerManagerSession session : sessions.values()) {
            session.logout();
        }
    }

    public void runnerAdded(Runner runner) {
        if (!(runner instanceof DarkstarRunner)) {
            return;
        }

        Status status = runner.addStatusListener(this);
        if (status == Status.RUNNING) {
            connectTo((DarkstarRunner) runner);
        }
    }

    // connect to a server
    protected void connectTo(DarkstarRunner dr) {
        // TODO: connect to a particular Darkstar server
        try {
            ServerSessionManager lm = LoginManager.getInstance(ServerInfo.getServerURL());
            ServerManagerSession session = lm.createSession(
                    new SessionCreator<ServerManagerSession>()
            {
                public ServerManagerSession createSession(
                        WonderlandServerInfo serverInfo, ClassLoader loader)
                {
                    // use our classloader
                    return new ServerManagerSession(serverInfo,
                                                   getClass().getClassLoader());
                }
            });

            session.addSessionStatusListener(this);

            // remember the session
            sessions.put(dr, session);

            // make a note of the new connection
            WonderlandServerInfo info = session.getServerInfo();
            PingData note = new PingData();
            note.setPingNoteTitle("Connected to server");
            note.setPingNoteText("Connected to server " + info.getHostname() +
                                 ":" + info.getSgsPort());
            data.add(note);
        } catch (LoginFailureException le) {
            logger.log(Level.WARNING, "Unable to log in to server " +
                       ServerInfo.getServerURL(), le);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error logging in to server " +
                       ServerInfo.getServerURL(), ioe);
        }
    }

    public void runnerRemoved(Runner runner) {
        if (!(runner instanceof DarkstarRunner)) {
            return;
        }

        disconnectFrom((DarkstarRunner) runner);
    }
    
    // disconnect from the server
    protected void disconnectFrom(DarkstarRunner dr) {
        // TODO get from runner
        String serverHost = dr.getHostname();
        int serverPort = dr.getPort();
        
        logger.warning("Disconnect from " + serverHost + " " + serverPort);
        
        // disconnect from the server
        ServerManagerSession session = sessions.remove(dr);
        if (session != null) {
            session.logout();
        }
        
        // make a note of the disconnect
        PingData note = new PingData();
        note.setPingNoteTitle("Disconnected from server");
        note.setPingNoteText("Disconnected from server " + serverHost + 
                             ":" + serverPort);
        data.add(note);
    }
    
    class ServerManagerSession extends WonderlandSessionImpl {
        private ServerManagerConnection smc;
        
        public ServerManagerSession(WonderlandServerInfo server,
                                    ClassLoader classLoader) 
        {
            super (server, classLoader);
        }

        @Override
        public void login(LoginParameters loginParams) 
                throws LoginFailureException 
        {
            super.login(loginParams);
            
            try {
                smc = new ServerManagerConnection();
                smc.connect(this);
                smc.addPingListener(PingDataCollector.this);
            } catch (ConnectionFailureException cfe) {
                logout();
                
                throw new LoginFailureException("Error connecting server " +
                                                "manager connection", cfe);
            }
            
        }
    }

    class ServerManagerLoginUI implements LoginUI {
        public void requestLogin(NoAuthLoginControl control) {
            try {
                control.authenticate("servermanager", "Server Manager");
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
}
