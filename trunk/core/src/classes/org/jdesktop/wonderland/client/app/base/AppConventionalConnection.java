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
package org.jdesktop.wonderland.client.app.base;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.common.app.base.AppConventionalConnectionType;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Handler for app base conventional messages.
 *
 * @author deronj
 */

@ExperimentalAPI
public class AppConventionalConnection extends BaseConnection {

    private static final Logger logger = Logger.getLogger(AppConventionalConnection.class.getName());

    /** 
     * Create a new instance of AppConventionalConnection.
     */
    public AppConventionalConnection () {}
    
    /**
     * Get the type of client
     * @return CellClientType.CELL_CLIENT_TYPE
     */
    public ConnectionType getConnectionType() {
        return AppConventionalConnectionType.CLIENT_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    public ResponseMessage sendAndWait(Message message) {
	try {
	    return super.sendAndWait(message);
	} catch (InterruptedException ex) {}
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void handleMessage (Message message) {
	// Note: currently there are no messages sent by the server to the client
	// over this connection except for response messages.
    }
}