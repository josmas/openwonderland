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

import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;

import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioParticipantSpeakingMessage;

import org.jdesktop.wonderland.server.WonderlandContext;

import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;

import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.mpk20.voicelib.app.Player;

import com.jme.math.Vector3f;

import com.sun.voip.client.connector.CallStatus;

import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;

/**
 *
 * @author jprovino
 */
public class AudioParticipantComponentMO extends CellComponentMO 
	implements ManagedCallStatusListener {

    private static final Logger logger =
            Logger.getLogger(AudioParticipantComponentMO.class.getName());

    private MyTransformChangeListener myTransformChangeListener;

    /**
     * Create a AudioParticipantComponent for the given cell. 
     * @param cell
     */
    public AudioParticipantComponentMO(CellMO cellMO) {
        super(cellMO);

	logger.info("Adding AudioParticpantComponent to " + cellMO.getName());
    }

    @Override
    public void setLive(boolean live) {
	if (live == false) {
	    if (myTransformChangeListener != null) {
	        cellRef.get().removeTransformChangeListener(myTransformChangeListener);
		myTransformChangeListener = null;
	    }
	    return;
	}

        ChannelComponentMO channelComponent = (ChannelComponentMO)
            cellRef.get().getComponent(ChannelComponentMO.class);

	myTransformChangeListener = new MyTransformChangeListener();

	CellMO cellMO = cellRef.get();

	cellMO.addTransformChangeListener(myTransformChangeListener);

	AppContext.getManager(VoiceManager.class).addCallStatusListener(this,
	    cellMO.getCellID().toString());
    }

    protected String getClientClass() {
	return "org.jdesktop.wonderland.modules.audiomanager.client.AudioParticipantComponent";
    }

    public void callStatusChanged(CallStatus status) {
	logger.finer("AudioParticipantComponent go call status:  " + status);

	WonderlandClientSender sender = 
	    WonderlandContext.getCommsManager().getSender(CellChannelConnectionType.CLIENT_TYPE);

	String callId = status.getCallId();

	if (callId == null) {
	    logger.warning("No callId in status:  " + status);
	    return;
	}

	CellID cellID = cellRef.get().getCellID();

	switch (status.getCode()) {
        case CallStatus.STARTEDSPEAKING:
	    sender.send(new AudioParticipantSpeakingMessage(cellID, true));
            break;

        case CallStatus.STOPPEDSPEAKING:
	    sender.send(new AudioParticipantSpeakingMessage(cellID, false));
            break;
	}
    }

    static class MyTransformChangeListener implements TransformChangeListenerSrv {

        public void transformChanged(ManagedReference<CellMO> cellRef, 
	        final CellTransform localTransform, final CellTransform localToWorldTransform) {

	    String clientId = cellRef.get().getCellID().toString();

	    logger.fine("localTransform " + localTransform + " world " 
	        + localToWorldTransform);

	    float[] angles = new float[3];

	    localToWorldTransform.getRotation(null).toAngles(angles);

	    double angle = Math.toDegrees(angles[1]) % 360 + 90;

	    Vector3f location = localToWorldTransform.getTranslation(null);
	
	    Player player = AppContext.getManager(VoiceManager.class).getPlayer(clientId);

	    AudioTreatmentComponentMO component = 
		cellRef.get().getComponent(AudioTreatmentComponentMO.class);

	    if (component != null) {
	        component.transformChanged(location, angle);   // let subclasses know
	    }

	    if (player == null) {
	        logger.fine("AudioParticipant:  got transformChanged, but can't find player for " + clientId);
		return;
	    }

	    player.moved(location.getX(), location.getY(), location.getZ(), angle);

	    logger.fine(player + " x " + location.getX()
	    	+ " y " + location.getY() + " z " + location.getZ()
	    	+ " angle " + angle);
        }

    }

}
