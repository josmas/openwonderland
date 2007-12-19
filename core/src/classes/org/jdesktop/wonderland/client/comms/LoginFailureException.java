/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client.comms;

/**
 *
 * @author jkaplan
 */
public class LoginFailureException extends Exception {

    /**
     * Creates a new instance of <code>LoginFailureException</code> without 
     * detail message or cause.
     */
    public LoginFailureException() {
    }


    /**
     * Constructs an instance of <code>LoginFailureException</code> with the 
     * specified detail message.
     * @param msg the detail message.
     */
    public LoginFailureException(String msg) {
        super (msg);
    }

    /**
     * Constructs an instance of <code>LoginFailureException</code> with the 
     * specified cause.
     * @param cause the cause of this error.
     */
    public LoginFailureException(Throwable cause) {
        super (cause);
    }

    /**
     * Constructs an instance of <code>LoginFailureException</code> with the 
     * specified detail message and cause.
     * @param msg the detail message.
     * @param cause the cause
     */
    public LoginFailureException(String msg, Throwable cause) {
        super (msg, cause);
    }
}
