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
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.WonderlandClient;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
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
        WonderlandClient wc = new WonderlandClient(serverInfo);
        wc.login(new LoginParameters(username, password.toCharArray()));

        logger.info("Login suceeded");
        
        wc.send(new TestMessageOne("TestOne"));
        wc.send(new TestMessageTwo("TestTwo"));
        wc.send(new TestMessageThree("TestThree", 42));
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
