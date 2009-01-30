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
package org.jdesktop.wonderland.modules.xremwin.server;

import java.util.UUID;
import java.util.logging.Logger;
import com.jme.math.Vector2f;
import com.jme.bounding.BoundingVolume;
import java.io.Serializable;
import org.jdesktop.wonderland.modules.xremwin.common.AppLaunchMethodsXrw;
import org.jdesktop.wonderland.modules.xremwin.common.AppTypeNameXrw;
import org.jdesktop.wonderland.modules.appbase.server.AppConventionalCellMO;
import org.jdesktop.wonderland.modules.appbase.server.AppTypeConventionalMO;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.common.AppLaunchMethods;

/**
 * The Xremwin app type on the server side.
 * 
 * @author deronj
 */
@ExperimentalAPI
public class AppTypeMOXrw extends AppTypeConventionalMO {

    /** The server-side logger for Xremwin */
    static final Logger logger = Logger.getLogger("wl.app.modules.xremwin");

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return AppTypeNameXrw.XREMWIN_APP_TYPE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public AppLaunchMethods getLaunchMethods() {
        return new AppLaunchMethodsXrw();
    }
    /**
     * {@inheritDoc}
     */
    /* TODO: I'm not sure this is used anymore.
    public AppConventionalCellMO createServerCell (String masterHost, String appName, UUID appId, 
    boolean bestView, BoundingVolume bounds,
    CellTransform transform, Vector2f pixelScale,
    Serializable connectionInfo) {
    return new AppCellMOXrw(bounds, transform, bestView, pixelScale, masterHost, appName, appId, connectionInfo);
    }
     */
}


