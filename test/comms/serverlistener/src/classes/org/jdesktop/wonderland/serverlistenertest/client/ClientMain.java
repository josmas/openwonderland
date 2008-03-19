/**
 * Project Wonderland
 *
 * $RCSfile:$
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
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.serverlistenertest.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.AttachFailureException;
import org.jdesktop.wonderland.client.comms.BaseClient;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSessionFactory;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.serverlistenertest.common.TestClientType;
import org.jdesktop.wonderland.serverlistenertest.common.TestMessageOne;
import org.jdesktop.wonderland.serverlistenertest.common.TestMessageThree;
import org.jdesktop.wonderland.serverlistenertest.common.TestMessageTwo;

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

    // clients
    private TestOneClient   t1c;
    private TestTwoClient   t2c;
    private TestThreeClient t3c;
    
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

        // create the client & login
        WonderlandSession session = WonderlandSessionFactory.getSession(serverInfo);
        session.login(new LoginParameters(username, password.toCharArray()));

        logger.info("Login suceeded");
        
        testMessages(session);
        testDetached();
        testReattach(session);
        
        logger.info("Tests complete");
    }
    
    public void testMessages(WonderlandSession session)
        throws AttachFailureException
    {
        logger.info("Sending test messages");
        
        t1c = new TestOneClient();
        t2c = new TestTwoClient();
        t3c = new TestThreeClient();
        
        t1c.attach(session);
        t2c.attach(session);
        t3c.attach(session);
        
        t1c.send(new TestMessageOne("TestOne"));
        t2c.send(new TestMessageTwo("TestTwo"));
        t3c.send(new TestMessageThree("TestThree", 42));
        
        t1c.detach();
        t2c.detach();
        t3c.detach();
    }
    
    // try to send to a detached session
    public void testDetached() {
        logger.info("Sending to detached client");
        
        try {
            t1c.send(new TestMessageOne("TestOne"));
            assert false : "Sending to detached channel should throw exception";
        } catch (IllegalStateException ise) {
            // good
        }
    }
    
    public void testReattach(WonderlandSession session)
        throws AttachFailureException
    {
        logger.info("Testing reattach");
        
        t2c = new TestTwoClient();
        t2c.attach(session);
        t2c.send(new TestMessageTwo("TestTwo"));

        // re-reattach
        try {
            t2c.attach(session);
            assert false : "Re-attaching client should fail";
        } catch (AttachFailureException afe) {
            // good
        }
        
        // attach new client of same type
        try {
            TestTwoClient t2c2 = new TestTwoClient();
            t2c2.attach(session);
            assert false : "Duplicate client should fail";
        } catch (AttachFailureException afe) {
            // good
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
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
        }
    }
    
    class TestOneClient extends BaseClient {

        public ClientType getClientType() {
            return TestClientType.CLIENT_ONE_TYPE;
        }

        public void send(TestMessageOne message) {
            super.send(message);
        }
        
        public void handleMessage(Message message) {
            assert false : "TestOneClient received message: " + message;
        }
    }
    
    class TestTwoClient extends BaseClient {

        public ClientType getClientType() {
            return TestClientType.CLIENT_TWO_TYPE;
        }

        public void send(TestMessageTwo message) {
            super.send(message);
        }
        
        public void handleMessage(Message message) {
             assert false : "TestOneClient received message: " + message;
        }
    }
    
    class TestThreeClient extends BaseClient {

        public ClientType getClientType() {
            return TestClientType.CLIENT_THREE_TYPE;
        }

        @Override
        public void send(Message message) {
            super.send(message);
        }
        
        public void handleMessage(Message message) {
            assert false : "TestOneClient received message: " + message;
        }
    }
}
