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
package org.jdesktop.wonderland.modules.xremwin.client;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.modules.appbase.client.ProcessReporter;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DDisplayer;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.modules.appbase.client.FirstVisibleInitializer;
import org.jdesktop.wonderland.modules.appbase.client.App2D;

/**
 * A Slave Xremwin app. This is the AppXrw subclass used on a client machine
 * other than that which is executing the app.
 *
 * @author deronj
 */
@InternalAPI
public class AppXrwSlave extends AppXrw {

    /**
     * Create a new instance of AppXrwSlave.
     *
     * @param appName The name of the app.
     * @param pixelScale The size of the window pixels.
     * @param connectionInfo Subclass-specific data for making a peer-to-peer connection between master 
     * and slave.
     * @param session This app's Wonderland session.
     * @param displayer The environment in which the app is going to be displayed.
     * @param fvi The first visible initializer.
     */
    public AppXrwSlave(String appName, Vector2f pixelScale, 
                       ProcessReporter reporter, AppXrwConnectionInfo connectionInfo, 
                       WonderlandSession session, View2DDisplayer displayer, 
                       FirstVisibleInitializer fvi) 
        throws InstantiationException
    {

        super(appName, new ControlArbXrw(), pixelScale);
        controlArb.setApp(this);
        
        // The displayer must be added early on. The client sync from the master requires this.
        addDisplayer(displayer);

        // Must be done before enabling client
        if (App2D.doAppInitialPlacement && fvi != null) {
            logger.info("Cell transferring fvi to app, fvi = " + fvi);
            setFirstVisibleInitializer(fvi);
        }

        // Create the Xremwin protocol client and start its interpreter loop running.
        client = null;
        try {
            client = new ClientXrwSlave(this, (ControlArbXrw) controlArb, session, connectionInfo, reporter);
        } catch (InstantiationException ex) {
            JOptionPane.showMessageDialog(null, "Cannot create Xremwin protocol client for " + appName, 
                                          "Error", JOptionPane.ERROR_MESSAGE);
            cleanup();
            throw new InstantiationException();
        }

        // Finally, enable the client
        client.enable();
    }

    /**
     * Clean up resources.
     */
    @Override
    public void cleanup() {
        super.cleanup();

        if (client != null) {
            client.cleanup();
            client = null;
        }
    }

    /**
     * {inheritDoc}
     */
    @Override
    int getTransientForWid(int wid) {
        // TODO: If the slave really needs to know this we need to
        // create a protocol for it to ask the master.
        return 0;
    }


    /** {@inheritDoc} */
    public boolean isMaster () {
        return false;
    }
}
