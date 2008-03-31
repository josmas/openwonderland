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

import org.jdesktop.wonderland.common.comms.HandlerType;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * Message used to request attaching a new client
 * @author jkaplan
 */
public class AttachClientMessage extends Message {
    /** the client type to attach */
    private HandlerType type;
    
    /** 
     * Create a new AttachClientMessage
     * @param type the type of client to attach
     */
    public AttachClientMessage(HandlerType type) {
        this.type = type;
    }
    
    /**
     * Get the client type
     * @return the client type
     */
    public HandlerType getClientType() {
        return type;
    }
}
