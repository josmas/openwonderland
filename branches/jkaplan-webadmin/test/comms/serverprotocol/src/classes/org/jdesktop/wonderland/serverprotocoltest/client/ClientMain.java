/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.serverprotocoltest.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSessionImpl;
import org.jdesktop.wonderland.common.comms.ProtocolVersion;
import org.jdesktop.wonderland.serverprotocoltest.common.BadProtocolVersion;
import org.jdesktop.wonderland.serverprotocoltest.common.TestProtocolVersion;

/**
 * Simple client
 * @author jkaplan
 */
public class ClientMain {

    /** logger */
    private static final Logger logger =
            Logger.getLogger(ClientMain.class.getName());
    
    // the server info
    private WonderlandServerInfo serverInfo;
    
    // whether we are done
    boolean finished = false;

    /**
     * Create a new client
     * @param serverInfo the information about the server
     */
    public ClientMain(WonderlandServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    /**
     * Run the test
     */
    public void runTest() throws Exception {
        // read the username and properties from files
        String username = System.getProperty("sgs.user", "sample");
        String password = System.getProperty("sgs.password", "sample");

        testGoodSession(username, password);
        testBadSession(username, password);
    }
    
    public void testGoodSession(String username, String password)
        throws LoginFailureException
    {
        logger.info("Test good session");
        
        // create the client & login
        GoodSession session = new GoodSession(serverInfo);
        session.login(new LoginParameters(username, password.toCharArray()));

        logger.info("Good login suceeded");
    
        session.logout();
    }
    
    public void testBadSession(String username, String password) {
        logger.info("Test bad session");
        
        // create the client & login
        BadSession session = new BadSession(serverInfo);
        
        try {
            session.login(new LoginParameters(username, password.toCharArray()));
            assert false : "Bad login should have failed";
        } catch (LoginFailureException lfe) {
            logger.info("Bad login failed");
        }
    
        session.logout();
    }

    class GoodSession extends WonderlandSessionImpl {

        public GoodSession(WonderlandServerInfo serverInfo) {
            super (serverInfo);
        }
        
        @Override
        protected String getProtocolName() {
            return TestProtocolVersion.PROTOCOL_NAME;
        }

        @Override
        protected ProtocolVersion getProtocolVersion() {
            return TestProtocolVersion.VERSION;
        }
    }

    class BadSession extends WonderlandSessionImpl {

        public BadSession(WonderlandServerInfo serverInfo) {
            super (serverInfo);
        }
        
        @Override
        protected String getProtocolName() {
            return BadProtocolVersion.PROTOCOL_NAME;
        }

        @Override
        protected ProtocolVersion getProtocolVersion() {
            return BadProtocolVersion.VERSION;
        }
    }
    
    public static void main(String[] args) {
        // read server and port from properties
        String server = System.getProperty("sgs.server", "locahost");
        int port = Integer.parseInt(System.getProperty("sgs.port", "1139"));

        // create a login information object
        WonderlandServerInfo serverInfo = new WonderlandServerInfo(server, port);

        // the main client
        ClientMain cm = new ClientMain(serverInfo);

        try {
            cm.runTest();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
