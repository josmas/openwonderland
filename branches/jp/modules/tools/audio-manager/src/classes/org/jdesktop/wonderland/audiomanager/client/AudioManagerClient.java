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
package org.jdesktop.wonderland.audiomanager.client;

import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveListener;

import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.ViewCell;

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.client.comms.SessionLifecycleListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSessionManager;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.audiomanager.common.AudioManagerConnectionType;
import org.jdesktop.wonderland.audiomanager.common.AvatarCellIDMessage;
import org.jdesktop.wonderland.audiomanager.common.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.audiomanager.common.PlaceCallMessage;

import org.jdesktop.wonderland.client.softphone.AudioQuality;
import org.jdesktop.wonderland.client.softphone.SoftphoneControl;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import java.io.IOException;

import java.util.logging.Logger;

/**
 *
 * @author paulby
 */
public class AudioManagerClient extends BaseConnection {
    private static final Logger logger =
        Logger.getLogger(AudioManagerClient.class.getName());

    private WonderlandSession session;

    public AudioManagerClient(WonderlandSession session)  
	    throws ConnectionFailureException {

	this.session = session;

	//logger.warning("Trying to connect...");
	//session.connect(this);
	new Connector(this);
    }

    class Connector extends Thread {

	AudioManagerClient client;

	public Connector(AudioManagerClient client) {
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

	    CellID cellID = ((CellClientSession)session).getLocalAvatar().getViewCell().getCellID();

	    session.send(client, new AvatarCellIDMessage(cellID));

	    logger.warning("Sending message to server to get voice bridge...");

	    session.send(client, new GetVoiceBridgeMessage());
	}
    }
    
    @Override
    public void handleMessage(Message message) {
	logger.warning("got a message...");

	if (message instanceof GetVoiceBridgeMessage) {
	    GetVoiceBridgeMessage msg = (GetVoiceBridgeMessage) message;

	    logger.warning("Got voice bridge " + msg.getBridgeInfo());

	    SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

            String tokens[] = msg.getBridgeInfo().split(":");

            String registrarAddress = tokens[2] + ";sip-stun:";

            registrarAddress += tokens[4];

	    try {
	        String sipURL = sc.startSoftphone(
		    "jp", registrarAddress, 10, "129.148.75.55", AudioQuality.VPN);


		sc.setVisible(true);

	        // XXX need location and direction
	        session.send(this, new PlaceCallMessage(sipURL, 0., 0., 0., 0., false));
	    } catch (IOException e) {
		logger.warning(e.getMessage());
	    }
	} else {
            throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    public ConnectionType getConnectionType() {
        return AudioManagerConnectionType.CONNECTION_TYPE;
    }

}
