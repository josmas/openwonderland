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

import java.util.Properties;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import com.sun.sgs.app.ClientSession;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.servermanager.common.ServerManagerConnectionType;

/**
 * Test listener, will eventually support Server Manager
 * 
 * @author paulby
 */
public class ServerManagerConnectionHandler implements ClientConnectionHandler {
    private static final Logger logger =
            Logger.getLogger(ServerManagerConnectionHandler.class.getName());
    
    public ServerManagerConnectionHandler() {
        super();
    }

    public ConnectionType getConnectionType() {
        return ServerManagerConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clientConnected(WonderlandClientSender sender, ClientSession session, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void messageReceived(WonderlandClientSender sender, ClientSession session, Message message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clientDisconnected(WonderlandClientSender sender, ClientSession session) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
