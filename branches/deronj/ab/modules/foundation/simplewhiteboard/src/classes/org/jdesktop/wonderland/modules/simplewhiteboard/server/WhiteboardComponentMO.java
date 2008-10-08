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
package org.jdesktop.wonderland.modules.simplewhiteboard.server;

import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
import java.util.Collection;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellMO.SpaceInfo;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.SpaceMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardCompoundCellMessage;

/**
 * The server side of the communication component that provides communication between the whiteboard client and server.
 * Requires ChannelComponent to also be connected to the cell prior to construction.
 *
 * @author deronj
 */

@ExperimentalAPI
public class WhiteboardComponentMO extends CellComponentMO {

    /** A managed reference to the cell channel communications component */
    private ManagedReference<ChannelComponentMO> channelComponentRef = null;

    /** The cell to which this component belongs. */
    private WhiteboardCellMO cell;

    /** 
     * Create a new instance of WhiteboardComponentMO. 
     * @param cell The cell to which this component belongs.
     * @throws IllegalStateException If the cell does not already have a ChannelComponent IllegalStateException will be thrown.
     */
    public WhiteboardComponentMO (WhiteboardCellMO cell) {
        super(cell);
	this.cell = cell;

        ChannelComponentMO channelComponent = (ChannelComponentMO) cell.getComponent(ChannelComponentMO.class);
        if (channelComponent==null)
            throw new IllegalStateException("Cell does not have a ChannelComponent");
        channelComponentRef = AppContext.getDataManager().createReference(channelComponent); 
                
        channelComponent.addMessageReceiver(WhiteboardCompoundCellMessage.class, new ComponentMessageReceiverImpl(this));
    }
    
    /**
     * Broadcast the given message to all clients.
     * @param message The message to broadcast.
     */
    public void sendAllClients (WhiteboardCompoundCellMessage message) {
        CellMO cell = cellRef.getForUpdate();
        ChannelComponentMO channelComponent = channelComponentRef.getForUpdate();
	channelComponent.sendAll(message);
    }
    
    /**
     * Receiver for for whiteboard messages.
     */
    private class ComponentMessageReceiverImpl implements ComponentMessageReceiver {

        private ManagedReference<WhiteboardComponentMO> compRef;
        
        public ComponentMessageReceiverImpl (WhiteboardComponentMO comp) {
            compRef = AppContext.getDataManager().createReference(comp);
        }

        public void messageReceived (WonderlandClientSender sender, ClientSession session, CellMessage message) {
	    WhiteboardCompoundCellMessage cmsg = (WhiteboardCompoundCellMessage)message;
	    ((WhiteboardCellMO)cell).receivedMessage(sender, session, cmsg);
        }
    }
}
