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
import java.util.Set;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * Manage communications protocols.
 * @author jkaplan
 */
@ExperimentalAPI
public interface CommsManager {
    /**
     * Add a new communications protocol.  When clients connect, they request
     * a communications protocol.  The protocol controls how they communicate
     * with the Wonderland server.  All communications from that user will
     * be processed by the session listener associated with the given protocol.
     * @param protocol the protocol to register
     */
    public void registerProtocol(CommunicationsProtocol protocol);
    
    /**
     * Remove a communications protocol from the list of available protocols
     * @param protocol the protocol to remove
     */
    public void unregisterProtocol(CommunicationsProtocol protocol);
    
    /**
     * Get a communications protocol by name
     * @param name the name of the protocol to search for
     * @return the protocol registered with the given name, or null
     * if no protocol exists with that name
     */
    public CommunicationsProtocol getProtocol(String name);
    
    /**
     * Get all communications protocols
     * @return all available protocols
     */
    public Set<CommunicationsProtocol> getProtocols();
    
    /**
     * Get all sessions that are connected using the given protocol
     * @param protocol the protocol to get session for
     * @return all sessions connected with the given protocol
     */
    public Set<ClientSession> getClients(CommunicationsProtocol protocol);
    
    /**
     * Register a handler that will handle connections from a particular
     * WonderlandClient type.  This listener will be notified of all 
     * connections and messages from listeners of the given type.  
     * <p>
     * This handler will be notified when a client session, which is connected
     * via the WonderlandProtocol, attaches a new WonderlandClient using the
     * <code>WonderlandSession.attach()</code> method.  It will be notified of
     * all messages sent using that client type.
     * <p>
     * The Handler will be stored in the Darkstar data store, so it must be 
     * either Serializable or a ManagedObject.  If a handler is a ManagedObject,
     * only a single copy of the handler will exist, and all messages will
     * be forwarded to this object.  If the handler is not a managed object,
     * a separate copy of the handler will be created in each WonderlandSession
     * that attaches a client of the given type.  It is recommended that
     * handlers that expect a large number messages be Serializable. 
     * <p>
     * This is identical to calling 
     * <code>WonderlandSessionListener.registerClientHandler()</code>.
     * 
     * @param handler the handler to handle messages from the given client
     * type
     */
    public void registerClientHandler(ClientHandler handler);
    
    /**
     * Get all clients connected via the WonderlandProtocol that have
     * attached clients of the given type.
     * @param clientType the type of client
     * @return all clients who have attached the given client type, or an
     * empty set if no clients are attached
     * @throws IllegalStateException if no handler is registered for the given
     * type
     */
    public Set<WonderlandClientSession> getClients(ClientType clientType);
    
    /**
     * Get a channel that can be used to send messages to all clients
     * of a given ClientType
     * @param clientType the type of client
     * @return a channel for sending to all clients of the given type
     * @throws IllegalStateException if no handler is registered for the given
     * type
     */
    public WonderlandClientChannel getChannel(ClientType clientType);
}
