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
package org.jdesktop.wonderland.modules.sas.server;

import java.util.Properties;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.sas.common.SasProviderConnectionType;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.AppContext;

/**
 * The connection between the SAS provider and the SAS server.
 * 
 * @author deronj
 */
public class SasProviderConnectionHandler implements ClientConnectionHandler, Serializable {

    private static final Logger logger = Logger.getLogger(SasProviderConnectionHandler.class.getName());
    
    /** The SAS server which lives in the Wonderland server. */
    private ManagedReference<SasServer> serverRef;

    public SasProviderConnectionHandler(SasServer server) {
        super();
        serverRef = AppContext.getDataManager().createReference(server);
    }

    public ConnectionType getConnectionType() {
        return SasProviderConnectionType.CLIENT_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        logger./*TODO: info*/severe("Sas provider connection registered.");
    }

    public void clientConnected(WonderlandClientSender sender, 
                                WonderlandClientID clientID,
                                Properties properties) 
    {
        SasServer server = (SasServer) serverRef.get();
        server.providerConnected(sender, clientID);
    }

    public void messageReceived(WonderlandClientSender sender, 
                                WonderlandClientID clientID,
                                Message message) 
    {
        logger.severe("***** Message received from sas provider: " + message);

        /* TODO
        if (message instanceof PingRequestMessage) {

            logger.fine("Received ping message");
            PingRequestMessage req = (PingRequestMessage) message;
            PingResponseMessage resp = new PingResponseMessage(req);

            sender.send(clientID, resp);
        }
        */
    }

    public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
        logger./*TODO: fine*/severe("SasProvider client disconnected.");
        SasServer server = (SasServer) serverRef.get();
        server.providerDisconnected(sender, clientID);
    }
}
