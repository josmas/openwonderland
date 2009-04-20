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
package org.jdesktop.wonderland.modules.webdav.client;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.login.AuthenticationException;
import org.jdesktop.wonderland.modules.webdav.common.WebdavContentCollection;
import org.apache.commons.httpclient.HttpURL;
import org.apache.webdav.lib.WebdavResource;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.login.AuthenticationManager;
import org.jdesktop.wonderland.common.login.AuthenticationService;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.webdav.common.FileContentCollection;
import org.jdesktop.wonderland.modules.webdav.common.AuthenticatedWebdavResource;


/**
 * Register the WebdavContentRepository as the content repository for this
 * session
 * @author jkaplan
 */
public class WebdavClientPlugin implements ClientPlugin {
    private static final Logger logger =
            Logger.getLogger(WebdavClientPlugin.class.getName());

    public void initialize(ServerSessionManager loginInfo) {
        String baseURL = loginInfo.getServerURL() + "/webdav/content";
        
        // get the authentication service for this session
        AuthenticationService as = 
                AuthenticationManager.get(loginInfo.getCredentialManager().getAuthenticationURL());

        // register the webdav repository for this session
        try {
            String authCookieName = as.getCookieName();
            String authCookieValue = as.getAuthenticationToken();

            AuthenticatedWebdavResource wdr =
                    new AuthenticatedWebdavResource(new HttpURL(baseURL),
                                                    authCookieName,
                                                    authCookieValue);
            WebdavContentCollection root =
                    new WebdavContentCollection(wdr, null)
            {
                // don't include the root node in the path
                @Override
                public String getPath() {
                    return "";
                }
            };
            WebdavContentRepository repo =
                    new WebdavContentRepository(root, loginInfo.getUsername());

            ContentRepositoryRegistry.getInstance().registerRepository(loginInfo, repo);
        } catch (AuthenticationException ae) {
            logger.log(Level.WARNING, "Unable to get auth cookie name", ae);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Unable to start content repository", ioe);
        }

        // register the local repository
        String dirName = "localRepo-" + loginInfo.getUsername();
        final File userDir = ClientContext.getUserDirectory(dirName);
        FileContentCollection localRepo = new FileContentCollection(userDir, null) {
            @Override
            protected URL getBaseURL() {
                try {
                    System.err.println("BASE URL "+userDir.toURI().toURL().toExternalForm());
                    return userDir.toURI().toURL();
                } catch (MalformedURLException ex) {
                    logger.log(Level.WARNING, "Unable to create local repository", ex);
                    return null;
                }
            }

            @Override
            public String getPath() {
                return "";
            }

        };
        ContentRepositoryRegistry.getInstance().registerLocalRepository(localRepo);
    }
}
