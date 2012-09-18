/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.login;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.common.login.AuthenticationException;
import org.jdesktop.wonderland.common.login.AuthenticationInfo;
import org.jdesktop.wonderland.common.login.AuthenticationManager;
import org.jdesktop.wonderland.common.login.AuthenticationService;
import org.jdesktop.wonderland.common.login.CredentialManager;

/**
 *
 * @author Ryan
 */
public abstract class WebServiceLoginControl extends LoginControl {
    private String username;
    private AuthenticationService authService;

    private static final Logger logger = Logger.getLogger(WebServiceLoginControl.class.getName());
    private static final ResourceBundle BUNDLE =  ResourceBundle.getBundle(
            "org/jdesktop/wonderland/client/login/Bundle");
    
    
    
    public WebServiceLoginControl(AuthenticationInfo authInfo, ServerSessionManager sessionManager) {
        super(authInfo, sessionManager);
    }

    public String getUsername() {
        return username;
    }

    protected void setUsername(String username) {
        this.username = username;
    }

    public CredentialManager getCredentialManager() {
        return authService;
    }

    protected void setAuthService(AuthenticationService authService) {
        this.authService = authService;
    }

    protected boolean needsLogin() {
        // check if we already have valid credentials
        synchronized (this) {
            if (authService == null) {
                authService = AuthenticationManager.get(getAuthInfo().getAuthURL());
            }
        }
        try {
            if (authService != null && authService.isTokenValid()) {
                // if this is the case, we already have a valid login
                // for this server.  Set things up properly.
                loginComplete(authService.getUsername(), authService.getAuthenticationToken());
                // all set
                return false;
            }
        } catch (AuthenticationException ee) {
            // ignore -- we'll just retry the login
            logger.log(Level.WARNING, "Error checking exiting service", ee);
        }
        // if we get here, there is no valid auth service for this server
        // url
        return true;
    }

    public void authenticate(String username, Object... credentials) throws LoginFailureException {
        getSessionManager().fireConnecting(BUNDLE.getString("Sending authentication details..."));
        try {
            AuthenticationService as = AuthenticationManager.login(getAuthInfo(), username, credentials);
            setAuthService(as);
            loginComplete(username, as.getAuthenticationToken());
        } catch (AuthenticationException ae) {
            throw new LoginFailureException(ae);
        }
    }

    protected void loginComplete(String username, String token) {
        setUsername(username);
        LoginParameters lp = new LoginParameters(token, new char[0]);
        super.loginComplete(lp);
    }
    
}
