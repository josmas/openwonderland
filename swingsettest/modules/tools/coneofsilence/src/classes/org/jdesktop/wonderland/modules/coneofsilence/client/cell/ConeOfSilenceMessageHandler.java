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
package org.jdesktop.wonderland.modules.coneofsilence.client.cell;

//import org.jdesktop.wonderland.avatarorb.client.cell.AvatarOrbCell;

import com.sun.sgs.client.ClientChannel;

import java.awt.event.MouseEvent;

import java.util.logging.Logger;

import org.jdesktop.mtgame.Entity;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;

import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;

import org.jdesktop.wonderland.client.jme.CellRefComponent;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;

import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import org.jdesktop.wonderland.modules.coneofsilence.common.messages.ConeOfSilenceEnterCellMessage;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import com.jme.bounding.BoundingVolume;

import javax.swing.SwingUtilities;

/**
 *
 * @author jprovino
 */
public class ConeOfSilenceMessageHandler implements ProximityListener {

    private static final Logger logger =
            Logger.getLogger(ConeOfSilenceMessageHandler.class.getName());

    private ConeOfSilenceCell coneOfSilenceCell;

    private ChannelComponent channelComp;

    public ConeOfSilenceMessageHandler(ConeOfSilenceCell coneOfSilenceCell) {
	this.coneOfSilenceCell = coneOfSilenceCell;

	channelComp = coneOfSilenceCell.getComponent(ChannelComponent.class);

	logger.fine("Channel comp is " + channelComp);

        ChannelComponent.ComponentMessageReceiver msgReceiver =
	    new ChannelComponent.ComponentMessageReceiver() {
                public void messageReceived(CellMessage message) {
                    //ConeOfSilenceResponseMessage msg = (ConeOfSilenceResponseMessage)message;

		    //processMessage((ConeOfSilenceResponseMessage) message);
                }
            };

        //channelComp.addMessageReceiver(CallEndedResponseMessage.class, msgReceiver);

	ProximityComponent comp = new ProximityComponent(coneOfSilenceCell);

	BoundingVolume[] boundingVolume = new BoundingVolume[1];

	boundingVolume[0] = coneOfSilenceCell.getLocalBounds();

	comp.addProximityListener(this, boundingVolume);

	coneOfSilenceCell.addComponent(comp);
    }

    public void processMessage(final Message message) {
    //public void processMessage(final ConeOfSilenceResponseMessage message) {
	//if (message instanceof CallEndedResponseMessage) {
    }
    
    public void leftChannel(ClientChannel arg0) {
        // ignore
    }
    
   public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume,
	    int proximityIndex) {

	logger.info("cellID " + cell.getCellID() + " viewCellID " + viewCellID + " entered = " + entered);

	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

	channelComp.send(new ConeOfSilenceEnterCellMessage(cell.getCellID(), viewCellID, sc.getCallID(), entered));
   }

}
