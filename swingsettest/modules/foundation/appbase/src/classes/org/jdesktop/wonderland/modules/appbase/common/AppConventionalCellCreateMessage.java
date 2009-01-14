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
package org.jdesktop.wonderland.modules.appbase.common;

import java.io.Serializable;
import java.util.UUID;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The app base conventional cell create message.
 * 
 * @author deronj
 */

@InternalAPI
public class AppConventionalCellCreateMessage extends AppConventionalMessage {

    /** The name of the app type for which to create the cell */
    private String appTypeName;

    /** The name of the master host */
    private String masterHost;

    /** The name of this app */
    private String appName;

    /** The unique ID of the app */
    private UUID appId;

    /** Should the first window made visible make the cell move to the best view position? */
    private boolean bestView;
   
    /** The bounds of the new cell (may be null if bestView is true) */
    private BoundingVolume bounds;

    /** The transform of the new cell (may be null if bestView is true) */
    private CellTransform transform;

    /** The size of the window pixels */
    private Vector2f pixelScale;

    /** Subclass-specific data for making a peer-to-peer connection between master and slave. */
    private Serializable connectionInfo;

    /** The default constructor */
    public AppConventionalCellCreateMessage () {}

    /**
     * Creates a new instance of AppConventionalMessage.
     * 
     * @param actionType The type of the message.
     * @param appTypeName The name of the app type.
     * @param masterHost The name of the master host.
     * @param appName The name of the app instance.
     * @param appId The app ID.
     * @param bestView True if the window should be should be moved into best view on startup.
     * @param bounds The bounds of the app cell.
     * @param transform The app cell transform.
     * @param pixelScale The size of the pixels in world coordinates.
     * @param connectionInfo Subclass-specific data for making a peer-to-peer connection between master and slave.
     */
    public AppConventionalCellCreateMessage (ActionType actionType, String appTypeName, String masterHost, String appName, 
					     UUID appId, boolean bestView, BoundingVolume bounds, CellTransform transform,
					     Vector2f pixelScale, Serializable connectionInfo) {
	super(actionType);
	this.appTypeName = appTypeName;
	this.masterHost = masterHost;
	this.appName = appName;
	this.appId = appId;
	this.bestView = bestView;
	this.bounds = bounds;
	this.transform = transform;
	this.pixelScale = pixelScale;
	this.connectionInfo = connectionInfo;
    }

    /**
     * Returns the name of the app type.
     */
    public String getAppTypeName () {
	return appTypeName;
    }

    /**
     * Returns the name of the master host.
     */
    public String getMasterHost () {
	return masterHost;
    }

    /**
     * Returns the name of the app.
     */
    public String getAppName () {
	return appName;
    }

    /**
     * Returns the unique ID of the app.
     */
    public UUID getAppId () {
	return appId;
    }

    /**
     * Returns the best view flag.
     */
    public boolean getBestView () {
	return bestView;
    }

    /**
     * Returns the bounds.
     */
    public BoundingVolume getBounds () {
	return bounds;
    }

    /**
     * Returns the cell transform.
     */
    public CellTransform getTransform() {
	return transform;
    }

    /**
     * Returns the pixel scale.
     */
    public Vector2f getPixelScale () {
	return pixelScale;
    }

    /**
     * Returns the subclass data.
     */
    public Serializable getConnectionInfo () {
	return connectionInfo;
    }
}

