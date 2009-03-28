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
package org.jdesktop.wonderland.modules.phone.server.cell;

import com.sun.sgs.app.ManagedReference;

import org.jdesktop.wonderland.modules.phone.common.CallListing;

import org.jdesktop.wonderland.modules.phone.common.messages.CallInvitedResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.CallEndedResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.CallEstablishedResponseMessage;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;

import com.sun.voip.CallParticipant;
import com.sun.voip.client.connector.CallStatus;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.server.WonderlandContext;

import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import org.jdesktop.wonderland.server.comms.CommsManager;

import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;

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

import com.jme.math.Vector3f;

/**
 * A server cell that provides conference phone functionality
 * @author jprovino
 */
public class PhoneStatusListener implements ManagedCallStatusListener, 
	Serializable {

    private static final Logger logger =
        Logger.getLogger(PhoneStatusListener.class.getName());
     
    private ConcurrentHashMap<String, WonderlandClientID> senderMap =
        new ConcurrentHashMap();

    private ConcurrentHashMap<String, CallListing> callListingMap = 
	new ConcurrentHashMap(); 

    private CellID cellID;

    public PhoneStatusListener(PhoneCellMO phoneCellMO) {
        cellID = phoneCellMO.getCellID();
    }

    public void mapCall(String externalCallID, WonderlandClientID clientID, 
	    CallListing listing) {

	senderMap.put(externalCallID, clientID);
	callListingMap.put(externalCallID, listing);
    }
	
    public void callStatusChanged(CallStatus status) {    
	logger.finer("got status " + status);

        String externalCallID = status.getCallId();

	if (externalCallID == null || externalCallID.length() == 0) {
	    logger.warning("Missing call id in status:  " + status);
	    return;
	}

	WonderlandClientID clientID = senderMap.get(externalCallID);

	if (clientID == null) {
	    logger.warning("Can't find clientID:  " + status);
	    return;
	}

        CommsManager cm = WonderlandContext.getCommsManager();

        WonderlandClientSender sender = cm.getSender(CellChannelConnectionType.CLIENT_TYPE);

	CallListing listing;

	listing = callListingMap.get(externalCallID);

	if (listing == null) {
	    logger.warning("No callListing for " + externalCallID);
	    return;
	}

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	Call externalCall = vm.getCall(externalCallID);
	Call softphoneCall = vm.getCall(listing.getSoftphoneCallID());

	logger.fine("external call:  " + externalCall);
	logger.fine("softphone call:  " + softphoneCall);

        switch(status.getCode()) {
        case CallStatus.INVITED:
            //The call has been placed, the phone should be ringing
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
		new CallInvitedResponseMessage(cellID, listing, true);

            sender.send(clientID, invitedResponse);
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
		new CallEstablishedResponseMessage(cellID, listing, true);

	    logger.fine("Sending ESTABLISHED RESPONSE");
            sender.send(clientID, EstablishedResponse);
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
                
            String softphoneCallID = listing.getSoftphoneCallID();
                
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

                if (listing.isPrivate()) {
		    String audioGroupId = softphoneCallID + "_" 
		        + listing.getPrivateClientName();

		    AudioGroup audioGroup = vm.getAudioGroup(audioGroupId);

		    if (audioGroup != null) {
		        if (softphoneCall.getPlayer() != null) {
	        	    softphoneCall.getPlayer().attenuateOtherGroups(audioGroup, 
			        AudioGroup.DEFAULT_SPEAKING_ATTENUATION,
		    	        AudioGroup.DEFAULT_LISTEN_ATTENUATION);
		        }

	                vm.removeAudioGroup(audioGroup);
		    }
		}
            } else {
                //   FakeVoiceHandler.getInstance().endCall(externalCallID);
            }    
                
	    senderMap.remove(externalCallID);
	    callListingMap.remove(externalCallID);

            CallEndedResponseMessage endedResponse = new CallEndedResponseMessage(cellID,
		listing, true, status.getOption("Reason"));

            sender.send(clientID, endedResponse);
            break;
        }
    }

}
