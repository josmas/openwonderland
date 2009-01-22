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

import com.sun.sgs.app.ClientSession;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.modules.appbase.server.AppConventionalCellMO;
import org.jdesktop.wonderland.modules.appbase.server.AppTypeMO;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalCellCreateMessage;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * The server-side cell for an Xremwin application.
 * 
 * @author deronj
 */

@ExperimentalAPI
public class AppCellMOXrw extends AppConventionalCellMO {
    
    private static final Logger logger = Logger.getLogger(AppCellMOXrw.class.getName());

    /** Whether the connection handler has been registered. */
    private boolean connectionHandlerRegistered;

    /** Default constructor, used when the cell is created via WFS */
    public AppCellMOXrw () {}

    /**
     * Creates a new instance of a user-launched <code>AppCellMOXrw</code>.
     * @param msg The creation message received from the client.
     */
    public AppCellMOXrw (AppConventionalCellCreateMessage msg) {
	super(msg);
	
	// Register the connection handler when the first cell is created
	if (!connectionHandlerRegistered) {
	    CommsManager cm = WonderlandContext.getCommsManager();
	    cm.registerClientHandler(new AppConnectionHandlerXrw());
	    connectionHandlerRegistered = true;
	}
    }
 
    /** 
     * {@inheritDoc}
     */
    public AppTypeMO getAppType () {
	return new AppTypeMOXrw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.lg3d.wonderland.client.app.xremwin.AppCellXrw";
    }
}
