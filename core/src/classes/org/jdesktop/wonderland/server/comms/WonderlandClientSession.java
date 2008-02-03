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
 * An extension to the Darkstar ClientSession for sending to a particular
 * WonderlandClient attached to a WonderlandSession.  Messages sent using
 * this session object will be handled by the messageReceived() method of
 * the WonderlandClient with the same ClientType as this session.
 * 
 * @author jkaplan
 */
@ExperimentalAPI
public interface WonderlandClientSession extends ClientSession {
    /**
     * Get the client type of the client this session is associated with
     * @return the client type
     */
    public ClientType getClientType();
    
    /**
     * Send a message to the WonderlandClient associated with this
     * session's client type
     * @param message the message to send
     */
    public void send(Message message);
}
