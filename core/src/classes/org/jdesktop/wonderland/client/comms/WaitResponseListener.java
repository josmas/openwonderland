/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client.comms;

import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A response listener super type that lets users register a listener for
 * a response message.
 * @author kaplanj
 */
@ExperimentalAPI
public class WaitResponseListener implements ResponseListener {
    /** The response messsage that we received.  Set to non-null when
        a response is received */
    private ResponseMessage response = null;
   
    public void responseReceived(ResponseMessage response) {
        notifyResponse(response);
    }
    
    /**
     * Wait for a response to the message.  This method will return once
     * a response to the given message is received.
     * @return the ResponseMessage that was received in response to this message
     * @throws InterruptedException if the response is delayed
     */
    public synchronized ResponseMessage waitForResponse() 
            throws InterruptedException 
    {
        while (response == null) {
            wait();
        }
        
        return response;
    }
    
    /**
     * Notify that a message is received.  Subclasses that override the
     * <code>responseReceived()</code> method must call this in order
     * to notify listeners.
     * @param response the response message
     */
    protected synchronized void notifyResponse(ResponseMessage response) {
        this.response = response;
        notify();
    }
}
