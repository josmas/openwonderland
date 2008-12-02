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
package org.jdesktop.wonderland.modules.audiomanager.server;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AvatarCellIDMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.DisconnectCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlaceCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.SpeakingMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.TransferCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage;

import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.UserMO;

import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.view.AvatarCellMO;

import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import java.io.Serializable;
import java.util.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;

import java.util.Properties;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.BridgeInfo;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.voip.CallParticipant;

import com.sun.voip.client.connector.CallStatus;
import com.sun.voip.client.connector.CallStatusListener;

import java.io.IOException;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * Audio Manager
 * 
 * @author jprovino
 */
public class AudioManagerConnectionHandler 
        implements ClientConnectionHandler, Serializable, CallStatusListener
{
    private static final Logger logger =
            Logger.getLogger(AudioManagerConnectionHandler.class.getName());
    
    private VoiceChatHandler voiceChatHandler = new VoiceChatHandler();

    private ConcurrentHashMap<WonderlandClientSender, String> senderCallIDMap = 
	new ConcurrentHashMap();

    private ConcurrentHashMap<String, WonderlandClientSender> callIDSenderMap = 
	new ConcurrentHashMap();

    public AudioManagerConnectionHandler() {
        super();
    }

    public ConnectionType getConnectionType() {
        return AudioManagerConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
	logger.fine("Audio Server manager connection registered");
    }

    public void clientConnected(WonderlandClientSender sender, 
	    WonderlandClientID clientID, Properties properties) {

        //throw new UnsupportedOperationException("Not supported yet.");
	logger.fine("client connected...");
    }

    public void messageReceived(WonderlandClientSender sender, 
	    WonderlandClientID clientID, Message message) {

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	if (message instanceof AvatarCellIDMessage) {
	    AvatarCellIDMessage msg = (AvatarCellIDMessage) message;
	    voiceChatHandler.addTransformChangeListener(msg.getCellID());
	    return;
	}

	if (message instanceof GetVoiceBridgeMessage) {
	    GetVoiceBridgeMessage msg = (GetVoiceBridgeMessage) message;

	    String username = 
		UserManager.getUserManager().getUser(clientID).getUsername();

	    logger.fine("Got voice bridge request message from " + username);

	    try {
		String voiceBridge = vm.getVoiceBridge().toString();

		logger.info("Got voice bridge '" + voiceBridge + "'");
	        msg.setBridgeInfo(voiceBridge);
		msg.setUsername(username);
	    } catch (IOException e) {
		logger.warning("unable to get voice bridge:  " + e.getMessage());
		return;
	    }

	    sender.send(msg);
	    return;
	}

	if (message instanceof PlaceCallMessage) {
	    logger.fine("Got place call message");

	    PlaceCallMessage msg = (PlaceCallMessage) message;

	    CallSetup setup = new CallSetup();

	    CallParticipant cp = new CallParticipant();

	    setup.cp = cp;
	    setup.listener = this;

	    String callID = msg.getSoftphoneCallID();

	    logger.fine("callID " + callID);

	    if (callID == null) {
	        logger.fine("Can't place call to " + msg.getSipURL()
		    + ".  No cell for " + callID);
		return;
	    }

	    cp.setCallId(callID);
	    cp.setName(UserManager.getUserManager().getUser(clientID).getUsername());
            cp.setPhoneNumber(msg.getSipURL());
            cp.setConferenceId(vm.getConferenceId());
            cp.setVoiceDetection(true);
            cp.setDtmfDetection(true);
            cp.setVoiceDetectionWhileMuted(true);
            cp.setHandleSessionProgress(true);
            cp.setJoinConfirmationTimeout(0);
	    cp.setCallAnsweredTreatment(null);

	    Call call;

            try {
                call = vm.createCall(callID, setup);
            } catch (IOException e) {
                logger.warning("Unable to create call " + cp + ": " + e.getMessage());
		return;
            }

	    callID = call.getId();

	    senderCallIDMap.put(sender, callID);
	    callIDSenderMap.put(callID, sender);

            PlayerSetup ps = new PlayerSetup();
            ps.x = (double) msg.getX();
            ps.y = (double) msg.getY();
            ps.z = (double) msg.getZ();
            ps.orientation = msg.getDirection();
            ps.isLivePlayer = true;

            Player player = vm.createPlayer(callID, ps);

            call.setPlayer(player);
            player.setCall(call);

            vm.getDefaultLivePlayerAudioGroup().addPlayer(player,
                new AudioGroupPlayerInfo(true, AudioGroupPlayerInfo.ChatType.PUBLIC));

            AudioGroupPlayerInfo info = new AudioGroupPlayerInfo(false,
                AudioGroupPlayerInfo.ChatType.PUBLIC);

            info.defaultSpeakingAttenuation = 0;

            vm.getDefaultStationaryPlayerAudioGroup().addPlayer(player, info);
	    return;
	}

	if (message instanceof TransferCallMessage) {
	    TransferCallMessage msg = (TransferCallMessage) message;

	    String callID = msg.getSoftphoneCallID();

	    if (callID == null) {
		logger.warning("Unable to transfer call.  Can't get callID for " 
		    + callID);
		return;
	    }

	    Call call = vm.getCall(callID);

	    if (call == null) {
		// XXX we should be nicer and place the call!
		logger.warning("Unable to transfer call.  No Call for " + callID);
		return;
	    }

	    CallParticipant cp = call.getSetup().cp;

            cp.setPhoneNumber(msg.getPhoneNumber());
            cp.setJoinConfirmationTimeout(90);

	    String callAnsweredTreatment = System.getProperty(
                "com.sun.sgs.impl.app.voice.CALL_ANSWERED_TREATMENT");

	    if (callAnsweredTreatment == null || callAnsweredTreatment.length() == 0) {
		callAnsweredTreatment = "dialtojoin.au";
	    }

            cp.setCallAnsweredTreatment(callAnsweredTreatment);

	    try {
	        call.transfer(cp);
	    } catch (IOException e) {
		logger.warning("Unable to transfer call:  " + e.getMessage());
	    }
	    return;
	}

	if (message instanceof DisconnectCallMessage) {
	    logger.fine("got DisconnectCallMessage");
	    return;
	}

	if (message instanceof VoiceChatMessage) {
	    voiceChatHandler.processVoiceChatMessage(sender, (VoiceChatMessage) message);
	    return;
	}

        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
//        throw new UnsupportedOperationException("Not supported yet.");

	String callID = senderCallIDMap.get(sender);

	if (callID == null) {
	    logger.warning("Unable to find callID for sender " + sender);
	    return;
	}

	senderCallIDMap.remove(sender);
	callIDSenderMap.remove(callID);

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	Call call = vm.getCall(callID);

	if (call == null) {
	    logger.warning("Can't find call for " + callID);
	    return;
	}

	try {
	    AppContext.getManager(VoiceManager.class).endCall(call, true);
	} catch (IOException e) {
	    logger.warning("Unable to end call " + call + " " + e.getMessage());
	}
    }

    public void callStatusChanged(CallStatus status) {
	logger.finer("GOT STATUS " + status);

	int code = status.getCode();

	String callId = status.getCallId();

	if (callId == null) {
	    logger.warning("No callId in status:  " + status);
	    return;
	}

	WonderlandClientSender sender = callIDSenderMap.get(callId);

	switch (code) {
	case CallStatus.ESTABLISHED:
	    Call call = AppContext.getManager(VoiceManager.class).getCall(callId);

	    if (call == null) {
		System.out.println("Couldn't find call for " + callId);
		return;
	    }

	    Player player = call.getPlayer();

	    if (player == null) {
		System.out.println("Couldn't find player for " + call);
		return;
	    }

	    /*
	     * XXX Fix me.  This is just to force the private mixes to be recalulated.
	     */
	    player.setCall(call);
	    break;

        case CallStatus.STARTEDSPEAKING:
	    //sender.send(new SpeakingMessage(true));
            break;

        case CallStatus.STOPPEDSPEAKING:
	    //sender.send(new SpeakingMessage(false));
            break;

	case CallStatus.BRIDGE_OFFLINE:
            logger.info("Bridge offline: " + status);

            /*
             * After the last bridge_offline call (callID=''),
             * we have to tell the voice manager to restore
             * all the pm's for live players.
             */
            if (callId.length() == 0) {
                logger.fine("Restoring private mixes...");

		// XXX need a way to tell the voice manager to reset all of the private mixes.
            }
            break;
        }
    }

}

