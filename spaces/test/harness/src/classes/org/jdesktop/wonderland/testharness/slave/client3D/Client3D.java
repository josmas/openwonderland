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
package org.jdesktop.wonderland.testharness.slave.client3D;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext3D;
import org.jdesktop.wonderland.client.avatar.LocalAvatar;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.testharness.common.LoginRequest;
import org.jdesktop.wonderland.testharness.common.TestRequest;

/**
 * A test client that simulates a 3D client
 */
public class Client3D
        implements SessionStatusListener
{
    /** a logger */
    private static final Logger logger = 
            Logger.getLogger(Client3D.class.getName());
    
    /** the name of this client */
    private String name;
    
    /** the mover thread */
    private UserSimulator userSim;
    
    
    public Client3D(LoginRequest loginRequest) throws LoginFailureException {
        this.name = loginRequest.getUsername();
        
        WonderlandServerInfo server = new WonderlandServerInfo(loginRequest.getSgsServerName(), 
                                                               loginRequest.getSgsServerPort());
        LoginParameters login = new LoginParameters(name, loginRequest.getPasswd());
                
        // login
        CellClientSession session = new CellClientSession(server);
        ClientContext3D.registerCellCache(session.getCellCache(), session);
        session.addSessionStatusListener(this);
        session.login(login);
        
        logger.info(getName() + " login succeeded");
        
        LocalAvatar avatar = session.getLocalAvatar();
               
        userSim = new UserSimulator(avatar);
        
        userSim.start();
    }
    
    public void processRequest(TestRequest request) {
        Logger.getAnonymousLogger().severe("Unsupported request "+request.getClass().getName());
    }
    
    public String getName() {
        return name;
    }
    
    public void sessionStatusChanged(WonderlandSession session, 
                                     Status status)
    {
        logger.info(getName() + " change session status: " + status);
        if (status == Status.DISCONNECTED  && userSim != null) {
            userSim.quit();
        }
    }
    
    public void waitForFinish() throws InterruptedException {
        if (userSim == null) {
            return;
        }
        
        // wait for the thread to end
        userSim.join();
    }
        
    class UserSimulator extends Thread {
        protected Vector3f location = new Vector3f();
        private Quaternion orientation = null;
        private LocalAvatar avatar;
        private boolean quit = false;
        private long sleepTime = 200;
                
        public UserSimulator(LocalAvatar avatar) {
            this.avatar = avatar;
            
        }

        public synchronized boolean isQuit() {
            return quit;
        }
        
        public synchronized void quit() {
            this.quit = true;
        }
        
        @Override
        public void run() {
            while(!isQuit()) {
                avatar.localMoveRequest(location, orientation);
                try {
                    sleep(sleepTime);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client3D.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
}
