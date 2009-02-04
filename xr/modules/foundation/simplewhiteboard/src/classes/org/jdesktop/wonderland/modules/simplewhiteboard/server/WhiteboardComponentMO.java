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
package org.jdesktop.wonderland.modules.simplewhiteboard.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.simplewhiteboard.common.cell.WhiteboardCompoundCellMessage;
import org.jdesktop.wonderland.modules.simplewhiteboard.server.cell.WhiteboardCellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

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

    /** 
     * Create a new instance of WhiteboardComponentMO. 
     * @param cell The cell to which this component belongs.
     * @throws IllegalStateException If the cell does not already have a ChannelComponent IllegalStateException will be thrown.
     */
    public WhiteboardComponentMO(WhiteboardCellMO cell) {
        super(cell);

        ChannelComponentMO channelComponent = (ChannelComponentMO) cell.getComponent(ChannelComponentMO.class);
        if (channelComponent == null) {
            throw new IllegalStateException("Cell does not have a ChannelComponent");
        }
        channelComponentRef = AppContext.getDataManager().createReference(channelComponent);

        channelComponent.addMessageReceiver(WhiteboardCompoundCellMessage.class,
                new ComponentMessageReceiverImpl(this, cellRef));
    }

    /**
     * Broadcast the given message to all clients.
     * @param sourceID the originator of this message, or null if it originated
     * with the server
     * @param message The message to broadcast.
     */
    public void sendAllClients(WonderlandClientID clientID, WhiteboardCompoundCellMessage message) {
        CellMO cell = cellRef.getForUpdate();
        ChannelComponentMO channelComponent = channelComponentRef.getForUpdate();
        channelComponent.sendAll(clientID, message);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.simplewhiteboard.client.WhiteboardComponent";
    }

    /**
     * Receiver for for whiteboard messages.
     * Note: inner classes of managed objects must be non-static.
     */
    private static class ComponentMessageReceiverImpl implements ComponentMessageReceiver {

        private ManagedReference<WhiteboardComponentMO> compRef;
        private ManagedReference<CellMO> cellRef;

        public ComponentMessageReceiverImpl(WhiteboardComponentMO comp, ManagedReference<CellMO> cellRef) {
            compRef = AppContext.getDataManager().createReference(comp);
            this.cellRef = cellRef;
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            WhiteboardCompoundCellMessage cmsg = (WhiteboardCompoundCellMessage) message;
            CellMO cell = cellRef.get();
            ((WhiteboardCellMO) cell).receivedMessage(sender, clientID, cmsg);
        }

        public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            //TODO: Consider making this class a subclass of AbstractComponentMessageReceiver
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
