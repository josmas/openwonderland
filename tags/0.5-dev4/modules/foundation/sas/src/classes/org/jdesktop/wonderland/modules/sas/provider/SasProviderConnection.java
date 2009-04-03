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

import com.jme.math.Vector2f;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageList;
import org.jdesktop.wonderland.modules.sas.common.SasProviderConnectionType;
import org.jdesktop.wonderland.modules.sas.common.SasProviderLaunchMessage;

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
        
        System.err.println("**** Received message from server: " + message);

        if (!(message instanceof SasProviderLaunchMessage)) {
            throw new RuntimeException("Unexpected message type "+message.getClass().getName());
        }
        SasProviderLaunchMessage msg = (SasProviderLaunchMessage) message;

        System.err.println("################### Received launch message from server");
        System.err.println("execCap = " + msg.getExecutionCapability());
        System.err.println("appName = " + msg.getAppName());
        System.err.println("command = " + msg.getCommand());

        if (listener == null) {
            logger.warning("No provider listener is registered.");
            respondFail();
        }

        System.err.println("################### Attempting to launch X app");
        String connInfo = listener.launch(msg.getAppName(), msg.getCommand(), 
                                                new Vector2f(0.01f, 0.01f)/*TODO: msg.getPixelScale()*/);
        if (connInfo != null) {
            respondSuccess(connInfo);
        } else {
            respondFail();
        }
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
    private void respondSuccess (String connInfo) {
        // TODO
        System.err.println("****** Respond success.");
    }

    /**
     * Respond with a failure message.
     */
    private void respondFail () {
        // TODO
        System.err.println("****** Respond fail.");
    }
}
