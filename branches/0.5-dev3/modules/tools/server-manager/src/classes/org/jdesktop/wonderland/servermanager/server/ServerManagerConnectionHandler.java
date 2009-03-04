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
package org.jdesktop.wonderland.servermanager.server;

import java.util.Properties;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import com.sun.sgs.app.ClientSession;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.servermanager.common.PingRequestMessage;
import org.jdesktop.wonderland.servermanager.common.PingResponseMessage;
import org.jdesktop.wonderland.servermanager.common.ServerManagerConnectionType;

/**
 * Test listener, will eventually support Server Manager
 * 
 * @author paulby
 */
public class ServerManagerConnectionHandler 
        implements ClientConnectionHandler, Serializable
{
    private static final Logger logger =
            Logger.getLogger(ServerManagerConnectionHandler.class.getName());
    
    public ServerManagerConnectionHandler() {
        super();
    }

    public ConnectionType getConnectionType() {
        return ServerManagerConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        logger.info("Sever manager connection registered");
    }

    public void clientConnected(WonderlandClientSender sender, 
                                WonderlandClientID clientID,
                                Properties properties) 
    {
        logger.fine("ServerManager client connected");
    }

    public void messageReceived(WonderlandClientSender sender, 
                                WonderlandClientID clientID,
                                Message message) 
    {
        if (message instanceof PingRequestMessage) {
            logger.fine("Received ping message");
            PingRequestMessage req = (PingRequestMessage) message;
            PingResponseMessage resp = new PingResponseMessage(req);
            sender.send(clientID, resp);
        }
    }

    public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
        logger.fine("ServerManager client disconnected");
    }
}
