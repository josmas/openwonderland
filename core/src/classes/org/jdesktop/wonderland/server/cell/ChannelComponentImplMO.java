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
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedReference;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ComponentLookupClass;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author paulby
 */
@ComponentLookupClass(ChannelComponentMO.class)
public class ChannelComponentImplMO extends ChannelComponentMO {

    private WonderlandClientSender cellSender;
    private ManagedReference<Channel> cellChannelRef;
    private HashMap<Class, ManagedReference<ComponentMessageReceiver>> messageReceivers = new HashMap();

    private HashMap<CellID, ManagedReference<ChannelComponentRefMO>> refChannelComponents = new HashMap();
    
    public ChannelComponentImplMO(CellMO cell) {
        super(cell);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void openChannel() {
        CellMO cell = cellRef.get();
        
        ChannelManager cm = AppContext.getChannelManager();
        Channel cellChannel = cm.createChannel("Cell "+cell.getCellID().toString(), 
                                               null,
                                               Delivery.RELIABLE);
        
        DataManager dm = AppContext.getDataManager();
        cellChannelRef = dm.createReference(cellChannel);
        
        // cache the sender for sending to cell clients.  This saves a
        // Darkstar lookup for every cell we want to send to.
        cellSender = WonderlandContext.getCommsManager().getSender(CellChannelConnectionType.CLIENT_TYPE);
        
    }
    
    /**
     * {@inheritDoc}
     */
    protected void closeChannel() {
        DataManager dm = AppContext.getDataManager();
        Channel channel = cellChannelRef.get();
        dm.removeObject(channel);
        
        cellSender=null;
        cellChannelRef = null;
    }  
    
    @Override
    protected void setLive(boolean live) {
        AppContext.getDataManager().markForUpdate(this);
        if (live)
            openChannel();
        else
            closeChannel();
    }
    
    /**
     * Send message to all clients on this channel
     * @param senderID the id of the sender session, or null if this
     * message being sent by the server
     * @param message
     *
     */
    public void sendAll(WonderlandClientID senderID, CellMessage message) {
        if (cellChannelRef==null) {
            return;
        }

        if (message.getCellID() == null) {
            message.setCellID(cellID);
        }

        if (senderID != null) {
            message.setSenderID(senderID.getID());
        }

//        System.err.println("---> Impl Sending message for cell "+message.getCellID());
//        System.out.println("Sending data "+cellSender.getSessions().size());
        cellSender.send(cellChannelRef.get(), message);
    }
    
    /**
     * Add user to the cells channel, if there is no channel simply return
     * @param userID
     */
    public void addUserToCellChannel(WonderlandClientID clientID) {
        if (cellChannelRef == null)
            return;
            
        cellChannelRef.getForUpdate().join(clientID.getSession());
    }
    
    /**
     * Remove user from the cells channel
     * @param userID
     */
    public void removeUserFromCellChannel(WonderlandClientID clientID) {
        if (cellChannelRef == null)
            return;
            
        cellChannelRef.getForUpdate().leave(clientID.getSession());
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
        return "org.jdesktop.wonderland.client.cell.ChannelComponentImpl";
    }

    /**
     * Register a channel component ref with this real channel component
     * @param ref
     */
    void addChannelComponentRef(ChannelComponentRefMO ref, CellID cellID) {
        refChannelComponents.put(cellID, AppContext.getDataManager().createReference(ref));
    }

    void removeChannelComponentRef(CellID cellID) {
        refChannelComponents.remove(cellID);
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

        CellID msgCellID = message.getCellID();
        if (!msgCellID.equals(cellID)) {
//            ManagedReference<ChannelComponentRefMO> refMO = refChannelComponents.get(msgCellID);
//            if (refMO==null) {
//                Logger.getAnonymousLogger().severe("Unable to find ChannelComponentRef for cell "+msgCellID);
//                return;
//            }
//            refMO.get().messageReceived(sender, clientID, message);
            Logger.getAnonymousLogger().severe("Message delivered to wrong ChannelComponent");
            return;
        }

        ManagedReference<ComponentMessageReceiver> recvRef = messageReceivers.get(message.getClass());
        if (recvRef==null) {
            Logger.getAnonymousLogger().warning("No listener for message "+message.getClass());
            return;
        }
        
        ComponentMessageReceiver receiver = recvRef.get();

        receiver.messageReceived(sender, clientID, message);
        receiver.recordMessage(sender, clientID, message);
    }
    
}
