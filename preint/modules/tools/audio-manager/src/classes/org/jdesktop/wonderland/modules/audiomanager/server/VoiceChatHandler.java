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

import java.lang.reflect.Method;

import java.util.Properties;

import java.util.ArrayList;
import java.util.Enumeration;

import java.util.concurrent.ConcurrentHashMap;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AvatarCellIDMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.DisconnectCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlaceCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage;

import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMO;

import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;

import java.io.Serializable;
import java.util.logging.Logger;

import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;

import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupSetup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.BridgeInfo;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VirtualPlayer;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.voip.CallParticipant;

import com.sun.voip.client.connector.CallStatus;
import com.sun.voip.client.connector.CallStatusListener;

import java.io.IOException;
import java.io.Serializable;

import com.jme.math.Vector3f;

/**
 * Test listener, will eventually support Audio Manager
 * 
 * @author jprovino
 */
public class VoiceChatHandler implements TransformChangeListenerSrv, 
	Serializable {

    private static final Logger logger =
	Logger.getLogger(VoiceChatHandler.class.getName());
    
    public VoiceChatHandler() {
    }

    public void processVoiceChatMessage(WonderlandClientSender sender, 
	    VoiceChatMessage message) {

        VoiceChatMessage.ActionType action = message.getActionType();
 
	if (action == VoiceChatMessage.ActionType.GET_CHAT_INFO_REQUEST) {
	    sendVoiceChatInfo(sender, message);
	    return;
	}

	if (action == VoiceChatMessage.ActionType.BUSY) {
	    sendVoiceChatBusyMessage(sender, message);
	    return;
	}

	String group = message.getGroup();
	String caller = message.getCaller();
	String calleeList = message.getCalleeList();
	VoiceChatMessage.ChatType chatType = message.getChatType();

	logger.fine(action + " group " + group + " caller " + caller 
	    + " calleeList " + calleeList + " " + chatType);

        VoiceManager vm = AppContext.getManager(VoiceManager.class);

	AudioGroup audioGroup = vm.getAudioGroup(group);

	if (audioGroup == null) {
	    if (action == VoiceChatMessage.ActionType.LEAVE ||
		    action == VoiceChatMessage.ActionType.END) {

		logger.warning("Audio group is null");
		return;
	    }

	    if (calleeList == null || calleeList.length() == 0) {
		return;
	    }

	    AudioGroupSetup setup = new AudioGroupSetup();
	    setup.spatializer = new FullVolumeSpatializer();
	    setup.spatializer.setAttenuator(
		DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME);
	    audioGroup = vm.createAudioGroup(group, setup);
	} else {
	    if (action == VoiceChatMessage.ActionType.LEAVE) {
	        Player player = vm.getPlayer(caller);

	        if (player == null) {
		    logger.warning("No player for " + caller);
		    return;
		}
	    
		removePlayerFromAudioGroup(audioGroup, player);

		if (audioGroup.getPlayers().size() == 0) {
		    endVoiceChat(vm, audioGroup);
		}

		vm.dump("all");
		return;
	    } else if (action == VoiceChatMessage.ActionType.END) {
		endVoiceChat(vm, audioGroup);
		vm.dump("all");
		return;
	    }

	    if (calleeList.length() == 0) {
		vm.removeAudioGroup(group);
		return;
	    }
	}

	if (action != VoiceChatMessage.ActionType.JOIN) {
	    logger.warning("Invalid action type " + action);
	    return;
	}

	String players[] = calleeList.split(" ");

	boolean added = addPlayerToChatGroup(vm, audioGroup, caller, chatType);

	if (added == false && players.length == 0) {
	    endVoiceChat(vm, audioGroup);
	    return;
	}

	for (int i = 0; i < players.length; i++) {
	    if (players[i].length() > 0) {
		Player player = vm.getPlayer(players[i]);

		if (audioGroup.getPlayerInfo(player) != null) {
		    logger.fine("Player " + players[i] 
			+ " is already in audio group " + audioGroup);
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

	        requestPlayerJoinAudioGroup(sender, group, null,
		    players[i], calleeList, chatType);
	    }
	}

	vm.dump("all");
	return;
    }

    private boolean addPlayerToChatGroup(VoiceManager vm, AudioGroup audioGroup,
	    String callee, VoiceChatMessage.ChatType chatType) {

	ConcurrentHashMap<Player, AudioGroupPlayerInfo> players = 
	    audioGroup.getPlayers();

	Player player = vm.getPlayer(callee);

	if (player == null) {
	    logger.warning("No player for " + callee);
	    return false;
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

	/*
	 * If this is a public chat, we need to create virtual players
	 */
	Enumeration<Player> pe = players.keys();

	while (pe.hasMoreElements()) {
	    player = pe.nextElement();

	    AudioGroupPlayerInfo info = audioGroup.getPlayerInfo(player);

	    if (info.chatType == AudioGroupPlayerInfo.ChatType.PUBLIC) {
	        createVirtualPlayers(audioGroup);
	    } else {
	        removeVirtualPlayers(audioGroup, player);
	    }
	}

	return true;
    }

    private void requestPlayerJoinAudioGroup(WonderlandClientSender sender,
	    String group, String caller, String callee, String calleeList, 
	    VoiceChatMessage.ChatType chatType) {

	logger.fine("Asking " + callee + " to join audio group " + group + " chatType " 
	    + chatType);

	VoiceChatMessage message = new VoiceChatMessage(
	     VoiceChatMessage.ActionType.REQUEST_TO_JOIN, null, group, 
		caller, calleeList, chatType);

        sender.send(message);
    }

    private void sendVoiceChatBusyMessage(WonderlandClientSender sender,
	    VoiceChatMessage message) {

	logger.fine("Sending busy message to " + message.getCaller());

	VoiceChatMessage msg = new VoiceChatMessage(
	     VoiceChatMessage.ActionType.BUSY, null,
	     message.getGroup(), message.getCaller(), message.getCalleeList(),
	     message.getChatType());

        sender.send(msg);
    }

    private void sendVoiceChatInfo(WonderlandClientSender sender,
	    VoiceChatMessage message) {

	String chatInfo = "";

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	AudioGroup audioGroup = vm.getAudioGroup(message.getGroup());

	if (audioGroup != null) {
	    ConcurrentHashMap<Player, AudioGroupPlayerInfo> players =
	        audioGroup.getPlayers();

	    Enumeration<Player> pk = players.keys();

            while (pk.hasMoreElements()) {
                Player player = pk.nextElement();

	        chatInfo += player.getId() + " ";
	    }
	}

        VoiceChatMessage msg = new VoiceChatMessage(
             VoiceChatMessage.ActionType.GET_CHAT_INFO_RESPONSE, null,
	     message.getGroup());
	    
	msg.setChatInfo(chatInfo);

        sender.send(msg);
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
	ConcurrentHashMap<Player, AudioGroupPlayerInfo> players = 
	    audioGroup.getPlayers();

	Enumeration<Player> pe = players.keys();

	while (pe.hasMoreElements()) {
	    Player p = pe.nextElement();

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

	ConcurrentHashMap<Player, AudioGroupPlayerInfo> players = 
	    audioGroup.getPlayers();

	Enumeration<Player> pe = players.keys();

	while (pe.hasMoreElements()) {
	    Player p = pe.nextElement();

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
	    double scale = vm.getScale();
	    setup.x = p.getX() * scale;
	    setup.y = p.getY() * scale;
	    setup.z = p.getZ() * scale;
	    setup.orientation = p.getOrientation();
	    setup.isLivePlayer = true;
	    setup.isVirtualPlayer = true;

	    logger.fine("Created virtual player " + callId);

	    Player vp = vm.createPlayer(callId, setup);

	    vp.setCall(call);

	    vm.getDefaultLivePlayerAudioGroup().addPlayer(vp, 
		new AudioGroupPlayerInfo(true, AudioGroupPlayerInfo.ChatType.PUBLIC));

	    String phoneNumber = call.getSetup().cp.getPhoneNumber();

	    logger.info("Spawning orb at " + p);

	    //CellGLO cellGLO = spawnOrb(callId, phoneNumber, p.getX(), p.getY(), p.getZ());

	    //player.addVirtualPlayer(new VirtualPlayer(vp, cellGLO.getGLOName(), p));
	}
    }

    private void removeVirtualPlayers(AudioGroup audioGroup, Player player) {
	VirtualPlayer[] virtualPlayersToRemove = 
	    player.getVirtualPlayers().toArray(new VirtualPlayer[0]);

	removeOrbs(virtualPlayersToRemove);

	for (int i = 0; i < virtualPlayersToRemove.length; i++) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    vm.removePlayer(virtualPlayersToRemove[i].player.getId());
	    vm.getDefaultLivePlayerAudioGroup().removePlayer(
		virtualPlayersToRemove[i].player);
	    player.removeVirtualPlayer(virtualPlayersToRemove[i]);
	}

	/* 
	 * Now remove virtual players that other players have for us.
	 */
        VoiceManager vm = AppContext.getManager(VoiceManager.class);

	ArrayList<VirtualPlayer> othersToRemove = new ArrayList();

	ConcurrentHashMap<Player, AudioGroupPlayerInfo> players = 
	    audioGroup.getPlayers();

	Enumeration<Player> keys = players.keys();

	while (keys.hasMoreElements()) {
	    Player p = keys.nextElement();

	    if (p.equals(player)) {
		continue;
	    }

	    VirtualPlayer[] virtualPlayers = p.getVirtualPlayers().toArray(new VirtualPlayer[0]);

	    for (int i = 0; i < virtualPlayers.length; i++) {
		VirtualPlayer virtualPlayer = virtualPlayers[i];

		logger.fine("possible vp for " + virtualPlayers[i] + " at " + player
		    + " vp.call " + virtualPlayer.realPlayer);

		if (virtualPlayer.realPlayer.equals(player)) {
		    othersToRemove.add(virtualPlayer);
		    p.removeVirtualPlayer(virtualPlayer);
		    vm.removePlayer(virtualPlayer.player.getId());
		}
	    }
	}

	logger.fine("othersToRemoveSize " + othersToRemove.size());

	removeOrbs(othersToRemove.toArray(new VirtualPlayer[0]));
    }

    private Method getAvatarOrbCellGLOMethod(String methodName) {
        String cellType =
            "com.sun.labs.mpk20.avatarorb.server.cell.AvatarOrbCellGLO";

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
	Method callEnded = getAvatarOrbCellGLOMethod("callEnded");

	if (callEnded == null) {
	    logger.warning("can't find callEnded() in avatarOrbCellGLO class!");
	    return;
	}
	
        VoiceManager vm = AppContext.getManager(VoiceManager.class);

	for (int i = 0; i < virtualPlayers.length; i++) {
	    //CellGLO cellGLO =
	    //	AppContext.getDataManager().getBinding(virtualPlayers[i].cellName, 
  	    //	CellGLO.class);

	    //vm.removeCallStatusListener((ManagedCallStatusListener) cellGLO);

	    //try {
	    //	callEnded.invoke(cellGLO);
	    //} catch (Exception e) {
	    //	logger.fine("Can't tell orb to end call:  " + e.getMessage());
	    //} 

	    logger.fine("Detaching orb " + virtualPlayers[i].player);
	}
    }

    private void endVoiceChat(VoiceManager vm, AudioGroup audioGroup) {
	ConcurrentHashMap<Player, AudioGroupPlayerInfo> players = 
	    audioGroup.getPlayers();

	Enumeration<Player> keys = players.keys();

	while (keys.hasMoreElements()) {
	    Player player = keys.nextElement();
	    
	    removePlayerFromAudioGroup(audioGroup, player);
	}

	vm.removeAudioGroup(audioGroup.getId());
    }

    private void moveVirtualPlayers(Player player, double x, double y, double z, 
	    double direction) {

	ArrayList<AudioGroup> audioGroups = player.getAudioGroups();

	for (AudioGroup audioGroup : audioGroups) {
	    ConcurrentHashMap<Player, AudioGroupPlayerInfo> players = audioGroup.getPlayers();

	    Enumeration<Player> pe = players.keys();

	    while (pe.hasMoreElements()) {
	        Player p = pe.nextElement();

		VirtualPlayer[] virtualPlayers = p.getVirtualPlayers().toArray(
		    new VirtualPlayer[0]);
	
		for (int i = 0; i < virtualPlayers.length; i++) {
		    if (virtualPlayers[i].realPlayer.equals(player)) {
			logger.fine("Moving " + virtualPlayers[i] + " to " + player);
			moveVirtualPlayer(virtualPlayers[i], x, y, z, direction);
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

    public void addTransformChangeListener(CellID cellID) {
        CellManagerMO.getCell(cellID).addTransformChangeListener(this);
    }

    public void transformChanged(ManagedReference<CellMO> cellMORef, 
	    final CellTransform localTransform, final CellTransform localToWorldTransform) {

	String clientId = cellMORef.getId().toString();

	logger.fine("localTransform " + localTransform + " world " 
	    + localToWorldTransform);

	Player player = AppContext.getManager(VoiceManager.class).getPlayer(clientId);

	if (player == null) {
	    logger.warning("got AvatarMovedMessage but can't find player");
	} else {
	    Vector3f heading = new Vector3f(0, 0, -1);

	    Vector3f angleV = heading.clone();

	    localToWorldTransform.transform(angleV);

	    double angle = heading.angleBetween(angleV);

	    Vector3f location = localToWorldTransform.getTranslation(null);
	
	    player.moved(location.getX(), location.getY(), location.getZ(), angle);
	}
    }

}
