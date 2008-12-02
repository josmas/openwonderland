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
package org.jdesktop.wonderland.modules.appbase.server;

import java.io.Serializable;
import java.util.UUID;
import com.jme.bounding.BoundingVolume;
import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalCellConfig;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalCellCreateMessage;
import org.jdesktop.wonderland.common.cell.CellTransform;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.setup.BasicCellSetupHelper;

/**
 * The server-side cell for an 2D conventional application.
 *
 * This cell can be created in two different ways:
 * <br><br>
 * 1. World-launched App
 * <br><br>
 * When WFS launches the app it uses the default constructor and
 * calls <code>configCell</code> to transfer the information from the wlc file
 * into the cell. 
 * <br><br>
 * In this case the wlc <code>cellConfig</code> must specify:
 * <ol>
 * + command: The command to execute. This must not be a non-empty string.         
 * </ol>
 * The wlc <code>cellConfig</code> can optionally specify:
 * <ol>
 * + <code>appName</code>: The name of the application (Default: "NoName").
 * </ol>
 * <ol>
 * + <code>pixelScaleX</code>: The number of world units per pixel in the cell local X direction (Default: 0.01).
 * </ol>
 * <ol>
 * + <code>pixelScaleY</code> The number of world units per pixel in the cell local Y direction (Default: 0.01).
 * </ol>
 * In this case <code>userLaunched</code> is set to false.
 *<br><br>
 * 2. User-launched App
 *<br><br> 
 * When the user launches an app it sends a command to the server. The handler for
 * this command uses the non-default constructor of this class to provide the
 * necessary information to the client
 *<br><br>
 * In this case <code>userLaunched</code> is set to true.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppConventionalCellMO extends App2DCellMO { 

    /** Whether the app has been launched by the world or user */
    protected boolean userLaunched;

    /** The name of the app */
    protected String appName;

    /** The host on which to run the app master */
    protected String masterHost;

    /** Will the app be moved to the best view on the master after start up? */
    protected boolean bestView;

    /** Subclass-specific data for making a peer-to-peer connection between master and slave. */
    private Serializable connectionInfo;

    /** 
     * The unique ID of the app. For user-launched apps this is assigned
     * by the master and therefore is only unique within the master client.
     * For world-launched apps this is assigned by the server and is
     * therefore unique within the entire system.
     */
    protected UUID appId;

    /** 
     * The command the master should use to execute the app program.
     * This is only used in the case of world-launched apps.
     */
    protected String command;

    /** Server-to-client Config Data */
    private AppConventionalCellConfig config;

    /** Default constructor, used when the cell is created via WFS */
    public AppConventionalCellMO() {
	super();
    }

    /**
     * Creates a new instance of a user-launched <code>AppConventionalCellMO</code>.
     *
     * @param msg The creation message received from the client.
     */
    public AppConventionalCellMO (AppConventionalCellCreateMessage msg) {
        super(calcBounds(msg.getBestView(), msg.getBounds()), 
	      calcTransform(msg.getBestView(), msg.getTransform()), 
	      msg.getPixelScale());
	this.masterHost = msg.getMasterHost();
	this.appName = msg.getAppName();
	this.appId = msg.getAppId();
	this.bestView = msg.getBestView();
	this.connectionInfo = msg.getConnectionInfo();
	userLaunched = true;
    }

    /**
     * If bestView is true, returns a reasonable "best view" bounds. Otherwise just returns the given bounds.
     */
    private static BoundingVolume calcBounds (boolean bestView, BoundingVolume bounds) {
	if (bestView) {
	    // Override bounds with a temporary value which will get the cell loaded into 
	    // the client caches before permanent positioning.
	    bounds = new BoundingBox(new Vector3f(0f, 0f, 0f),  1f, 1f, 1f);
	}
	return bounds;
    }

    /**
     * If bestView is true, supply a reasonable "best view" transform. Otherwise just return the given transform.
     */
    private static CellTransform calcTransform (boolean bestView, CellTransform transform) {
	if (bestView) {
	    // Override origin with a temporary value which will get the cell loaded into 
	    // the client caches before permanent positioning.
	    transform = new CellTransform(null, null);
	}
	return transform;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected CellConfig getCellConfig (WonderlandClientID clientID, ClientCapabilities capabilities) {
	if (config == null) {
	    config = new AppConventionalCellConfig(masterHost, appName, pixelScale, connectionInfo);
	    if (userLaunched) {
		config.setUserLaunched(true);
		config.setAppId(appId);
		config.setBestView(bestView);
		config.setConnectionInfo(connectionInfo);
	    } else {
		config.setUserLaunched(false);
		config.setCommand(command);
	    }
	}
	return config;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setupCell(BasicCellSetup setupData) {
	super.setupCell(setupData);

	AppConventionalCellSetup setup = (AppConventionalCellSetup) setupData;

	// TODO: what should this be?
	//masterHost = NetworkAddress.getDefaultHostAddress();
	masterHost = "localHost";

	appName = setup.getAppName();

	command = setup.getCommand();
	if (command == null || command.length() <= 0) {
	    // TODO: what is the proper way to signal this error which is non-fatal to the server?
	    throw new RuntimeException("Invalid app cell command");
	}

	pixelScale = setup.getPixelScale();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconfigureCell(BasicCellSetup setup) {
        super.reconfigureCell(setup);
        setupCell(setup);
    }

    /**
     * Return a new BasicCellSetup Java bean class that represents the current
     * state of the cell.
     * 
     * @return a JavaBean representing the current state
     */
    public BasicCellSetup getCellMOSetup() {

        /* Create a new BasicCellState and populate its members */
        AppConventionalCellSetup setup = new AppConventionalCellSetup();
	setup.setMasterHost(this.masterHost);
	setup.setAppName(this.appName);
	setup.setCommand(this.command);
	setup.setPixelScale(this.pixelScale);
        
        /* Set the bounds of the cell */
        BoundingVolume bounds = this.getLocalBounds();
        if (bounds != null) {
            setup.setBounds(BasicCellSetupHelper.getSetupBounds(bounds));
        }

        /* Set the origin, scale, and rotation of the cell */
        CellTransform transform = this.getLocalTransform(null);
        if (transform != null) {
            setup.setOrigin(BasicCellSetupHelper.getSetupOrigin(transform));
            setup.setRotation(BasicCellSetupHelper.getSetupRotation(transform));
            setup.setScaling(BasicCellSetupHelper.getSetupScaling(transform));
        }
        return setup;
    }
}
