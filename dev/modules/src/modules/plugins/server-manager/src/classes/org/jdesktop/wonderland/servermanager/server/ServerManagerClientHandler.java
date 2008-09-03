/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.servermanager.server;

import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.messages.Message;
import com.sun.sgs.app.ClientSession;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.comms.ClientHandler;
import org.jdesktop.wonderland.server.comms.ClientSender;
import org.jdesktop.wonderland.servermanager.common.ServerManagerClientType;

/**
 * Test listener, will eventually support Server Manager
 * 
 * @author paulby
 */
public class ServerManagerClientHandler implements ClientHandler {
    private static final Logger logger =
            Logger.getLogger(ServerManagerClientHandler.class.getName());
    
    public ServerManagerClientHandler() {
        super();
    }

    public ClientType getClientType() {
        return ServerManagerClientType.CLIENT_TYPE;
    }

    public void clientAttached(ClientSender sender) {
        logger.info("ServerManager: Client attached: " + 
                    sender.getSession().getName());
        
        // send initial state when a client connects
    }

    public void messageReceived(ClientSender sender, Message message) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("ServerManager: Received " + message + 
                          " from session " + sender.getSession().getName());
        }
        
        // handle message
    }

    public void clientDetached(ClientSession session) {
        logger.info("ServerManager: Client detached: " + session.getName());
        
        // handle client detaching
    }
}
