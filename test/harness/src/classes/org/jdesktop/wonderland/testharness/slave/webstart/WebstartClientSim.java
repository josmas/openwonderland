/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.testharness.slave.webstart;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.testharness.common.TestRequest;
import org.jdesktop.wonderland.testharness.slave.ProcessingException;
import org.jdesktop.wonderland.testharness.slave.RequestProcessor;

/**
 *
 * @author Paul
 */
public class WebstartClientSim implements RequestProcessor {

    private Process process = null;
    private ObjectOutputStream out;
    private ServerSocket serverSocket;
    private Socket socket;

    public String getName() {
        return "WebstartClient";
    }

    public void initialize(String username, Properties props) throws ProcessingException {
        try {
            serverSocket = new ServerSocket(53421);
            socket = serverSocket.accept();
            out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            System.err.println("Webstart init " + username);
            props.list(System.err);
            String serverURL = props.getProperty("serverURL");
            System.err.println("javaws " + serverURL + "wonderland-web-front/app/Wonderland.jnlp");
            ProcessBuilder builder = new ProcessBuilder("javaws", "-open", "-p /Users/Paul/src/java.net/wonderland/trunk/wonderland/test/harness/run-client.properties", serverURL + "wonderland-web-front/app/Wonderland.jnlp");
            process = builder.start();
        } catch (IOException ex) {
            Logger.getLogger(WebstartClientSim.class.getName()).log(Level.SEVERE,null, ex);
        }

    }

    public void processRequest(TestRequest request) throws ProcessingException {
        System.err.println("Webstart processRequest "+request);
//        out.write(request);
    }

    public void destroy() {
        System.err.println("Webstart destroy");
    }

    class TestRequestServer extends Thread {
        private ServerSocket serverSocket;
        private boolean quit = false;

        public TestRequestServer() {
                setDaemon(true);
        }

        @Override
        public void run() {
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                while(!quit) {
                    
                }
            } catch (IOException ex) {
                Logger.getLogger(WebstartClientSim.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
