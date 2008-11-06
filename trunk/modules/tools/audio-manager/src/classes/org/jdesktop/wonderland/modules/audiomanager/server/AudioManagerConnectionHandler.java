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

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AvatarCellIDMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.DisconnectCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlaceCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.TransferCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage;

import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.UserMO;

import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.view.AvatarCellMO;

import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import java.io.Serializable;
import java.util.logging.Logger;

import java.util.Properties;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.BridgeInfo;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.voip.CallParticipant;

import com.sun.voip.client.connector.CallStatus;
import com.sun.voip.client.connector.CallStatusListener;

import java.io.IOException;

import com.jme.math.Vector3f;

/**
 * Audio Manager
 * 
 * @author jprovino
 */
public class AudioManagerConnectionHandler 
        implements ClientConnectionHandler, Serializable, CallStatusListener
{
    private static final Logger logger =
            Logger.getLogger(AudioManagerConnectionHandler.class.getName());
    
    private VoiceChatHandler voiceChatHandler = new VoiceChatHandler();

    public AudioManagerConnectionHandler() {
        super();
    }

    public ConnectionType getConnectionType() {
        return AudioManagerConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
	logger.fine("Audio Server manager connection registered");
    }

    public void clientConnected(WonderlandClientSender sender, 
	    ClientSession session, Properties properties) {

        //throw new UnsupportedOperationException("Not supported yet.");
	logger.fine("client connected...");
    }

    public void messageReceived(WonderlandClientSender sender, 
	    ClientSession session, Message message) {

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	if (message instanceof AvatarCellIDMessage) {
	    AvatarCellIDMessage msg = (AvatarCellIDMessage) message;
	    voiceChatHandler.addTransformChangeListener(msg.getCellID());
	    return;
	}

	if (message instanceof GetVoiceBridgeMessage) {
	    GetVoiceBridgeMessage msg = (GetVoiceBridgeMessage) message;

	    String username = 
		UserManager.getUserManager().getUser(session).getUsername();

	    logger.fine("Got voice bridge request message from " + username);

	    try {
		String voiceBridge = vm.getVoiceBridge().toString();

		logger.info("Got voice bridge '" + voiceBridge + "'");
	        msg.setBridgeInfo(voiceBridge);
		msg.setUsername(username);
	    } catch (IOException e) {
		logger.warning("unable to get voice bridge:  " + e.getMessage());
		return;
	    }

	    sender.send(msg);
	    return;
	}

	if (message instanceof PlaceCallMessage) {
	    logger.fine("Got place call message");

	    PlaceCallMessage msg = (PlaceCallMessage) message;

	    CallSetup setup = new CallSetup();

	    CallParticipant cp = new CallParticipant();

	    setup.cp = cp;

	    String callID = msg.getSoftphoneCallID();

	    logger.fine("callID " + callID);

	    if (callID == null) {
	        logger.fine("Can't place call to " + msg.getSipURL()
		    + ".  No cell for " + callID);
		return;
	    }

	    cp.setCallId(callID);
	    cp.setName(UserManager.getUserManager().getUser(session).getUsername());
            cp.setPhoneNumber(msg.getSipURL());
            cp.setConferenceId(vm.getConferenceId());
            cp.setVoiceDetection(true);
            cp.setDtmfDetection(true);
            cp.setVoiceDetectionWhileMuted(true);
            cp.setHandleSessionProgress(true);
            cp.setJoinConfirmationTimeout(0);
	    cp.setCallAnsweredTreatment(null);

            vm.addCallStatusListener(this, null);

	    Call call;

            try {
                call = vm.createCall(callID, setup);
            } catch (IOException e) {
                logger.warning("Unable to create call " + cp + ": " + e.getMessage());
		return;
            }

	    callID = call.getId();

            vm.addCallStatusListener(this, callID);

            PlayerSetup ps = new PlayerSetup();
            ps.x = (double) msg.getX();
            ps.y = (double) msg.getY();
            ps.z = (double) msg.getZ();
            ps.orientation =  getAngle(msg.getX(), msg.getY(), msg.getZ(),
                msg.getDirection());
            ps.isLivePlayer = true;

            Player player = vm.createPlayer(callID, ps);

            call.setPlayer(player);
            player.setCall(call);

            vm.getDefaultLivePlayerAudioGroup().addPlayer(player,
                new AudioGroupPlayerInfo(true, AudioGroupPlayerInfo.ChatType.PUBLIC));

            AudioGroupPlayerInfo info = new AudioGroupPlayerInfo(false,
                AudioGroupPlayerInfo.ChatType.PUBLIC);

            info.defaultSpeakingAttenuation = 0;

            vm.getDefaultStationaryPlayerAudioGroup().addPlayer(player, info);
	    return;
	}

	if (message instanceof TransferCallMessage) {
	    TransferCallMessage msg = (TransferCallMessage) message;

	    String callID = msg.getSoftphoneCallID();

	    if (callID == null) {
		logger.warning("Unable to transfer call.  Can't get callID for " 
		    + callID);
		return;
	    }

	    Call call = vm.getCall(callID);

	    if (call == null) {
		// XXX we should be nicer and place the call!
		logger.warning("Unable to transfer call.  No Call for " + callID);
		return;
	    }

	    CallParticipant cp = call.getSetup().cp;

            cp.setPhoneNumber(msg.getPhoneNumber());
            cp.setJoinConfirmationTimeout(90);

	    String callAnsweredTreatment = System.getProperty(
                "com.sun.sgs.impl.app.voice.CALL_ANSWERED_TREATMENT");

	    if (callAnsweredTreatment == null || callAnsweredTreatment.length() == 0) {
		callAnsweredTreatment = "dialtojoin.au";
	    }

            cp.setCallAnsweredTreatment(callAnsweredTreatment);

	    try {
	        call.transfer(cp);
	    } catch (IOException e) {
		logger.warning("Unable to transfer call:  " + e.getMessage());
	    }
	    return;
	}

	if (message instanceof DisconnectCallMessage) {
	    logger.warning("got DisconnectCallMessage");
	    return;
	}

	if (message instanceof VoiceChatMessage) {
	    voiceChatHandler.processVoiceChatMessage(sender, 
		(VoiceChatMessage) message);
	    return;
	}

        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clientDisconnected(WonderlandClientSender sender, ClientSession session) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void callStatusChanged(CallStatus status) {
	logger.fine("got status " + status);
    }

    /*
     * XXX This probably doesn't belong here!
     *
     * Calculate the angle based on the direction vector
     * We ignore the y value which is the up/down direction.
     *
     * The direction (-1, 0) is east or 0 degrees
     *		     (0, 1) is north or 90 degrees
     *	             (1, 0) is west or 180 degrees
     *		     (0, -1 is south) or 270 degrees
     */
    public static double getAngle(double x, double y, double z, double direction) {
	/*
	 * x is given to us pointing in the wrong direction.
	 */
        x = -x;
        
        double angle;
        
        if (x == 0) {
	    /*
	     * x is 0, so we can't divide by x.
	     * if z is non-negative, then the angle is 90.
	     * Otherwise, it's 270.
	     */
            if (z < 0) {
                angle = 90;
            } else {
                angle = 270;
            }
        } else {
            angle = Math.toDegrees(Math.atan(z / x));
            
            if (x < 0) {
          /*
           * atan only produces a result between 0 and 180
           * so we have to adjust it based on the z value
           */
                if (z > 0) {
                    angle -= 180;
                } else {
                    angle += 180;
                }
            }
        }
        
	/*
	 * For wonderland, a clockwise rotation results in a bigger
	 * angle, i. e., rotation is in the opposite direction of
	 * what the voice manager expects.  Correct for that here.
	 */
        angle = 360 - angle;
        
        if (angle < 0) {
            angle += 360;
        }
        
        angle %= 360;
        
        return angle;
    }

}
