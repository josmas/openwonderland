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
package org.jdesktop.wonderland.modules.microphone.client.cell;

//import org.jdesktop.wonderland.avatarorb.client.cell.AvatarOrbCell;

import com.sun.sgs.client.ClientChannel;

import java.util.logging.Logger;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.client.jme.JmeClientMain;

import org.jdesktop.wonderland.modules.microphone.common.messages.MicrophoneEnterCellMessage;

import com.jme.bounding.BoundingVolume;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingUtilities;

/**
 *
 * @author jprovino
 */
public class MicrophoneMessageHandler implements ProximityListener {

    private static final Logger logger =
            Logger.getLogger(MicrophoneMessageHandler.class.getName());

    private MicrophoneCell microphoneCell;

    private ChannelComponent channelComp;

    public MicrophoneMessageHandler(MicrophoneCell microphoneCell) {
	this.microphoneCell = microphoneCell;

	channelComp = microphoneCell.getComponent(ChannelComponent.class);

	logger.fine("Channel comp is " + channelComp);

        ChannelComponent.ComponentMessageReceiver msgReceiver =
	    new ChannelComponent.ComponentMessageReceiver() {
                public void messageReceived(CellMessage message) {
                    //MicrophoneResponseMessage msg = (MicrophoneResponseMessage)message;

		    //processMessage((MicrophoneResponseMessage) message);
                }
            };

        //channelComp.addMessageReceiver(CallEndedResponseMessage.class, msgReceiver);

        ProximityComponent comp = new ProximityComponent(microphoneCell);

	// TODO figure out how to add a second BoundingVolume for the activeArea of the mic

        BoundingVolume[] boundingVolume = new BoundingVolume[1];

        boundingVolume[0] = microphoneCell.getLocalBounds();

        comp.addProximityListener(this, boundingVolume);

        microphoneCell.addComponent(comp);
    }

    public void processMessage(final Message message) {
    //public void processMessage(final MicrophoneResponseMessage message) {
	//if (message instanceof CallEndedResponseMessage) {
    }
    
    public void leftChannel(ClientChannel arg0) {
        // ignore
    }
    
    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume,
            int proximityIndex) {

        logger.warning("cell " + cell + " entered = " + entered);

        MicrophoneEnterCellMessage message = new MicrophoneEnterCellMessage(
            cell.getCellID(), entered);

        channelComp.send(message);
    }

}
