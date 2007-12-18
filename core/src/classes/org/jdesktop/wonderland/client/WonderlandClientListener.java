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

package org.jdesktop.wonderland.client;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClientListener;
import java.net.PasswordAuthentication;

/**
 *
 * @author paulby
 */
public class WonderlandClientListener implements SimpleClientListener {

    private WonderlandClient client;
    private LoginParameters loginParams;
    private boolean loginComplete;
    private LoginResult loginResult = new LoginResult();
    
    WonderlandClientListener(WonderlandClient client, LoginParameters loginParams) {
        this.client = client;
        this.loginParams = loginParams;
    }
    
    public PasswordAuthentication getPasswordAuthentication() {
        // This is called to get the user name and authentication data (eg password)
        // to be authenticated server side.
        return new PasswordAuthentication(loginParams.getUserName(), loginParams.getPassword());
    }

    public void loggedIn() {
        synchronized(this) {
            loginComplete = true;
            loginResult.setStatus(LoginResult.Status.SUCCESS);
            notifyAll();
        }
    }

    public void loginFailed(String reason) {
        synchronized(this) {
            loginComplete = true;
            loginResult.setStatus(LoginResult.Status.BAD_AUTH);
            loginResult.setReason(reason);
            notifyAll();
        }
    }

    public ClientChannelListener joinedChannel(ClientChannel channel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void receivedMessage(byte[] arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void reconnecting() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void reconnected() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void disconnected(boolean arg0, String arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Waits for the login process to be complete and returns true if it
     * succeeded and false if failed.
     */
    public LoginResult waitForLogin() {
        System.out.println("WAIT FOR LOGIN");
        synchronized(this) {
            while (!loginComplete) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return loginResult;
    }
    
    /**
     * Encapsulate the results of a login attempt
     */
    public static class LoginResult {

        public enum Status {
            CONNECTING, SUCCESS, BAD_AUTH, BAD_SERVER
        }
        
        private Status status;
        private String reason;
        
        public LoginResult() {
            status = Status.CONNECTING;
        }
        
        public synchronized Status getStatus() {
            return status;
        }
        public synchronized void setStatus(Status status) {
            this.status = status;
        }
        
        public synchronized String getReason() {
            return reason;
        }
        public synchronized void setReason(String reason) {
            this.reason = reason;
        }
    }
}
