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
package org.jdesktop.wonderland.server.comms;

import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 * Implementation of CommsManager
 * @author jkaplan
 */
class CommsManagerImpl 
        implements CommsManager, ManagedObject, Serializable 
{
    /** a map from protocol names to protocol objects */
    private Map<String, CommunicationsProtocol> protocols;
    
    /**
     * Create a new instance of CommsManagerImpl.
     */
    public CommsManagerImpl() {
        // create the protocol map
        protocols = new HashMap<String, CommunicationsProtocol>();
    }
    
    /**
     * Static initializer sets up the ProtocolSessionListener and
     * WonderlandSessionListener
     */
    public static void initialize() {
         // initialize the default session listener
        ProtocolSessionListener.initialize();
        
        // initialize the Wonderland session listener
        WonderlandSessionListener.initialize();
    }
    
    public void registerProtocol(CommunicationsProtocol protocol) {
        protocols.put(protocol.getName(), protocol);
    }

    public void unregisterProtocol(CommunicationsProtocol protocol) {
        protocols.remove(protocol.getName());
    }

    public CommunicationsProtocol getProtocol(String name) {
        return protocols.get(name);
    }

    public Set<CommunicationsProtocol> getProtocols() {
        return Collections.unmodifiableSet(new HashSet(protocols.values()));
    }

    public CommunicationsProtocol getProtocol(ClientSession session) {
        return ProtocolSessionListener.getProtocol(session);
    }
    
    public Set<ClientSession> getClients(CommunicationsProtocol protocol) {
        return ProtocolSessionListener.getClients(protocol);
    }

    public void registerClientHandler(ClientConnectionHandler handler) {
        WonderlandSessionListener.registerClientHandler(handler);
    }
  
    public void unregisterClientHandler(ClientConnectionHandler handler) {
        WonderlandSessionListener.unregisterClientHandler(handler);
    }

    public ClientConnectionHandler getClientHandler(ConnectionType clientType) {
        return WonderlandSessionListener.getClientHandler(clientType);
    }

    public Set<ClientConnectionHandler> getClientHandlers() {
        return WonderlandSessionListener.getClientHandlers();
    }
    
    public WonderlandClientSender getSender(ConnectionType clientType) {
        return WonderlandSessionListener.getSender(clientType);
    }
}
