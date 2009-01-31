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
package org.jdesktop.wonderland.common.app.sas;

/**
 * A message which SAS user uses to tell a SAS provider to launch an app.
 * 
 * @author deronj
 */

Adds:
int sequenceNumber

@InternalAPI
public abstract class SasServerLaunchMessage extends SasUserLaunchMessage {

    /** The default constructor */
    public SasLaunchMessage () {}

TODO
    /**
     * Create a new instance of SasLaunchMessage
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
    public static AppConventionalCellCreateMessage newCreateCellMessage (String appTypeName, String masterHost, 
								 String appName, UUID appId, boolean bestView, 
								 BoundingVolume bounds, CellTransform transform, 
								 Vector2d pixelScale, Serializable connectionInfo) {
        AppBaseMessage ret = new AppBaseCellCreateMessage(ActionType.CELL_CREATE, appTypeName, masterHost, appName, 
							  appId, bestView, bounds, transform, pixelScale, 
							  connectionInfo);
        return ret;
    }
}
