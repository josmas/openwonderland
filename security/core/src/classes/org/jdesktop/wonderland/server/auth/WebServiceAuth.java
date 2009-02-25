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
package org.jdesktop.wonderland.server.auth;

import com.sun.sgs.auth.Identity;
import com.sun.sgs.auth.IdentityAuthenticator;
import com.sun.sgs.auth.IdentityCredentials;
import com.sun.sgs.impl.auth.NamePasswordCredentials;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.login.AuthenticationException;
import org.jdesktop.wonderland.common.login.AuthenticationInfo;
import org.jdesktop.wonderland.common.login.AuthenticationManager;
import org.jdesktop.wonderland.common.login.AuthenticationService;

/**
 * An authenticator that reads all data from what the user passes in in
 * the username field.  No attempt is made to authenticate the user,
 * but the user's name is filled in based on the passed in data.
 * @author jkaplan
 */
public class WebServiceAuth implements IdentityAuthenticator {
    // logger
    private static final Logger logger =
            Logger.getLogger(WebServiceAuth.class.getName());

    // properties we read
    private static final String AUTH_URL_PROP = "wonderland.authentication.url";
    private static final String SERVER_URL_PROP = "wonderland.web.server.url";
    private static final String AUTH_SERVICE_PATH =
            "security-session-noauth/security-session-noauth/identity";

    private static final String DARKSTAR_USERNAME = "sgs.auth.username";
    private static final String DARKSTAR_USERNAME_DEFAULT = "darkstar";
    private static final String DARKSTAR_PASSWORD_FILE = "sgs.password.file";


    // authentication service
    private AuthenticationService auth;
    
    public WebServiceAuth(Properties prop) {
        logger.info("Loading WebServie authenticator");

        // get the authentication URL
        String authUrl = System.getProperty(AUTH_URL_PROP);
        if (authUrl == null) {
            // fall back to the default, based on the web server URL
            authUrl = System.getProperty(SERVER_URL_PROP);
            authUrl += AUTH_SERVICE_PATH;
        }

        // the login username
        String username = System.getProperty(DARKSTAR_USERNAME,
                                             DARKSTAR_USERNAME_DEFAULT);

        // get the login password file.  If this is null, we will do
        // a login with no authentication
        String passwordFile = System.getProperty(DARKSTAR_PASSWORD_FILE);

        try {
            if (passwordFile == null) {
                auth = noAuthLogin(authUrl, username);
            } else {
                auth = authLogin(authUrl, username, passwordFile);
            }
        } catch (AuthenticationException ae) {
            throw new IllegalStateException("Error authenticating Darkstar " +
                                            "server", ae);
        }
    }

    public String[] getSupportedCredentialTypes() {
        return new String[] { "NameAndPasswordCredentials" };
    }
    
    public Identity authenticateIdentity(IdentityCredentials credentials) 
        throws LoginException 
    {
        logger.warning("Auth: " + credentials);

        if (!(credentials instanceof NamePasswordCredentials)) {
            throw new CredentialException("Wrong credential class: " +
                    credentials.getClass().getName());
        }
        
        NamePasswordCredentials npc = (NamePasswordCredentials) credentials;
        
        // make sure the name is specified
        if (npc.getName() == null) {
            logger.warning("No name specified");
            throw new CredentialException("Invalid username");
        }

        logger.warning("Auth token: " + npc.getName());

        // unpack the components of the id
        try {
            String username = null;
            String fullname = null;
            String email = null;

            Attributes attrs = auth.getAttributes(npc.getName(), "uid", "cn", "mail");

            Attribute usernameAttr = attrs.get("uid");
            if (usernameAttr != null) {
                username = (String) usernameAttr.get();
            }

            Attribute fullnameAttr = attrs.get("cn");
            if (fullnameAttr != null) {
                fullname = (String) fullnameAttr.get();
            }

            Attribute emailAttr = attrs.get("mail");
            if (emailAttr != null) {
                email = (String) emailAttr.get();
            }
        
            logger.warning("un: " + username + " fn: " + fullname + " m: " + email);

            // make sure at least a username was specified
            if (username == null || username.trim().length() == 0) {
                logger.warning("Unable to find username");
                throw new CredentialException("No username specified");
            }

            // construct a new WonderlandServerIdentity to return
            return new WonderlandServerIdentity
                    (new WonderlandIdentity(username, fullname, email));
        } catch (AuthenticationException ae) {
            logger.warning("Authentication error: " + ae);
            
            CredentialException ce = new CredentialException("Error authenticating token");
            ce.initCause(ae);
            throw ce;
        } catch (NamingException ne) {
            logger.warning("Authentication error: " + ne);
            
            CredentialException ce = new CredentialException("Error reading attribute");
            ce.initCause(ne);
            throw ce;
        }
    }

    /**
     * Log in with no credentials
     * @param username the username
     */
    protected AuthenticationService noAuthLogin(String authUrl, String username)
        throws AuthenticationException
    {
        AuthenticationInfo info = new AuthenticationInfo(
                AuthenticationInfo.Type.NONE, authUrl);
        return AuthenticationManager.login(info, username, "Darkstar server");
    }

    /**
     * Log in with the given username and password
     * @param username the username to log in with
     * @param password the password to log in with
     */
    protected AuthenticationService authLogin(String authUrl, String username,
                                              String passwordFile)
            throws AuthenticationException
    {
        String password;

        // read the password from the password file
        try {
            File f = new File(passwordFile);
            BufferedReader br = new BufferedReader(new FileReader(f));
            password = br.readLine();
        } catch (IOException ioe) {
            throw new AuthenticationException("Error reading password file " +
                                              passwordFile, ioe);
        }

        AuthenticationInfo info = new AuthenticationInfo(
                AuthenticationInfo.Type.WEB_SERVICE, authUrl);
        return AuthenticationManager.login(info, username, password);
    }
}
