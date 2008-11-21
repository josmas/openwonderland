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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.jdesktop.wonderland.testharness.common.TestRequest;

/**
 * 
 * @author paulby
 */
public class SlaveConnection extends Thread {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private TestDirector director;
    private boolean done = false;
    private int slaveID;
    
    private static final Logger logger = Logger.getLogger(SlaveConnection.class.getName());

    SlaveConnection(Socket socket, int slaveID) {
        this.slaveID = slaveID;
        
        System.out.println("New Slave Controller "+slaveID);
        this.socket = socket;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(MasterMain.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        start();
    }
    
    void setDirector(TestDirector director) {
        this.director = director;
    }
    
    TestDirector getDirector() {
        return director;
    }

    @Override
    public void run() {
        while(!done) {
            try {
                Object msg = in.readObject();
                if (msg instanceof LogRecord) {
                    LogRecord logR = (LogRecord)msg;
                    logR.setLoggerName("slave"+slaveID+":"+logR.getLoggerName());
                    logger.log(logR);
                }
            } catch(OptionalDataException e) {
                Logger.getLogger(SlaveConnection.class.getName()).log(Level.SEVERE, "Exception length "+((OptionalDataException)e).length, e);
            } catch(EOFException eof) {
                done=true;
                MasterMain.getMaster().slaveLeft(this);
            } catch (IOException ex) {
                Logger.getLogger(SlaveConnection.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SlaveConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void send(TestRequest request) {
        System.out.println("Sending Request "+request);
               
        try {
            out.writeObject(request);
        } catch (IOException ex) {
            Logger.getLogger(MasterMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
