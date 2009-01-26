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

import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.ClientConnection.Status;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.ComponentLookupClass;
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
public class ChannelComponentRef extends ChannelComponent {
    
    private HashMap<Class, ComponentMessageReceiver> messageReceivers = new HashMap();
    private ChannelComponentImpl channelImpl;
    
    public ChannelComponentRef(Cell cell) {
        super(cell);
    }

    @Override
    public void setStatus(CellStatus status) {
        super.setStatus(status);

        Cell parent = cell;
        Cell tmp = parent.getParent();
        // Find first parent that has a full channel impl
        while(tmp!=null && !(parent.getComponent(ChannelComponent.class) instanceof ChannelComponentImpl)) {
            parent = tmp;
            tmp = parent.getParent();
        }

        channelImpl = (ChannelComponentImpl) parent.getComponent(ChannelComponent.class);

        switch(status) {
            case BOUNDS :
                channelImpl.addChannelComponentRef(this, cell.getCellID());
                break;
            case DISK :
                channelImpl.removeChannelComponentRef(cell.getCellID());
                break;
        }
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
     * @param sender
     * @param session
     * @param message
     */
    public void messageReceived(CellMessage message ) {
//        System.out.println("Receved "+message);
//        System.err.println("---> Ref received message for "+message.getCellID());
        ComponentMessageReceiver recvRef = messageReceivers.get(message.getClass());
        if (recvRef==null) {
            Logger.getAnonymousLogger().warning("No listener for message "+message.getClass()+"  from cell "+cell.getClass().getName());
           
            return;
        }
        
        recvRef.messageReceived(message);
    }
    
    public Status getStatus() {
        return channelImpl.getStatus();
    }
    
    public void send(CellMessage message, ResponseListener listener) {
        if (message.getCellID() == null) {
            message.setCellID(cell.getCellID());
        }
        channelImpl.send(message, listener);
    }
    
    public void send(CellMessage message) {
        if (message.getCellID() == null) {
            message.setCellID(cell.getCellID());
        }
        channelImpl.send(message);
    }
}
