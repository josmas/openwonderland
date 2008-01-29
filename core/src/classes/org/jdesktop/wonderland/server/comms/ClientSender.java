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
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageID;

/**
 * Send messages to a client from a ClientHandler.  These messages
 * will be sent directly to the client's session as if by using
 * <code>CommsManager.send(handler.getClientType(), session, message)</code>.
 * 
 * @author jkaplan
 */
public interface ClientSender {
    /**
     * Send a message directly to a client's session.  This will send a message
     * to the given client directly.  The client type of the message will
     * be the client type of the handler this sender is passed in to.
     * <p>
     * A client sender is only valid within the context of the current
     * Darkstar transaction.  It cannot be persisted and will not work once
     * the transaction ends.
     * 
     * @param message the message to send
     */
    public void send(Message message);
    
    /**
     * Send an error message to the client's session
     * @param messageID the source message's ID
     * @param error the error to send
     */
    public void sendError(MessageID messageID, String error);
    
    /**
     * Send an error message to the client's session
     * @param messageID the source message's ID
     * @param cause the cause of the error
     */
    public void sendError(MessageID messageID, Throwable cause);
    
    /**
     * Send an error message to the client's session
     * @param messageID the source message's ID
     * @param error the error message
     * @param cause the underlying exception
     */
    public void sendError(MessageID messageID, String error, Throwable cause);
    
    /**
     * Get the client session this sender is sending to
     * @return the session this sender will send to
     */
    public ClientSession getSession();
}
