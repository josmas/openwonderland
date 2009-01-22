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
package org.jdesktop.wonderland.modules.microphone.server.cell;

import com.sun.sgs.app.ManagedReference;

import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellClientState;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState;

import org.jdesktop.wonderland.modules.microphone.common.messages.MicrophoneEnterCellMessage;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.sgs.app.AppContext;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.server.WonderlandContext;

import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;

import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import java.io.IOException;
import java.io.Serializable;

import java.util.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.common.cell.MultipleParentException;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.server.UserManager;

import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellMOFactory;

import org.jdesktop.wonderland.modules.microphone.server.cell.MicrophoneCellMO;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.server.cell.ChannelComponentImplMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell that provides conference microphone functionality
 * @author jprovino
 */
public class MicrophoneMessageHandler implements Serializable, ComponentMessageReceiver {

    private static final Logger logger =
        Logger.getLogger(MicrophoneMessageHandler.class.getName());
     
    private ManagedReference<MicrophoneCellMO> microphoneCellMORef;

    private ManagedReference<ChannelComponentMO> channelComponentRef = null;

    private String name;

    public MicrophoneMessageHandler(MicrophoneCellMO microphoneCellMO, String name) {
	this.name = name;

	microphoneCellMORef = AppContext.getDataManager().createReference(
	        (MicrophoneCellMO) CellManagerMO.getCell(microphoneCellMO.getCellID()));

        ChannelComponentMO channelComponentMO = (ChannelComponentMO) 
	    microphoneCellMO.getComponent(ChannelComponentMO.class);

        if (channelComponentMO == null) {
            throw new IllegalStateException("Cell does not have a ChannelComponent");
	}

        channelComponentRef = AppContext.getDataManager().createReference(channelComponentMO);

        channelComponentMO.addMessageReceiver(MicrophoneEnterCellMessage.class, this);
    }

    public void done() {
	channelComponentRef.get().removeMessageReceiver(MicrophoneEnterCellMessage.class);
    }

    public void messageReceived(final WonderlandClientSender sender, 
	    final WonderlandClientID clientID, final CellMessage message) {

	MicrophoneEnterCellMessage msg = (MicrophoneEnterCellMessage) message;

	if (msg.getEntered()) {
	    cellEntered(msg);
	} else {
	    cellExited(msg);
	}
    }

    public void cellEntered(MicrophoneEnterCellMessage msg) {
	/*
	 * The avatar has entered the microphone cell.
	 * Set the public and incoming spatializers for the avatar to be 
	 * the zero volume spatializer.
	 * Set a private spatializer for the given fullVolume radius
	 * for all the other avatars in the cell.
	 * For each avatar already in the cell, set a private spatializer
	 * for this avatar.
	 */
	String callId = msg.getCellID().toString();

	MicrophoneCellServerState cellServerState = (MicrophoneCellServerState) 
	    microphoneCellMORef.get().getServerState(null);

	logger.warning(callId + " entered microphone " + name);

	VoiceManager vm = AppContext.getManager(VoiceManager.class);
	
	Player player = vm.getPlayer(callId);

	AudioGroup audioGroup = vm.getAudioGroup(name);

	audioGroup.addPlayer(player, new AudioGroupPlayerInfo(true, 
	     AudioGroupPlayerInfo.ChatType.SECRET));

	player.attenuateOtherGroups(audioGroup, 0, 0);
    }

    public void cellExited(MicrophoneEnterCellMessage msg) {
	String callId = msg.getCellID().toString();

	logger.warning(callId + " exited microphone " + name);

        VoiceManager vm = AppContext.getManager(VoiceManager.class);

        AudioGroup audioGroup = vm.getAudioGroup(name);

        if (audioGroup == null) {
	    logger.warning("Not a member of audio group " + name);
	    return;
	}

	Player player = vm.getPlayer(callId);

	audioGroup.removePlayer(player);

	player.attenuateOtherGroups(audioGroup, AudioGroup.DEFAULT_SPEAKING_ATTENUATION,
	    AudioGroup.DEFAULT_LISTEN_ATTENUATION);
    }

    public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        //TODO: consider making a subclass of AbstractComponentMessageReceiver
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
