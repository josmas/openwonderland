/**
 * Project Wonderland
 *
 * $RCSfile: LogControl.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2007/10/23 18:27:41 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.comms;

import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.OKMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A base class of message response listeners that handles the case of a message
 * that is guaranteed to get an OK or an Error.
 * @author kaplanj
 */
@ExperimentalAPI
public abstract class OKErrorResponseListener extends WaitResponseListener {
    
    /** 
     * Called when the response to a message is received
     * @param responseMessage the message
     */
    public void responseReceived(ResponseMessage response) {
        if (response instanceof OKMessage) {
            onSuccess(response.getMessageID());
        } else if (response instanceof ErrorMessage) {
            ErrorMessage em = (ErrorMessage) response;
            onFailure(response.getMessageID(), em.getErrorMessage(),
                      em.getErrorCause());
        } else {
            onFailure(response.getMessageID(), "Unexpected message type: " +
                      response.getClass(), null);
        }
        
        notifyResponse(response);
    }
    
    /**
     * Called when a message receives an OK response
     * @param messageID the ID of the message that succeeded
     */
    public abstract void onSuccess(MessageID messageID);
    
    /**
     * Called when a message fails
     * @param messageID the ID of the message that failed
     * @param message the reason for failure or null if there is no reason
     * @param cause the exception, or null if there is no exception
     */
    public abstract void onFailure(MessageID messageID, String message, 
                                   Throwable cause);
}
