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
package org.jdesktop.wonderland.client.comms;

import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.HandlerType;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * This class provides the client side instance of a particular Wonderland
 * service. All interaction with a service on a given server are handled
 * by this a WonderlandClient.
 * <p>
 * The client starts out in the DETACHED status, meaning it is not associated
 * with any WonderlandSession.  Once a client is attached to a session,
 * it is able to communicate with the server.
 * 
 * @author kaplanj
 */
@ExperimentalAPI
public interface WonderlandClient {
    /** status of this listener */
    public enum Status { DETACHED, ATTACHED };
    
    /**
     * Get the type this client represents.
     * @return the type of client
     */
    public HandlerType getClientType();
    
    /**
     * Get the session this client is attached to
     * @return the session this client is attached to, or null if
     * the client is not attached to a session.
     */
    public WonderlandSession getSession();
    
    /**
     * Get the status of this client
     * @return the status of the client
     */
    public Status getStatus();
    
    /**
     * Notify this client that it is attached to given session
     * @param session the session the client is now attached to
     */
    public void attached(WonderlandSession session);
    
    /**
     * Notify this client that it is detached from the current session
     */
    public void detached();
    
    /**
     * Handle a message sent to this client
     * @param message the message
     */
    public void messageReceived(Message message);
}
