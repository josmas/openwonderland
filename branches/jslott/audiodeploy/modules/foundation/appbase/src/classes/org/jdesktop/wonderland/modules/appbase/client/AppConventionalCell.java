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
package org.jdesktop.wonderland.modules.appbase.client;

import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalCellConfig;
import org.jdesktop.wonderland.modules.appbase.client.utils.net.NetworkAddress;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The client-side cell for an 2D conventional application.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppConventionalCell extends App2DCell {

    private static final Logger logger = Logger.getLogger(AppConventionalCell.class.getName());

    /** The server-determined host on which the master is to run */
    protected String masterHost;

    /** The user-visible app name */
    protected String appName;

    /** pixelScale The size of the window pixels in world coordinates.
    protected Vector2f pixelScale;

    /** The connection info. */
    protected Serializable connectionInfo;

    /** 
     * Creates a new instance of AppConventionalCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public AppConventionalCell (CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure (CellConfig configData) {
	super.configure(configData);

	AppConventionalCellConfig config = (AppConventionalCellConfig) configData;
	masterHost = config.getMasterHost();
	appName = config.getAppName();
	pixelScale = config.getPixelScale();
	connectionInfo = config.getConnectionInfo();

	if (masterHost.equals(NetworkAddress.getDefaultHostAddress())) {

	    // App Master case
    	    boolean bestView = config.isBestView();

	    // Master User launch case: See if app has already been executed on this host 
	    if (config.isUserLaunched()) {
		UUID appId = config.getAppId();
		App appToAttach = AppConventional.findDisembodiedApp(appId);
		if (appToAttach == null) {
		    logger.severe("Cannot find master app to attach to cell");
		    return;
		}
		app = appToAttach;
		AppConventional.removeDisembodiedApp(appId);
		((AppConventional)app).setInitInBestView(bestView);
		appToAttach.setCell(this);
		// The master app is now connected to its cell and
		// it's protocol client and is free to run.
		return;
		
	    } else {

		// World launch case: execute the app now. 
		startMaster(config.getCommand(), false);
	    }

	} else {

	    // App Slave case
	    startSlave();
	}
    }

    /**
     * A utility method used to report a launch error.
     */
    protected static void reportLaunchError (String message) {
	JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** 
     * Launch a master client.
     * @param command The command string which launches the master app program (used only by master).
     * @param initInBestView Force this cell to be initialized in approximately the best view
     * based on the viewer position at the time of client cell creation.
     */
    protected abstract void startMaster (String command, boolean initInBestView);

    /** Launch a slave client */
    protected abstract void startSlave ();
}
