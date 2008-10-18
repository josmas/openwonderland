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

import org.jdesktop.wonderland.modules.phone.common.messages.PhoneCellMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PhoneMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PhoneCellMessage.PhoneAction;

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
import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;

import org.jdesktop.wonderland.server.UserManager;

import com.jme.math.Vector3f;

/**
 * A server cell that provides conference phone functionality
 * @author JHarris
 */
public class PhoneMessageHandler implements 
	ManagedCallStatusListener, TransformChangeListenerSrv, Serializable {

    private static final Logger logger =
        Logger.getLogger(PhoneCellMO.class.getName());
     
    private int callNumber = 0;

    private ConcurrentHashMap<String, WonderlandClientSender> senderMap =
	new ConcurrentHashMap();

    public PhoneMessageHandler() {
	AppContext.getManager(VoiceManager.class).addCallStatusListener(this);
    }

    public void processMessage(WonderlandClientSender sender, 
	    ClientSession session, PhoneCellMessage message) {

	ManagedReference<PhoneCellMO> phoneCellMORef = AppContext.getDataManager().createReference(
	    (PhoneCellMO) CellManagerMO.getCell(message.getPhoneCellID()));

	if (message.getAction() == PhoneAction.LOCK_OR_UNLOCK) {
	    boolean successful = 
		message.getPassword().equals(phoneCellMORef.get().getPassword());

	    if (successful) {
		phoneCellMORef.get().setLocked(!phoneCellMORef.get().getLocked());
	        phoneCellMORef.get().setKeepUnlocked(message.keepUnlocked());
	    }

	    logger.fine("locked " + phoneCellMORef.get().getLocked() + " successful " 
		+ successful + " pw " + message.getPassword());

            PhoneMessage phoneMessage = 
		new PhoneMessage(phoneCellMORef.get().getLocked(), successful);

	    sender.send(phoneMessage);
	    return;
        }

	VoiceManager vm = AppContext.getManager(VoiceManager.class);
	
        CallListing listing = message.getCallListing();
              
	String externalCallID = getCallID(listing);

	Call externalCall = vm.getCall(externalCallID);

	Player externalPlayer = null;

	if (externalCall != null) {
	    externalPlayer = externalCall.getPlayer();
	}

	CellMO clientCellMO = CellManagerMO.getCell(message.getClientCellID());

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

	logger.warning("EXTERNAL CALLID IS " + externalCallID + " action " 
	    + message.getAction());

        //Our phone cell is asking us to begin a new call.
        if (message.getAction() == PhoneAction.PLACE_CALL) {
            //phoneCellMORef.get().addTransformChangeListener(this);

	    if (listing.demoMode() == false) {
		relock(sender, phoneCellMORef);
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

            if (listing.demoMode()) {
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
                if (listing.demoMode() == false) {
                    //Mute the two participants to the outside world
                    logger.warning("attenuate other groups");
		    softphonePlayer.attenuateOtherGroups(audioGroup, 0, 0);
                    logger.warning("back from attenuate other groups");
                }
            } else {
                spawnAvatarOrb(externalCallID, listing);
	    }

            if (listing.demoMode() == false) {
                //Place the calls audio at the phones position
                //translation = new vector3f();                
                //getOriginWorld().get(translation);                
                //externalPlayer.moved(translation.x, translation.y, translation.z, 0);
            }
          
            /*
	     * Echo the PLACE_CALL message back to all the clients 
	     * to signal success.
	     */
            PhoneMessage phoneMessage = 
		new PhoneMessage(PhoneAction.PLACE_CALL, listing, true);

	    logger.warning("NOtifying user...");
            sender.send(phoneMessage);    

	    logger.warning("back from notifying user");
	    return;
        }

	if (message.getAction() == PhoneAction.JOIN_CALL) {
            //Our phone cell wants us to join the call into the world.
            
            if (listing.demoMode() == false) {
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
            PhoneMessage phoneMessage = new PhoneMessage(PhoneAction.JOIN_CALL, 
		listing, true);

            sender.send(phoneMessage);
            
            spawnAvatarOrb(externalCallID, listing);
	    return;
        } 

	if (message.getAction() == PhoneAction.PLAY_TREATMENT) {
	    logger.warning("play treatment " + message.getTreatment() 
		+ " to " + externalCallID + " echo " + message.echo());

            if (listing.demoMode() == false) {
		try {
		    externalCall.playTreatment(message.getTreatment());
	        } catch (IOException e) {
		    logger.warning("Unable to play treatment to " + externalCall + ":  "
		        + e.getMessage());
	        }

		if (message.echo()) {
		    logger.warning("echoing treatment to " + softphoneCallID);

		    try {
		        softphoneCall.playTreatment(message.getTreatment());
	            } catch (IOException e) {
		        logger.warning("Unable to play treatment to " + softphoneCall + ":  "
		            + e.getMessage());
	            }
		}
	    }
	    return;
	}

        if (message.getAction() == PhoneAction.END_CALL) {
            if (listing.demoMode() == false) {
		relock(sender, phoneCellMORef);

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
            
            //Echo SUCCESS to phone cell
            PhoneMessage phoneMessage = 
		new PhoneMessage(PhoneAction.END_CALL, listing, true);

            sender.send(phoneMessage);
	    return;
        } 
    }
   
    private void relock(WonderlandClientSender sender, ManagedReference<PhoneCellMO> phoneCellMORef) {
	if (phoneCellMORef.get().getKeepUnlocked() == false && phoneCellMORef.get().getLocked() == false) {
	    phoneCellMORef.get().setLocked(true);

            PhoneMessage phoneMessage = new PhoneMessage(true, true);

            sender.send(phoneMessage);
	}
    }

    private String getCallID(CallListing listing) {
	String callID = listing.getCallID();

	if (callID != null && callID.length() > 0) {
	    logger.finer("using existing call id " + callID);
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

	    callID += "_" + listing.getContactNumber() + "_" + callNumber;
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

        //if (listing.demoMode()) { 
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

        PhoneMessage phoneMessage;    
    
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
            
            phoneMessage = 
		new PhoneMessage(PhoneAction.CALL_INVITED, listing, true);

            sender.send(phoneMessage);
                
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
            phoneMessage = 
		new PhoneMessage(PhoneAction.CALL_ESTABLISHED, listing, true);

            sender.send(phoneMessage);
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

            if (listing.demoMode() == false) {
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

            phoneMessage = new PhoneMessage(PhoneAction.CALL_ENDED, listing, true);
	    phoneMessage.setReasonCallEnded(status.getOption("Reason"));
            sender.send(phoneMessage);
            break;
        }
    }

    
    public void transformChanged(ManagedReference<CellMO> cellRef, 
	    final CellTransform localTransform, final CellTransform localToWorldTransform) {

	logger.warning("localTransform " + localTransform + " world " 
	    + localToWorldTransform);

	Player player = AppContext.getManager(
	    VoiceManager.class).getPlayer(cellRef.getId().toString());

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
