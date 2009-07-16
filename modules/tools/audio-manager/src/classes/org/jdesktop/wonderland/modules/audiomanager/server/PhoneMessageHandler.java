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
package org.jdesktop.wonderland.modules.audiomanager.server;

import org.jdesktop.wonderland.modules.orb.server.cell.Orb;



import org.jdesktop.wonderland.modules.audiomanager.common.messages.CallEndedResponseMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatDialOutMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinAcceptedMessage;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.sgs.app.AppContext;

import com.sun.voip.CallParticipant;



import org.jdesktop.wonderland.server.comms.WonderlandClientSender;


import java.io.IOException;
import java.io.Serializable;

import java.util.logging.Logger;









import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell that provides conference phone functionality
 * @author jprovino
 */
public class PhoneMessageHandler implements Serializable {

    private static final Logger logger =
        Logger.getLogger(PhoneMessageHandler.class.getName());
     
    private int callNumber = 0;

    private static PhoneMessageHandler phoneMessageHandler;

    public static PhoneMessageHandler getInstance() {
	if (phoneMessageHandler == null) {
	    phoneMessageHandler = new PhoneMessageHandler();
	}

	return phoneMessageHandler;
    }

    public PhoneMessageHandler() {
    }

    public void dialOut(final WonderlandClientSender sender, 
	    final WonderlandClientID clientID, final VoiceChatDialOutMessage message) {

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	String group = message.getGroup();

	PresenceInfo presenceInfo = message.getPresenceInfo();

	String externalCallID = group + "-" + callNumber++;

	presenceInfo.callID = externalCallID;

	String softphoneCallID = message.getSoftphoneCallID();

	Call softphoneCall = vm.getCall(softphoneCallID);

	Player softphonePlayer = null;

	if (softphoneCall != null) {
	    softphonePlayer = softphoneCall.getPlayer();
	}
        
 	if (softphonePlayer == null) {
	    logger.warning("Softphone player is not connected!");
            sender.send(clientID, new CallEndedResponseMessage(
	        group, presenceInfo, "Softphone is not connected!"));
	    return;
	}

	logger.fine("EXTERNAL CALLID IS " + externalCallID + " " 
	    + " softphone callID " + softphoneCallID + " softphone call " 
	    + softphoneCall + " softphone player " + softphonePlayer);

	logger.fine("Got place call message " + externalCallID);

	PlayerSetup playerSetup = new PlayerSetup();
	playerSetup.x = softphonePlayer.getX();
	playerSetup.y = 1.5;
	playerSetup.z = softphonePlayer.getZ();
	playerSetup.isOutworlder = true;
	playerSetup.isLivePlayer = true;

	CallSetup setup = new CallSetup();
	
	setup.externalOutgoingCall = true;

	CallParticipant cp = new CallParticipant();

	setup.cp = cp;

	try {
	    setup.bridgeInfo = vm.getVoiceBridge();
	} catch (IOException e) {
	    logger.warning("Unable to get voice bridge for call " + cp + ":  "
		+ e.getMessage());
            sender.send(clientID, new CallEndedResponseMessage(
	        group, presenceInfo, "No voice bridge available!"));
	    return;
	}

	AudioGroup audioGroup = vm.getAudioGroup(group);

	if (audioGroup == null) {
	    logger.warning("No audio group " + group);
            sender.send(clientID, new CallEndedResponseMessage(
	        group, presenceInfo, "Audio group not found!"));
	    return;
	}

	cp.setPhoneNumber(message.getPhoneNumber());
	cp.setName(presenceInfo.usernameAlias);
	cp.setCallId(externalCallID);
	cp.setConferenceId(vm.getVoiceManagerParameters().conferenceId);
	cp.setVoiceDetection(true);
	cp.setDtmfDetection(true);
	cp.setVoiceDetectionWhileMuted(true);
	cp.setHandleSessionProgress(true);

	new PhoneStatusListener(group, presenceInfo, externalCallID);
	
	Call externalCall;

	try {
            externalCall = vm.createCall(externalCallID, setup);
	} catch (IOException e) {
	    logger.warning("Unable to create call " + cp + ":  "
		+ e.getMessage());
            sender.send(clientID, new CallEndedResponseMessage(
	        group, presenceInfo, "Can't create call!"));
	    return;
	}

	Player externalPlayer = vm.createPlayer(externalCallID, playerSetup);

	externalCall.setPlayer(externalPlayer);

	externalPlayer.setCall(externalCall);

	presenceInfo.callID = externalCallID;
	presenceInfo.cellID = null;
	presenceInfo.clientID = null;

	VoiceChatHandler.getInstance().addPlayerToAudioGroup(audioGroup,
	    externalPlayer, presenceInfo, message.getChatType());

	//if (message.getChatType() == ChatType.PUBLIC) {
	//    Vector3f center = new Vector3f((float) externalPlayer.getX(), 
	//	(float) externalPlayer.getY(), (float) externalPlayer.getZ());

        //    new Orb(message.getName(), externalCallID, center, .1, false, "");
	//}

        /*
	 * Send VoiceChatJoinAcceptedMessage back to all the clients 
	 * to signal success.
	 */
	sender.send(clientID, new VoiceChatJoinAcceptedMessage(group, presenceInfo, 
	    message.getChatType()));
	return;
    }

}
