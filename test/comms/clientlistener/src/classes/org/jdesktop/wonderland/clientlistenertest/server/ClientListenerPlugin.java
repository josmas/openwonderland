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

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSessionId;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.clientlistenertest.common.TestClientType;
import org.jdesktop.wonderland.clientlistenertest.common.TestMessageOne;
import org.jdesktop.wonderland.clientlistenertest.common.TestMessageThree;
import org.jdesktop.wonderland.clientlistenertest.common.TestMessageTwo;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientHandler;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientChannel;

/**
 * Sample plugin that doesn't do anything
 * @author jkaplan
 */
public class ClientListenerPlugin implements ServerPlugin {
    private static final Logger logger =
            Logger.getLogger(ClientListenerPlugin.class.getName());
    
    public void initialize() {
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerClientHandler(new TestClientHandler());
    }
    
    static class TestClientHandler
            implements ClientHandler, ManagedObject, Serializable
    {
        public ClientType getClientType() {
            return TestClientType.CLIENT_ONE_TYPE;
        }

        public void registered(WonderlandClientChannel channel) {
        }

        public void clientAttached(WonderlandClientChannel channel,
                                   ClientSessionId sessionId) 
        {
            logger.info("Session connected: " + sessionId);
            
            // send message over session channel
            channel.send(sessionId, new TestMessageOne("TestOne"));
            
            // send message over all-clients channel
            channel.send(new TestMessageTwo("TestTwo"));
            
            // now schedule a task to send more messages
            AppContext.getTaskManager().scheduleTask(new SendTask(channel, 
                                                                  sessionId));
        }

        public void messageReceived(WonderlandClientChannel channel,
                                    ClientSessionId sessionId, 
                                    Message message)
        {
            // ignore
        }

        public void clientDetached(WonderlandClientChannel channel,
                                   ClientSessionId sessionId) {
            // ignore
        }
        
        static class SendTask implements Task, Serializable {
            private WonderlandClientChannel channel;
            private ClientSessionId sessionId;
            
            public SendTask(WonderlandClientChannel channel, 
                            ClientSessionId sessionId) 
            {
                this.channel = channel;
                this.sessionId = sessionId;
            }

            public void run() throws Exception {
                channel.send(new TestMessageOne("TestOneFromTask"));
                channel.send(sessionId, new TestMessageThree("TestThree", 42));
            }   
        }
    }
}
