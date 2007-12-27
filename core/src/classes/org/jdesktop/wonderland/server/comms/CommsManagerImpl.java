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

import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.common.messages.Message;
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
    
    /**
     * {@inheritDoc}
     */
    public void registerProtocol(CommunicationsProtocol protocol) {
        protocols.put(protocol.getName(), protocol);
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterProtocol(CommunicationsProtocol protocol) {
        protocols.remove(protocol.getName());
    }

    /**
     * {@inheritDoc}
     */
    public CommunicationsProtocol getProtocol(String name) {
        return protocols.get(name);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<CommunicationsProtocol> getProtocols() {
        return Collections.unmodifiableCollection(protocols.values());
    }

    /**
     * {@inheritDoc}
     */
    public void sendToAllClients(Message message) {
        WonderlandSessionListener.sendToAllClients(message);
    }

    /**
     * {@inheritDoc}
     */
    public void registerMessageListener(Class<? extends Message> messageClass, 
                                        ClientMessageListener listener) 
    {
        WonderlandSessionListener.registerMessageListener(messageClass, listener);
    }
    
    /**
     * {@inheritDoc}
     */
    public void registerConnectionListener(ClientConnectionListener listener) 
    {
        WonderlandSessionListener.registerConnectionListener(listener);
    }
}
