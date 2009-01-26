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
package org.jdesktop.wonderland.client.cell;

import org.jdesktop.wonderland.common.cell.ComponentLookupClass;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.ClientConnection.Status;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * A Component that provides a cell specific communication channel with 
 * the server.
 * 
 * @author paulby
 */
@ExperimentalAPI
@ComponentLookupClass(ChannelComponent.class)
public class ChannelComponentImpl extends ChannelComponent {
    
    private HashMap<Class, ComponentMessageReceiver> messageReceivers = new HashMap();
    private HashMap<CellID, ChannelComponentRef> refChannelComponents = new HashMap();
    
    private CellChannelConnection connection;
    
    public ChannelComponentImpl(Cell cell) {
        super(cell);
        setCellChannelConnection(cell.getCellCache().getCellChannelConnection());
    }


    /**
     * Notification of the CellChannelConnection to use when sending
     * data to the server for this cell.  This method will be called 
     * automatically at cell creation time.
     */
    public void setCellChannelConnection(CellChannelConnection connection) {
        this.connection = connection;
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
        Object old = messageReceivers.put(msgClass, receiver);

        if (old!=null)
            throw new IllegalStateException("Duplicate Message class added "+msgClass);
    }
    
    /**
     * Remove the message receiver listening on the specifed message class
     * @param msgClass
     */
    public void removeMessageReceiver(Class<? extends CellMessage> msgClass) {
        messageReceivers.remove(msgClass);
    }
    
    /**
     * Dispatch messages to any receivers registered for the particular message class
     * @param message
     */
    public void messageReceived(CellMessage message ) {
//        System.err.println("---> Impl received message for "+message.getCellID()+"   impl cell "+(cell.getCellID())+"  recievers "+messageReceivers.size());
        if (message.getCellID().equals(cell.getCellID())) {
//        System.out.println("Receved "+message);
            ComponentMessageReceiver recvRef = messageReceivers.get(message.getClass());
            if (recvRef==null) {
                Logger.getAnonymousLogger().warning("No listener for message "+message.getClass()+"  from cell "+cell.getClass().getName());
                for(Class s : messageReceivers.keySet()) {
                    System.err.println(s.getName());
                }
                return;
            }

            recvRef.messageReceived(message);
        } else {
            ChannelComponentRef ref = refChannelComponents.get(cell.getCellID());
            if (ref==null) {
                Logger.getAnonymousLogger().severe("No ChannelComponentRef for cell "+cell.getCellID());
                return;
            }
            ref.messageReceived(message);
        }
    }
    
    public Status getStatus() {
        return connection.getStatus();
    }
    
    public void send(CellMessage message, ResponseListener listener) {
        if (message.getCellID() == null) {
            message.setCellID(cell.getCellID());
        }
        connection.send(message, listener);
    }
    
    public void send(CellMessage message) {
        if (message.getCellID() == null) {
            message.setCellID(cell.getCellID());
        }
        connection.send(message);
    }

    /**
     * Register a channel component ref with this real channel component
     * @param ref
     */
    void addChannelComponentRef(ChannelComponentRef ref, CellID cellID) {
        refChannelComponents.put(cellID, ref);
    }

    void removeChannelComponentRef(CellID cellID) {
        refChannelComponents.remove(cellID);
    }
    
}
