/**
 * Project Wonderland
 *
 * $Id$
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
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
 */
package org.jdesktop.wonderland.client.comms;

import org.jdesktop.wonderland.client.cell.*;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.client.comms.BaseHandler;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.cell.ViewHandlerType;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.ViewMessage;
import org.jdesktop.wonderland.common.comms.HandlerType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * The ViewHandler handles view setup and communication
 * @author paulby
 */
@ExperimentalAPI
public class ViewHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(ViewHandler.class.getName());
    
    private ArrayList<ViewMessageListener> listeners = new ArrayList();
    
    /**
     * Get the type of client
     * @return CellClientType.CELL_CLIENT_TYPE
     */
    public HandlerType getHandlerType() {
        return ViewHandlerType.CLIENT_TYPE;
    }

    /**
     * Add a listener for avatar actions. This should be called during setup
     * not once the system is running
     * @param listener
     */
    public void addListener(ViewMessageListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Send a message to the associated server view object
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#send(WonderlandClient, Message)
     * 
     * @param message the cell message to send
     */
    public void send(ViewMessage message) {
        super.send(message);
    }
    
    /**
     * Send a message to the associated server view object with the given
     * listener.
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#send(WonderlandClient, Message, ResponseListener)
     * 
     * @param message the message to send
     * @param listener the response listener to notify when a response
     * is received.
     */
    public void send(ViewMessage message, ResponseListener listener) {
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
    public ResponseMessage sendAndWait(ViewMessage message)
        throws InterruptedException
    {
        return super.sendAndWait(message);
    }
    
    /**
     * Handle a message from the server
     * @param message the message to handle
     */
    public void handleMessage(Message message) {
        if (!(message instanceof ViewMessage))
            throw new RuntimeException("Unexpected message type "+message.getClass().getName());
        
        ViewMessage msg = (ViewMessage)message;
        switch(msg.getActionType()) {
            case MOVED :
                for(ViewMessageListener l : listeners) {
                    l.viewMoved(new CellTransform(msg.getRotation(), msg.getLocation()));
                }
                break;
            default :
                logger.warning("Message type not implemented "+msg.getActionType());
        }
    }

    @Override
    public void detached() { 
        // remove any action listeners
        listeners.clear();
    }
    
    /**
     * Listener interface for view move information.
     * Views associated with avatars do not use this mechanism, instead
     * the information is sent directly to the AvatarCell
     */
    public static interface ViewMessageListener {
        /**
         * The view has moved (in world coordinates)
         */
        public void viewMoved(CellTransform transform);
    }
}
