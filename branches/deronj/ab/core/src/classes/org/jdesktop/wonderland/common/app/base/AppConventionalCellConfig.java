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
package org.jdesktop.wonderland.common.app.base;

import java.util.UUID;
import com.jme.math.Vector2f;
import java.io.Serializable;
import org.jdesktop.wonderland.common.app.base.App2DCellConfig;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Container for 2D conventional app cell data. The server gives this to the client
 * in order to initialize the client cell.
 *
 * @author deronj
 */

@ExperimentalAPI
public class AppConventionalCellConfig extends App2DCellConfig {

    /** Whether the app has been launched by the world or user */
    private boolean userLaunched;

    /** The host on which to run the app master */
    private String masterHost;

    /** The name of the app */
    private String appName;

    /** 
     * The unique ID of the app. For user-launched apps this is assigned
     * by the master and therefore is only unique within the master client.
     * For world-launched apps this is assigned by the server and is
     * therefore unique within the entire system.
     */
    private UUID appId;

    /** 
     * The command the master should use to execute the app program.
     * This is only used in the case of world-launched apps.
     */
    private String command;

    /** Will the app be moved to the best view on the master after start up? */
    private boolean bestView;

    /** Subclass-specific data for making a peer-to-peer connection between master and slave. */
    private Serializable connectionInfo;

    /** 
     * Create a new instance of AppConventionalCellConfig with default state.
     */
    public AppConventionalCellConfig () {
	this(null, null, null, null);
    }

    /** 
     * Create a new instance of AppConventionalCellConfig.
     *
     * @param masterHost The master host on which the app master will run.
     * @param appName The name of the application.
     * @param pixelScale The number of world units per pixel in the cell local X and Y directions.
     * @param connectionInfo Subclass-specific data for making a peer-to-peer connection between master and slave.
     */
    public AppConventionalCellConfig (String masterHost, String appName, Vector2f pixelScale, 
				      Serializable connectionInfo) {
	super(pixelScale);
	this.masterHost = masterHost;
	this.appName = appName;
	this.connectionInfo = connectionInfo;
    }

    /**
     * Specify the master host.
     *
     * @param masterHost The host name of the master host.
     */
    public void setMasterHost (String masterHost) {
	this.masterHost = masterHost;
    }

    /**
     * Returns the master host name.
     */
    public String getMasterHost () {
	return masterHost;
    }

    /**
     * Specify the app name.
     *
     * @param appName The name of the app.
     */
    public void setAppName (String appName) {
	this.appName = appName;
    }

    /**
     * Returns the app name.
     */
    public String getAppName () {
	return appName;
    }

    /**
     * Specify whether the app was launched by a user.
     *
     * @param userLaunched True if the app was launched by the user. 
     * False if the app was launched by the world.
     */
    public void setUserLaunched (boolean userLaunched) {
	this.userLaunched = userLaunched;
    }

    /**
     * Was the app launched by a user?
     * Note: This information is used only by the master client.
     */
    public boolean isUserLaunched () {
	return userLaunched;
    }

    /**
     * Specify the app ID.
     *
     * @param appId The app ID.
     */
    public void setAppId (UUID appId) {
	this.appId = appId;
    }

    /**
     * Returns the app ID.
     * Note: This information is used only by the master client for user-launched apps.
     */
    public UUID getAppId () {
	return appId;
    }

    /**
     * Specify the command the master should use to execute the app program.
     *
     * @param command The platform command string (contains both the command name and arguments).
     */
    public void setCommand (String command) {
	this.command = command;
    }

    /**
     * Returns the command string.
     * Note: This information is used only by the master client for world-launched apps.
     */
    public String getCommand () {
	return command;
    }

    /**
     * Specify whether the app will be moved to the best view on the master after start up.
     *
     * @param bestView True indicates that this movement should take place.
     */
    public void setBestView (boolean bestView) {
	this.bestView = bestView;
    }

    /**
     * Should the the app will be moved to the best view on the master after start up?
     * Note: This information is used only by the master client.
     */
    public boolean isBestView () {
	return bestView;
    }

    /**
     * Specify the connection info.
     */
    public void setConnectionInfo (Serializable connectionInfo) {
	this.connectionInfo = connectionInfo;
    }

    /**
     * Returns the connection info.
     */
    public Serializable getConnectionInfo () {
	return connectionInfo;
    }
}
