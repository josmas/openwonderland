/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.comms.session;

import org.jdesktop.wonderland.client.comms.LoginFailureException;

/**
 * The result of a login attempt
 */
class LoginResult {
    boolean success;
    LoginFailureException exception;
    private final WonderlandSessionImpl session;

    public LoginResult(boolean success, LoginFailureException exception, final WonderlandSessionImpl session) {
        this.session = session;
        this.success = success;
        this.exception = exception;
    }
    
}
