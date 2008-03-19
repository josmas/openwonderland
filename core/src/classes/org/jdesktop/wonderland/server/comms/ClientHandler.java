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

import com.sun.sgs.app.ClientSessionId;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * Handles client connections to the given client type.
 * @author jkaplan
 */
@ExperimentalAPI
public interface ClientHandler {
    /**
     * Get the type of client this handler deals with
     * @return the client types this handler can be used for
     */
    public ClientType getClientType();
    
    /**
     * Called when the handler is registered with the CommsManager
     * @param channel the WonderlandClientChannel that can be used to
     * send to all clients of the given type
     */
    public void registered(WonderlandClientChannel channel);
    
    /**
     * Handle when a new session attaches to this handler.  A session 
     * attaches when a client calls <code>WonderlandSession.attach()</code>
     * to attach a new session.
     * @param channel the channel that can be used to send to clients
     * of this handler
     * @param sessionId the id of the session that attached
     */
    public void clientAttached(WonderlandClientChannel channel,
                               ClientSessionId sessionId);
    
    /**
     * Handle a message from a client
     * @param channel the channel that can be used to send to clients of
     * this handler
     * @param sessionId the sessionId that sent the message
     * @param message the message that was generated
     */
    public void messageReceived(WonderlandClientChannel channel,
                                ClientSessionId sessionId,
                                Message message);
    
    /**
     * Handle when a session detaches from this handler
     * @param channel the channel that can be used to send to clients
     * of this handler
     * @param sessionId the id of the session that attached
     */
    public void clientDetached(WonderlandClientChannel channel,
                               ClientSessionId sessionId);
}
