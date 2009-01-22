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
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author paulby
 */
public class ChannelComponentRefMO extends ChannelComponentMO {

    private HashMap<Class, ManagedReference<ComponentMessageReceiver>> messageReceivers = new HashMap();
    private ManagedReference<ChannelComponentImplMO> channelImplRef;
    
    public ChannelComponentRefMO(CellMO cell) {
        super(cell);
    }
    
    @Override
    protected void setLive(boolean live) {
        CellMO parent = cellRef.get();
        CellMO tmp = parent.getParent();
        // Find first parent that has a full channel implementation
        while(tmp!=null && !(parent.getComponent(ChannelComponentMO.class) instanceof ChannelComponentImplMO)) {
            parent = tmp;
            tmp = parent.getParent();
        }

        ChannelComponentImplMO channelImpl = (ChannelComponentImplMO) parent.getComponent(ChannelComponentMO.class);
        channelImplRef = AppContext.getDataManager().createReference(channelImpl);

        AppContext.getDataManager().markForUpdate(this);
        if (live)
            channelImpl.addChannelComponentRef(this, cellID);
        else
            channelImpl.removeChannelComponentRef(cellID);
    }

    /**
     * Send message to all clients on this channel
     * @param senderID the id of the sender session, or null if this
     * message being sent by the server
     * @param message
     *
     */
    public void sendAll(WonderlandClientID senderID, CellMessage message) {
        if (message.getCellID()==null)
            message.setCellID(cellID);

//        System.err.println("---> Ref Sending message for cell "+message.getCellID());
        channelImplRef.get().sendAll(senderID, message);
    }
    
    /**
     * Add user to the cells channel, if there is no channel simply return
     * @param userID
     */
    public void addUserToCellChannel(WonderlandClientID clientID) {
        channelImplRef.getForUpdate().addUserToCellChannel(clientID);
    }
    
    /**
     * Remove user from the cells channel
     * @param userID
     */
    public void removeUserFromCellChannel(WonderlandClientID clientID) {
        channelImplRef.getForUpdate().removeUserFromCellChannel(clientID);
    }
     
    /**
     * Register a receiver for a specific message class. Only a single receiver
     * is allowed for each message class, calling this method to add a duplicate
     * receiver will cause an IllegalStateException to be thrown.
     * 
     * @param msgClass
     * @param receiver
     */
    public void addMessageReceiver(Class<? extends CellMessage> msgClass, ComponentMessageReceiver receiver) {
        Object old = messageReceivers.put(msgClass, AppContext.getDataManager().createReference(receiver));
        if (old!=null)
            throw new IllegalStateException("Duplicate Message class added "+msgClass);
    }

    @Override
    public void removeMessageReceiver(Class<? extends CellMessage> msgClass) {
        messageReceivers.remove(msgClass);
    }

    
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.client.cell.ChannelComponentRef";
    }

    /**
     * Dispatch messages to any receivers registered for the particular message class
     * @param sender
     * @param session
     * @param message
     */
    void messageReceived(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                CellMessage message ) {

        ManagedReference<ComponentMessageReceiver> recvRef = messageReceivers.get(message.getClass());
        if (recvRef==null) {
            Logger.getAnonymousLogger().warning("No listener for message "+message.getClass());
            return;
        }

        recvRef.get().messageReceived(sender, clientID, message);
    }

}
