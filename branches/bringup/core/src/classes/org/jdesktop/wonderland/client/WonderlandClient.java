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

import com.sun.sgs.client.simple.SimpleClient;
import java.io.IOException;
import java.util.Properties;
import org.jdesktop.wonderland.client.WonderlandClientListener.LoginResult;


/**
 * This class provides the client side instance of a particular Wonderland server.
 * All interaction with the server is handled by this and it's related class WonderlandClientListener
 * 
 * @author paulby
 */
public class WonderlandClient {
    
    private SimpleClient simpleClient;
    private WonderlandClientListener listener;

    private WonderlandClient(WonderlandServerInfo server, 
                             LoginParameters loginParams) throws LoginFailureException {
        
        listener = new WonderlandClientListener(this, loginParams);
        simpleClient = new SimpleClient(listener);
        
        Properties connectProperties = new Properties();
        connectProperties.setProperty("host", server.getHostname());
        connectProperties.setProperty("port", Integer.toString(server.getSgsPort()));
        try {
            simpleClient.login(connectProperties);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new LoginFailureException("Login failed :"+e.getMessage());
        }
        
        LoginResult result = listener.waitForLogin();
        
        if (result.getStatus()!=LoginResult.Status.SUCCESS) {
            throw new LoginFailureException(result);
        }
    }
    
    /**
     * Log into the specified server with the supplied user identity and return
     * a corresponding WonderlandClient object. If login fails a LoginFailureException
     * will be thrown.
     */
    static WonderlandClient login(WonderlandServerInfo server, LoginParameters loginParams) throws LoginFailureException {
        return new WonderlandClient(server, loginParams);
    }
    
        
    public static class LoginFailureException extends Exception {

        private LoginResult loginResult = null;
        
        private LoginFailureException(String string) {
            super(string);
        }
        
        private LoginFailureException(LoginResult result) {
            
        }
        
        /**
         * Return the results of the login attempt, or null if
         * it failed before we got a response, in which case the exception
         * message will contain the information
         * 
         * @return
         */
        public LoginResult getLoginResult() {
            return loginResult;
        }
        
    }
}
