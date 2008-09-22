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

import org.jdesktop.wonderland.common.messages.Message;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.cell.CellTransform;
import java.io.Serializable;
import java.util.UUID;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * Superclass for messages that the App Base uses to communicate between the client and server
 * about conventional cells.
 * 
 * @author deronj
 */

@InternalAPI
public abstract class AppConventionalMessage extends Message {

    /** The action type of this message */
    protected ActionType actionType;

    /**
     * The types of app conventional messages.
     *
     * CELL_CREATE: Client tells the server to create a new app cell.
     */
    public enum ActionType { CELL_CREATE };

    /** The default constructor */
    public AppConventionalMessage () {}

    /**
     * Creates a new instance of AppConventionalMessage.
     * 
     * @param actionType The type of the message.
     */
    public AppConventionalMessage (ActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * Specify the action type of this message.
     * @param actionType The action type of this message.
     */
    public void setActionType (ActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * Return the action type of this message.
     * @return The action type of this message.
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Create a new CELL_CREATE message.
     *
     * @param appTypeName The name of the app type.
     * @param masterHost The name of the master host.
     * @param appName The name of the app instance.
     * @param appId The app ID.
     * @param bestView True if the window should be should be moved into best view on startup.
     * @param bounds The bounds of the app cell.
     * @param transform The app cell transform.
     * @param pixelScale The size of the pixels in world coordinates.
     * @param connectionInfo Subclass-specific data for making a peer-to-peer connection between master and slave.
     * @return Returns a new CELL_CREATE message.
     */
    public static AppConventionalCellCreateMessage newCellCreateMessage (String appTypeName, String masterHost, 
								 String appName, UUID appId, boolean bestView, 
								 BoundingVolume bounds, CellTransform transform, 
								 Vector2f pixelScale, Serializable connectionInfo) {
        AppConventionalCellCreateMessage ret = new AppConventionalCellCreateMessage(ActionType.CELL_CREATE, appTypeName, 
									  masterHost, appName, appId, bestView, 
									  bounds, transform, pixelScale, 
									  connectionInfo);
        return ret;
    }
}

