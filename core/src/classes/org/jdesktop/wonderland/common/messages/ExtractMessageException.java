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

package org.jdesktop.wonderland.common.messages;

import org.jdesktop.wonderland.ExperimentalAPI;

/**
 * An exception extracting a message.  This exception includes the messageID
 * of the message in error, in order to help when sending error responses.
 * @author jkaplan
 */
@ExperimentalAPI
public class ExtractMessageException extends MessageException {
    /** the extracted message id */
    private MessageID messageID;
    
    /**
     * Creates a new instance of <code>ExtractMessageException</code> with the
     * given extracted message ID.
     * @param messageID the extracted message id
     */
    public ExtractMessageException(MessageID messageID) {
        this (messageID, null);
    }

    /**
     * Constructs an instance of <code>ExtractMessageException</code> with the 
     * specified cause and messageID.
     * @param messageID the extracted messageID
     * @param cause the cause of this error.
     */
    public ExtractMessageException(MessageID messageID, Throwable cause) {
        super (cause);
        
        this.messageID = messageID;
    }

    /**
     * Get the MessageID of the message that could not be extracted
     * @return the messageID
     */
    public MessageID getMessageID() {
        return messageID;
    }
}
