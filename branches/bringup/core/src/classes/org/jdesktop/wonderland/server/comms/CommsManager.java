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

import java.util.Collection;
import org.jdesktop.wonderland.ExperimentalAPI;
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
    public Collection<CommunicationsProtocol> getProtocols();
    
    /**
     * Send a message to all clients.  This will send a message to all
     * clients that are connected via the WonderlandSessionListener.  Clients
     * connected via other protocols will not receive the message. 
     * <p>
     * This is identical to calling 
     * <code>WonderlandSessionListener.sendToAllClients()</code>.
     * 
     * @param message the message to send
     */
    public void sendToAllClients(Message message);
    
    /**
     * Register a listener for message from Wonderland clients.  
     * This listener will listen for messages on all sessions connected via the 
     * WonderlandSessionListener.  Clients connected via other protocols will 
     * not receive the message.
     * <p>
     * Listeners are not dynamic -- a particular session will copy the set of 
     * listeners that are registered at the time it is created, and will
     * never update that list.  Server plugins should register all listeners
     * at initialization time.  The listener will be stored in the Darkstar 
     * data store, so it must be either Serializable or a ManagedObject.
     * <p>
     * This is identical to calling 
     * <code>WonderlandSessionListener.registerMessageListener()</code>.
     * 
     * @param messageClass the class of message to list for
     * @param listener the listener
     */
    public void registerMessageListener(Class<? extends Message> messageClass,
                                        ClientMessageListener listener);
    
    /**
     * Register a listener for Wonderland clients connecting and disconnecting.  
     * This listener will listen for messages on all sessions connected via the 
     * WonderlandSessionListener.  Clients connected via other protocols will 
     * not receive the message.
     * <p>
     * Listeners are not dynamic -- a particular session will copy the set of 
     * listeners that are registered at the time it is created, and will
     * never update that list.  Server plugins should register all listeners
     * at initialization time.  The listener will be stored in the Darkstar 
     * data store, so it must be either Serializable or a ManagedObject.
     * <p>
     * This is identical to calling 
     * <code>WonderlandSessionListener.registerConnectionListener()</code>.
     * 
     * @param messageClass the class of message to list for
     * @param listener the listener
     */
    public void registerConnectionListener(ClientConnectionListener listener);
}
