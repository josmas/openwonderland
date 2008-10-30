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

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;

import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;

import org.jdesktop.wonderland.server.UserManager;

import com.jme.math.Vector3f;

/**
 * A server cell that provides conference phone functionality
 * @author JHarris
 */
public class PhoneMessageHandler implements 
	ManagedCallStatusListener, Serializable {

    private static final Logger logger =
        Logger.getLogger(PhoneCellMO.class.getName());
     
    private int callNumber = 0;

    private ConcurrentHashMap<String, WonderlandClientSender> senderMap =
	new ConcurrentHashMap();

    public PhoneMessageHandler() {
	AppContext.getManager(VoiceManager.class).addCallStatusListener(this);
    }

    public void processMessage(WonderlandClientSender sender, 
	    ClientSession session, Message message) {

	if (message instanceof PhoneControlMessage == false) {
	    logger.warning("Invalid message:  " + message);
	    return;
	}

	PhoneControlMessage msg = (PhoneControlMessage) message;

	if (message instanceof LockUnlockMessage) {
	    LockUnlockMessage m = (LockUnlockMessage) message;

	    boolean successful = true;

	    ManagedReference<PhoneCellMO> clientCellMORef = null;

	    if (msg.getClientCellID() != null) {
	        clientCellMORef = AppContext.getDataManager().createReference(
	            (PhoneCellMO) CellManagerMO.getCell(msg.getClientCellID()));
	    }

	    if (m.getPassword() != null) {
		successful = m.getPassword().equals(clientCellMORef.get().getPassword());
	    }

	    if (successful) {
		clientCellMORef.get().setLocked(!clientCellMORef.get().getLocked());
	        clientCellMORef.get().setKeepUnlocked(m.keepUnlocked());
	    }

	    logger.fine("locked " + clientCellMORef.get().getLocked() + " successful " 
		+ successful + " pw " + m.getPassword());

            LockUnlockResponseMessage response = 
		new LockUnlockResponseMessage(clientCellMORef.get().getLocked(), successful);

	    sender.send(response);
	    return;
        }

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

        CallListing listing = msg.getCallListing();
              
	String externalCallID = getCallID(listing);

	Call externalCall = vm.getCall(externalCallID);

	Player externalPlayer = null;

	if (externalCall != null) {
	    externalPlayer = externalCall.getPlayer();
	}

	CellMO clientCellMO = CellManagerMO.getCell(msg.getClientCellID());

	ManagedReference clientCellMORef = 
	    AppContext.getDataManager().createReference(clientCellMO);

	String softphoneCallID = clientCellMORef.getId().toString();

	Call softphoneCall = vm.getCall(softphoneCallID);

	Player softphonePlayer = null;

	if (softphoneCall != null) {
	    softphonePlayer = softphoneCall.getPlayer();
	}
        
	String audioGroupId = softphoneCallID + "_" + externalCallID;

	AudioGroup audioGroup = vm.getAudioGroup(audioGroupId);

	logger.warning("EXTERNAL CALLID IS " + externalCallID + " " + msg);

	if (message instanceof PlayTreatmentMessage) {
	    PlayTreatmentMessage m = (PlayTreatmentMessage) message;

	    logger.warning("play treatment " + m.getTreatment() 
		+ " to " + getCallID(listing) + " echo " + m.echo());

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

	    logger.warning("echoing treatment to " + softphoneCallID);

	    try {
		softphoneCall.playTreatment(m.getTreatment());
	    } catch (IOException e) {
		logger.warning("Unable to play treatment to " + softphoneCall + ":  "
		    + e.getMessage());
	    }

	    return;
	}

	ManagedReference<PhoneCellMO> externalCallCellMORef = null;

	if (msg.getExternalCallCellID() != null) {
	    externalCallCellMORef = AppContext.getDataManager().createReference(
	        (PhoneCellMO) CellManagerMO.getCell(msg.getExternalCallCellID()));
	}

	if (msg instanceof PlaceCallMessage) {
            //Our phone cell is asking us to begin a new call.

	    if (listing.simulateCalls() == false) {
		relock(sender, externalCallCellMORef);
	    }

	    logger.warning("Got place call message");

	    synchronized (callListingMap) {
	        callListingMap.put(externalCallID, listing);
	    }

	    senderMap.put(externalCallID, sender);

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
		CallSetup callSetup = new CallSetup();
	
		CallParticipant cp = new CallParticipant();

		callSetup.cp = cp;

		try {
		    callSetup.bridgeInfo = vm.getVoiceBridge();
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

		try {
                    externalCall = vm.createCall(externalCallID, callSetup);
	 	} catch (IOException e) {
		    logger.warning("Unable to create call " + cp + ":  "
			+ e.getMessage());
		    return;
		}

		logger.warning("About to create call");
	    	externalPlayer = vm.createPlayer(externalCallID, playerSetup);
		logger.warning("back from creating call");

		externalCall.setPlayer(externalPlayer);

		logger.warning("set external player");

		externalPlayer.setCall(externalCall);

		logger.warning("set external call");

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

		logger.warning("done with audio groups");
            }
            
	    externalCallID = externalCall.getId();

	    logger.warning("Setting actual call id to " + externalCallID);

	    listing.setCallID(externalCallID);  // set actual call Id

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
                    logger.warning("attenuate other groups");
		    softphonePlayer.attenuateOtherGroups(audioGroup, 0, 0);
                    logger.warning("back from attenuate other groups");
                }
            } else {
                spawnAvatarOrb(externalCallID, listing);
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
            sender.send(new PlaceCallResponseMessage(listing, true));

	    logger.warning("back from notifying user");
	    return;
	}

	if (msg instanceof JoinCallMessage) {
            //Our phone cell wants us to join the call into the world.
            
            if (listing.simulateCalls() == false) {
                //Stop any current ringing.
	        try {
                    softphoneCall.stopTreatment("ring_tone.au");
	        } catch (IOException e) {
		    logger.warning("Unable to stop treatment to " + softphoneCall + ":  "
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
            sender.send(new JoinCallResponseMessage(listing, true));
            
            spawnAvatarOrb(externalCallID, listing);
	    return;
	}

	if (msg instanceof EndCallMessage) {
	    logger.warning("simulate is " + listing.simulateCalls() 
		+ " external call " + externalCall);

            if (listing.simulateCalls() == false) {
		relock(sender, externalCallCellMORef);

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
            sender.send(new EndCallResponseMessage(listing, true, "User requested call end"));
	    return;
        } 

	logger.warning("Uknown message type:  " + msg);
    }
   
    private void relock(WonderlandClientSender sender, ManagedReference<PhoneCellMO> externalCallCellMORef) {
	if (externalCallCellMORef == null) {
	    return;
	}

	if (externalCallCellMORef.get().getKeepUnlocked() == false && externalCallCellMORef.get().getLocked() == false) {
	    externalCallCellMORef.get().setLocked(true);

            LockUnlockResponseMessage response = new LockUnlockResponseMessage(true, true);

            sender.send(response);
	}
    }

    private String getCallID(CallListing listing) {
	String callID = listing.getCallID();

	if (callID != null && callID.length() > 0) {
	    logger.warning("using existing call id " + callID);
	    return callID;
	}

	callID = listing.getContactNumber();

	int ix;

	if ((ix = callID.indexOf("@")) >= 0) {
	    callID = callID.substring(0, ix);

	    String pattern = "sip:";
	
	    if ((ix = callID.indexOf(pattern)) >= 0) {
	        callID = callID.substring(ix + pattern.length());
	    }

	    callID = callID.replaceAll(":", "_");
	}

	synchronized (this) {
	    callNumber++;

	    callID = listing.getContactName() + "_" + callID + "_" + callNumber;
	}

        logger.finer("new call id " + callID);
	return callID;
    }

    private void spawnAvatarOrb(String externalCallID, CallListing listing) {
	/*
	 * XXX I was trying to get this to delay for 2 seconds,
	 * But there are no managers in the system context in which run() runs.
	 */
        //Spawn the AvatarOrb to represent the new public call.
        String cellType = 
	    "com.sun.labs.mpk20.avatarorb.server.cell.AvatarOrbCellGLO";

        //CellGLO cellGLO = CellGLOFactory.loadCellGLO(cellType, cellID, 
	//    externalCallID, listing);

        //if (listing.simulateCalls()) { 
        //    FakeVoiceManager.getInstance().addCallStatusListener(
	//	(ManagedCallStatusListener)cellGLO, externalCallID);
	//} else {
	//    VoiceManager vm = AppContext.getManager(VoiceManager.class);
        //    vm.addCallStatusListener((ManagedCallStatusListener)cellGLO);
	//}
    }
    
    private ConcurrentHashMap<String, CallListing> callListingMap = 
	new ConcurrentHashMap();

    public void callStatusChanged(CallStatus status) {    
	logger.warning("got status " + status);

        String callID = status.getCallId();

	if (callID == null || callID.length() == 0) {
	    logger.warning("Missing call id in status:  " + status);
	    return;
	}

	WonderlandClientSender sender = senderMap.get(callID);

	if (sender == null) {
	    logger.warning("Can't find sender for status:  " + status);
	    return;
	}

	CallListing listing;

	synchronized(callListingMap) {
	    listing = callListingMap.get(callID);
	}

	if (listing == null) {
	    logger.finer("No callListing for " + callID);
	    return;
	}

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	Call externalCall = vm.getCall(callID);
	Call softphoneCall = vm.getCall(listing.getPrivateClientName());

        switch(status.getCode()) {

        //The call has been placed, the phone should be ringing
        case CallStatus.INVITED:
            /** HARRISDEBUG: It should be tested whether or not we'll catch
             * callStatus changes for calls which we've just set up.
             * If not, this code will have to be moved back to the
             * "messageReceived->PlaceCall" function.
             **/
            if (listing.isPrivate()) {
                //Start playing the phone ringing sound                    
		try {
                    softphoneCall.playTreatment("ring_tone.au");
	        } catch (IOException e) {
		    logger.warning("Unable to play treatment " + softphoneCall + ":  "
		        + e.getMessage());
	        }
            }
            
            CallInvitedResponseMessage invitedResponse = 
		new CallInvitedResponseMessage(listing, true);

            sender.send(invitedResponse);
                
            break;

        //Something's picked up, the call has been connected
        case CallStatus.ESTABLISHED:
            if (listing.isPrivate()) {
                //Stop playing the phone ringing sound
		try {
                    softphoneCall.stopTreatment("ring_tone.au");
	        } catch (IOException e) {
		    logger.warning("Unable to stop treatment " + softphoneCall + ":  "
		        + e.getMessage());
	        }
            }

            CallEstablishedResponseMessage EstablishedResponse = 
		new CallEstablishedResponseMessage(listing, true);

            sender.send(EstablishedResponse);
            break;

        case CallStatus.STARTEDSPEAKING:
            break;

        case CallStatus.STOPPEDSPEAKING:
            break;

        case CallStatus.ENDED: 
            //Stop the ringing
	    if (softphoneCall != null) {
	        try {
                    softphoneCall.stopTreatment("ring_tone.au");
	        } catch (IOException e) {
		    logger.warning(
			"Unable to stop treatment " + softphoneCall + ":  "
		    	+ e.getMessage());
	        }
	    }
                
            String softphoneCallID = listing.getPrivateClientName();
                
            //This may appear redundant, but it's necessary for the VoiceManager
	    // to remove its internal data structures.

            if (listing.simulateCalls() == false) {
		if (externalCall != null) {
		    try {
                        vm.endCall(externalCall, true);
	            } catch (IOException e) {
		        logger.warning(
			    "Unable to end call " + externalCall + ":  "
		            + e.getMessage());
	            }
		}

		String audioGroupId = softphoneCallID + "_" 
		    + listing.getPrivateClientName();

		AudioGroup audioGroup = vm.getAudioGroup(audioGroupId);

		if (audioGroup != null) {
		    if (softphoneCall.getPlayer() != null) {
	        	softphoneCall.getPlayer().attenuateOtherGroups(audioGroup, 
			    AudioGroup.DEFAULT_SPEAKING_ATTENUATION,
		    	    AudioGroup.DEFAULT_LISTEN_ATTENUATION);
		    }

	            vm.removeAudioGroup(audioGroupId);
		}
            } else {
                //   FakeVoiceHandler.getInstance().endCall(callID);
            }    
                
	    synchronized (callListingMap) {
		callListingMap.remove(callID);
	    }

	    senderMap.remove(callID);

            CallEndedResponseMessage endedResponse = new CallEndedResponseMessage(
		listing, true, status.getOption("Reason"));

            sender.send(endedResponse);
            break;
        }
    }

}
