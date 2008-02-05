/**
 * Project Wonderland
 *
 * $RCSfile:$
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
 * $Revision:$
 * $Date:$
 * $State:$
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
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.server.ProtocolSessionListener;
import org.jdesktop.wonderland.server.WonderlandSessionListener;

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

    public Set<ClientSession> getClients(CommunicationsProtocol protocol) {
        return ProtocolSessionListener.getClients(protocol);
    }

    public void registerClientHandler(ClientHandler handler) {
        WonderlandSessionListener.registerClientHandler(handler);
    }
  
    public void unregisterClientHandler(ClientHandler handler) {
        WonderlandSessionListener.unregisterClientHandler(handler);
    }

    public ClientHandler getClientHandler(ClientType clientType) {
        return WonderlandSessionListener.getClientHandler(clientType);
    }

    public Set<ClientHandler> getClientHandlers() {
        return WonderlandSessionListener.getClientHandlers();
    }
    
    public WonderlandClientChannel getChannel(ClientType clientType) {
        return WonderlandSessionListener.getChannel(clientType);
    }
}
