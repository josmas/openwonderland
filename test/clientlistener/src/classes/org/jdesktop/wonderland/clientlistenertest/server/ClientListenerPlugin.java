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
package org.jdesktop.wonderland.clientlistenertest.server;

import com.sun.sgs.app.ClientSession;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.clientlistenertest.common.TestMessageOne;
import org.jdesktop.wonderland.clientlistenertest.common.TestMessageThree;
import org.jdesktop.wonderland.clientlistenertest.common.TestMessageTwo;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientConnectionListener;
import org.jdesktop.wonderland.server.comms.CommsManager;

/**
 * Sample plugin that doesn't do anything
 * @author jkaplan
 */
public class ClientListenerPlugin implements ServerPlugin {
    private static final Logger logger =
            Logger.getLogger(ClientListenerPlugin.class.getName());
    
    public void initialize() {
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerConnectionListener(new TestConnectionListener());
    }
    
    static class TestConnectionListener
            implements ClientConnectionListener, Serializable 
    {

        public void connected(ClientSession session) {
            logger.info("Session connected: " + session.getName());
            
            // send message over session channel
            session.send(new TestMessageOne("TestOne").getBytes());
            
            // send message over all-clients channel
            CommsManager cm = WonderlandContext.getCommsManager();
            cm.sendToAllClients(new TestMessageTwo("TestTwo"));
            
            // send another message over session channel
            session.send(new TestMessageThree("TestThree", 42).getBytes());
        }

        public void disconnected(ClientSession session) {
            logger.info("Session disconnected: " + session.getName());
        }
        
    }
    
    
}
