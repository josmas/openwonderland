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
package org.jdesktop.wonderland.modules.xremwin.client;

import com.jme.math.Vector2f;
import java.io.Serializable;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.AppTypeConventional;
import org.jdesktop.wonderland.modules.appbase.client.ProcessReporter;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbAlways;

/**
 * A Slave Xremwin app. This is the AppXrw subclass used on a client machine
 * other than that which is executing the app.
 *
 * @author deronj
 */
@ExperimentalAPI
class AppXrwSlave extends AppXrw {

    /**
     * Create a new instance of AppXrwSlave.
     *
     * @param appType The type of app to create.
     * @param appName The name of the app.
     * @param pixelScale The size of the window pixels.
     * @param connectionInfo Subclass-specific data for making a peer-to-peer connection between master and slave.
     * @param session This app's Wonderland session.
     */
    AppXrwSlave(AppTypeConventional appType, String appName, Vector2f pixelScale, ProcessReporter reporter,
            Serializable connectionInfo, WonderlandSession session) {

        // TODO: temporary
        //super(appType, appName, new ControlArbXrw(), pixelScale);
        super(appType, appName, new ControlArbAlways(), pixelScale);
        controlArb.setApp(this);

        // Create the Xremwin protocol client and start its interpreter loop running.
        client = null;
        try {
            client = new ClientXrwSlave(this, (ControlArbXrw) controlArb, session,
                    (AppXrwConnectionInfo) connectionInfo, reporter);
        } catch (InstantiationException ex) {
            ex.printStackTrace();
            reportLaunchError("Cannot create Xremwin protocol client for " + appName);
            cleanup();
        }
    }

    /**
     * {inheritDoc}
     */
    int getTransientForWid(int wid) {
        // TODO: If the slave really needs to know this we need to
        // create a protocol for it to ask the master.
        return 0;
    }
}
