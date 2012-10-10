/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.login;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.modules.ModuleUtils;
import org.jdesktop.wonderland.common.JarURI;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.login.AuthenticationInfo;
import org.jdesktop.wonderland.common.login.CredentialManager;
import org.jdesktop.wonderland.common.modules.ModulePluginList;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;

/**
 *
 * @author Ryan
 */
public abstract class LoginControl {

    private AuthenticationInfo authInfo;
    private boolean started = false;
    private boolean finished = false;
    private boolean success = false;
    private LoginParameters params;
    private ScannedClassLoader classLoader;
    private final ServerSessionManager sessionManager;

    private static final Logger logger = Logger.getLogger(LoginControl.class.getName());
        private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/client/login/Bundle");
    
    /**
     * Create a new login control for the given server
     *
     * @param authInfo the authentication server
     */
    public LoginControl(AuthenticationInfo authInfo, final ServerSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.authInfo = authInfo;
    }

    /**
     * Get the authentication info for this login
     *
     * @return the authentication info
     */
    protected AuthenticationInfo getAuthInfo() {
        return authInfo;
    }

    /**
     * Get the server URL for this login control object
     *
     * @return the server URL to connect to
     */
    public String getServerURL() {
        return sessionManager.getServerURL();
    }

    /**
     * Get the server session manager associated with this login control object.
     *
     * @return the server session manager
     */
    public ServerSessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Determine if login is complete and successful.
     *
     * @return true of the login is complete and successful, false if the login
     * is in progress or failed.
     */
    public synchronized boolean isAuthenticated() {
        return finished && success;
    }

    /**
     * Determine if login is in progress. This will return true if a login has
     * been requested from the client, but they have not yet responded.
     *
     * @return true if a login is in progress, or false if not
     */
    public synchronized boolean isAuthenticating() {
        return started && !finished;
    }

    /**
     * Request a login from the given login UI
     */
    public void requestLogin(LoginUI ui) {
        setStarted();
    }

    /**
     * Indicate that a login is in progress with this control object
     */
    protected synchronized void setStarted() {
        started = true;
    }

    /**
     * Get the classloader to use when connecting to the Darkstar server. This
     * method is only valid when isAuthenticated() returns true.
     *
     * @return the classloader to use
     */
    public synchronized ScannedClassLoader getClassLoader() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Not authenticated");
        }
        return classLoader;
    }

    /**
     * Get the LoginParameters to use when connecting to the Darkstar server.
     * This method is valid starting after the server login has happened, but
     * before any plugins have been initialized.
     *
     * @return the LoginParameters to use, or null if the parameters have not
     * been set yet
     */
    public synchronized LoginParameters getLoginParameters() {
        return params;
    }

    /**
     * Get the username that this user has connected as. This should be a unique
     * identifier for the user based on the authentication information they
     * provided. This method must return a value any time after
     * <code>loginComplete()</code> has been called.
     *
     * @return the username the user has logged in as
     */
    public abstract String getUsername();

    /**
     * Get the credential manager associated with this login control.
     *
     * @return the credential manager
     */
    public abstract CredentialManager getCredentialManager();

    /**
     * Indicate that the login attempt was successful, and pass in the
     * LoginParameters that should be sent to the Darkstar server to create a
     * session. <p> This method indicates that login has been successful, so
     * sets up the plugin classloader for use in session creation. Once the
     * classloader is setup, it notifies any listeners that login is complete.
     *
     * @param loginParams the parameters to login with. A null LoginParameters
     * object indicates that the login attempt has failed.
     */
    protected synchronized void loginComplete(LoginParameters params) {
        this.params = params;
        if (params != null) {
            // setup the classloader
            this.classLoader = setupClassLoader(getServerURL());
            // initialize plugins
            initializePlugins(classLoader);
            
            
            logger.warning("\n************************************\n"
                            +"* PLUGINS INITIALIZED!             *\n"
                            +"************************************");
            
            // if we get here, the login has succeeded
            this.success = true;
        }
        this.started = false;
        this.finished = true;
        notify();
    }

    /**
     * Cancel the login in progress
     */
    public synchronized void cancel() {
        loginComplete(null);
    }

    /**
     * Wait for the current login in progress to end
     *
     * @return true if the login is successful, or false if not
     * @throws InterruptedException if the thread is interrupted before the
     * login parameters are determined
     */
    protected synchronized boolean waitForLogin() throws InterruptedException {
        while (isAuthenticating()) {
            wait();
        }
        return isAuthenticated();
    }

    /**
     * Set up the classloader with module jar URLs for this server
     *
     * @param serverURL the URL of the server to connect to
     * @return the classloader setup with this server's URLs
     */
    private ScannedClassLoader setupClassLoader(String serverURL) {
        sessionManager.fireConnecting(BUNDLE.getString("Creating classloader"));

        // TODO: use the serverURL
        ModulePluginList list = ModuleUtils.fetchPluginJars(serverURL);
        List<URL> urls = new ArrayList<URL>();
        if (list == null) {
            logger.warning("Unable to configure classlaoder, falling back to "
                    + "system classloader");
            return new ScannedClassLoader(new URL[0],
                    getClass().getClassLoader());
        }

        for (JarURI uri : list.getJarURIs()) {
            try {
                // check the filter to see if we should add this URI
                if (LoginManager.getPluginFilter().shouldDownload(sessionManager, uri)) {
                    urls.add(uri.toURL());
                }
            } catch (Exception excp) {
                excp.printStackTrace();
            }
        }

        return new ScannedClassLoader(urls.toArray(new URL[0]),
                getClass().getClassLoader());
    }
    
    
    
    
    
    /**
     * Initialize plugins
     */
    public void initializePlugins(ScannedClassLoader loader) {
        // At this point, we have successfully logged in to the server,
        // and the session should be connected.

        // Collect all plugins from service provides and from annotated
        // classes, then initialize each one
        Iterator<ClientPlugin> it = loader.getAll(Plugin.class,
                                                  ClientPlugin.class);

        while (it.hasNext()) {
            ClientPlugin plugin = it.next();

            String message = BUNDLE.getString("Initialize plugin");
            message = MessageFormat.format(
                    message, plugin.getClass().getSimpleName());
            sessionManager.fireConnecting(message);

            // check with the filter to see if we should load this plugin
            if (LoginManager.getPluginFilter().shouldInitialize(sessionManager, plugin)) {
                try {
                    
                    plugin.initialize(sessionManager);
                    sessionManager.getSetOfPlugins().add(plugin);
                } catch(Exception e) {
                    logger.log(Level.WARNING, "Error initializing plugin " +
                               plugin.getClass().getName(), e);
                } catch(Error e) {
                    logger.log(Level.WARNING, "Error initializing plugin " +
                               plugin.getClass().getName(), e);
                }
            }
        }
    }
    
}
