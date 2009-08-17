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

import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardCompoundCellMessage;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardAction;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardAction.Action;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardCellMessage;

/**
 * A buffered message sender that coalesces similar messages before
 * sending them
 *
 * @author nsimpson
 */

@ExperimentalAPI
public class BufferedCompoundMessageSender extends BufferedSender {
    
    private static final Logger logger =
            Logger.getLogger(BufferedCompoundMessageSender.class.getName());
    
    private Action currentAction;
    private Date lastSend;
    private static final int MAX_IDLE_TIME = 5*1000;      // 5 seconds
    private static final int ACTIVE_INTERVAL = 250;       // 0.25 seconds
    
    /** The cell channel communications component. */
    private ChannelComponent channelComp;

    public BufferedCompoundMessageSender (ChannelComponent channelComp) {
        super();
        lastSend = new Date();
        setSendInterval(ACTIVE_INTERVAL);
	this.channelComp = channelComp;
    }
   
    /**
     * Add a message to the queue
     * @param obj the message to add to the queue
     */
    @Override
    public synchronized void enqueue(Object obj) {
        super.enqueue(obj);
        if (isSenderEnabled() == false) {
            // scheduler was running at idle rate, ramp it up to respond
            // to potential future activity
            logger.finest("message enqueued, enabling sender");
            enableSender(true);
        }
    }
    
    /**
     * Remove messages from the queue and send them.
     * Sequential messages with the same action are coalesced into a
     * single message
     */
    @Override
    public synchronized void dequeue() {
        if (isSenderEnabled()) {
            if (queue.size() > 0) {
                WhiteboardCompoundCellMessage cmsg = null;
                currentAction = WhiteboardAction.NO_ACTION;
                
                Iterator iter = queue.iterator();
                while (iter.hasNext()) {
                    WhiteboardCellMessage msg = (WhiteboardCellMessage)queue.remove();
                    Action action = msg.getAction();
                    
                    if (cmsg != null) {
                        // a coalesced message is under construction
                        if (action != currentAction) {
                            // new action, send the coalesced message for the
                            // previous action
                            send(cmsg);
                            cmsg = null;
                        } else {
                            // same action, add action position
                            cmsg.addPosition(msg.getPosition());
                        }
                    }
                    if (cmsg == null) {
                        // new action, create a new coalesced message
                        currentAction = action;
                        cmsg = new WhiteboardCompoundCellMessage(msg);
                    }
                }
                if (cmsg != null) {
                    // send any remaining queued messages
                    send(cmsg);
                }
            } else {
                // nothing to dequeue
                Date now = new Date();
                if (((now.getTime() - lastSend.getTime()) > MAX_IDLE_TIME) &&
                        (isSenderEnabled() == true)) {
                    logger.finest("queue idle, disabling sender");
                    enableSender(false);
                }
            }
        }
    }
    
    /**
     * Send a queued message
     * @param obj the message to send
     */
    public void send(Object obj) {
        WhiteboardCompoundCellMessage cmsg = (WhiteboardCompoundCellMessage)obj;
        int n = (cmsg.getPositions() != null) ? cmsg.getPositions().size() : 0;
        logger.finest("sending coalesced message containing " + n + " messages: " + cmsg);
        channelComp.send(cmsg);
        
        lastSend = new Date();
    }
}
