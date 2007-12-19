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

import com.sun.sgs.client.simple.SimpleClient;
import java.io.IOException;
import java.util.Properties;

/**
 * This class provides the client side instance of a particular Wonderland server.
 * All interaction with the server is handled by this and it's related class WonderlandClientListener
 * 
 * @author paulby
 */
public class WonderlandClient {
    
    private WonderlandServerInfo server;
    private SimpleClient simpleClient;
    private WonderlandClientListener listener;

    /**
     * Create a new client to log in to the given server
     * @param server the server to connect to
     */
    public WonderlandClient(WonderlandServerInfo server) {
        this.server = server;
    }
    
    /**
     * Get the server this client is connected to
     * @param loginParams the parameters required for login
     * @return the result of the login
     * @throws org.jdesktop.wonderland.client.comms.WonderlandClient.LoginFailureException
     */
    public LoginResult login(LoginParameters loginParams) 
        throws LoginFailureException
    {
        listener = createListener(loginParams);
        simpleClient = new SimpleClient(listener);
        
        Properties connectProperties = new Properties();
        connectProperties.setProperty("host", server.getHostname());
        connectProperties.setProperty("port", Integer.toString(server.getSgsPort()));
        
        try {
            System.out.println("Login: " + server.getHostname() + " : " +
                               server.getSgsPort());
            
            simpleClient.login(connectProperties);
        
            // wait for the login
            LoginResult lr = listener.waitForLogin();
            if (lr.getStatus() != LoginResult.Status.SUCCESS) {
                throw new LoginFailureException("Login failed: " + 
                        lr.getStatus() + " : " + lr.getReason());
            }
            return lr;
        
        } catch (IOException ioe) {
            throw new LoginFailureException(ioe);
        } catch (InterruptedException ie) {
            throw new LoginFailureException(ie);
        }
    }
    
    /**
     * Get the simple client connected to the Darkstar server.  This will
     * only be valid after the login method succeeds.
     * @return the darkstar client
     */
    public SimpleClient getClient() {
        return simpleClient;
    }
    
    /**
     * Get the underlying listener.  
     * @return the client listener
     */
    protected WonderlandClientListener createListener(LoginParameters loginParams) {
        return new WonderlandClientListener(this, loginParams);
    }
}
