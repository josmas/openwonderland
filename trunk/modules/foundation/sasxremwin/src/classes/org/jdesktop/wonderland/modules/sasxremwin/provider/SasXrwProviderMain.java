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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.sasxremwin.provider;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.ProcessReporterFactory;
import org.jdesktop.wonderland.modules.sas.provider.SasProvider;
import org.jdesktop.wonderland.modules.sas.provider.SasProviderConnection;
import org.jdesktop.wonderland.modules.sas.provider.SasProviderConnectionListener;
import org.jdesktop.wonderland.modules.sas.provider.SasProviderSession;
import org.jdesktop.wonderland.modules.xremwin.client.AppXrwMaster;
import org.jdesktop.wonderland.common.messages.MessageID;
import java.util.logging.Logger;
import java.util.HashMap;

/**
 * The main logic for the SAS Xremwin provider client.
 *
 * @author deronj
 */
@ExperimentalAPI
public class SasXrwProviderMain 
    implements SasProviderConnectionListener, AppXrwMaster.ExitListener {

    static final Logger logger = Logger.getLogger(SasXrwProviderMain.class.getName());

    /** The session associated with this provider. */
    private SasProviderSession session;

    /** An entry which holds information needed to notify the SasServer of app exit. */
    private class AppInfo {
        private SasProviderConnection connection;
        private MessageID launchMessageID;
        private AppInfo (SasProviderConnection connection, MessageID launchMessageID) {
            this.connection = connection;
            this.launchMessageID = launchMessageID;
        }
    }

    /** Information about the apps which are running in this provider. */
    private HashMap<AppXrwMaster,AppInfo> runningAppInfos = new HashMap<AppXrwMaster,AppInfo>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SasXrwProviderMain providerMain = new SasXrwProviderMain();
    }

    private SasXrwProviderMain () {

        checkPlatform();

        System.err.println("****** Enter SasXrwProviderMain");

        // TODO: parse args

        String userName = "sasxprovider";
        String fullName = "SAS Provider for Xremwin";
        String password = "foo";

        
        String serverUrl = System.getProperty("wonderland.web.server.url", "http://localhost:8080");
        System.err.println("Connecting to server " + serverUrl);

        try {
            SasProvider provider = new SasProvider(userName, fullName, password, serverUrl, this);
        } catch (Exception ex) {
            System.err.println("Exception " + ex);
            ex.printStackTrace();
            System.err.println("Cannot connect to server " + serverUrl);
            System.exit(1);
        }        
    }

    /**
     * SAS can only run on a Unix platform. Exit if this is not the case.
     */
    private void checkPlatform() {
        String osName = System.getProperty("os.name");
        if (!"Linux".equals(osName) &&
            !"SunOS".equals(osName)) {
            System.err.println("SasXrwProviderMain cannot run on platform " + osName);
            System.err.println("Program terminated.");
            System.exit(1);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setSession (SasProviderSession session) {
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    public String launch (String appName, String command, Vector2f pixelScale, 
                          SasProviderConnection connection, MessageID launchMessageID) {
        AppXrwMaster app = null;
        try {
            app = new AppXrwMaster(appName, command, pixelScale, 
                                   ProcessReporterFactory.getFactory().create(appName), session, true);
        } catch (InstantiationException ex) {
            return null;
        }

        app.setExitListener(this);

        synchronized (runningAppInfos) {
            runningAppInfos.put(app, new AppInfo(connection, launchMessageID));
        }

        // Now it is time to enable the master client loop
        app.getClient().enable();

        return app.getConnectionInfo().toString();
    }


    /**
     * Called when an app exits.
     */
    public void appExitted (AppXrwMaster app) {
        logger.warning("App Exitted: " + app.getName());
        synchronized (runningAppInfos) {
            AppInfo appInfo = runningAppInfos.get(app);
            if (appInfo != null) {
                runningAppInfos.remove(app);
                appInfo.connection.appExitted(appInfo.launchMessageID, app.getExitValue());
            }
        }
    }
}

