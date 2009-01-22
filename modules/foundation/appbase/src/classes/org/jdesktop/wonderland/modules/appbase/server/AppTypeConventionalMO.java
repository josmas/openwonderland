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
package org.jdesktop.wonderland.modules.appbase.server;

import java.util.UUID;
import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.common.cell.CellTransform;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import java.io.Serializable;

/**
 * A conventional app type on the server side. 
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppTypeConventionalMO extends AppTypeMO {

    /**
     * Create a new instance of a conventional app server cell.
     *
     * @param masterhost The name of the master host.
     * @param appName The name of the app instance.
     * @param appId The app's unique ID (note: the ID is unique only within the master client session).
     * @param bestView Force this cell to be initialized in approximately the best view based on the viewer 
     * position at the time of client cell creation.
     * @param bounds The bounds of the new app cell.
     * @param transform The cell transform.
     * @param pixelScale The size of the window pixels in world coordinates.
     * @param connectionInfo Subclass-specific data for making a peer-to-peer connection between 
     * master and slave.
     */
    /* TODO: I'm not sure this is used anymore.
    public abstract AppConventionalCellMO createServerCell (String masterHost, String appName, UUID appId, 
							    boolean bestView, BoundingVolume bounds, 
							    CellTransform transform, Vector2f pixelScale,
							    Serializable connectionInfo);
    */
}
