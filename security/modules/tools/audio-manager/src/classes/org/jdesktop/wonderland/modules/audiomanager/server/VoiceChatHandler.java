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

import java.lang.reflect.Method;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;

import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatBusyMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatEndMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoResponseMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinAcceptedMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatLeaveMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinRequestMessage;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.UserMO;

import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMO;

import org.jdesktop.wonderland.server.cell.view.AvatarCellMO;

import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.CommsManagerFactory;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;

import java.util.logging.Logger;

import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupSetup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VirtualPlayer;
import com.sun.mpk20.voicelib.app.VoiceManager;

import java.io.IOException;
import java.io.Serializable;

import com.jme.math.Vector3f;

/**
 * Test listener, will eventually support Audio Manager
 * 
 * @author jprovino
 */
public class VoiceChatHandler implements Serializable {

    private static final Logger logger =
	Logger.getLogger(VoiceChatHandler.class.getName());
    
    public VoiceChatHandler() {
    }

    public void processVoiceChatMessage(WonderlandClientSender sender, 
	    WonderlandClientID clientID, VoiceChatMessage message) {

	String group = message.getGroup();

	if (message instanceof VoiceChatInfoRequestMessage) {
	    sendVoiceChatInfo(sender, clientID, group);
	    return;
	}

	if (message instanceof VoiceChatBusyMessage) {
	    VoiceChatBusyMessage msg = (VoiceChatBusyMessage) message;

	    CommsManager cm = CommsManagerFactory.getCommsManager();
	    
            WonderlandClientID id = cm.getWonderlandClientID(msg.getCaller().clientID);

            if (id == null) {
                logger.warning("No WonderlandClientID for caller "
                    + msg.getCaller());
                return;
            }

	    sendVoiceChatBusyMessage(sender, id, msg);
	    return;
	}

        VoiceManager vm = AppContext.getManager(VoiceManager.class);

	AudioGroup audioGroup = vm.getAudioGroup(group);

	if (message instanceof VoiceChatLeaveMessage) {
	    if (audioGroup == null) {
		logger.info("audioGroup is null");
		return;
	    }

	    VoiceChatLeaveMessage msg = (VoiceChatLeaveMessage) message;

	    Player player = vm.getPlayer(msg.getCallee().callID);

	    if (player == null) {
		logger.warning("No player for " + msg.getCallee());

	        if (audioGroup.getNumberOfPlayers() == 0) {
		    endVoiceChat(vm, audioGroup);  // cleanup
	        }
		return;
	    }
	    
	    removePlayerFromAudioGroup(audioGroup, player);

	    if (audioGroup.getNumberOfPlayers() <= 1) {
		endVoiceChat(vm, audioGroup);
	    }

	    vm.dump("all");
	    return;
	}

	if (message instanceof VoiceChatEndMessage) {
	    if (audioGroup == null) {
		logger.info("audioGroup is null");
		return;
	    }

	    endVoiceChat(vm, audioGroup);
	    vm.dump("all");
	    return;
	}

	if (message instanceof VoiceChatJoinAcceptedMessage == true) {
	    if (audioGroup == null) {
		logger.warning("Audio group " + group + " no longer exists");
		return;
	    }

	    VoiceChatJoinAcceptedMessage msg = (VoiceChatJoinAcceptedMessage) message;

	    addPlayerToChatGroup(vm, audioGroup, msg.getCallee(), msg.getChatType());
	    return;
	}

	if (message instanceof VoiceChatJoinMessage == false) {
	    logger.warning("Invalid message type " + message);
	    return;
	}

	VoiceChatJoinMessage msg = (VoiceChatJoinMessage) message;

	if (audioGroup == null) {
	    AudioGroupSetup setup = new AudioGroupSetup();
	    setup.spatializer = new FullVolumeSpatializer();
	    setup.spatializer.setAttenuator(DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME);
	    audioGroup = vm.createAudioGroup(group, setup);
	}

	PresenceInfo[] calleeList = msg.getCalleeList();

	PresenceInfo caller = msg.getCaller();

	boolean added = addPlayerToChatGroup(vm, audioGroup, caller, msg.getChatType());

	if (added == false && calleeList.length == 0) {
	    endVoiceChat(vm, audioGroup);
	    return;
	}

	logger.fine("Request to join AudioGroup " + group + " caller " + caller);

	for (int i = 0; i < calleeList.length; i++) {
	    PresenceInfo info = calleeList[i];

	    CellID cellID = calleeList[i].cellID;

	    logger.fine("  callee:  " + calleeList[i]);

	    String callID = calleeList[i].callID;

	    Player player = vm.getPlayer(callID);

	    if (player == null) {
		logger.warning("No player for callID " + callID);
		continue;
	    }

	    if (audioGroup.getPlayerInfo(player) != null) {
		logger.fine("Player " + info
		    + " is already in audio group " + audioGroup);
		continue;
	    }

            WonderlandClientID id = 
	       CommsManagerFactory.getCommsManager().getWonderlandClientID(info.clientID);

	    if (id == null) {
		logger.warning("No WonderlandClientID for " + info);
		continue;
	    }

	    Call call = player.getCall();

	    if (call != null) {
		try {
		    call.playTreatment("audioGroupInvite.au");
		} catch (IOException e) {
		    logger.warning("Unable to play audioGroupInvite.au:  "
			+ e.getMessage());
		}
	    }

	    logger.info("Asking " + info + " to join audio group " 
		+ group + " chatType " + msg.getChatType());

	    /*
	     * put callee first in the list
	     */
	    PresenceInfo pi = calleeList[0];

	    calleeList[0] = info;

	    calleeList[i] = info;

	    requestPlayerJoinAudioGroup(sender, id, group, caller,
		calleeList, msg.getChatType());
	}

	vm.dump("all");
	return;
    }

    private ConcurrentHashMap<String, PresenceInfo> playerMap = new ConcurrentHashMap();

    private boolean addPlayerToChatGroup(VoiceManager vm, AudioGroup audioGroup,
	    PresenceInfo info, VoiceChatMessage.ChatType chatType) {

	String callID = info.callID;

	Player player = vm.getPlayer(callID);

	if (player == null) {
	    logger.warning("No player for " + callID);
	    return false;
	}

	if (audioGroup.getPlayerInfo(player) != null) {
	    logger.fine("Player " + info
		+ " is already in audio group " + audioGroup);

	    return true;
	}

	AudioGroupPlayerInfo.ChatType type;

	if (chatType.equals(VoiceChatMessage.ChatType.SECRET)) {
	    type = AudioGroupPlayerInfo.ChatType.SECRET;
	} else if (chatType.equals(VoiceChatMessage.ChatType.PRIVATE)) {
	    type = AudioGroupPlayerInfo.ChatType.PRIVATE;
	} else {
	    type = AudioGroupPlayerInfo.ChatType.PUBLIC;
	}
	
	audioGroup.addPlayer(player, new AudioGroupPlayerInfo(true, type));

	/*
	 * XXX All of the virtual player work should be moved into AudioGroupImpl.
	 * The problem is figuring out how to create the orbs.
	 * Maybe a virtual player listener is needed.
	 */

	Player[] players = audioGroup.getPlayers();

	/*
	 * If this is a public chat, we need to create virtual players
	 */
	for (int i = 0; i < players.length; i++) {
	    Player p = players[i];

	    AudioGroupPlayerInfo audioGroupPlayerinfo = audioGroup.getPlayerInfo(p);

	    if (audioGroupPlayerinfo.chatType == AudioGroupPlayerInfo.ChatType.PUBLIC) {
	        createVirtualPlayers(audioGroup);
	    } else {
	        removeVirtualPlayers(audioGroup, p);
	    }
	}

	playerMap.put(callID, info);
	return true;
    }

    private void requestPlayerJoinAudioGroup(WonderlandClientSender sender,
	    WonderlandClientID clientID, String group, PresenceInfo caller, 
	    PresenceInfo[] calleeList, VoiceChatMessage.ChatType chatType) {

	VoiceChatMessage message = new VoiceChatJoinRequestMessage(group, 
	    caller, calleeList, chatType);

        sender.send(clientID, message);
    }

    private void sendVoiceChatBusyMessage(WonderlandClientSender sender,
	    WonderlandClientID clientID, VoiceChatBusyMessage message) {

	logger.fine(message.getCallee() + " sending busy message to " 
	    + message.getCaller());

        sender.send(clientID, message);
    }

    private void sendVoiceChatInfo(WonderlandClientSender sender,
	    WonderlandClientID clientID, String group) {

	String chatInfo = "";

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	AudioGroup audioGroup = vm.getAudioGroup(group);

	if (audioGroup == null) {
	    logger.warning("Can't find audio group " + group);
	    return;
	}

	ArrayList<PresenceInfo> chatters = new ArrayList();

	Player[] players = audioGroup.getPlayers();

	for (int i = 0; i < players.length; i++) {
	    PresenceInfo info = playerMap.get(players[i].getId());
	
	    if (info == null) {
		logger.warning("Unable to find " + players[i].getId());
		continue;
	    }
		
	    chatters.add(info);
	}

        VoiceChatMessage msg = new VoiceChatInfoResponseMessage(group, 
	    chatters.toArray(new PresenceInfo[0]));

        sender.send(clientID, msg);
    }

    private void removePlayerFromAudioGroups(String callId) {
        VoiceManager vm = AppContext.getManager(VoiceManager.class);

	Player player = vm.getPlayer(callId);

	if (player == null) {
	    logger.warning("Can't find player for callId " + callId);
	    return;
	}

	AudioGroup[] audioGroups = player.getAudioGroups().toArray(new AudioGroup[0]);

	for (int i = 0; i < audioGroups.length; i++) {
	    removePlayerFromAudioGroup(audioGroups[i], player);
	}
    }

    private void removePlayerFromAudioGroup(AudioGroup audioGroup, 
	    Player player) {

	audioGroup.removePlayer(player);

	// XXX If a player can be in more than one public audio group
	// then the player must have a separate list of virtual calls
	// for each audio group.

	removeVirtualPlayers(audioGroup, player);
    }

    private void createVirtualPlayers(AudioGroup audioGroup) {
	Player[] players = audioGroup.getPlayers();

	for (int i = 0; i < players.length; i++) {
	    Player p = players[i];

	    if (p.getSetup().isLivePlayer == false) {
		continue;
	    }

	    if (audioGroup.getPlayerInfo(p).chatType != 
		    AudioGroupPlayerInfo.ChatType.PUBLIC) {

		continue;
	    }

	    logger.fine("Creating virtual players for " + p);
	    createVirtualPlayer(audioGroup, p);
	}
    }

    private void createVirtualPlayer(AudioGroup audioGroup, Player player) {
        VoiceManager vm = AppContext.getManager(VoiceManager.class);

	Player[] players = audioGroup.getPlayers();

	for (int i = 0; i < players.length; i++) {
            Player p = players[i];

	    if (player.equals(p)) {
		continue;
	    }

	    if (p.getSetup().isLivePlayer == false) {
		continue;
	    }

	    if (p.getSetup().isVirtualPlayer) {
	 	continue;
	    }

	    if (audioGroup.getPlayerInfo(p).chatType  != 
		    AudioGroupPlayerInfo.ChatType.PUBLIC) {

		continue;
	    }

	    String callId = "V-" + player.getId() + "-to-" + p.getId();

	    if (vm.getPlayer(callId) != null) {
		logger.warning("Player " + callId + " already exists");
		continue;
	    }

	    Call call = player.getCall();

	    PlayerSetup setup = new PlayerSetup();
	    double scale = vm.getVoiceManagerParameters().scale;
	    setup.x = p.getX() * scale;
	    setup.y = p.getY() * scale;
	    setup.z = p.getZ() * scale;
	    setup.orientation = p.getOrientation();
	    setup.isLivePlayer = true;
	    setup.isVirtualPlayer = true;

	    logger.fine("Created virtual player " + callId);

	    Player vp = vm.createPlayer(callId, setup);

	    vp.setCall(call);

	    vm.getVoiceManagerParameters().livePlayerAudioGroup.addPlayer(vp, 
		new AudioGroupPlayerInfo(true, AudioGroupPlayerInfo.ChatType.PUBLIC));

	    String phoneNumber = call.getSetup().cp.getPhoneNumber();

	    logger.info("Spawning orb at " + p);

	    //CellGLO cellGLO = spawnOrb(callId, phoneNumber, p.getX(), p.getY(), p.getZ());

	    //player.addVirtualPlayer(new VirtualPlayer(vp, cellGLO.getGLOName(), p));
	}
    }

    private void removeVirtualPlayers(AudioGroup audioGroup, Player player) {
	VirtualPlayer[] virtualPlayersToRemove = player.getVirtualPlayers();

	removeOrbs(virtualPlayersToRemove);

	for (int i = 0; i < virtualPlayersToRemove.length; i++) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    vm.removePlayer(virtualPlayersToRemove[i].player);
	    vm.getVoiceManagerParameters().livePlayerAudioGroup.removePlayer(
		virtualPlayersToRemove[i].player);
	    player.removeVirtualPlayer(virtualPlayersToRemove[i]);
	}

	/* 
	 * Now remove virtual players that other players have for us.
	 */
        VoiceManager vm = AppContext.getManager(VoiceManager.class);

	ArrayList<VirtualPlayer> othersToRemove = new ArrayList();

	Player[] players = audioGroup.getPlayers();

	for (int i = 0; i < players.length; i++) {
	    Player p = players[i];

	    if (p.equals(player)) {
		continue;
	    }

	    VirtualPlayer[] virtualPlayers = p.getVirtualPlayers();

	    for (int j = 0; j < virtualPlayers.length; j++) {
		VirtualPlayer virtualPlayer = virtualPlayers[j];

		logger.fine("possible vp for " + virtualPlayers[j] + " at " + player
		    + " vp.call " + virtualPlayer.realPlayer);

		if (virtualPlayer.realPlayer.equals(player)) {
		    othersToRemove.add(virtualPlayer);
		    p.removeVirtualPlayer(virtualPlayer);
		    vm.removePlayer(virtualPlayer.player);
		}
	    }
	}

	logger.fine("othersToRemoveSize " + othersToRemove.size());

	removeOrbs(othersToRemove.toArray(new VirtualPlayer[0]));
    }

    private Method getAvatarOrbCellGLOMethod(String methodName) {
        String cellType =
            "org.jdesktop.wonderland.modules.orb.server.cell.OrbCellMO";

	Class avatarOrbCellGLOClass = null;

	try {
	    avatarOrbCellGLOClass = Class.forName(cellType);
	} catch (ClassNotFoundException e) {
	    logger.warning("Class not found:  " + cellType);
	    return null;
	}

	Method[] methods = avatarOrbCellGLOClass.getMethods();

	for (int i = 0; i < methods.length; i++) {
	    Method m = methods[i];

            if (m.getName().equals(methodName)) {
		return m;
	    }
	}

	return null;
    }

    private void removeOrbs(VirtualPlayer[] virtualPlayers) {
	Method endCall = getAvatarOrbCellGLOMethod("endCall");

	if (endCall == null) {
	    logger.warning("can't find endCall() in avatarOrbCellGLO class!");
	    return;
	}
	
        VoiceManager vm = AppContext.getManager(VoiceManager.class);

	for (int i = 0; i < virtualPlayers.length; i++) {
	    //CellGLO cellGLO =
	    //	AppContext.getDataManager().getBinding(virtualPlayers[i].cellName, 
  	    //	CellGLO.class);

	    //vm.removeCallStatusListener((ManagedCallStatusListener) cellGLO);

	    //try {
	    //	endCall.invoke(cellGLO);
	    //} catch (Exception e) {
	    //	logger.fine("Can't tell orb to end call:  " + e.getMessage());
	    //} 

	    logger.fine("Detaching orb " + virtualPlayers[i].player);
	}
    }

    private void endVoiceChat(VoiceManager vm, AudioGroup audioGroup) {
	Player[] players = audioGroup.getPlayers();

	for (int i = 0; i < players.length; i++) {
	    Player player = players[i];
	    
	    removePlayerFromAudioGroup(audioGroup, player);
	}

	vm.removeAudioGroup(audioGroup);
    }

    private void moveVirtualPlayers(Player player, double x, double y, double z, 
	    double direction) {

	AudioGroup[] audioGroups = player.getAudioGroups().toArray(new AudioGroup[0]);

	for (int i = 0 ; i < audioGroups.length; i++) {

	    Player[] players = audioGroups[i].getPlayers();

	    for (int j = 0; j < players.length; j++) {
	        Player p = players[j];

		VirtualPlayer[] virtualPlayers = p.getVirtualPlayers();
	
		for (int k = 0; k < virtualPlayers.length; k++) {
		    if (virtualPlayers[k].realPlayer.equals(player)) {
			logger.fine("Moving " + virtualPlayers[k] + " to " + player);
			moveVirtualPlayer(virtualPlayers[k], x, y, z, direction);
		    }
		}
	    }
	}
    }

    private void moveVirtualPlayer(VirtualPlayer virtualPlayer, double x, double y, double z,
	    double direction) {

	Method avatarMoved = getAvatarOrbCellGLOMethod("avatarMoved");

	if (avatarMoved == null) {
	    logger.warning("Can't find avatarMoved method!");
	    return;
	}

	//CellGLO cellGLO = AppContext.getDataManager().getBinding(virtualPlayer.cellName, 
	//    CellGLO.class);

	//AvatarCellMessage message = new AvatarCellMessage(cellGLO.getCellID(),
	//    position, direction);

	//logger.fine(virtualPlayer + " cellGLO " + cellGLO + " cell name " 
	//    + virtualPlayer.cellName);

	//try {
	//    avatarMoved.invoke(cellGLO, message);
	//} catch (Exception e) {
	//    logger.fine("Can't tell orb to move:  " + e.getMessage());
	//    e.printStackTrace();
	//}
    }

}
