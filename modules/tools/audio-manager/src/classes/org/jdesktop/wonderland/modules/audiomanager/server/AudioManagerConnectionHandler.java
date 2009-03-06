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

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AvatarCellIDMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.CellStatusChangeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.DisconnectCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetUserListMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.MuteCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlaceCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.SpeakingMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.TransferCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage;

import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.UserMO;

import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.view.AvatarCellMO;

import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import java.io.Serializable;

import java.util.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.BridgeInfo;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.voip.CallParticipant;

import com.sun.voip.client.connector.CallStatus;

import java.io.IOException;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * Audio Manager
 * 
 * @author jprovino
 */
public class AudioManagerConnectionHandler 
        implements ClientConnectionHandler, Serializable, ManagedCallStatusListener
{
    private static final Logger logger =
            Logger.getLogger(AudioManagerConnectionHandler.class.getName());
    
    private VoiceChatHandler voiceChatHandler = new VoiceChatHandler();

    private ConcurrentHashMap<WonderlandClientSender, String> senderCallIDMap = 
	new ConcurrentHashMap();

    private static ConcurrentHashMap<String, String> callIDUsernameMap = 
	new ConcurrentHashMap();

    private static ConcurrentHashMap<String, String> usernameCallIDMap = 
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

    public static String getUsername(String callID) {
	return callIDUsernameMap.get(callID);
    }

    public static String getCallID(String username) {
	return usernameCallIDMap.get(username);
    }

    public void messageReceived(WonderlandClientSender sender, 
	    WonderlandClientID clientID, Message message) {

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	if (message instanceof AvatarCellIDMessage) {
	    AvatarCellIDMessage msg = (AvatarCellIDMessage) message;
	    voiceChatHandler.addTransformChangeListener(msg.getCellID());
	    return;
	}

	if (message instanceof CellStatusChangeMessage) {
	    CellStatusChangeMessage msg = (CellStatusChangeMessage) message;

	    if (msg.getActive()) {
	        voiceChatHandler.addTransformChangeListener(msg.getCellID());
	    } else {
	        voiceChatHandler.removeTransformChangeListener(msg.getCellID());
	    }

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

	    sender.send(clientID, msg);
	    return;
	}

	if (message instanceof GetUserListMessage) {
	    sendUserList(sender);
	    return;
	}

	if (message instanceof PlaceCallMessage) {
	    logger.fine("Got place call message from " + clientID);

	    PlaceCallMessage msg = (PlaceCallMessage) message;

	    CallSetup setup = new CallSetup();

	    CallParticipant cp = new CallParticipant();

	    setup.cp = cp;
	    //setup.listener = this;

	    String callID = msg.getSoftphoneCallID();
	
	    vm.addCallStatusListener(this, callID);
	
	    logger.fine("callID " + callID);

	    if (callID == null) {
	        logger.fine("Can't place call to " + msg.getSipURL()
		    + ".  No cell for " + callID);
		return;
	    }

	    
	    String username = UserManager.getUserManager().getUser(clientID).getUsername();

	    callIDUsernameMap.put(callID, username);
	    usernameCallIDMap.put(username, callID);

	    cp.setCallId(callID);
	    cp.setName(username);
            cp.setPhoneNumber(msg.getSipURL());
            cp.setConferenceId(vm.getVoiceManagerParameters().conferenceId);
            cp.setVoiceDetection(true);
            cp.setDtmfDetection(true);
            cp.setVoiceDetectionWhileMuted(true);
            cp.setHandleSessionProgress(true);
            cp.setJoinConfirmationTimeout(0);
	    cp.setCallAnsweredTreatment(null);

	    senderCallIDMap.put(sender, callID);

	    try {
	        setupCall(callID, setup, msg.getX(), 
		    msg.getY(), msg.getZ(), msg.getDirection());
	    } catch (IOException e) {
		logger.warning("Unable to place call " + cp + " " + e.getMessage());
		senderCallIDMap.remove(sender);
		callIDUsernameMap.remove(callID);
	    }

	    sendUserList(sender, username + " (Invited)");
	    return;
	}

	if (message instanceof MuteCallMessage) {
	    MuteCallMessage msg = (MuteCallMessage) message;

	    //sender.send(new MuteCallMessage(msg.getCallID(), getUsername(msg.getCallID()), 
	    //	msg.isMuted()));
	    Call call = vm.getCall(msg.getCallID());

	    if (call == null) {
		logger.warning("Unable to mute/unmute call " + call.getId());
		return;
	    }	

	    try {
	        call.mute(msg.isMuted());
	    } catch (IOException e) {
		logger.warning("Unable to mute/unmute call " + call.getId() + ": "
		    + e.getMessage());
	    }

	    String username = call.getSetup().cp.getName();

	    if (msg.isMuted()) {
		username = "[" + username + "]";
	    }
	    sendUserList(sender, username);
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
	    voiceChatHandler.processVoiceChatMessage(sender, clientID, 
		(VoiceChatMessage) message);
	    return;
	}

        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void sendUserList(WonderlandClientSender sender) {
	sendUserList(sender, null);
    }

    private void sendUserList(WonderlandClientSender sender, String user) {
	ArrayList<String> userList = getUserList(user);

        GetUserListMessage message = new GetUserListMessage();
	message.setUserList(getUserList(user));
	sender.send(message);
    }

    private ArrayList<String> getUserList() {
	return getUserList(null);
    }

    private ArrayList<String> getUserList(String user) {
	UserManager userManager = UserManager.getUserManager();

	Iterator<ManagedReference<UserMO>> it = userManager.getAllUsers().iterator();
	    
	ArrayList<String> userList= new ArrayList();

	while (it.hasNext()) {
	    UserMO userMO = it.next().get();

	    String username = userMO.getIdentity().getUsername();

	    if (user != null && user.indexOf(username) >= 0) {
		userList.add(user);
		continue;
	    }

	    String callID = getCallID(username);

	    if (callID != null) {
		Call call = AppContext.getManager(VoiceManager.class).getCall(callID);

		if (call != null && call.isMuted()) {
		    username = "[" + username + "]";
		}
	    }

	    userList.add(username);
	}

	addOutworlders(userList);
	userList.remove("servermanager");  // not a real user.
	return userList;
    }

    private void addOutworlders(ArrayList<String> userList) {
	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	Player[] players = vm.getPlayers();

	for (int i = 0; i < players.length; i++) {
	    Player p = players[i];

	    if (p.getSetup().isLivePlayer == false) {
		continue;
	    }
	    
	    if (p.getSetup().isOutworlder== false) {
		continue;
	    }
	    
	    Call call = p.getCall();

	    if (call == null) {
		continue;
	    }

	    String name = call.getSetup().cp.getName();

	    if (userList.contains(name)) {
		return;
	    }

	    userList.add(name + " (Outworlder)");
	}
    }

    private void setupCall(String callID, CallSetup setup, double x, 
	    double y, double z, double direction) throws IOException {

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	Player p = vm.getPlayer(callID);

	Call call;

        call = vm.createCall(callID, setup);

	callID = call.getId();

        PlayerSetup ps = new PlayerSetup();

	if (p == null) {
            ps.x = x;
            ps.y = y;
            ps.z = z;
	} else {
	    ps.x = p.getSetup().x;
	    ps.y = p.getSetup().y;
	    ps.z = p.getSetup().z;
	}

        ps.orientation = direction;
        ps.isLivePlayer = true;

	Player player = vm.createPlayer(callID, ps);

        call.setPlayer(player);
        player.setCall(call);

        vm.getVoiceManagerParameters().livePlayerAudioGroup.addPlayer(player,
            new AudioGroupPlayerInfo(true, 
	    AudioGroupPlayerInfo.ChatType.PUBLIC));

        AudioGroupPlayerInfo info = new AudioGroupPlayerInfo(false,
            AudioGroupPlayerInfo.ChatType.PUBLIC);

        info.defaultSpeakingAttenuation = 0;

        vm.getVoiceManagerParameters().stationaryPlayerAudioGroup.addPlayer(
	    player, info);
    }

    public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
//        throw new UnsupportedOperationException("Not supported yet.");

	String callID = senderCallIDMap.get(sender);

	if (callID == null) {
	    logger.warning("Unable to find callID for sender " + sender);
	    return;
	}

	senderCallIDMap.remove(sender);
	callIDUsernameMap.remove(callID);

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

	CommsManager cm = WonderlandContext.getCommsManager();

	WonderlandClientSender sender = cm.getSender(AudioManagerConnectionType.CONNECTION_TYPE);

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	switch (code) {
	case CallStatus.ESTABLISHED:
	    Call call = vm.getCall(callId);

	    if (call == null) {
		logger.warning("Couldn't find call for " + callId);
		return;
	    }

	    Player player = call.getPlayer();

	    if (player == null) {
		logger.warning("Couldn't find player for " + call);
		return;
	    }

	    vm.dump("all");
	    player.setPrivateMixes(true);
	    sendUserList(sender);
	    break;

        case CallStatus.STARTEDSPEAKING:
	    sender.send(new SpeakingMessage(callId, getUsername(callId), true));
	    sendUserList(sender, getUsername(callId) + "...");
            break;

        case CallStatus.STOPPEDSPEAKING:
	    sender.send(new SpeakingMessage(callId, getUsername(callId), false));
	    sendUserList(sender, getUsername(callId));
            break;

	case CallStatus.ENDED:
	    ArrayList<String> userList = getUserList();

	    userList.remove(getUsername(callId));

            GetUserListMessage message = new GetUserListMessage();

	    message.setUserList(userList);
	    sender.send(message);
            break;
	  
	case CallStatus.BRIDGE_OFFLINE:
            logger.info("Bridge offline: " + status);
		// XXX need a way to tell the voice manager to reset all of the private mixes.
		Call c = vm.getCall(callId);

	    if (callId == null || callId.length() == 0) {
                /*
                 * After the last BRIDGE_OFFLINE notification
                 * we have to tell the voice manager to restore
                 * all the pm's for live players.
                 */
                logger.fine("Restoring private mixes...");
	    } else {
		if (c == null) {
		    logger.warning("No call for " + callId);
		    break;
		}

		Player p = c.getPlayer();

		if (p == null) {
		    logger.warning("No player for " + callId);
		    break;
		}

		try {
		    c.end(true);
		} catch (IOException e) {
		    logger.warning("Unable to end call " + callId);
		}

		try {
		    setupCall(callId, c.getSetup(), -p.getX(), p.getY(), p.getZ(), 
		        p.getOrientation());
		} catch (IOException e) {
		    logger.warning("Unable to setupCall " + c + " "
			+ e.getMessage());
		}
	    }

            break;
        }
    }

}

