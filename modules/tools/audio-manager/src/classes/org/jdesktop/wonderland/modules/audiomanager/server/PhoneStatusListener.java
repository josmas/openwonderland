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

import com.sun.sgs.app.ManagedReference;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.CallEndedResponseMessage;

import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.sgs.app.AppContext;

import com.sun.voip.client.connector.CallStatus;

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

import org.jdesktop.wonderland.common.cell.CellTransform;

import org.jdesktop.wonderland.server.cell.CellMO;

import com.jme.math.Vector3f;

/**
 * @author jprovino
 */
public class PhoneStatusListener implements ManagedCallStatusListener, Serializable {

    private static final Logger logger =
        Logger.getLogger(PhoneStatusListener.class.getName());
     
    String group;
    private WonderlandClientID clientID;
    private String softphoneCallID;
    private String externalCallID;

    public PhoneStatusListener(String group, WonderlandClientID clientID, String softphoneCallID, 
	    String externalCallID) {

	this.group = group;
	this.clientID = clientID;
	this.softphoneCallID = softphoneCallID;
	this.externalCallID = externalCallID;

	AppContext.getManager(VoiceManager.class).addCallStatusListener(this, 
	    externalCallID);
    }

    public void callStatusChanged(CallStatus status) {    
	logger.finer("got status " + status);

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

        if (status.getCode() == CallStatus.ESTABLISHED) {
	    stopRinging(vm);
        } else if (status.getCode() == CallStatus.ENDED) {
	    stopRinging(vm);
	    vm.removeCallStatusListener(this);
                
            WonderlandClientSender sender = WonderlandContext.getCommsManager().getSender(
	        AudioManagerConnectionType.CONNECTION_TYPE);

	    Player player = vm.getPlayer(externalCallID);

	    if (player != null) {
		vm.removePlayer(player);
	    }

            //sender.send(clientID, new CallEndedResponseMessage(group, externalCallID, 
	    //    status.getOption("Reason")));
	}
    }

    private void stopRinging(VoiceManager vm) {
        //Stop the ringing
	Call softphoneCall = vm.getCall(softphoneCallID);

	if (softphoneCall != null) {
	    try {
                softphoneCall.stopTreatment("ring_tone.au");
	    } catch (IOException e) {
		logger.warning("Unable to stop treatment " + softphoneCall + ":  "
		    + e.getMessage());
	    }
	}
    }

}
