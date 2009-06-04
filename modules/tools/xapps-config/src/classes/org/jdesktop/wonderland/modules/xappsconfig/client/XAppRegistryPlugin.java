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
package org.jdesktop.wonderland.modules.xappsconfig.client;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.xappsconfig.client.XAppsClientConfigConnection.XAppsConfigListener;
import org.jdesktop.wonderland.modules.xappsconfig.common.XAppRegistryItem;

/**
 * Client-size plugin for registering items in the Cell Registry that come from
 * the configured list of X Apps.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class XAppRegistryPlugin extends BaseClientPlugin {

    private static Logger logger = Logger.getLogger(XAppRegistryPlugin.class.getName());
    private XAppsClientConfigConnection xappsConfigConnection = null;
    private X11AppConfigListener listener = null;

    /**
     * @inheritDoc()
     */
    @Override
    protected void activate() {
        CellRegistry registry = CellRegistry.getCellRegistry();

        // Fetch the list of X Apps registered for the user and register them
        // with the Cell Registry
//        List<XAppRegistryItem> userItems =
//                XAppRegistryItemUtils.getUserXAppRegistryItemList();
//        for (XAppRegistryItem item : userItems) {
//            String appName = item.getAppName() + " (User)";
//            String command = item.getCommand();
//            XAppCellFactory factory = new XAppCellFactory(appName, command);
//            registry.registerCellFactory(factory);
//        }

        // Fetch the list of X Apps registered for the system and register them
        // with the Cell Registry
        List<XAppRegistryItem> systemItems =
                XAppRegistryItemUtils.getSystemXAppRegistryItemList();
        for (XAppRegistryItem item : systemItems) {
            String appName = item.getAppName();
            String command = item.getCommand();
            XAppCellFactory factory = new XAppCellFactory(appName, command);
            registry.registerCellFactory(factory);
        }

        // Create a new connection to receive messages about changes in the
        // X11 Apps configured.
        ServerSessionManager session = getSessionManager();
        session.addLifecycleListener(new SessionLifecycleListener() {
            public void sessionCreated(WonderlandSession session) {
                // Do nothing
            }

            public void primarySession(WonderlandSession session) {
                try {
                    xappsConfigConnection = new XAppsClientConfigConnection();
                    xappsConfigConnection.connect(session);
                    listener = new X11AppConfigListener();
                    xappsConfigConnection.addX11AppConfigListener(listener);

                } catch (ConnectionFailureException excp) {
                    logger.log(Level.WARNING, "Unable to connect to X11" +
                            " Apps Config connection", excp);
                }
            }
        });
    }

    /**
     * @inheritDoc()
     */
    @Override
    protected void deactivate() {
        // Remove the listen for changes to the X11 app configuration
        if (xappsConfigConnection != null) {
            xappsConfigConnection.removeX11AppConfigListener(listener);
            listener = null;
            xappsConfigConnection = null;
        }
    }

    /**
     * Listens for when X11 Apps are added or removed.
     */
    private class X11AppConfigListener implements XAppsConfigListener {

        public void xappAdded(String appName, String command) {
            CellRegistry registry = CellRegistry.getCellRegistry();
            XAppCellFactory factory = new XAppCellFactory(appName, command);
            registry.registerCellFactory(factory);
        }

        public void xappRemoved(String appName) {
            // Remove the X11 App from the Cell registry. We only need the app
            // name to create a suitable XAppCellFactory to remove.
            CellRegistry registry = CellRegistry.getCellRegistry();
            XAppCellFactory factory = new XAppCellFactory(appName, null);
            registry.unregisterCellFactory(factory);
        }
    }
}
