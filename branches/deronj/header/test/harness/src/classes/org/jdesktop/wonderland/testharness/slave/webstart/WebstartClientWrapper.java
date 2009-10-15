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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.testharness.common.TestRequest;
import org.jdesktop.wonderland.testharness.common.ClientReply;
import org.jdesktop.wonderland.testharness.common.UserSimReply;
import org.jdesktop.wonderland.testharness.common.UserSimRequest;
import org.jdesktop.wonderland.testharness.slave.ProcessingException;
import org.jdesktop.wonderland.testharness.slave.RequestProcessor;
import org.jdesktop.wonderland.testharness.slave.SlaveMain.ReplySender;

/**
 * This class starts the Wonderland client via webstart and then acts as a proxy
 * between the test harness and the Wonderland test harness actor module.
 *
 * @author Paul
 */
public class WebstartClientWrapper implements RequestProcessor {

    private Process process = null;
    private Process xvfbProcess = null;
    private UserSimRequestServer requestServer;
    private int actorPort;
    private ReplySender replySender;

    public String getName() {
        return "WebstartClient";
    }

    public void initialize(String username, Properties props, ReplySender replyHandler) throws ProcessingException {
        boolean useXvfb = false;
        this.replySender = replyHandler;

        String osName = System.getProperty("os.name");
        System.err.println("OS "+osName);
        if (osName.equalsIgnoreCase("linux") || osName.equalsIgnoreCase("Solaris")) {
            useXvfb = true;
        }

        actorPort = Integer.parseInt(props.getProperty("testharness.actorPort"));
        System.err.println("ACTOR PORT "+actorPort);

        try {
            requestServer = new UserSimRequestServer(username);
            requestServer.start();

            System.err.println("Webstart init " + username);
            props.list(System.err);
            String serverURL = props.getProperty("serverURL");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(WebstartClientWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }

            String userDir = System.getProperty("user.dir")+File.separatorChar+"testharness_"+username;
//            new File(userDir).mkdir();

            ProcessBuilder builder = new ProcessBuilder("javaws", "-Xnosplash", "-open", "-b"+
                    " -username "+username+
                    " -userdir "+userDir+
                    " -actorPort "+actorPort, serverURL + "wonderland-web-front/app/Wonderland.jnlp");
            System.err.println("Process "+builder.toString());
            if (useXvfb) {
                builder.environment().put("DISPLAY", ":1.0");
                ProcessBuilder xvfb = new ProcessBuilder("Xvfb -ac :1");
                xvfbProcess = xvfb.start();
            }

            process = builder.start();
        } catch (IOException ex) {
            Logger.getLogger(WebstartClientWrapper.class.getName()).log(Level.SEVERE,null, ex);
        }

    }

    public void processRequest(TestRequest request) throws ProcessingException {
        System.err.println("Webstart processRequest "+request);
        try {
            requestServer.send((UserSimRequest) request);
        } catch (IOException ex) {
            Logger.getLogger(WebstartClientWrapper.class.getName()).log(Level.SEVERE, "writeObject failed", ex);
        }
    }

    public void destroy() {
        System.err.println("Webstart destroy");
        requestServer.quit();

        try {
            Thread.sleep(10000); // Give the client a chance to quit
        } catch (InterruptedException ex) {
            Logger.getLogger(WebstartClientWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        process.destroy();          // Force it to quit;
    }

    class UserSimRequestServer extends Thread {
        private ObjectOutputStream out;
        private ServerSocket serverSocket;
        private Socket socket;
        private boolean quit = false;
        private LinkedList<UserSimRequest> pendingRequests = new LinkedList();
        private final Object pendingLock = new Object();
        private String username;

        public UserSimRequestServer(String username) {
            setDaemon(true);
            this.username = username;
        }

        @Override
        public void run() {
            UserSimReply reply;
            
            try {
                serverSocket = new ServerSocket(actorPort);
                socket = serverSocket.accept();
                out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                synchronized(pendingLock) {
                    for(UserSimRequest r : pendingRequests) {
                        out.writeObject(r);
                    }
                    pendingRequests = null;
                }
                while(!quit) {
                    try {
                        reply = (UserSimReply) in.readObject();
                        System.err.println("GOT REPLY "+reply);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(WebstartClientWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch(EOFException eof) {
                        System.err.println("Quiting");
                        quit = true;
                    }
                }

                socket.close();
            } catch(SocketException se) {
                if (!quit)
                    Logger.getLogger(WebstartClientWrapper.class.getName()).log(Level.SEVERE, null, se);
            } catch (IOException ex) {
                Logger.getLogger(WebstartClientWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.err.println("TestRequestServer has quit");
            replySender.sendReply(ClientReply.newQuitMessage(username));
        }

        void send(UserSimRequest request) throws IOException {
            synchronized(pendingLock) {
                if (pendingRequests==null)
                    out.writeObject(request);
                else
                    pendingRequests.add(request);
            }
        }

        void quit() {
            this.quit = true;
            try {
                if (socket!=null)
                    socket.close();
            } catch (IOException ex) {
                Logger.getLogger(WebstartClientWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


}
