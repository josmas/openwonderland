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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.ClientConnection.Status;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 *
 * A Component that provides a cell specific communication channel with 
 * the server.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class ChannelComponent extends CellComponent {
    private static final Logger logger =
            Logger.getLogger(ChannelComponent.class.getName());

    /** receivers for each message type */
    private final Map<Class, ComponentMessageReceiver> messageReceivers =
            new LinkedHashMap<Class, ComponentMessageReceiver>();

    /** the connection to send on */
    private CellChannelConnection connection;

    /** a list of delayed messages to replay when the cell becomes active */
    private List<CellMessage> delayedMessages;

    /** a lock to make sure delayed messages are delivered before any others */
    private final Object delayLock = new Object();

    public ChannelComponent(Cell cell) {
        super(cell);

        setCellChannelConnection(cell.getCellCache().getCellChannelConnection());

        // add a status change listener to the parent cell.  When the status
        // changes to bounds, this listener will deliver delayed messages
        cell.addStatusChangeListener(new CellStatusChangeListener() {
            public void cellStatusChanged(Cell cell, CellStatus status) {
                logger.fine("[ChannelComponent] status of cell " +
                               cell.getCellID() + " is " + status);

                if (status == CellStatus.INACTIVE) {
                    deliverDelayedMessages();
                }
            }
        });
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

        // XXX hack to ignore duplicate registrations XXX
        if (old != null && old != receiver)
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
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("---> Impl received message for " + 
                          message.getCellID() + "   impl cell " + 
                          cell.getCellID() + "  recievers " + 
                          messageReceivers.size());
        }

        // make sure the message is being delivered to the right cell
        if (!message.getCellID().equals(cell.getCellID())) {
            logger.severe("Message for wrong cell " + message.getCellID());
            return;
        }

        // if the component status is DISK it means the cell has been
        // instantiated but not yet activated to receive messages.  Queue
        // up messages to deliver when the cell becomes active
        synchronized (delayLock) {
            if (delayedMessages != null || cell.getStatus() == CellStatus.DISK) {
                logger.warning("Delaying message " + message.getClass() +
                               " to cell " + cell.getCellID() +
                               " (" + cell.getClass().getName() + ")");
                if (delayedMessages == null) {
                    delayedMessages = new LinkedList<CellMessage>();
                }

                delayedMessages.add(message);
                return;
            }
        }

        deliverMessage(message);
    }

    /**
     * Deliver the message to the proper receiver on the cell
     * @param message the message to deliver
     */
    protected void deliverMessage(CellMessage message) {
        // if we get here, we can actually deliver the message
        ComponentMessageReceiver recvRef = messageReceivers.get(message.getClass());
        if (recvRef == null) {
            logger.warning("No listener for message " + message.getClass() +
                           " from cell " + cell.getClass().getName() +
                           " status " + cell.getStatus());
            return;
        }

        recvRef.messageReceived(message);
    }

    /**
     * When the status is set to bounds, deliver any queued messages
     */
    protected void deliverDelayedMessages() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Delivering delayed messages to cell " +
                       cell.getCellID() + " (" + cell.getClass().getName() + ")");
        }

        // deliver delayed messaged
        synchronized (delayLock) {
            if (delayedMessages != null) {
//                logger.warning("Delivering " + delayedMessages.size() +
//                               " messages to cell " + cell.getCellID());

                try {
                    for (CellMessage message : delayedMessages) {
                        deliverMessage(message);
                    }
                } finally {
                    // make sure to clear the delayed messages list, otherwise
                    // all future messages will be delayed
                    delayedMessages = null;
                }
            }
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

    public ResponseMessage sendAndWait(CellMessage message)
        throws InterruptedException
    {
        if (message.getCellID() == null) {
            message.setCellID(cell.getCellID());
        }
        return connection.sendAndWait(message);
    }
    
    static public interface ComponentMessageReceiver {
        public void messageReceived(CellMessage message );        
    }
}
