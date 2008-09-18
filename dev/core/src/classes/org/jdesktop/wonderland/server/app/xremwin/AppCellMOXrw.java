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
package org.jdesktop.wonderland.modules.app.xremwin.server;

import java.util.UUID;
import com.jme.math.Vector2f;
import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.server.app.base.AppConventionalCellGLO;
import org.jdesktop.wonderland.server.app.base.AppTypeCellGLO;
import org.jdesktop.wonderland.server.app.base.AppTypeGLO;
import org.jdesktop.wonderland.common.app.xremwin.AppTypeNameXrw;

/**
 * The server-side cell for an Xremwin application.
 * 
 * @author deronj
 */

@ExperimentalAPI
public class AppCellMOXrw extends AppConventionalCellMO {
    
    /**
     * Creates a new instance of AppCellMOXrw. Used in the 
     * world-launched case. The cell creator will initialize the cell
     * via cellSetup.
     */
    public AppCellMOXrw () {}

    /**
     * Creates a new instance of a user-launched AppCellMOXrw.
     *
     * TODO: Note: eventually channel will be deleted.
     *
     * @param bounds The bounds of the cell.
     * @param transform The cell transform.
     * @param bestView Position the cell to the best view in the master client.
     * (If true, this will override bounds and transform).
     * @param pixelScale The size of the application pixels in world coordinates.
     * @param masterHost The host on which to run the app master.
     * @param appName The name of the app.
     * @param appId The ID of the app (unique within the master host client.
     * @param channelName The server channel name used for master and slaves to rendezvous.
     */
    public AppCellMOXrw (BoundingVolume bounds, CellTransform transform, boolean bestView, Vector2f pixelScale, 
			 String masterHost, String appName, UUID appId, Serializable connectionInfo) {
	super(bounds, transform, bestView, pixelScale, masterHost, appName, appId, connectionInfo);
    }

    /** 
     * Returns the full class name of the corresponding client app cell.
     */
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.client.app.xremwin.AppCellXrw";
    }

    /** 
     * {@inheritDoc}
     */
    public AppTypeMO getAppType () {
	return AppTypeCellMO.findAppType(AppTypeNameXrw.XREMWIN_APP_TYPE_NAME);
    }
}
