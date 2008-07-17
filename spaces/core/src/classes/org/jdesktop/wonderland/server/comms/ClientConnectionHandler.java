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
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * Handles client connections to the given client type.
 * @author jkaplan
 */
@ExperimentalAPI
public interface ClientConnectionHandler {
    /**
     * Get the type of connection this handler deals with
     * @return the connection types this handler can be used for
     */
    public ConnectionType getConnectionType();
    
    /**
     * Called when the handler is registered with the CommsManager
     * @param sender the WonderlandClientSender that can be used to
     * send to all clients of the given type
     */
    public void registered(WonderlandClientSender sender);
    
    /**
     * Handle when a new session connectes to this handler.  A session 
     * connects when a client calls <code>WonderlandSession.connect()</code>.
     * @param sender the sender that can be used to send to clients
     * of this handler
     * @param session the session that connected
     */
    public void clientConnected(WonderlandClientSender sender,
                               ClientSession session);
    
    /**
     * Handle a message from a client
     * @param sender the sender that can be used to send to clients of
     * this handler
     * @param session the session that sent the message
     * @param message the message that was generated
     */
    public void messageReceived(WonderlandClientSender sender,
                                ClientSession session,
                                Message message);
    
    /**
     * Handle when a session disconnects from this handler
     * @param sender the sender that can be used to send to clients
     * of this handler
     * @param session the session that disconnected
     */
    public void clientDisconnected(WonderlandClientSender sender,
                               ClientSession session);
}
