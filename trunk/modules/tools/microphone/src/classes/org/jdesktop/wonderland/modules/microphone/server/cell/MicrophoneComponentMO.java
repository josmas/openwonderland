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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneComponentServerState;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneComponentServerState.TalkArea;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneComponentServerState.ListenArea;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneComponentServerState.MicrophoneBoundsType;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.AudioGroupSetup;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.VoiceManager;

/**
 * A server component that provides microphone functionality
 * @author jprovino
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
public class MicrophoneComponentMO extends CellComponentMO {

    private static final Logger LOGGER =
            Logger.getLogger(MicrophoneComponentMO.class.getName());
    private static final String DEFAULT_NAME = "Microphone";
    private String name = DEFAULT_NAME;
    private String currentName;
    private double volume = 1;
    private ListenArea listenArea = new ListenArea();
    private boolean showBounds = false;
    private TalkArea talkArea = new TalkArea();
    private boolean showTalkArea = false;
    private ManagedReference<MicrophoneListenAreaProximityListener> listenAreaProximityListenerRef;
    private ManagedReference<MicrophoneTalkAreaProximityListener> talkAreaProximityListenerRef;

    public MicrophoneComponentMO(CellMO cellMO) {
        super(cellMO);

        if (cellMO.getComponent(ProximityComponentMO.class) == null) {
            cellMO.addComponent(new ProximityComponentMO(cellMO));
        }

        name += "-" + cellMO.getCellID();
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void setServerState(CellComponentServerState serverState) {
        super.setServerState(serverState);

        MicrophoneComponentServerState state =
                (MicrophoneComponentServerState) serverState;

        setMyName(state);

        volume = state.getVolume();

        listenArea = state.getListenArea();

        showBounds = state.getShowBounds();

        talkArea = state.getTalkArea();

        showTalkArea = state.getShowTalkArea();

        LOGGER.info("name " + name + " volume " + volume + " fva " +
                listenArea + " aa " + talkArea);

        //System.out.println("name " + name + " volume " + volume + " fva " +
        //        listenArea + " aa " + talkArea);

        addProximityListeners(isLive());
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public CellComponentServerState getServerState(
            CellComponentServerState serverState) {
        MicrophoneComponentServerState state =
                (MicrophoneComponentServerState) serverState;

        // Create the proper server state object if it does not yet exist
        if (state == null) {
            state = new MicrophoneComponentServerState();
        }

        setMyName(state);

        state.setName(name);
        state.setVolume(volume);
        state.setListenArea(listenArea);
        state.setShowBounds(showBounds);
        state.setTalkArea(talkArea);
        state.setShowTalkArea(showTalkArea);

        return super.getServerState(state);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public CellComponentClientState getClientState(
            CellComponentClientState state, WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        // TODO: Create own client state object?
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.microphone.client.cell.MicrophoneComponent";
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void setLive(boolean live) {
        super.setLive(live);

        addProximityListeners(live);

	if (live == false && name != null) {
	    removeAudioGroup(name);
	}
    }

    private void setMyName(MicrophoneComponentServerState state) {
        if (name == null) {
            name = DEFAULT_NAME;
        } else {
            name = state.getName();
        }

        String appendName = "-" + cellRef.get().getCellID();
        if (name.indexOf(appendName) < 0) {
            name += appendName;
        }
    }

    public AudioGroup createAudioGroup(String name) {
	VoiceManager vm = AppContext.getManager(VoiceManager.class);

        AudioGroupSetup ags = new AudioGroupSetup();

        ags.spatializer = new FullVolumeSpatializer();

        ags.spatializer.setAttenuator(DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME);

	ags.removeWhenLastPlayerRemoved = false;

	System.out.println("Create audio group for " + name);

        AudioGroup newAudioGroup = vm.createAudioGroup(name, ags);

	if (currentName != null && currentName.equals(name) == false) {
	    System.out.println("Renaming audio group " + currentName
		+ " to " + name);

	    AudioGroup currentAudioGroup = vm.getAudioGroup(currentName);

	    if (currentAudioGroup == null) {
		LOGGER.warning("Can't find audio group for " + currentName);
		return newAudioGroup;
	    }

	    changeName(currentAudioGroup, newAudioGroup);

	    vm.removeAudioGroup(currentAudioGroup);
	}

	currentName = name;
	return newAudioGroup;
    }

    private void changeName(AudioGroup currentAudioGroup, AudioGroup newAudioGroup) {
	Player[] players = currentAudioGroup.getPlayers();

	for (int i = 0; i < players.length; i++) {
	    AudioGroupPlayerInfo info = currentAudioGroup.getPlayerInfo(players[i]);

	    currentAudioGroup.removePlayer(players[i]);
	    newAudioGroup.addPlayer(players[i], info);
	}
    }

    private void removeAudioGroup(String name) {
        VoiceManager vm = AppContext.getManager(VoiceManager.class);

        AudioGroup audioGroup = vm.getAudioGroup(name);

        if (audioGroup == null) {
            return;
        }

        vm.removeAudioGroup(audioGroup);
    }

    private void addProximityListeners(boolean live) {
        // Fetch the proximity component, we will need this below. If it does
        // not exist (it should), then log an error

        ProximityComponentMO component = cellRef.get().getComponent(ProximityComponentMO.class);

        if (component == null) {
            LOGGER.warning("The Microphone Component does not have a " +
                    "Proximity Component for Cell ID " + cellID);
            return;
        }

        if (listenAreaProximityListenerRef != null) {
	    System.out.println("Removing existing proximity listeners");

            MicrophoneListenAreaProximityListener listenAreaProximityListener = 
		listenAreaProximityListenerRef.get();
            component.removeProximityListener(listenAreaProximityListener);
	    listenAreaProximityListener = null;

            MicrophoneTalkAreaProximityListener talkAreaProximityListener =
                talkAreaProximityListenerRef.get();
            component.removeProximityListener(talkAreaProximityListener);
	    talkAreaProximityListener = null;
        }

        // If we are making this component live, then add a listener to the proximity component.
        if (live == true) {
            Vector3f talkOrigin = new Vector3f((float) talkArea.talkAreaOrigin.getX(),
                    (float) talkArea.talkAreaOrigin.getY(),
                    (float) talkArea.talkAreaOrigin.getZ());

            BoundingVolume[] bounds = new BoundingVolume[1];

            if (talkArea.talkAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
                bounds[0] = cellRef.get().getLocalBounds();
            } else if (talkArea.talkAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
                bounds[0] = new BoundingSphere((float) talkArea.talkAreaBounds.getX(), talkOrigin);
            } else {
                bounds[0] = new BoundingBox(talkOrigin, (float) talkArea.talkAreaBounds.getX(),
                        (float) talkArea.talkAreaBounds.getY(),
                        (float) talkArea.talkAreaBounds.getZ());

            }

            MicrophoneTalkAreaProximityListener talkAreaProximityListener = 
		new MicrophoneTalkAreaProximityListener(cellRef.get(), name, volume);

	    talkAreaProximityListenerRef = AppContext.getDataManager().createReference(
		talkAreaProximityListener);

	    LOGGER.info("mic talk area using:  " + bounds[0] + " origin " 
		+ talkOrigin);

	    System.out.println("mic talk area using:  " + bounds[0] + " origin " 
		+ talkOrigin);

            component.addProximityListener(talkAreaProximityListener, bounds);

            bounds = new BoundingVolume[1];

            if (listenArea.boundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
                bounds[0] = cellRef.get().getLocalBounds();
            } else if (listenArea.boundsType.equals(MicrophoneBoundsType.SPHERE)) {
                bounds[0] = new BoundingSphere((float) listenArea.bounds.getX(),
                        new Vector3f());
            } else {
                bounds[0] = new BoundingBox(new Vector3f(), listenArea.bounds.getX(),
                        listenArea.bounds.getY(), listenArea.bounds.getZ());
            }

            MicrophoneListenAreaProximityListener listenAreaProximityListener = 
		new MicrophoneListenAreaProximityListener(cellRef.get(), name, volume);

	    listenAreaProximityListenerRef = AppContext.getDataManager().createReference(
		listenAreaProximityListener);

            LOGGER.info("Microphone Using Box:  " + bounds[0]);

            System.out.println("Microphone Listen Area using:  " + bounds[0]);

            component.addProximityListener(listenAreaProximityListener, bounds);
        } 
    }

}
