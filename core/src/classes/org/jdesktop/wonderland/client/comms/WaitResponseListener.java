/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client.comms;

import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A response listener super type that lets 
 * @author kaplanj
 */
@ExperimentalAPI
public abstract class WaitResponseListener implements ResponseListener {
    // latch for indicating that the response has happened
    private boolean response = false;
   
    /**
     * Wait for a response to the message.  This method will return once
     * a response to the given message is received.
     * @throws InterruptedException if the response is delayed
     */
    public synchronized void waitForResponse() throws InterruptedException {
        wait();
    }
    
    /**
     * Notify that a message is received.  Subclasses must call this in order
     * to notify listeners.
     */
    protected synchronized void notifyResponse() {
        response = true;
        notify();
    }
}
