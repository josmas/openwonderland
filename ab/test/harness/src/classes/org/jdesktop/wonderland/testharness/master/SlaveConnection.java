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
import java.net.Socket;
import java.util.logging.Level;
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

    SlaveConnection(Socket socket) {
        System.out.println("New Slave Controller");
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
                in.readObject();
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
