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
package org.jdesktop.wonderland.modules.phone.client.cell;

import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.ViewCell;

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSessionManager;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.phone.common.PhoneConnectionType;

import java.io.IOException;

import java.util.logging.Logger;

/**
 *
 * @author jprovino
 */
public class PhoneClient extends BaseConnection {

    private static final Logger logger =
        Logger.getLogger(PhoneClient.class.getName());

    private WonderlandSession session;

    private PhoneMessageHandler phoneMessageHandler;

    public PhoneClient(WonderlandSession session) throws ConnectionFailureException {
	this.session = session;

	logger.warning("Trying to connect...");
	//session.connect(this);
	new Connector(this);
    }

    class Connector extends Thread {

	PhoneClient client;

	public Connector(PhoneClient client) {
	    this.client = client;
	    start();
	}

	public void run() {
	    try {
		Thread.sleep(5000);
	    } catch (InterruptedException e) {
	    }

	    logger.warning("Trying to connect...");

	    try {
	        session.connect(client);
	    } catch (ConnectionFailureException e) {
		logger.warning("FOO:  " + e.getMessage());
	    }
	}
    }
    
    public void setPhoneMessageHandler(PhoneMessageHandler phoneMessageHandler) {
	this.phoneMessageHandler = phoneMessageHandler;
    }

    @Override
    public void handleMessage(Message message) {
	logger.warning("got a message...");

	if (phoneMessageHandler == null) {
            throw new UnsupportedOperationException("Not supported yet.");
	}

	phoneMessageHandler.processMessage(message);
    }

    public ConnectionType getConnectionType() {
        return PhoneConnectionType.CONNECTION_TYPE;
    }

}
