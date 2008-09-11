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
package org.jdesktop.wonderland.testharness.slave;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.testharness.common.LoginRequest;
import org.jdesktop.wonderland.testharness.common.TestRequest;
import org.jdesktop.wonderland.testharness.slave.client3D.Client3DSim;

/**
 *
 * @author paulby
 */
public class SlaveMain {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private HashMap<String, Client3DSim> clientSim = new HashMap();
    private boolean done = false;
    
    public SlaveMain(String[] args) {
        
        String masterHostname;
        int masterPort;
        
        if (args.length<2) {
            System.err.println("Usage: SlaveMain <master hostname> <master port>");
            System.exit(1);
        }
        
        masterHostname = args[0];
        masterPort = Integer.parseInt(args[1]);
        
        try {
            Socket s = new Socket(masterHostname, masterPort);
            System.out.println("Opening streams");
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
            do {
                try {
                    System.out.println("Waiting for request...");
                    TestRequest request = (TestRequest) in.readObject();
                    System.out.println("Slave got request "+request);
                    processRequest(request);
                } catch (IOException ex) {
                    done=true;
                    Logger.getLogger(SlaveMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(SlaveMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            } while(!done);
        } catch (UnknownHostException ex) {
            Logger.getLogger(SlaveMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SlaveMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void processRequest(TestRequest request) {
        if (request instanceof LoginRequest) {
            try {
                // Hardcoded Client3D, TODO make configurable
                clientSim.put(request.getUsername(), new Client3DSim((LoginRequest) request));
            } catch (LoginFailureException ex) {
                // TODO send error to server
                Logger.getLogger(SlaveMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (clientSim!=null)
                clientSim.get(request.getUsername()).processRequest(request);
            else
                Logger.getAnonymousLogger().severe("Unrecognized request "+request.getClass().getName());
        }
    }
    
    public static void main(String[] args) {
        new SlaveMain(args);
    }
}
