/**
 * Project Wonderland
 *
 * $RCSfile: LogControl.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2007/10/23 18:27:41 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.tools;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext3D;
import org.jdesktop.wonderland.client.avatar.LocalAvatar;
import org.jdesktop.wonderland.client.cell.CellCacheBasicImpl;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;

/**
 *
 * @author jkaplan
 */
public class BoundsMultiClient
        implements SessionStatusListener
{
    /** a logger */
    private static final Logger logger = 
            Logger.getLogger(BoundsMultiClient.class.getName());
    
    /** the name of this client */
    private String name;
    
    /** the mover thread */
    private MoverThread mover;
    
    public BoundsMultiClient(WonderlandServerInfo server, 
                             LoginParameters login) 
        throws Exception
    {
        this.name = login.getUserName();
        
        // login
        CellCacheBasicImpl cellCache = new CellCacheBasicImpl();
        BoundsTestClientSession session =
                new BoundsTestClientSession(server, cellCache);
        ClientContext3D.registerCellCache(cellCache, session);
        session.addSessionStatusListener(this);
        session.login(login);
        
        logger.info(getName() + " login succeeded");
        
        LocalAvatar avatar = session.getLocalAvatar();
        
        // pick a direction to move
        int dir = (int) Math.random() * 10;
        if (dir < 2) {
            mover = new RandomMover(avatar);
        } else if (dir < 6) {
            mover = new XMover(avatar);
        } else {
            mover = new YMover(avatar);
        }
        
        mover.start();
    }
    
    public String getName() {
        return name;
    }
    
    public void sessionStatusChanged(WonderlandSession session, 
                                     Status status)
    {
        logger.info(getName() + " change session status: " + status);
        if (status == Status.DISCONNECTED  && mover != null) {
            mover.quit();
        }
    }
    
    public void waitForFinish() throws InterruptedException {
        if (mover == null) {
            return;
        }
        
        // wait for the thread to end
        mover.join();
    }
        
    public static void main(String[] args) {
        WonderlandServerInfo server = new WonderlandServerInfo("localhost", 1139);
        
        int buildNumber = Integer.parseInt(args[0]);
        
        int count = 1;
        
        BoundsMultiClient[] bmc = new BoundsMultiClient[count];
        
        for (int i = 0; i < count; i++) {
            LoginParameters login = 
                    new LoginParameters("foo" + buildNumber+"_"+i, "test".toCharArray());
            
            try {
                bmc[i] = new BoundsMultiClient(server, login);
                Thread.sleep(500);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error logging in", ex);
            }
        }
        
        // wait for each client 
        try {   
            for (BoundsMultiClient client : bmc) {
                client.waitForFinish();
            }
        } catch (InterruptedException ie) {
        }
    }

    abstract class MoverThread extends Thread {
        protected Vector3f location = new Vector3f();
        private Quaternion orientation = null;
        private LocalAvatar avatar;
        private boolean quit = false;
        
        public MoverThread(LocalAvatar avatar) {
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
            randomPosition();
                            
            while(!isQuit()) {
                nextPosition();
                avatar.localMoveRequest(location, orientation);
                try {
                    sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BoundsMultiClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        protected void randomPosition() {
            location.x = FastMath.rand.nextFloat()*50;
            location.z = FastMath.rand.nextFloat()*50;
        }
        
        protected abstract void nextPosition();
        
    }
    
    class RandomMover extends MoverThread {
        public RandomMover(LocalAvatar avatar) {
            super (avatar);
        }
        
        @Override
        protected void nextPosition() {
            randomPosition();
        }
    }
    
    class XMover extends MoverThread {
        public XMover(LocalAvatar avatar) {
            super (avatar);
        }
        
        @Override
        protected void nextPosition() {
            location.x = Math.abs(location.x--) % 50;
        }
    }
    
    class YMover extends MoverThread {
        public YMover(LocalAvatar avatar) {
            super (avatar);
        }
        
        @Override
        protected void nextPosition() {
            location.y = Math.abs(location.y++) % 50;
        }
    }

    
}
