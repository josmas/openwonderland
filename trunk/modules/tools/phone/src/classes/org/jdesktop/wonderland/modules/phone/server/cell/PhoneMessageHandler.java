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

package org.jdesktop.wonderland.modules.phone.server.cell;

import com.sun.sgs.app.ManagedReference;

import org.jdesktop.wonderland.modules.phone.common.CallListing;
import org.jdesktop.wonderland.modules.phone.common.PhoneCellSetup;

import org.jdesktop.wonderland.modules.phone.common.messages.CallInvitedResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.CallEndedResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.CallEstablishedResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.EndCallMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.EndCallResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.JoinCallMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.JoinCallResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.LockUnlockMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.LockUnlockResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PlaceCallMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PlaceCallResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PlayTreatmentMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PhoneControlMessage;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.AudioGroupSetup;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.mpk20.voicelib.app.ZeroVolumeSpatializer;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;

import com.sun.voip.CallParticipant;
import com.sun.voip.client.connector.CallStatus;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.server.WonderlandContext;

import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;

import org.jdesktop.wonderland.server.comms.WonderlandClientSender;


import java.io.IOException;
import java.io.Serializable;

import java.lang.String;
import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.common.cell.MultipleParentException;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;

import org.jdesktop.wonderland.server.UserManager;

import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellMOFactory;

import org.jdesktop.wonderland.server.setup.BeanSetupMO;

import org.jdesktop.wonderland.modules.orb.common.OrbCellSetup;

import org.jdesktop.wonderland.modules.orb.server.cell.OrbCellMO;

import com.jme.math.Vector3f;

/**
 * A server cell that provides conference phone functionality
 * @author jprovino
 */
public class PhoneMessageHandler implements Serializable, ComponentMessageReceiver {

    private static final Logger logger =
        Logger.getLogger(PhoneMessageHandler.class.getName());
     
    private ManagedReference<PhoneCellMO> phoneCellMORef;

    private ManagedReference<ChannelComponentMO> channelComponentRef = null;

    private ManagedReference<PhoneStatusListener> phoneStatusListenerRef;

    private int callNumber = 0;

    public PhoneMessageHandler(PhoneCellMO phoneCellMO) {
	phoneCellMORef = AppContext.getDataManager().createReference(
	        (PhoneCellMO) CellManagerMO.getCell(phoneCellMO.getCellID()));

	PhoneStatusListener phoneStatusListener = new PhoneStatusListener(phoneCellMORef);
	
	phoneStatusListenerRef =  AppContext.getDataManager().createReference(phoneStatusListener);

        ChannelComponentMO channelComponent = (ChannelComponentMO) 
	    phoneCellMO.getComponent(ChannelComponentMO.class);

        if (channelComponent == null) {
            throw new IllegalStateException("Cell does not have a ChannelComponent");
	}

        channelComponentRef = AppContext.getDataManager().createReference(channelComponent);

        channelComponent.addMessageReceiver(EndCallMessage.class, this);
        channelComponent.addMessageReceiver(JoinCallMessage.class, this);
        channelComponent.addMessageReceiver(LockUnlockMessage.class, this);
        channelComponent.addMessageReceiver(PlaceCallMessage.class, this);
        channelComponent.addMessageReceiver(PlayTreatmentMessage.class, this);
    }

    public void messageReceived(final WonderlandClientSender sender, 
	    final ClientSession session, final CellMessage message) {

	PhoneControlMessage msg = (PhoneControlMessage) message;

	logger.fine("got message " + msg);

	if (message instanceof LockUnlockMessage) {
	    LockUnlockMessage m = (LockUnlockMessage) message;

	    boolean successful = true;

	    if (m.getPassword() != null) {
		successful = m.getPassword().equals(phoneCellMORef.get().getPassword());
	    }

	    if (successful) {
		phoneCellMORef.get().setLocked(!phoneCellMORef.get().getLocked());
	        phoneCellMORef.get().setKeepUnlocked(m.keepUnlocked());
	    }

	    logger.fine("locked " + phoneCellMORef.get().getLocked() + " successful " 
		+ successful + " pw " + m.getPassword());

            LockUnlockResponseMessage response = 
		new LockUnlockResponseMessage(phoneCellMORef.get().getCellID(), phoneCellMORef.get().getLocked(), successful);

	    sender.send(response);
	    return;
        }

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

        CallListing listing = msg.getCallListing();
              
	String externalCallID = getExternalCallID(listing);

	Call externalCall = vm.getCall(externalCallID);

	Player externalPlayer = null;

	if (externalCall != null) {
	    externalPlayer = externalCall.getPlayer();
	}

	String softphoneCallID = listing.getSoftphoneCallID();

	Call softphoneCall = null;

	Player softphonePlayer = null;

	AudioGroup audioGroup = null;

	String audioGroupId = null;

	if (softphoneCallID != null) {
	    softphoneCall = vm.getCall(softphoneCallID);

	    if (softphoneCall != null) {
	        softphonePlayer = softphoneCall.getPlayer();
	    }
        
	    audioGroupId = softphoneCallID + "_" + externalCallID;

	    audioGroup = vm.getAudioGroup(audioGroupId);
	}

	logger.fine("EXTERNAL CALLID IS " + externalCallID + " " + msg
	    + " softphone callID " + softphoneCallID + " softphone call " 
	    + softphoneCall + " softphone player " + softphonePlayer);

	if (message instanceof PlayTreatmentMessage) {
	    PlayTreatmentMessage m = (PlayTreatmentMessage) message;

	    logger.fine("play treatment " + m.getTreatment() 
		+ " to " + listing.getExternalCallID() + " echo " + m.echo());

            if (listing.simulateCalls() == true) {
		return;
	    }

	    try {
		externalCall.playTreatment(m.getTreatment());
	    } catch (IOException e) {
		logger.warning("Unable to play treatment to " + externalCall + ":  "
		    + e.getMessage());
	    }

	    if (m.echo() == false) {
		return;
	    }

	    logger.fine("echoing treatment to " + softphoneCallID);

	    try {
		softphoneCall.playTreatment(m.getTreatment());
	    } catch (IOException e) {
		logger.warning("Unable to play treatment to " + softphoneCall + ":  "
		    + e.getMessage());
	    }

	    return;
	}

	if (msg instanceof PlaceCallMessage) {
            //Our phone cell is asking us to begin a new call.

	    if (listing.simulateCalls() == false) {
		relock(sender);
	    }

	    logger.fine("Got place call message " + externalCallID);

	    phoneStatusListenerRef.get().mapCall(externalCallID, sender, listing);

	    PlayerSetup playerSetup = new PlayerSetup();
	    //playerSetup.x =  translation.x;
	    //playerSetup.y =  translation.y;
	    //playerSetup.z =  translation.z;
	    playerSetup.isOutworlder = true;
	    playerSetup.isLivePlayer = true;

            if (listing.simulateCalls()) {
                FakeVoiceManager.getInstance().setupCall(
		    externalCallID, listing.getContactNumber());
            } else {                               
		CallSetup setup = new CallSetup();
	
		CallParticipant cp = new CallParticipant();

		setup.cp = cp;
		try {
		    setup.bridgeInfo = vm.getVoiceBridge();
	 	} catch (IOException e) {
		    logger.warning("Unable to get voice bridge for call " + cp + ":  "
			+ e.getMessage());
		    return;
		}

		cp.setPhoneNumber(listing.getContactNumber());
		cp.setCallId(externalCallID);
		cp.setConferenceId(vm.getConferenceId());
		cp.setVoiceDetection(true);
		cp.setDtmfDetection(true);
		cp.setVoiceDetectionWhileMuted(true);
		cp.setHandleSessionProgress(true);

        	if (listing.simulateCalls()) { 
            	    FakeVoiceManager.getInstance().addCallStatusListener(
			phoneStatusListenerRef.get(), externalCallID);
		} else {
		    setup.listener = phoneStatusListenerRef.get();
		}

		try {
                    externalCall = vm.createCall(externalCallID, setup);
	 	} catch (IOException e) {
		    logger.warning("Unable to create call " + cp + ":  "
			+ e.getMessage());
		    return;
		}

		logger.fine("About to create call");
	    	externalPlayer = vm.createPlayer(externalCallID, playerSetup);
		logger.fine("back from creating call");

		externalCall.setPlayer(externalPlayer);

		logger.fine("set external player");

		externalPlayer.setCall(externalCall);

		logger.fine("set external call");

		/*
		 * Allow caller and callee to hear each other
		 */
		AudioGroupSetup audioGroupSetup = new AudioGroupSetup();
		audioGroupSetup.spatializer = new FullVolumeSpatializer();

		audioGroup = vm.createAudioGroup(audioGroupId, audioGroupSetup);
		audioGroup.addPlayer(externalPlayer, 
		    new AudioGroupPlayerInfo(true, 
		    AudioGroupPlayerInfo.ChatType.PRIVATE));
		audioGroup.addPlayer(softphonePlayer, 
		    new AudioGroupPlayerInfo(true, 
		    AudioGroupPlayerInfo.ChatType.PRIVATE));

		logger.fine("done with audio groups");
            }
            
	    if (externalCall != null) {
	        externalCallID = externalCall.getId();
	    }

	    logger.fine("Setting actual call id to " + externalCallID);

	    listing.setExternalCallID(externalCallID);  // set actual call Id

            //Check implicit privacy settings
            if (listing.isPrivate()) {
                /** HARRISNOTE: We need our client name later in order to 
                 * setup private spatializers. But because we didn't know 
                 * our proper client name in the PhoneCell, we update the 
                 * callListing now that we do.
                 **/
		listing.setPrivateClientName(externalCallID);

                /*
		 * Set the call audio to whisper mode until the caller 
		 * chooses to join the call.
		 */
                if (listing.simulateCalls() == false) {
                    //Mute the two participants to the outside world
                    logger.fine("attenuate other groups");
		    softphonePlayer.attenuateOtherGroups(audioGroup, 0, 0);
                    logger.fine("back from attenuate other groups");
                }
            } else {
                spawnOrb(externalCallID);
	    }

            if (listing.simulateCalls() == false) {
                //Place the calls audio at the phones position
                //translation = new vector3f();                
                //getOriginWorld().get(translation);                
                //externalPlayer.moved(translation.x, translation.y, translation.z, 0);
            }
          
            /*
	     * Send PLACE_CALL_RESPONSE message back to all the clients 
	     * to signal success.
	     */
            sender.send(new PlaceCallResponseMessage(phoneCellMORef.get().getCellID(), listing, true));

	    logger.fine("back from notifying user");
	    return;
	}

	if (msg instanceof JoinCallMessage) {
            //Our phone cell wants us to join the call into the world.
            
            if (listing.simulateCalls() == false) {
                //Stop any current ringing.
	        try {
                    softphoneCall.stopTreatment("ring_tone.au");
	        } catch (IOException e) {
		    logger.fine("Unable to stop treatment to " + softphoneCall + ":  "
		        + e.getMessage());
	        }

		AudioGroup defaultLivePlayerAudioGroup = 
		    vm.getDefaultLivePlayerAudioGroup();

		defaultLivePlayerAudioGroup.addPlayer(externalPlayer, 
		    new AudioGroupPlayerInfo(true, 
		    AudioGroupPlayerInfo.ChatType.PUBLIC));

		AudioGroup defaultStationaryPlayerAudioGroup = 
		    vm.getDefaultStationaryPlayerAudioGroup();

		defaultStationaryPlayerAudioGroup.addPlayer(externalPlayer, 
		    new AudioGroupPlayerInfo(false, 
		    AudioGroupPlayerInfo.ChatType.PUBLIC));

	        softphonePlayer.attenuateOtherGroups(audioGroup, 
		    AudioGroup.DEFAULT_SPEAKING_ATTENUATION,
		    AudioGroup.DEFAULT_LISTEN_ATTENUATION);

	        vm.removeAudioGroup(audioGroupId);
            }
            
            listing.setPrivateClientName("");
              
            //Inform the PhoneCells that the call has been joined successfully
            sender.send(new JoinCallResponseMessage(phoneCellMORef.get().getCellID(), listing, true));
            
            spawnOrb(externalCallID);
	    return;
	}

	if (msg instanceof EndCallMessage) {
	    logger.fine("simulate is " + listing.simulateCalls() 
		+ " external call " + externalCall);

            if (listing.simulateCalls() == false) {
		relock(sender);

		if (externalCall != null) {
		    try {
                        vm.endCall(externalCall, true);
	            } catch (IOException e) {
		        logger.warning(
			    "Unable to end call " + externalCall + ":  "
		            + e.getMessage());
	            }
		}

		if (audioGroup != null) {
                    if (listing.isPrivate()) {
	        	softphonePlayer.attenuateOtherGroups(audioGroup, 
			    AudioGroup.DEFAULT_SPEAKING_ATTENUATION,
		    	    AudioGroup.DEFAULT_LISTEN_ATTENUATION);
	            }

	            vm.removeAudioGroup(audioGroupId);
		}
            } else {                
                FakeVoiceManager.getInstance().endCall(externalCallID);
            }         
            
            //Send SUCCESS to phone cell
            sender.send(new EndCallResponseMessage(phoneCellMORef.get().getCellID(), listing, true, 
		"User requested call end"));
	    return;
        } 

	logger.fine("Uknown message type:  " + msg);
    }
   
    private void relock(WonderlandClientSender sender) {
	if (phoneCellMORef.get().getKeepUnlocked() == false && phoneCellMORef.get().getLocked() == false) {
	    phoneCellMORef.get().setLocked(true);

            LockUnlockResponseMessage response = new LockUnlockResponseMessage(phoneCellMORef.get().getCellID(), true, true);

            sender.send(response);
	}
    }

    private String getExternalCallID(CallListing listing) {
	String externalCallID = listing.getExternalCallID();

	if (externalCallID != null && externalCallID.length() > 0) {
	    logger.fine("using existing call id " + externalCallID);
	    return externalCallID;
	}

	synchronized (this) {
	    callNumber++;

            return phoneCellMORef.get().getCellID() + "_" + callNumber;
	}
    }

    private void spawnOrb(String externalCallID) {
	/*
	 * XXX I was trying to get this to delay for 2 seconds,
	 * But there are no managers in the system context in which run() runs.
	 */
        //Spawn the Orb to represent the new public call.

	logger.fine("Spawning orb...");

        String cellType = 
	    "org.jdesktop.wonderland.modules.orb.server.cell.OrbCellMO";

        OrbCellMO orbCellMO = (OrbCellMO) CellMOFactory.loadCellMO(cellType, externalCallID);

	if (orbCellMO == null) {
	    logger.warning("Unable to spawn orb");
	    return;
	}

	try {
            ((BeanSetupMO)orbCellMO).setupCell(new OrbCellSetup());
        } catch (ClassCastException e) {
            logger.warning("Error setting up new cell " +
                orbCellMO.getName() + " of type " +
                orbCellMO.getClass() + ", it does not implement " +
                "BeanSetupMO. " + e.getMessage());
            return;
        }

	try {
	    WonderlandContext.getCellManager().insertCellInWorld(orbCellMO);
	} catch (MultipleParentException e) {
	    logger.warning("Can't insert orb in world:  " + e.getMessage());
	    return;
	}
    }
    
}
