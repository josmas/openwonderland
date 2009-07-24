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
package org.jdesktop.wonderland.modules.securitysession.auth.weblib;

import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import org.apache.catalina.util.Base64;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.UserPlugin.PasswordResult;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.UserPluginConfigList.UserPluginConfig;
import org.jdesktop.wonderland.modules.securitysession.weblib.SessionLoginException;
import org.jdesktop.wonderland.modules.securitysession.weblib.SessionManager;
import org.jdesktop.wonderland.modules.securitysession.weblib.UserRecord;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;

/**
 * Implementation of a session manager that performs authentication against
 * a set of registered user plugins. The session manager implementation
 * delegates all calls to a singleton that implements the actual checking.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class AuthSessionManagerImpl implements SessionManager {
    private static final Logger logger =
            Logger.getLogger(AuthSessionManagerImpl.class.getName());

    private static final String USER_PLUGIN_CONFIG_PROP =
            AuthSessionManagerImpl.class.getSimpleName() + ".UserPluginConfig";

    private static final String LOGIN_PAGE =
            "/security-session-auth/security-session-auth/login";

    /** singleton instance of session manager implementation */
    private static final AuthSessionManagerSingleton SINGLETON =
            new AuthSessionManagerSingleton();

    public void initialize(Map opts) {
        // ignore
    }

    public UserRecord login(String userId, Object... credentials)
        throws SessionLoginException
    {
        // make sure we got what we expect
        if (credentials.length != 1) {
            throw new SessionLoginException("Username and password required.");
        }

        // decode the credentials
        String password = ((String) credentials[0]).trim();

        // now ask the singleton for the information
        return SINGLETON.login(userId, password);
    }

    public UserRecord get(String userId) {
        return SINGLETON.get(userId);
    }

    public UserRecord getByToken(String token) {
        return SINGLETON.getByToken(token);
    }

    public String getUserId(String token) {
        String out = null;

        UserRecord record = getByToken(token);
        logger.fine("[AuthSessionManagerImpl] record for " + token + " is " +
                    record);

        if (record != null) {
            out = record.getUserId();
        }
        return out;
    }

    public UserRecord logout(String token) {
        return SINGLETON.logout(token);
    }

    public String handleUnauthenticated(HttpServletRequest request,
                                        boolean mandatory,
                                        HttpServletResponse response)
        throws IOException
    {
        // if security is mandatory, then redirect unauthenticated users to the
        // login page
        if (mandatory) {
            String loginPage = LOGIN_PAGE + "?forwardPage=" +
                    URLEncoder.encode(request.getRequestURI(), "UTF-8");
            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            response.sendRedirect(loginPage);
        }

        // no mapping for unauthenticated user
        return null;
    }

    private static class AuthSessionManagerSingleton {
        /** an ordered list of user plugins to search while authenticating */
        private final List<UserPlugin> userPlugins;

        private final Map<String, UserRecord> byUserId =
                new LinkedHashMap<String, UserRecord>();
        private final Map<String, UserRecord> byToken =
                new LinkedHashMap<String, UserRecord>();

        public AuthSessionManagerSingleton() {
            // load the list of plugins
            try {
                userPlugins = loadPlugins();
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            } catch (JAXBException je) {
                throw new IllegalStateException(je);
            }
        }

        /**
         * Load plugin configuration
         * @return the list of plugins
         */
        private List<UserPlugin> loadPlugins() throws IOException, JAXBException {
            List<UserPlugin> out = new ArrayList<UserPlugin>();

            // see if there is a plugin config file
            String pluginConfigFile = SystemPropertyUtil.getProperty(USER_PLUGIN_CONFIG_PROP);
            if (pluginConfigFile != null) {
                // load plugins from the file
                UserPluginConfigList upcl =
                    UserPluginConfigList.decode(new FileReader(pluginConfigFile));
                for (UserPluginConfig config : upcl.getUserPluginConfigs()) {
                    out.add(loadPlugin(config));
                }
            } else {
                // load the default plugin
                UserPlugin dbPlugin = new DBUserPluginImpl();
                dbPlugin.configure(new Properties());
                out.add(dbPlugin);
            }

            return out;
        }

        /**
         * Load a plugin given its configuration
         * @param config the plugin to load
         * @return the loaded plugin
         */
        private UserPlugin loadPlugin(UserPluginConfig config) {
            try {
                Class<UserPlugin> upc = (Class<UserPlugin>)
                    Class.forName(config.getClassName());
                UserPlugin out = upc.newInstance();
                out.configure(config.getProperties());
                return out;
            } catch (InstantiationException ex) {
                throw new IllegalStateException(ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public synchronized UserRecord login(String userId, String password) {
            // go through each plugin until we find one which answers when we
            // ask for this user id
            UserPlugin plugin = null;
            PasswordResult result = PasswordResult.UNKNOWN_USER;
            Iterator<UserPlugin> pi = userPlugins.iterator();

            while (result == PasswordResult.UNKNOWN_USER && pi.hasNext()) {
                plugin = pi.next();
                result = plugin.passwordMatches(userId, password.toCharArray());
            }

            if (result != PasswordResult.MATCH) {
                // bad password or unknown user
                return null;
            }

            // At this point, we know the user's password is correct.
            // If they have an existing login, just return that token.
            // Otherwise, create a new user record by looking the user
            // up in the database.
            UserRecord rec = byUserId.get(userId);
            if (rec == null) {
                rec = plugin.getUserRecord(userId, new TokenGenerator() {
                    public String generateToken(String userId) {
                        return newToken(userId);
                    }
                });

                // add to our internal maps
                byUserId.put(userId, rec);
                byToken.put(rec.getToken(), rec);
            }

            logger.fine("[AuthSessionManagerImpl] Login returns token " +
                        rec.getToken() + " for user " + userId);

            return rec;
        }

        public synchronized UserRecord get(String userId) {
            // get the existing value for this user
            return byUserId.get(userId);
        }

        public synchronized UserRecord getByToken(String token) {
            return byToken.get(token);
        }

        public synchronized UserRecord logout(String token) {
            UserRecord rec = byToken.remove(token);
            if (rec != null) {
                byUserId.remove(rec.getUserId());
            }

            return rec;
        }
    }

    /**
     * An interface to a token generator provided by this manager.  This
     * generator is used by UserPlugins to create new records.
     */
    public interface TokenGenerator {
        /**
         * Generate a new token for the given user id.  Tokens will be
         * generated in a random fashion, so multiple calls to this method
         * will return multiple tokens.
         * @param userId the user to generate a token for
         * @return a token for the given user
         */
        public String generateToken(String userId);
    }

    /**
     * Create a token for the given user id.  This will produce a unique
     * token for this user.  Tokens are non-deterministic, so calling this
     * method multiple times for the same token should result in different
     * tokens every time.
     * @param userId the user id to create a token for
     * @return a token for the user
     */
    private static String newToken(String userId) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException("Unable to find SHA", nsae);
        }

        md.update(userId.getBytes());

        // add some random data to the message to make it unique
        SecureRandom sr = new SecureRandom();
        byte[] buffer = new byte[128];
        sr.nextBytes(buffer);
        md.update(buffer);

        byte[] res = md.digest();
        return new String(Base64.encode(res));
    }
}
