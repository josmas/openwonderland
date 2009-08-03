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
package org.jdesktop.wonderland.modules.swingwhiteboard.client;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A lazy ClientChannel sender that buffers up messages and sends them
 * on a regular schedule
 *
 * @author nsimpson
 */

@ExperimentalAPI
public abstract class BufferedSender {
    
    private static final Logger logger =
            Logger.getLogger(BufferedSender.class.getName());
    
    protected Timer sendTimer;
    protected boolean senderEnabled = false;
    protected long maxQueueSize = 100; // maximum number of queued events
    protected LinkedList<Object> queue;
    protected long sendInterval = 250; // miliseconds between sends
    
    public BufferedSender() {
        sendTimer = new Timer();
        queue = new LinkedList<Object>();
    }
    
    /**
     * Sets the interval between message sends
     * @param sendInterval the interval in milliseconds
     */
    public void setSendInterval(long sendInterval) {
        this.sendInterval = sendInterval;
    }
    
    /**
     * Gets the interval between message sends
     * @return the send interval in milliseconds
     */
    public long getSendInterval() {
        return sendInterval;
    }
    
    /**
     * Enable or disable periodic sending of messages
     * @param enableSender true to enable sending
     */
    public void enableSender(boolean enableSender) {
        if (this.senderEnabled == true) {
            // sender is currently enabled
            if (enableSender == false) {
                // disabling active sender
                sendTimer.cancel();
                this.senderEnabled = false;
            }
        } else {
            // sender is currently disabled
            if (enableSender == true) {
                // enabling inactive sender
                sendTimer = new Timer();
                sendTimer.scheduleAtFixedRate(new DequeueTask(), 0, getSendInterval());
                this.senderEnabled = true;
            }
        }
    }
    
    /**
     * A timer task
     */
    private class DequeueTask extends TimerTask {
        public void run() {
            if (isSenderEnabled()) {
                logger.finest("starting scheduled dequeue");
                dequeue();
                logger.finest("scheduled dequeue complete");
            }
        }
    }
    
    /**
     * Gets whether the sender is enabled
     * @return true if the sender is enabled, false otherwise
     */
    public boolean isSenderEnabled() {
        return senderEnabled;
    }
    
    /**
     * Set the maximum number of messages that can be queued
     * before the queued messages are sent.
     *
     * The maximum queue size is not currently enforced.
     *
     * @param maxQueueSize the maximum size of the queue
     */
    public void setMaximumQueueSize(long maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }
    
    /**
     * Gets the maximum message queue size.
     *
     * The maximum queue size is not currently enforced.
     *
     * @return the maximum queue size
     */
    public long getMaximumQueueSize() {
        return maxQueueSize;
    }
    
    /**
     * Add a message to the queue
     * @param obj the message to add to the queue
     */
    public synchronized void enqueue(Object obj) {
        queue.addLast(obj);
    }
    
    /**
     * Remove and send messages in the queue
     */
    public synchronized void dequeue() {
        if (isSenderEnabled()) {
            if (queue.size() > 0) {
                Iterator iter = queue.iterator();
                while (iter.hasNext()) {
                    Object obj = queue.remove();
                    logger.finest("sending queued message");
                    send(obj);
                }
            }
        }
    }
    
    /**
     * Send a message
     * @param obj the message to send
     */
    public abstract void send(Object obj);
}
