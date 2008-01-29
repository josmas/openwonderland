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
     * Handle when a new session attaches to this handler.  A session 
     * attaches when a client calls <code>WonderlandSession.attach()</code>
     * to attach a new session.
     * @param sender a sender that allows messages to be sent to the
     * attached client
     */
    public void clientAttached(ClientSender sender);
    
    /**
     * Handle a message from a client
     * @param sender a sender that allows message to be sent to the client
     * that sent the message
     * @param message the message that was generated
     */
    public void messageReceived(ClientSender sender, Message message);
    
    /**
     * Handle when a session detaches from this handler
     * @param session the session that detached
     */
    public void clientDetached(ClientSession session);
}
