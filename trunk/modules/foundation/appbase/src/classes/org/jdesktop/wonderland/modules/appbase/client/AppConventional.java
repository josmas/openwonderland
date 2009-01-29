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
package org.jdesktop.wonderland.modules.appbase.client;

import java.util.HashMap;
import java.util.UUID;
import javax.swing.JOptionPane;
import com.jme.math.Vector2f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An abstract 2D conventional application.
 *
 * @author deronj
 */
@ExperimentalAPI
public abstract class AppConventional extends App2D {

    private static final Logger logger = Logger.getLogger(AppConventional.class.getName());
    /** A list of apps without cells that are awaiting attachment to their cells. This map is keyed on the app ID */
    protected static HashMap<UUID, App> disembodiedApps = new HashMap<UUID, App>();
    /** The name of the app */
    protected String appName;
    /** A lock object for server cell creation */
    protected Integer serverCellCreateLock = new Integer(0);
    /** Whether the server cell creation succeeded */
    private boolean createSuccess;
    /** True when the server has replied to the cell creation command */
    private boolean gotCreateReply;
    /** Should the first window made visible make the cell move to the best view position? (Master only) */
    private boolean initInBestView;
    /** The app conventional connection to the server */
    // TODO: notyet protected static AppConventionalConnection connection;
    /** The session of the Wonderland server with which the app is associated */
    protected static WonderlandSession session;

    /**
     * Create a new instance of AppConventional.
     *
     * @param appType The type of app to create.
     * @param appName The name of the app.
     * @param controlArb The control arbiter to use. null means that all users can control at the same time.
     * @param pixelScale The size of the window pixels in world coordinates.
     */
    public AppConventional(AppType appType, String appName, ControlArb controlArb, Vector2f pixelScale) {
        super(appType, controlArb, pixelScale);
        logger.severe("AppConventional: appType = " + appType);
        this.appName = appName;
    }

    /**
     * Returns the name of the app.
     */
    public String getName() {
        return appName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setCell(AppCell cell)
            throws IllegalArgumentException, IllegalStateException {
        super.setCell(cell);
        notify();
    }

    /**
     * This should be called by application-specific code prior to attempting to use the app cell. If the app cell 
     * has not yet been associated with this cell this method blocks until it is.
     */
    public synchronized void waitForCell() {
        while (cell == null) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * A utility method used to report launch errors to the user.
     */
    protected static void reportLaunchError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Specify whether, when the cell for this app is created, it should be moved to approximately the best view 
     * based on the viewer position at the time of client cell creation.
     *
     * @param initInBestView Whether the cell should be moved to approximately the best view on cell creation.
     */
    public void setInitInBestView(boolean initInBestView) {
        this.initInBestView = initInBestView;
    }

    /**
     * Returns the initInBestView property.
     */
    public boolean getInitInBestView() {
        return initInBestView;
    }
}
