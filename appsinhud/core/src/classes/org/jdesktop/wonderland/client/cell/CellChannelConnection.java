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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.CellCache.CellCacheListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * Handler for Cell Channels. All data to/from cells is handled via this
 * class.
 * @author jkaplan
 */
@InternalAPI
public class CellChannelConnection extends BaseConnection {
    private static Logger logger = Logger.getLogger(CellChannelConnection.class.getName());

    /** The cell cache this connection is associated with */
    private CellCache cache;

    /** The delayed message handler */
    private DelayedMessageHandler delayQueue = new DelayedMessageHandler();

    public CellChannelConnection() {
        super();
    }
    
    /**
     * Get the type of client
     * @return CellChannelConnectionType.CELL_CLIENT_TYPE
     */
    public ConnectionType getConnectionType() {
        return CellChannelConnectionType.CLIENT_TYPE;
    }

    /**
     * Send a cell message to a specific cell on the server
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#send(WonderlandClient, Message)
     * 
     * @param message the cell message to send
     */
    public void send(CellMessage message) {
        super.send(message);
    }
    
    /**
     * Send a cell message to a specific cell on the server with the given
     * listener.
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#send(WonderlandClient, Message, ResponseListener)
     * 
     * @param message the message to send
     * @param listener the response listener to notify when a response
     * is received.
     */
    public void send(CellMessage message, ResponseListener listener) {
        super.send(message, listener);
    }
    
    /**
     * Send a cell messag to a specific cell on the server and wait for a 
     * response.
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#sendAndWait(WonderlandClient, Message)
     * 
     * @param message the message to send
     * @throws InterruptedException if there is a problem sending a message
     * to the given cell
     */
    public ResponseMessage sendAndWait(CellMessage message)
        throws InterruptedException
    {
        return super.sendAndWait(message);
    }

    /**
     * Set the cell cache for this connection
     * @param cache the cache
     */
    void setCellCache(CellCache cache) {
        this.cache = cache;
        
        // add a listener for dealing with cell delay queues
        cache.addCellCacheListener(delayQueue);
    }

    /**
     * Handle a message from the server
     * @param message the message to handle
     */
    public void handleMessage(Message message) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Handling Message "+message.getClass().getName());
        }
        
        CellMessage cellMessage = (CellMessage)message;
        CellCache cellCache = ClientContext.getCellCache(getSession());
        
        if (cellCache==null) {
            logger.severe("Unable to deliver CellMessage, CellCache is null");
            return;
        }
        
        Cell cell = cellCache.getCell(cellMessage.getCellID());
        if (cell == null) {
            delayQueue.delay(cellMessage);
            return;
        }
        
        ChannelComponent channelComp = cell.getComponent(ChannelComponent.class);
        if (channelComp==null) {
            logger.severe("Attempting to deliver message to cell that does not have a ChannelComponent "+cell.getCellID());
//            throw new RuntimeException("Illegal message target");
            return;
        }
        
        channelComp.messageReceived(cellMessage);
    }

    /**
     * Delay messages for cells that we haven't created yet.
     */
    private static class DelayedMessageHandler implements CellCacheListener {
        /** Executor to schedule queue removals */
        private static final ScheduledExecutorService executor = 
                Executors.newSingleThreadScheduledExecutor();

        /** The delay (ms) to wait for cell messages after a cell is unloaded */
        private static final long CLEANUP_DELAY = 1000;

        /** a map from CellID to the message queue for that cell */
        private final Map<CellID, CellQueue> messages =
                new HashMap<CellID, CellQueue>();

        /** noop queue */
        private final CellQueue noopQueue = new NoopQueue();

        /** error queue */
        private final CellQueue errorQueue = new ErrorQueue();

        public synchronized void delay(CellMessage message) {
            logger.warning("Delaying message " + message.getClass() +
                           " to cell " + message.getCellID());

            CellQueue queue = messages.get(message.getCellID());
            if (queue == null) {
                logger.fine("Creating delay queue for " + message.getCellID());

                // create a new queue to delay the messages
                queue = new DelayQueue();
                messages.put(message.getCellID(), queue);
            }

            queue.addMessage(message);
        }

        public synchronized void cellLoaded(CellID cellID, Cell cell) {
            logger.warning("Cell loaded " + cellID);

            // When a cell is loaded, deliver any queued messages for the cell.
            // Since the cell is guaranteed to be in the DISK state at this 
            // point, the messages will just be added to the cell's delay
            // queue.
            CellQueue queue = messages.get(cellID);
            if (queue != null) {
                ChannelComponent cc = cell.getComponent(ChannelComponent.class);
                if (cc == null) {
                    logger.log(Level.WARNING, "No channel component on " +
                               "cell " + cellID);
                    return;
                }
                
                // deliver the messages to the cell
                logger.log(Level.FINE, "Delivering delayed messages to " +
                           "cell " + cellID);
                for (CellMessage message : queue.getMessages()) {
                    cc.messageReceived(message);
                }
            }
            
            // now that the cell is in use, set up a delay queue that
            // throws an error if any further messages are delayed
            messages.put(cellID, errorQueue);
        }

        public synchronized void cellLoadFailed(CellID cellID, String className,
                                   CellID parentCellID, Throwable cause)
        {
            // If a cell fails to load, add a queue that won't store the
            // messages
            logger.log(Level.WARNING, "Failed to load cell " + cellID +
                       " of type " + className +".  Discarding messages.");
            messages.put(cellID, noopQueue);
        }

        public synchronized void cellUnloaded(final CellID cellID, Cell cell) {
            logger.warning("Cell unloaded: " + cellID);

            // When a cell is unloaded, set the queue to an error queue so
            // we will be notified of any messages that come after the
            // unload.  After a little bit of time, remove this error
            // queue so that a new queue will be created if the cell is
            // reloaded.
            // XXX this could cause messages to be lost if a cell is
            // repeatedly unloaded and reloaded in a short timespan XXX
            messages.put(cellID, errorQueue);

            // schedule a task to clean up
            executor.schedule(new Runnable() {
                public void run() {
                    synchronized (DelayedMessageHandler.this) {
                        // make sure the queue hasn't changed in the
                        // interim
                        if (messages.get(cellID) == errorQueue) {
                            messages.remove(cellID);
                        }
                    }
                }
            }, CLEANUP_DELAY, TimeUnit.MILLISECONDS);
        }

        interface CellQueue {
            public void addMessage(CellMessage message);
            public List<CellMessage> getMessages();
        }

        class DelayQueue implements CellQueue {
            private List<CellMessage> messages = new LinkedList<CellMessage>();

            public void addMessage(CellMessage message) {
                messages.add(message);
            }

            public List<CellMessage> getMessages() {
                return messages;
            }
        }

        class ErrorQueue implements CellQueue {
            public void addMessage(CellMessage message) {
                logger.log(Level.WARNING, "Adding unexpected message " +
                           message.getClass() + " for cell " +
                           message.getCellID());
            }

            public List<CellMessage> getMessages() {
                return Collections.emptyList();
            }
        }

        class NoopQueue implements CellQueue {
            public void addMessage(CellMessage message) {
                // do nothing
            }

            public List<CellMessage> getMessages() {
                return Collections.emptyList();
            }
        }
    }
}
