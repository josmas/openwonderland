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
import org.jdesktop.wonderland.common.messages.Message;

/**
 * A listener that gets notified when the server receives a message
 * @author jkaplan
 */
@ExperimentalAPI
public interface ServerMessageListener {
    /** 
     * Called when a message is received
     * @param message the message which was received
     * @param session the session that generated the message
     */
    public void messageReceived(Message message, ClientSession session);
}
