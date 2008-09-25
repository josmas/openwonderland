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
package org.jdesktop.wonderland.server.simplewhiteboard;

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
    private WhiteboardCell cell;

    /** 
     * Create a new instance of WhiteboardComponentMO. 
     * @param cell The cell to which this component belongs.
     * @throws IllegalStateException If the cell does not already have a ChannelComponent IllegalStateException will be thrown.
     */
    public WhiteboardComponentMO (WhitboardCellMO cell) {
        super(cell);
	this.cell = cell;

        ChannelComponentMO channelComponent = (ChannelComponentMO) cell.getComponent(ChannelComponentMO.class);
        if (channelComponent==null)
            throw new IllegalStateException("Cell does not have a ChannelComponent");
        channelComponentRef = AppContext.getDataManager().createReference(channelComponent); 
                
        channelComponent.addMessageReceiver(CompoundWhiteboardCellMessage.class, new WhiteboardMessageReceiverImpl(this));
    }
    
    /**
     * Broadcast the given message to all clients.
     * @param message The message to broadcast.
     */
    public void sendAllClients (CompoundWhiteboardCellMessage message) {
        CellMO cell = cellRef.getForUpdate();
        ChannelComponentMO channelComponent = channelComponentRef.getForUpdate();
	channelComponent.sendAll(message);
    }
    
    /**
     * Check if the object needs to be added as 'in' other spaces
     */
    private void checkForNewSpace(CellMO cell, Collection<SpaceInfo> currentSpaces) {
        Vector3f origin = cell.getLocalToWorld(cellTransformTmp).getTranslation(v3fTmp);
        
        for(SpaceInfo spaceInfo : currentSpaces) {
            Collection<ManagedReference<SpaceMO>> proximity = spaceInfo.getSpaceRef().get().getSpaces(cell.getWorldBounds());
            for(ManagedReference<SpaceMO> spaceCellRef : proximity) {
                if (spaceCellRef.get().getWorldBounds(null).contains(origin)) {
                    cell.addToSpace(spaceCellRef.getForUpdate());
                }
            }
        }
    }
       
    /**
     * Receiver for for whiteboard messages.
     */
    private static class ComponentMessageReceiverImpl implements ComponentMessageReceiver {

        private ManagedReference<WhiteboardComponentMO> compRef;
        
        public ComponentMessageReceiverImpl (WhiteboardComponentMO comp) {
            compRef = AppContext.getDataManager().createReference(comp);
        }

        public void messageReceived (WonderlandClientSender sender, ClientSession session, CellMessage message) {
	    CompoundWhiteboardCellMessage cmsg = (CompoundWhiteboardCellMessage)message;
	    cell.processMessage(sender, session, cmsg);
        }
    }
}
