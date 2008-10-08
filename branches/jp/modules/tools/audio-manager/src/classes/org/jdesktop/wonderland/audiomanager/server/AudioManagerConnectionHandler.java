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
package org.jdesktop.wonderland.audiomanager.server;

import java.util.Properties;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.audiomanager.common.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.audiomanager.common.PlaceCallMessage;
import org.jdesktop.wonderland.audiomanager.common.DisconnectCallMessage;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import com.sun.sgs.app.ClientSession;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.audiomanager.common.AudioManagerConnectionType;

import com.sun.sgs.app.AppContext;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.voip.CallParticipant;

import com.sun.voip.client.connector.CallStatus;
import com.sun.voip.client.connector.CallStatusListener;

import java.io.IOException;

/**
 * Test listener, will eventually support Audio Manager
 * 
 * @author jprovino
 */
public class AudioManagerConnectionHandler 
        implements ClientConnectionHandler, Serializable, CallStatusListener
{
    private static final Logger logger =
            Logger.getLogger(AudioManagerConnectionHandler.class.getName());
    
    public AudioManagerConnectionHandler() {
        super();
    }

    public ConnectionType getConnectionType() {
        return AudioManagerConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        logger.info("Sever manager connection registered");
    }

    public void clientConnected(WonderlandClientSender sender, ClientSession session, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void messageReceived(WonderlandClientSender sender, ClientSession session, Message message) {

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	if (message instanceof GetVoiceBridgeMessage) {
	    GetVoiceBridgeMessage msg = (GetVoiceBridgeMessage) message;

	    msg.setBridgeInfo("129.148.75.55:6666:5060:129.148.75.55:6666:5060");

	    sender.send(msg);
	} else if (message instanceof PlaceCallMessage) {
	    PlaceCallMessage msg = (PlaceCallMessage) message;

	    CallSetup setup = new CallSetup();

	    CallParticipant cp = new CallParticipant();

            cp.setPhoneNumber(msg.getSipURL());
            cp.setConferenceId(vm.getConferenceId());
            cp.setVoiceDetection(true);
            cp.setDtmfDetection(true);
            cp.setVoiceDetectionWhileMuted(true);
            cp.setHandleSessionProgress(true);

            vm.addCallStatusListener(this);

	    Call call;

            try {
                call = vm.createCall("jp", setup);
            } catch (IOException e) {
                logger.warning("Unable to create call " + cp + ": " + e.getMessage());
		return;
            }

            PlayerSetup ps = new PlayerSetup();
            ps.x = (double) msg.getX();
            ps.y = (double) msg.getY();
            ps.z = (double) msg.getZ();
            ps.orientation =  getAngle(msg.getX(), msg.getY(), msg.getZ(),
                msg.getDirection());
            ps.isLivePlayer = true;

            Player player = vm.createPlayer(call.getId(), ps);

            call.setPlayer(player);
            player.setCall(call);

            vm.getDefaultLivePlayerAudioGroup().addPlayer(player,
                new AudioGroupPlayerInfo(true, AudioGroupPlayerInfo.ChatType.PUBLIC));

            AudioGroupPlayerInfo info = new AudioGroupPlayerInfo(false,
                AudioGroupPlayerInfo.ChatType.PUBLIC);

            info.defaultSpeakingAttenuation = 0;

            vm.getDefaultStationaryPlayerAudioGroup().addPlayer(player, info);
	} else if (message instanceof DisconnectCallMessage) {
	    logger.warning("got DisconnectCallMessage");
	} else {
            throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    public void clientDisconnected(WonderlandClientSender sender, ClientSession session) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void callStatusChanged(CallStatus status) {
	logger.warning("got status " + status);
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
