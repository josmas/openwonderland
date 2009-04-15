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
package org.jdesktop.wonderland.modules.textchat.server;

import java.io.Serializable;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.textchat.common.TextChatMessage;
import org.jdesktop.wonderland.modules.textchat.common.TextChatConnectionType;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Handles text chat messages from the client.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
class TextChatConnectionHandler implements ClientConnectionHandler, Serializable {

    private static Logger logger = Logger.getLogger(TextChatConnectionHandler.class.getName());

    public ConnectionType getConnectionType() {
        return TextChatConnectionType.CLIENT_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        // ignore
    }

    public void clientConnected(WonderlandClientSender sender,
            WonderlandClientID clientID, Properties properties) {
        // ignore
    }

    public void clientDisconnected(WonderlandClientSender sender,
            WonderlandClientID clientID) {
        // ignore
    }

    public void messageReceived(WonderlandClientSender sender,
            WonderlandClientID clientID, Message message) {

        // Check to see if the message is a meant for everyone by looking at
        // the "to" field. If so, then echo the message back to all clients
        // except the one that sent the message.
        TextChatMessage tcm = (TextChatMessage)message;
        String toUser = tcm.getToUserName();
        Set<WonderlandClientID> clientIDs = sender.getClients();

        if (toUser == null || toUser.equals("") == true) {
            clientIDs.remove(clientID);
            sender.send(clientIDs, message);
            return;
        }

        // Otherwise, we need to send the message to a specific client, based
        // upon the "to" field. Loop through the list of clients and find the
        // one with the matching user name
        for (WonderlandClientID id : clientIDs) {
            String name = id.getSession().getName();
            logger.warning("Looking at " + name + " for " + toUser);
            if (name.equals(toUser) == true) {
                sender.send(id, message);
                return;
            }
        }
    }
}
