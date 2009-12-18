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

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupListener;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.AudioGroupSetup;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.Spatializer;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;

import java.lang.String;

import java.util.logging.Logger;

import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;

import com.jme.bounding.BoundingVolume;

import java.io.Serializable;

import org.jdesktop.wonderland.common.cell.security.ViewAction;
import org.jdesktop.wonderland.common.security.Action;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellResourceManager;
import org.jdesktop.wonderland.server.security.ActionMap;
import org.jdesktop.wonderland.server.security.Resource;
import org.jdesktop.wonderland.server.security.ResourceMap;
import org.jdesktop.wonderland.server.security.SecureTask;
import org.jdesktop.wonderland.server.security.SecurityManager;

/**
 * A server cell that provides a microphone proximity listener
 * @author jprovino
 */
public class MicrophoneTalkAreaProximityListener implements ProximityListenerSrv, 
	ManagedObject, Serializable {

    private static final Logger logger =
            Logger.getLogger(MicrophoneTalkAreaProximityListener.class.getName());

    private CellID cellID;
    private String name;
    private double volume;

    public MicrophoneTalkAreaProximityListener(CellMO cellMO, String name, double volume) {
	cellID = cellMO.getCellID();
        this.name = name;
	this.volume = volume;
    }

    public void viewEnterExit(boolean entered, CellID cellID,
            CellID viewCellID, BoundingVolume proximityVolume,
            int proximityIndex) {

	logger.info("viewEnterExit talk area:  " + entered + " cellID " + cellID
	    + " viewCellID " + viewCellID);

	System.out.println("viewEnterExit talk area:  " + entered + " cellID " + cellID
	    + " viewCellID " + viewCellID + " bounds " + proximityVolume);

	String callId = CallID.getCallID(viewCellID);

	if (entered) {
	    talkAreaEntered(callId);
	} else {
	    talkAreaExited(callId);
	}
    }

    private void talkAreaEntered(String callId) {
	logger.info(callId + " entered talk area " + name);

        // get the security manager
        SecurityManager security = AppContext.getManager(SecurityManager.class);
        CellResourceManager crm = AppContext.getManager(CellResourceManager.class);

        // create a request
        Action viewAction = new ViewAction();
        Resource resource = crm.getCellResource(this.cellID);
        if (resource != null) {
            // there is security on this cell perform the enter notification
            // securely
            ActionMap am = new ActionMap(resource, new Action[] { viewAction });
            ResourceMap request = new ResourceMap();
            request.put(resource.getId(), am);

            // perform the security check
            security.doSecure(request, new CellEnteredTask(resource.getId(), callId));
        } else {
            // no security, just make the call directly
            cellEntered(callId);
        }
    }

    private class CellEnteredTask implements SecureTask, Serializable {
        private String resourceID;
        private String callId;

        public CellEnteredTask(String resourceID, String callId) {
            this.resourceID = resourceID;
            this.callId = callId;
        }

        public void run(ResourceMap granted) {
            ActionMap am = granted.get(resourceID);
            if (am != null && !am.isEmpty()) {
                // request was granted -- the user has permission to enter
                cellEntered(callId);
            } else {
                logger.warning("Access denied to enter microphone talk area.");
            }
        }
    }

    private void cellEntered(String callId) {

	VoiceManager vm = AppContext.getManager(VoiceManager.class);
	
        Player player = vm.getPlayer(callId);

	System.out.println("Talk area entered, Setting speaking volume to " 
	    + volume + " for " + player);

        if (player == null) {
            logger.warning("Can't find player for " + callId);
            return;
        }

        AudioGroup audioGroup = vm.getAudioGroup(name);

	if (audioGroup == null) {
	    CellMO cellMO = CellManagerMO.getCellManager().getCell(cellID);

	    MicrophoneComponentMO microphoneComponentMO = 
		cellMO.getComponent(MicrophoneComponentMO.class);

	    audioGroup = microphoneComponentMO.createAudioGroup(name);
	}

	if (audioGroup.getPlayerInfo(player) == null) {
            audioGroup.addPlayer(player, new AudioGroupPlayerInfo(true,
                AudioGroupPlayerInfo.ChatType.PUBLIC));
	} else {
	    audioGroup.setSpeaking(player, true);
	}

	audioGroup.setSpeakingAttenuation(player, volume);
    }

    private void talkAreaExited(String callId) {
	logger.info(callId + " exited talk area " + name);

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

        AudioGroup audioGroup = vm.getAudioGroup(name);

	if (audioGroup == null) {
	    logger.info("Audio group doesn't exit:  " + name);
	    return;
	}

        Player player = vm.getPlayer(callId);

        if (player == null) {
            logger.warning("Can't find player for " + callId);
            return;
        }

	System.out.println("Talk area exit, Player is no longer speaking in audio group. " 
	    + player);

	audioGroup.setSpeaking(player, false);
	audioGroup.setSpeakingAttenuation(player, volume);
    }

}
