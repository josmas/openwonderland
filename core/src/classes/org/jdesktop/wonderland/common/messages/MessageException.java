/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.common.messages;

import org.jdesktop.wonderland.ExperimentalAPI;

/**
 * A runtime exception when there is a problem serializing or deserializing
 * an message
 * @author jkaplan
 */
@ExperimentalAPI
public class MessageException extends RuntimeException {

    /**
     * Creates a new instance of <code>MessageException</code> without 
     * detail message or cause.
     */
    public MessageException() {
    }

    /**
     * Constructs an instance of <code>MessageException</code> with the 
     * specified detail message.
     * @param msg the detail message.
     */
    public MessageException(String msg) {
        super (msg);
    }

    /**
     * Constructs an instance of <code>MessageException</code> with the 
     * specified cause.
     * @param cause the cause of this error.
     */
    public MessageException(Throwable cause) {
        super (cause);
    }

    /**
     * Constructs an instance of <code>MessageException</code> with the 
     * specified detail message and cause.
     * @param msg the detail message.
     * @param cause the cause
     */
    public MessageException(String msg, Throwable cause) {
        super (msg, cause);
    }
}
