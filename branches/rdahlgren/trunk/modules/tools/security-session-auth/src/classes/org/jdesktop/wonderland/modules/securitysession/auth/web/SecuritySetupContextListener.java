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
package org.jdesktop.wonderland.modules.securitysession.auth.web;

import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdesktop.wonderland.common.login.AuthenticationInfo;
import org.jdesktop.wonderland.front.admin.AdminRegistration;
import org.jdesktop.wonderland.front.admin.AdminRegistration.RegistrationFilter;
import org.jdesktop.wonderland.front.admin.ServerInfo;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.db.UserDAO;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.db.UserEntity;
import org.jdesktop.wonderland.utils.Constants;

/**
 *
 * @author jkaplan
 */
public class SecuritySetupContextListener implements ServletContextListener {
    private static final Logger logger =
            Logger.getLogger(SecuritySetupContextListener.class.getName());

    private static final String SECURITY_PATH =
            "security-session-auth/security-session-auth/identity";

    private static final String DEFAULT_USERS_PROP =
            SecuritySetupContextListener.class.getName() + ".disableCreateDefaultUsers";

    /** the group database persistence unit (injected automatically) */
    @PersistenceUnit(unitName="WonderlandUserPU")
    private EntityManagerFactory emf;

    private AdminRegistration ar;
    private AdminRegistration logout;

    public void contextInitialized(ServletContextEvent evt) {
        // get the URL for the web server
        String serverUrl = System.getProperty(Constants.WEBSERVER_URL_PROP);
        serverUrl += SECURITY_PATH;
        
        // set the login type and URL
        AuthenticationInfo authInfo = new AuthenticationInfo(
                                       AuthenticationInfo.Type.WEB_SERVICE, serverUrl);
        
        logger.fine("Setting auth URL: " + serverUrl);

        ServerInfo.getServerDetails().setAuthInfo(authInfo);

        // create initial users
        if (!Boolean.parseBoolean(DEFAULT_USERS_PROP)) {
            createInitialUsers();
        }

        // register with the admininstration page
        ServletContext sc = evt.getServletContext();
        ar = new AdminRegistration("User Manager",
                                   "/security-session-auth/security-session-auth/users");
        ar.setFilter(AdminRegistration.LOGGED_IN_FILTER);
        AdminRegistration.register(ar, sc);

        logout = new AdminRegistration("Logout", "/security-session-auth/security-session-auth/login?action=logout");
        logout.setFilter(AdminRegistration.LOGGED_IN_FILTER);
        logout.setPosition(Integer.MAX_VALUE);
        AdminRegistration.register(logout, sc);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // register with the admininstration page
        ServletContext sc = sce.getServletContext();
        AdminRegistration.unregister(ar, sc);
        AdminRegistration.unregister(logout, sc);
    }

    private void createInitialUsers() {
        UserDAO users = new UserDAO(emf);
        if (users.getUserCount() == 0) {
            UserEntity admin = new UserEntity();
            admin.setId("admin");
            admin.setFullname("System Administrator");
            admin.setPassword("admin");
            users.updateUser(admin);

            UserEntity darkstar = new UserEntity();
            darkstar.setId("darkstar");
            darkstar.setFullname("Darkstar Server");
            darkstar.setPassword("darkstar");
            users.updateUser(darkstar);

            UserEntity webserver = new UserEntity();
            webserver.setId("webserver");
            webserver.setFullname("Web Server");
            webserver.setPassword("webserver");
            users.updateUser(webserver);

            logger.warning("Created initial users " + admin.getId() + " " +
                           darkstar.getId() + " " + webserver.getId() + ". " +
                           "Be sure to change default passwords.");
        }
    }
}