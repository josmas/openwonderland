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
package org.jdesktop.wonderland.testharness.master;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.testharness.common.MasterInfo;
import org.jdesktop.wonderland.testharness.manager.common.ManagerMessage;
import org.jdesktop.wonderland.testharness.manager.common.MasterStatus;

/**
 *
 * @author paulby
 */
public class MasterMain {
    
    public static final int MANAGER_PORT = 5567;

    private HashSet<SlaveConnection> activeSlaves = new HashSet();
    private HashSet<SlaveConnection> passiveSlaves = new HashSet();
    
    private TestDirector director;
    private Properties props;
    
    private String sgsServerName;
    private int sgsServerPort;
    
    // standard properties
    private static final String SERVER_NAME_PROP = "sgs.server";
    private static final String SERVER_PORT_PROP = "sgs.port";
    
            
    // default values
    private static final String SERVER_NAME_DEFAULT = "localhost";
    private static final String SERVER_PORT_DEFAULT = "1139";
    
    private static MasterMain masterMain=null;
    
    private ManagerController manager;
    
    public MasterMain(String[] args) {
        masterMain = this;
        props = loadProperties(args[0]);
        sgsServerName = props.getProperty(SERVER_NAME_PROP,
                                              SERVER_NAME_DEFAULT);
        sgsServerPort = Integer.valueOf(props.getProperty(SERVER_PORT_PROP,
                                              SERVER_PORT_DEFAULT));
        
        manager = new ManagerController();
        manager.start();
        
        director = new SimpleTestDirector();
        try {
            ServerSocket serverSocket = new ServerSocket(MasterInfo.PORT);
            while(true) {
                Socket s = serverSocket.accept();
                SlaveConnection slaveController = new SlaveConnection(s); 
                if (director.slaveJoined(slaveController)) {
                    // Director is using the slave
                    activeSlaves.add(slaveController);
                    slaveController.setDirector(director);
                } else {
                    // Director did not want slave
                    passiveSlaves.add(slaveController);
                }
                manager.sendStatusMessage(activeSlaves.size(), passiveSlaves.size());
            }
        } catch (IOException ex) {
            Logger.getLogger(MasterMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static MasterMain getMaster() {
        return masterMain;
    }
    
    public String getSgsServerName() {
        return sgsServerName;
    }
    
    public int getSgsPort() {
        return sgsServerPort;
    }
    
    /**
     * Called when a slave connection is lost
     * @param slaveConnection
     */
    void slaveLeft(SlaveConnection slaveConnection) {
        if (slaveConnection.getDirector()==null) {
            passiveSlaves.remove(slaveConnection);
        } else {
            activeSlaves.remove(slaveConnection);
        }
        manager.sendStatusMessage(activeSlaves.size(), passiveSlaves.size());
    }
    
    private static Properties loadProperties(String fileName) {
        // start with the system properties
        Properties props = new Properties(System.getProperties());
    
        // load the given file
        if (fileName != null) {
            try {
                props.load(new FileInputStream(fileName));
            } catch (IOException ioe) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Error reading properties from " +
                           fileName, ioe);
            }
        }
        
        return props;
    }
    
    /**
     * Manage the connected managers
     */
    class ManagerController extends Thread {
        private HashSet<ManagerConnection> connections = new HashSet();
        
        private MasterStatus lastStatusMessage = null;
        
        public ManagerController() {
            
        }
        
        @Override
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(MANAGER_PORT);
                while (true) {
                    try {
                        System.err.println("Listening for manager connection");
                        Socket s = ss.accept();
                        ManagerConnection connection = new ManagerConnection(s);
                        connections.add(connection);
                        if (lastStatusMessage != null) {
                            connection.send(lastStatusMessage);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(MasterMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(MasterMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        void sendStatusMessage(int activeSlaves, int passiveSlaves) {
            lastStatusMessage = new MasterStatus(activeSlaves, passiveSlaves);
            synchronized(connections) {
                for(ManagerConnection l : connections)
                    l.send(lastStatusMessage);
            }
        }
        
        /**
         * Notification that a manager has closed
         * @param manager
         */
        void managerClosed(ManagerConnection manager) {
            synchronized(connections) {
                connections.remove(manager);
            }
        }
    }
    
    class ManagerConnection extends Thread {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private boolean done = false;
        
        public ManagerConnection(Socket s) {
            socket = s;
            System.out.println("Started ManagerListener");
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                Logger.getLogger(MasterMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            start();
        }
        
        @Override
        public void run() {

            while (!done) {
                try {
                    in.readObject();
                } catch(EOFException eofe) {
                    manager.managerClosed(this);
                    done=true;
                } catch (IOException ex) {
                    Logger.getLogger(MasterMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(MasterMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        private void send(ManagerMessage msg) {
            try {
                out.writeObject(msg);
            } catch (IOException ex) {
                Logger.getLogger(MasterMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void main(String[] args) {
        new MasterMain(args);
    }
}
