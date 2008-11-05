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
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.modules.ModulePluginList;
import org.jdesktop.wonderland.client.modules.ModuleUtils;
import org.jdesktop.wonderland.common.AssetURI;
import sun.misc.Service;

/**
 * Handle logins for the Wonderland system.
 *
 * @author jkaplan
 */
public class LoginManager {
    private static final Logger logger =
            Logger.getLogger(LoginManager.class.getName());

    /** where on the server to find the details object */
    private static final String DETAILS_URL =
            "wonderland-web-front/resources/ServerDetails";

    /** the UI to prompt the user */
    private static LoginUI ui;


    /** the singleton instance */
    private static final Map<String, LoginManager> managers =
            Collections.synchronizedMap(new HashMap<String, LoginManager>());

    /** the server this manager represents */
    private String serverURL;

    /** details about the server (read from server in constructor) */
    private ServerDetails details;

    /** whether or not we are authenticated to the server */
    private boolean authenticated = false;

    /** the classloader for this manager */
    private ClassLoader loader;

    /** the session for this login */
    private WonderlandSession session;

    /**
     * Constructor is private, use getInstance() instead.
     * @param serverURL the url to connect to
     * @throws IOException if there is an error connecting to the server
     */
    private LoginManager(String serverURL) throws IOException {
        // load the server details
        try {
            URL detailsURL = new URL(new URL(serverURL), DETAILS_URL);
            this.details = ServerDetails.decode(new InputStreamReader(detailsURL.openStream()));
        } catch (JAXBException jbe) {
            IOException ioe = new IOException("Error reading server details " +
                                              "from: " + serverURL);
            ioe.initCause(jbe);
            throw ioe;
        }

        // set the server URL to the canonical URL sent by the server
        this.serverURL = details.getServerURL();
    }

    /**
     * Set the LoginUI to call back to during login attempts.
     * @param ui the user interface to login with
     */
    public synchronized static void setLoginUI(LoginUI ui) {
        if (ui != null && LoginManager.ui != null) {
            throw new IllegalStateException("Login manager already set");
        }

        LoginManager.ui = ui;
    }

    /**
     * Get the login manager for a particular server URL
     * @param serverURL the serverURL to get a login manager for
     * @return the login manager
     * @throws IOException if there is an error connecting to the given
     * server URL
     */
    public static LoginManager getInstance(String serverURL)
        throws IOException
    {
        synchronized (managers) {
            LoginManager manager = managers.get(serverURL);
            if (manager == null) {
                manager = new LoginManager(serverURL);
                managers.put(serverURL, manager);
            }

            return manager;
        }
    }

    /**
     * Get the login manager that is responsible for a particular session.
     * @param session the session to find a login manager for.
     * @return the LoginManager associated with the given session, or null
     * if no login manager is associated with the given session.
     */
    public static LoginManager findLoginManager(WonderlandSession session) {
        synchronized (managers) {
            for (LoginManager lm : managers.values()) {
                if (lm.getSession().equals(session)) {
                    return lm;
                }
            }
        }

        return null;
    }

    /**
     * Get the server URL this login manager represents.  This is the
     * canonical URL returned by the server that was originally requested,
     * not necessarily the original URL that was passed in
     * @return the canonical server URL
     */
    public String getServerURL() {
        return serverURL;
    }

    /**
     * Get the details for this server
     * @return the details for this server
     */
    public ServerDetails getDetails() {
        return details;
    }

    /**
     * Return whether or not we are authenticated
     * @return true if we have successfully authenticated to this
     * server, or false if not
     */
    public synchronized boolean isAuthenicated() {
        return authenticated;
    }

    /**
     * Authenticate to the server.  This will request authentication details
     * from the LoginUI, and set up the connection on success.
     * @return true if the authentication succeeded, or false if not
     * @throws IllegalStateException if the LoginUI is not set.
     */
    public boolean authenticate() {
        // make sure we aren't already authenticated
        synchronized (this) {
            if (authenticated) {
                return true;
            }
        }

        // make sure there is a LoginUI
        if (ui == null) {
            throw new IllegalStateException("No login UI");
        }

        // figure out what type of authentication it is, and then request
        // that type of login from the UI
        BaseLoginControl lc = null;
        AuthenticationInfo info = getDetails().getAuthInfo();
        switch (info.getType()) {
            case NONE:
                lc = new NoAuthLoginControl();
                ui.requestLogin((NoAuthLoginControl) lc);
                break;
            case WEB_SERVICE:
                lc = new UserPasswordLoginControl();
                ui.requestLogin((UserPasswordLoginControl) lc);
                break;
            case WEB:
                lc = new WebURLLoginControl(info.getAuthURL());
                ui.requestLogin((WebURLLoginControl) lc);
                break;
            default:
                throw new IllegalStateException("Unknown login type " +
                                                info.getType());
        }

        // wait for success or cancel
        try {
            boolean loggedIn = lc.waitForLoginResult();
        
            // make sure we logged in successfully
            if (!loggedIn) {
                return false;
            }
        } catch (InterruptedException ie) {
            logger.log(Level.WARNING, "Login interrupted", ie);
            return false;
        }
        
        // at this point, we have logged in, so we shouldn't attempt any more
        // logins until we disconnect
        synchronized (this) {
            authenticated = true;
        }

        // At this point, we have successfully logged in to the server,
        // and the session should be connected.
        Iterator<ClientPlugin> it = Service.providers(ClientPlugin.class,
                                                      loader);
        while (it.hasNext()) {
            ClientPlugin plugin = it.next();
            try {
                plugin.initialize(session);
            } catch(Exception e) {
                logger.log(Level.WARNING, "Error initializing plugin " +
                           plugin.getClass().getName(), e);
            } catch(Error e) {
                logger.log(Level.WARNING, "Error initializing plugin " +
                           plugin.getClass().getName(), e);
            }
        }

        ClientContext.getWonderlandSessionManager().registerSession(session);
        return true;
    }

    /**
     * Return the session for this login
     * @return the session
     */
    public WonderlandSession getSession() {
        return session;
    }

    /**
     * Create a Wonderland session with the given login parameters
     * @param loginParams the parameters to login with
     * @throws LoginFailureException if there is an error logging in
     * to the Darkstar server
     */
    private synchronized void createSession(LoginParameters loginParams)
            throws LoginFailureException
    {
        // set up a classloader with the module jars
        loader = setupClassLoader(getDetails().getServerURL());

        // request the UI create an appropriate session
        DarkstarServer ds = getDetails().getDarkstarServers()[0];
        WonderlandServerInfo serverInfo =
                new WonderlandServerInfo(ds.getHostname(), ds.getPort());
        session = ui.createSession(serverInfo, loader);

        // now log in to the session
        session.login(loginParams);
    }

    /**
     * Set up the classloader with module jar URLs for this server
     * @param serverURL the URL of the server to connect to
     * @return the classloader setup with this server's URLs
     */
    private ClassLoader setupClassLoader(String serverURL) {
        // TODO: use the serverURL
        ModulePluginList list = ModuleUtils.fetchPluginJars(serverURL);
        List<URL> urls = new ArrayList<URL>();
        if (list==null) {
            logger.warning("Unable to configure classlaoder, falling back to " +
                           "system classloader");
            return getClass().getClassLoader();
        }

        for (AssetURI uri : list.getJarURIs()) {
            try {
                logger.warning("[JARS] " + uri.getURI().toString());
                urls.add(uri.getURI().toURL());
            } catch (Exception excp) {
                excp.printStackTrace();
           }
        }

        return new URLClassLoader(urls.toArray(new URL[0]),
                                  getClass().getClassLoader());
    }

    public abstract class BaseLoginControl {
        private boolean finished = false;
        private boolean success = false;

        /**
         * Get the server URL
         * @return the server URL to connect to
         */
        public String getServerURL() {
            return LoginManager.this.getServerURL();
        }

        /**
         * Cancel the login in progress
         */
        public synchronized void cancel() {
            finished = true;
            success = false;

            notify();
        }

        /**
         * This method should be called after the web service or external login
         * is complete.  It then continues the login process by initializing
         * the client plugins and logging in to the Darkstar server with the
         * credentials it is given.  If the Darkstar login succeeds, the login
         * is finished and the rest of the system will proceed normally.  If
         * the login fails, it is up to the caller to notify the user.
         * @param loginParams the parameters to login with
         * @throws LoginFailureException if the darkstar login fails for
         * any reason
         */
        protected void createSession(LoginParameters params) 
                throws LoginFailureException
        {
            // create the session and log in
            LoginManager.this.createSession(params);
            
            // if there was no exception, we've succeeded.  Notify any
            // listeners
            synchronized (this) {
                finished = true;
                success = true;

                notify();
            }
        }

        /**
         * Wait for the login to finish.  This method returns the status
         * of the login on completion, either success or cancel.
         * @return true if the login succeeded, or false if the login was
         * cancelled
         * @throws InterruptedException if the thread is interrupted before
         * the
         */
        protected synchronized boolean waitForLoginResult()
            throws InterruptedException
        {
            while (!finished) {
                wait();
            }

            return success;
        }

    }

    public class NoAuthLoginControl extends BaseLoginControl {
        public void authenticate(String username, String fullname)
            throws LoginFailureException
        {
            // no other authentication to do, just do the Darkstar login
            // with all the data packed into the username
            String packed = formatUsername(username, fullname);
            createSession(new LoginParameters(packed, new char[0]));
        }
        
        /**
         * Combine values into a single username argument that can be
         * passed to the Darkstar server to populate our identity.
         */
        protected String formatUsername(String username, String fullname) {
            StringBuffer sb = new StringBuffer();
            sb.append("un=" + username + ";");
            sb.append("fn=" + fullname + ";");
            return sb.toString();
        }
    }

    public class UserPasswordLoginControl extends BaseLoginControl {
        public boolean authenticate(String username, char[] password) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public class WebURLLoginControl extends BaseLoginControl {
        private String url;

        public WebURLLoginControl(String url) {
            this.url = url;
        }

        public String getURL() {
            return url;
        }
    }
}
