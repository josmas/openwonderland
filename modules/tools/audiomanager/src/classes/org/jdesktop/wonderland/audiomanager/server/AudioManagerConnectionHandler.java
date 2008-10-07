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
package org.jdesktop.wonderland.audiomanager.server;

import java.util.Properties;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.common.messages.PlaceCallMessage;
import org.jdesktop.wonderland.common.messages.DisconnectCallMessage;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import com.sun.sgs.app.ClientSession;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.audiomanager.common.AudioManagerConnectionType;

import com.sun.sgs.app.AppContext;

import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;

import com.sun.voip.CallParticipant;

import com.sun.voip.client.connector.CallStatus;
import com.sun.voip.client.connector.CallStatusListener;

import java.io.IOException;

/**
 * Test listener, will eventually support Audio Manager
 * 
 * @author paulby
 */
public class AudioManagerConnectionHandler 
        implements ClientConnectionHandler, Serializable, CallStatusListener
{
    private static final Logger logger =
            Logger.getLogger(AudioManagerConnectionHandler.class.getName());
    
    public AudioManagerConnectionHandler() {
        super();
    }

    public ConnectionType getConnectionType() {
        return AudioManagerConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        logger.info("Sever manager connection registered");
    }

    public void clientConnected(WonderlandClientSender sender, ClientSession session, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void messageReceived(WonderlandClientSender sender, ClientSession session, Message message) {

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	if (message instanceof GetVoiceBridgeMessage) {
	    GetVoiceBridgeMessage msg = (GetVoiceBridgeMessage) message;

	    msg.setBridgeInfo("129.148.75.55:6666:5060:129.148.75.55:6666:5060");

	    sender.send(msg);
	} else if (message instanceof PlaceCallMessage) {
	    PlaceCallMessage msg = (PlaceCallMessage) message;

	    CallSetup setup = new CallSetup();

	    CallParticipant cp = new CallParticipant();

            cp.setPhoneNumber(msg.getSipURL());
            cp.setConferenceId(vm.getConferenceId());
            cp.setVoiceDetection(true);
            cp.setDtmfDetection(true);
            cp.setVoiceDetectionWhileMuted(true);
            cp.setHandleSessionProgress(true);

            vm.addCallStatusListener(this);

            try {
                Call call = vm.createCall("jp", setup);
            } catch (IOException e) {
                logger.warning("Unable to create call " + cp + ": " + e.getMessage());
            }
	} else if (message instanceof DisconnectCallMessage) {
	    logger.warning("got DisconnectCallMessage");
	} else {
            throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    public void clientDisconnected(WonderlandClientSender sender, ClientSession session) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
