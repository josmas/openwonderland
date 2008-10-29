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
package org.jdesktop.wonderland.client;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.modules.ModulePluginList;
import org.jdesktop.wonderland.client.modules.ModuleUtils;
import sun.misc.Service;


/**
 * Manage the connection between this client and the wonderland server
 * 
 * TODO RENAME, there must be a better name for this class !  LoginManager & JMELoginManager
 * 
 * @author paulby
 */
public abstract class LoginHelper {
    private static final Logger logger = Logger.getLogger(LoginHelper.class.getName());
    
    private WonderlandSession session;
    
    // the classloader to use with this manager
    private ClassLoader loader;
    
    public LoginHelper(String serverName, int serverPort, String userName) {
        
        WonderlandServerInfo server = new WonderlandServerInfo(serverName,
                                                  serverPort);
        
        LoginParameters loginParams = new LoginParameters(userName, 
                                                          "test".toCharArray());

        // TODO OpenSSO login, global authentication
        
        // setup a classloader with the module jars
        loader = setupClassLoader();
        
        session = createSession(server, loader);
        
        // load any client plugins from that class loader
        Iterator<ClientPlugin> it = Service.providers(ClientPlugin.class,
                                                      loader);
        while (it.hasNext()) {
            ClientPlugin plugin = it.next();
            try {
                plugin.initialize(session);
            } catch(Exception e) {
                logger.log(Level.WARNING, "Error initializing plugin "+plugin.getClass().getName(), e);
            } catch(Error e) {
                logger.log(Level.WARNING, "Error initializing plugin "+plugin.getClass().getName(), e);
                e.printStackTrace();
            }
        }
                          
        // Darkstar login
        try {
            session.login(loginParams);
        } catch (LoginFailureException ex) {
            logger.log(Level.SEVERE, "Login Failure", ex);
            return;
        }

        ClientContext.getWonderlandSessionManager().registerSession(session);
    }

    /**
     * Return the session for this login
     * @return the session
     */
    public WonderlandSession getSession() {
        return session;
    }

    public abstract WonderlandSession createSession(WonderlandServerInfo serverInfo, ClassLoader loader);

    // TODO this should probably be a utility, it's currently duplicated in CellBoundsViewer
    private ClassLoader setupClassLoader() {
        ModulePluginList list = ModuleUtils.fetchPluginJars();
        List<URL> urls = new ArrayList<URL>();
        if (list==null) {
            logger.warning("Unable to configure classlaoder, falling back to system classloader");
            return getClass().getClassLoader();
        }
        
        for (String uri : list.getJarURIs()) {
            try {
                urls.add(new URL(uri));
            } catch (Exception excp) {
                excp.printStackTrace();
           }
        }
        
        return new URLClassLoader(urls.toArray(new URL[0]),
                                  getClass().getClassLoader());
    }


}
