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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.sas.provider;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageList;
import org.jdesktop.wonderland.modules.sas.common.SasProviderConnectionType;

/**
 * The SAS provider client.
 *
 * @author deronj
 */

public class SasProviderConnection extends BaseConnection {

    private static final Logger logger = Logger.getLogger(SasProviderConnection.class.getName());
    
    /** The listener for messages from the server over the SAS Provider connection. */
    private SasProviderConnectionListener listener;

    /**
     * Create an instance of SasProviderConnection.
     * @param listener The listener for messages from the server over the SAS Provider connection.
     */
    public SasProviderConnection (SasProviderConnectionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Get the type of client
     * @return CellClientType.CELL_CLIENT_TYPE
     */
    public ConnectionType getConnectionType() {
        return SasProviderConnectionType.CLIENT_TYPE;
    }

    /**
     * Handle a message from the server
     * @param message the message to handle
     */
    public void handleMessage(Message message) {
        if (message instanceof MessageList) {
            List<Message> list = ((MessageList)message).getMessages();
            for(Message m : list)
                handleMessage(m);
            return;
        }
        
        System.err.println("**** Receive message from server: " + message);
        /* TODO: notyet
        if (!(message instanceof SasServerToProviderMessage))
            throw new RuntimeException("Unexpected message type "+message.getClass().getName());
        
        SasServerToProvider msg = (SasServerToProviderMessage)message;
        switch(msg.getActionType()) {

        case LAUNCH:
            if (listener == null) {
                logger.warning("No provider listener is registered.");
                respondFail();
            }
            Serializable connInfo = listener.launch(msg.getAppName(), msg.getCommand(), msg.getPixelScale());
            if (connInfo != null) {
                respondSuccess(connInfo);
            } else {
                respondFail();
            }
            break;

        default :
            logger.warning("Message type not implemented "+msg.getActionType());
        }
         **/
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnected() {
        listener = null;
    }
    
    /**
     * Respond with a success message which contains the given connection info.
     */
    private void respondSuccess (Serializable connInfo) {
        // TODO
    }

    /**
     * Respond with a failure message.
     */
    private void respondFail (Serializable connInfo) {
        // TODO
    }
}
