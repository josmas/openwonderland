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

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;
import com.jme.bounding.BoundingVolume;
import javax.swing.JOptionPane;
import com.jme.math.Vector2f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalMessage;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalCellCreateMessage;
import org.jdesktop.wonderland.modules.appbase.client.utils.net.NetworkAddress;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
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
    protected static HashMap<UUID,App> disembodiedApps = new HashMap<UUID,App>();

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
    protected static AppConventionalConnection connection;

    /** The session of the Wonderland server with which the app is associated */
    protected static WonderlandSession session;

    /**
     * Initialize a connection to use to create conventional app cells.
     * @throws ConnectionFailureException if could not create the connection.
     */
    static public void initializeConnection () throws ConnectionFailureException {
	if (connection != null) {
	    connection = new AppConventionalConnection();

        // JK: updated to use new interfaces.
        // XXX This may break in the multiple-server case XXX
        ServerSessionManager primary = LoginManager.getPrimary();
        session = primary.getPrimarySession();

        //WonderlandServerInfo serverInfo = ClientContext.getWonderlandSessionManager().getPrimaryServer();
	    //session = ClientContext.getWonderlandSessionManager().getSession(serverInfo);
	    connection.connect(session);
	}
    }

    /**
     * Create a new instance of AppConventional.
     *
     * @param appType The type of app to create.
     * @param appName The name of the app.
     * @param controlArb The control arbiter to use. null means that all users can control at the same time.
     * @param pixelScale The size of the window pixels in world coordinates.
     */
    public AppConventional (AppType appType, String appName, ControlArb controlArb, Vector2f pixelScale) {
	super(appType, controlArb, pixelScale);
	logger.severe("AppConventional: appType = " + appType);
	this.appName = appName;
    }

    /**
     * Returns the name of the app.
     */
    public String getName () {
	return appName;
    }

    /**
     * Used by the master client to create a new app cell and execute the app with a default process reporter.
     *
     * @param appType The type of the app.
     * @param appName The name of the app instance.
     * @param command The platform command used to execute the app.
     * @param bestView Force this cell to be initialized in approximately the best view based on the viewer 
     * position at the time of client cell creation.
     * @param bounds The bounds of the new app cell.
     * @param transform The cell transform.
     * @param pixelScale The size of the window pixels in world coordinates.
     */
    public static void userLaunchApp (AppTypeConventional appType, String appName, String command, boolean bestView, 
				      BoundingVolume bounds, CellTransform transform, Vector2f pixelScale) {
	userLaunchApp(appType, appName, command, bestView, bounds, transform, pixelScale, null);
    }

    /**
     * Used by the master client to create a new app cell and execute the app.
     *
     * @param appType The type of the app.
     * @param appName The name of the app instance.
     * @param command The platform command used to execute the app.
     * @param bestView Force this cell to be initialized in approximately the best view based on the viewer 
     * position at the time of client cell creation.
     * @param bounds The bounds of the new app cell.
     * @param transform The cell transform.
     * @param pixelScale The size of the window pixels in world coordinates.
     * @param reporter The reporter with which to report to the user. If null, a default reporter is used.
     */
    public static void userLaunchApp (AppTypeConventional appType, String appName, String command, boolean bestView, 
				      BoundingVolume bounds, CellTransform transform, Vector2f pixelScale,
				      ProcessReporter reporter) {
	
	// Next, make sure we can start up the app. 
	AppTypeConventional.ExecuteMasterProgramReturn empr = appType.executeMasterProgram(appName, command, 
											   pixelScale, reporter);
	if (empr == null) {
	    return;
	}

	// The app client cannot run until it is assigned to the cell. Notify the
	// system that the app is awaiting its cell.
	addDisembodiedApp(empr.appId, empr.app);

	// Create server cell
	if (!empr.app.createServerCell(appType.getName(), appName, empr.appId, bestView, bounds, transform, pixelScale,
				       empr.connectionInfo)) {
	    // Cell creation failed
	    removeDisembodiedApp(empr.appId);
	    reportLaunchError("Cannot create server cell for app: " + empr.app.getName());
	    empr.app.cleanup();
	}
    }

    /**
     * Request the Wonderland server to create a server-side cell for this app.
     * Subclasses should use this to create the app cell. If the creation
     * succeeds some undetermined about of time later the corresponding client 
     * cell will be loaded and attached to this app.
     *
     * @param appTypeName The name of the app type.
     * @param appName The name of the app instance.
     * @param appId The app's unique ID (note: the ID is unique only within the master client session).
     * @param bestView Force this cell to be initialized in approximately the best view based on the viewer 
     * position at the time of client cell creation.
     * @param bounds The bounds of the new app cell.
     * @param transform The cell transform.
     * @param pixelScale The size of the window pixels in world coordinates.
     * @param connectionInfo Subclass-specific data for making a peer-to-peer connection between master and slave.
     */
    protected boolean createServerCell (String appTypeName, String appName, UUID appId, boolean bestView, 
					BoundingVolume bounds, CellTransform transform, Vector2f pixelScale,
					Serializable connectionInfo) {

	String masterHost = NetworkAddress.getDefaultHostAddress();
	
        AppConventionalCellCreateMessage msg = AppConventionalMessage.newCellCreateMessage(
            appTypeName, masterHost, appName, appId, bestView, bounds, transform, pixelScale, connectionInfo);

	CellID cellID = createCellWithMessage(msg);
	if (cellID == null) {
	    logger.warning("Cannot create conventional app cell.");
	    return false;
	}
	logger.warning("Created conventional app cell, cellID = " + cellID);
	
	return true;
    }

    /**
     * Create the cell by sending the given message.
     * @param msg The creation message.
     */
    protected CellID createCellWithMessage (AppConventionalMessage msg) {
	ResponseMessage response = connection.sendAndWait(msg);
	if (response instanceof ErrorMessage) {
	    logger.warning("Cannot create conventional app cell. Error = " + response);
	    return null;
	}

	// TODO:
	//AppConventionalResponseMessage resp = (AppConventionalResponseMessage) response;
	//return resp.getCellID();
	return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setCell (AppCell cell) 
	throws IllegalArgumentException, IllegalStateException 
    {
	super.setCell(cell);
	notify();
    }

    /**
     * This should be called by application-specific code prior to attempting to use the app cell. If the app cell 
     * has not yet been associated with this cell this method blocks until it is.
     */
    public synchronized void waitForCell () {
	while (cell == null) {
	    try { wait(); } catch (InterruptedException ex) {}
	}
    }

    /**
     * A utility method used to report launch errors to the user.
     */
    protected static void reportLaunchError (String message) {
	JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Specify whether, when the cell for this app is created, it should be moved to approximately the best view 
     * based on the viewer position at the time of client cell creation.
     *
     * @param initInBestView Whether the cell should be moved to approximately the best view on cell creation.
     */
    public void setInitInBestView (boolean initInBestView) {
	this.initInBestView = initInBestView;
    }

    /**
     * Returns the initInBestView property.
     */
    public boolean getInitInBestView () {
	return initInBestView;
    }

    /**
     * Add the given app to the list of apps awaiting cell assignment.
     *
     * @param appId The unique ID of the app.
     * @param app The app.
     */
    protected static void addDisembodiedApp (UUID appId, App app) {
	// Note: the old (0.3) method of keying this map on the cellID had 
	// a race--there was potential for the client cell to be created 
	// before the server returned the cellID and the cell-app 
	// connection would be missed. Keying on a quantity that 
	// is already known before the server cell is created (viz. appId) 
	// prevents this. 
	disembodiedApps.put(appId, app);
    }

    /**
     * Remove the given app from the list of apps awaiting cell assignment.
     * This is to be called after the app has been attached to its cell
     * or the cell creation has failed.
     *
     * @param appId The unique ID of the app.
     */
    protected static void removeDisembodiedApp (UUID appId) {
	disembodiedApps.remove(appId);
    }

    /**
     * Find the given app which is awaiting its cell assignment.
     *
     * @param appId The unique ID of the app.
     * @return null if no such app is waiting.
     */
    public static App findDisembodiedApp (UUID appId) {
	return disembodiedApps.get(appId);
    }
}
