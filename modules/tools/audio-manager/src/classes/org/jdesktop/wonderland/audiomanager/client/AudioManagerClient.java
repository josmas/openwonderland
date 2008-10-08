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

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.client.comms.SessionLifecycleListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSessionManager;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.audiomanager.common.AudioManagerConnectionType;
import org.jdesktop.wonderland.audiomanager.common.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.audiomanager.common.PlaceCallMessage;

import org.jdesktop.wonderland.client.softphone.AudioQuality;
import org.jdesktop.wonderland.client.softphone.SoftphoneControl;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import java.io.IOException;

/**
 *
 * @author paulby
 */
public class AudioManagerClient extends BaseConnection {

    private WonderlandSession session;

    public AudioManagerClient(WonderlandSession session)  
	    throws ConnectionFailureException {

	this.session = session;

	session.connect(this);

	session.send(this, new GetVoiceBridgeMessage());
    }
    
    @Override
    public void handleMessage(Message message) {
	if (message instanceof GetVoiceBridgeMessage) {
	    GetVoiceBridgeMessage msg = (GetVoiceBridgeMessage) message;

	    System.out.println("Got voice bridge " + msg.getBridgeInfo());

	    SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

            String tokens[] = msg.getBridgeInfo().split(":");

            String registrarAddress = tokens[0] + ";sip-stun:";

            if (tokens.length >= 2) {
                registrarAddress += tokens[1];
            } else {
                registrarAddress += "5060";
            }

	    try {
	        String sipURL = sc.startSoftphone(
		    "jp", registrarAddress, 10, "129.148.75.55", AudioQuality.VPN);

	        // XXX need location and direction
	        session.send(this, new PlaceCallMessage(sipURL, 0., 0., 0., 0., false));
	    } catch (IOException e) {
		System.out.println(e.getMessage());
	    }
	} else {
            throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    public ConnectionType getConnectionType() {
        return AudioManagerConnectionType.CONNECTION_TYPE;
    }

}
