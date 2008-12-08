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

package org.jdesktop.wonderland.modules.orb.server.cell;

import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.AppContext;

import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.VoiceManager;

import org.jdesktop.wonderland.common.cell.CellID;

import com.sun.voip.client.connector.CallStatus;

import org.jdesktop.wonderland.modules.orb.common.messages.OrbSpeakingMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbEndCallMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbMuteCallMessage;

import java.io.IOException;
import java.io.Serializable;

import java.util.logging.Logger;

public class OrbStatusListener implements ManagedCallStatusListener,
        Serializable {

    private static final Logger logger =
        Logger.getLogger(OrbStatusListener.class.getName());

    private ManagedReference<OrbCellMO> orbCellMORef;

    private CellID cellID;

    public OrbStatusListener(ManagedReference<OrbCellMO> orbCellMORef) {
        this.orbCellMORef = orbCellMORef;

	cellID = orbCellMORef.get().getCellID();
    }

    private boolean muteMessageSpoken;
    private boolean isMuted;

    public void addCallStatusListener(String callID) {
	VoiceManager vm = AppContext.getManager(VoiceManager.class);
	vm.addCallStatusListener(this, callID);
    }

    public void callStatusChanged(CallStatus status) {
	logger.fine("Status:  " + status);
        
	String callID = status.getCallId();

        switch (status.getCode()) {
	case CallStatus.DTMF_KEY:
	    handleDtmfKey(status);
	    break;
	
        case CallStatus.STARTEDSPEAKING:            
	    if (isMuted && muteMessageSpoken == false) {
		muteMessageSpoken = true;

		VoiceManager vm = AppContext.getManager(VoiceManager.class);

		Call call = vm.getCall(callID);

	 	try {
                    call.playTreatment("tts:You are muted.");
                    call.playTreatment("tts:Press star, three to unmute");
		} catch (IOException e) {
		    logger.warning("unable to play you are muted treatment to " + call
			+ ":  " + e.getMessage());
		}
	    }

            OrbSpeakingMessage orbStartedSpeakingMessage = new OrbSpeakingMessage(cellID, true);
            // TODO send message to all clients.
            break;
            
        case CallStatus.STOPPEDSPEAKING:
            OrbSpeakingMessage orbStoppedSpeakingMessage = new OrbSpeakingMessage(cellID, false);
	    // TODO send message to all clients.
            break;
            
	case CallStatus.TREATMENTDONE:
	    String treatment = status.getOption("Treatment");

	    logger.fine("Treatment done:  " + treatment);

	    if (treatment.equals(help)) {
		//VoiceHandler voiceHandler = VoiceHandlerImpl.getInstance();
	        //voiceHandler.setListenAttenuator(callID, listenAttenuator);
		playingHelp = false;
	    }

	    break;

        case CallStatus.ENDED: 
	    callEnded(callID);
	    break;
	}
    }

    private void callEnded(String callID) {
	OrbEndCallMessage message = new OrbEndCallMessage(cellID, callID);

        // notify everybody cellChannel.send(cellChannel.getSessions(), orbMessage.getBytes());            
    }
    
    private boolean starPressed;

    private boolean playingHelp;
    private double listenAttenuator;

    private String help = "tts:star, pound, lists the number of people in range."
        + " star, two, mutes your call."
        + " star, three, unmutes your call."
        + " star, eight, lowers the volume you hear."
        + " star, nine, raises the volume you hear.";

    private void handleDtmfKey(CallStatus status) {
	String callID = status.getCallId();

	if (callID == null) {
	    return;
	}

	String dtmfKey = status.getDtmfKey();

	if (dtmfKey.equals("*")) {
	    starPressed = true;
	    return;
	}

	if (starPressed == false) {
	    return;
	}

	starPressed = false;

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	Call call = vm.getCall(callID);

	if (call == null) {
	    logger.warning("No Call for " + callID);
	    return;
	}

	if (dtmfKey.equals("4")) {
	    if (playingHelp) {
		return;
	    }

	    playingHelp = true;

	    //listenAttenuator = voiceHandler.getListenAttenuator(callID);
	    //voiceHandler.setListenAttenuator(callID, listenAttenuator / 3);

	    try {
	        call.playTreatment(help);
	    } catch (IOException e) {
		logger.warning("unable to play help treatment to " + call
		    + ":  " + e.getMessage());
	    }
	    return;
	}
    
	if (dtmfKey.equals("6")) {
	    try {
	        call.playTreatment("tts:Mary had a little lamb, little lamb, little lamb.");
	        call.playTreatment("tts:Mary had a little lamb, its fleece was white as snow.");
	    } catch (IOException e) {
		logger.warning("unable to play treatment to " + call
		    + ":  " + e.getMessage());
	    }

	    return;
	}

	if (dtmfKey.equals("7")) {
	    try {
	        call.playTreatment("tts:And everywhere that Mary went, the lamb was sure to go.");
	    } catch (IOException e) {
		logger.warning("unable to play treatment to " + call
		    + ":  " + e.getMessage());
	    }
	    return;
	}

	if (dtmfKey.equals("#")) {
	    Player p = vm.getPlayer(callID);

	    if (p == null) {
		logger.warning("No Player for " + callID);
		return;
	    }

	    int n = p.getNumberOfPlayersInRange();

	    try {
                if (n == 0) {
                    call.playTreatment("tts:There is no one in range.");
                } else if (n == 1) {
                    call.playTreatment("tts:There is one person in range.");
                } else {
                    call.playTreatment("tts:There are " + n + " people in range.");
	        }
	    } catch (IOException e) {
		logger.warning("unable to play treatment with players in rang to " + call
		    + ":  " + e.getMessage());
	    }

	    return;
	}

	if (dtmfKey.equals("2")) {
	    try {
	        call.mute(true);
	    } catch (IOException e) {
		logger.warning("Unable to mute call " + call
		    + ":  " + e.getMessage());
	    }

	    isMuted = true;

	    OrbMuteCallMessage orbMuteCallMessage = new OrbMuteCallMessage(cellID, callID, true);

            // send to everybody cellChannel.send(cellChannel.getSessions(), orbMessage.getBytes());            
	    try {
	        call.playTreatment("tts:muted");
	    } catch (IOException e) {
		logger.warning("unable to play muted treatment to " + call
		    + ":  " + e.getMessage());
	    }
	    return;
	}

	if (dtmfKey.equals("3")) {
	    try {
	        call.mute(false);
	    } catch (IOException e) {
		logger.warning("Unable to unmute call " + call
		    + ":  " + e.getMessage());
	    }

	    isMuted = false;

	    try {
	        call.playTreatment("tts:un muted");
	    } catch (IOException e) {
		logger.warning("unable to play unmuted treatment to " + call
		    + ":  " + e.getMessage());
	    }
	    OrbMuteCallMessage orbUnmuteCallMessage = new OrbMuteCallMessage(cellID, callID, false);

            // send to everybody cellChannel.send(cellChannel.getSessions(), orbMessage.getBytes());            
	    return;
	}

	if (dtmfKey.equals("8")) {
	    double volume = 0; //voiceHandler.getListenAttenuator(callID);
	    volume -= .2;

	    if (volume < 0) {
	        volume = 0;
	    }

	    logger.finer("decrease volume to " + volume);
	    //voiceHandler.setListenAttenuator(callID, volume);
	    return;
	}

	if (dtmfKey.equals("9")) {
	    double volume = 0; ///voiceHandler.getListenAttenuator(callID);
	    volume += .2;

	    logger.finer("increase volume to " + volume);
	    //voiceHandler.setListenAttenuator(callID, volume);
	    return;
	}
    }
}
