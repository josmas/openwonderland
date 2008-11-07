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
package org.jdesktop.wonderland.common.comms.messages;

import java.math.BigInteger;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * Message sent from the server to the client immediately on session
 * creation.  This message give the client necessary information about
 * the session, such as its unique id.
 * @author jkaplan
 */
public class SessionInitializationMessage extends Message {
    /** the ID of this session */
    private BigInteger sessionID;
    
    /** 
     * Create a new SessionInitializationMessage
     * @param sessionID the unique id of this session
     */
    public SessionInitializationMessage(BigInteger sessionID) {
        this.sessionID = sessionID;
    }
    
    /**
     * Get the session ID
     * @return the session ID
     */
    public BigInteger getSessionID() {
        return sessionID;
    }
}
