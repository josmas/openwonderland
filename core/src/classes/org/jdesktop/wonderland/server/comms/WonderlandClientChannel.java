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

import com.sun.sgs.app.Delivery;
import java.util.Set;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * A channel that sends messages to to any client connected with
 * the given ClientType.  While this interface is based on the
 * Darkstar Channel interface, it does not allow sessions
 * to be added or removed from the Channel.  All additions and removals
 * are done by the WonderlandSessionListener in response to attach
 * and detach requests.  
 * <p>
 * Channel that implement WonderlandClientChannel are serializable, and 
 * will be valid for as long as a handler is registered for the given
 * client type.  If the handler is unregistered, the channel will be closed,
 * and any further attempts to use it will result in an exception. 
 * 
 * @author jkaplan
 */
@ExperimentalAPI
public interface WonderlandClientChannel {
    /**
     * Get the client type this channel sends to
     * @return the client type
     */
    public ClientType getClientType();
    
   /**
     * Get the WonderlandClientSessions associated with this channel.  The
     * returned values here are WonderlandClientSessions.
     * @return a set of WonderlandClientSessions that are attached to
     * this client type
     */
    public Set<WonderlandClientSession> getSessions();
    
    /**
     * Get the name of this channel.
     * @return the name of the channel
     */
     public String getName();
     
     /**
      * Get the delivery requirements set on the channel
      * @return the delivery requirments for this channel
      */
     public Delivery getDeliveryRequirement();
     
     /**
      * Return if there are any sessions associated with this channel.
      * @return true if this channel has any sessions, or false if not
      */
    public boolean hasSessions();
    
    /**
     * Send a message to all clients connected via this channel.  The message
     * will be handled by the WonderlandClient attached with the ClientType
     * of this channel.
     * @param message the message to send
     * @throws IllegalStateException if the handler for this client type
     * has been unregistered
     */
    public void send(Message message);
    
    /**
     * Send a message to a single of client connected via this channel.  The
     * message will be handled by the WonderlandClient attached with the
     * ClientType of this channel.
     * @param session the session to send to
     * @param message the message to send
     * @throws IllegalStateException if the handler for this client type
     * has been unregistered
     */
    public void send(WonderlandClientSession session, Message message);
        
    /**
     * Send a message to a set of clients connected via this channel.  The
     * message will be handled by the WonderlandClient attached with the
     * ClientType of this channel.
     * @param sessions the sessions to send to
     * @param message the message to send
     * @throws IllegalStateException if the handler for this client type
     * has been unregistered
     */
    public void send(Set<WonderlandClientSession> sessions, Message message);
}
