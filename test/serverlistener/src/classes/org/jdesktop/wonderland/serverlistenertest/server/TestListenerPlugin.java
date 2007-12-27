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
package org.jdesktop.wonderland.serverlistenertest.server;

import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientConnectionListener;
import org.jdesktop.wonderland.server.comms.ClientMessageListener;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.serverlistenertest.common.TestMessageOne;
import org.jdesktop.wonderland.serverlistenertest.common.TestMessageThree;
import org.jdesktop.wonderland.serverlistenertest.common.TestMessageTwo;

/**
 * Sample plugin that doesn't do anything
 * @author jkaplan
 */
public class TestListenerPlugin implements ServerPlugin {
    private static final Logger logger =
            Logger.getLogger(TestListenerPlugin.class.getName());
    
    public void initialize() {
        CommsManager cm = WonderlandContext.getCommsManager();
       
        cm.registerConnectionListener(new TestConnectionListener());
        
        cm.registerMessageListener(TestMessageOne.class, 
                                   new TestMessageOneListener());
        cm.registerMessageListener(TestMessageTwo.class, 
                                   new TestMessageTwoListener());
        cm.registerMessageListener(TestMessageThree.class, 
                                   new TestMessageThreeListener());
    }
    
    public static class TestConnectionListener
            implements ClientConnectionListener, ManagedObject, Serializable
    {
        public void connected(ClientSession session) {
            logger.info("Connected: " + session.getName());
        }

        public void disconnected(ClientSession session) {
            logger.info("Disconnected: " + session.getName());
        }
    }
    
    public static class TestMessageOneListener 
            implements ClientMessageListener, Serializable 
    {
        public void messageReceived(Message message, ClientSession session) {
            if (!(message instanceof TestMessageOne)) {
                logger.warning("Received bad message: " + message);
            }
            
            logger.info("Received TestMessageOne: " + message + " from " + 
                        session.getName());
        }
    }
    
    public static class TestMessageTwoListener 
            implements ClientMessageListener, ManagedObject, Serializable 
    {
        private int count;
        
        public void messageReceived(Message message, ClientSession session) {
            if (!(message instanceof TestMessageTwo)) {
                logger.warning("Received bad message: " + message);
            }
            
            logger.info("Received TestMessageTwo " + count + ": " + message + 
                        " from " + session.getName());
            count++;
        }
    }
    
    public static class TestMessageThreeListener 
            implements ClientMessageListener, Serializable 
    {
        public void messageReceived(Message message, ClientSession session) {
            if (!(message instanceof TestMessageThree)) {
                logger.warning("Received bad message: " + message);
            }
            
            logger.info("Received TestMessageThree: " + message + " from " + 
                        session.getName());
        }
    }
}
