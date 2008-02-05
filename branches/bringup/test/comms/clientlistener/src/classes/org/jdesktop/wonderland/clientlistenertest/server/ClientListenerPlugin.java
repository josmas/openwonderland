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
import org.jdesktop.wonderland.server.comms.WonderlandClientSession;

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
        private WonderlandClientChannel channel;
        
        public ClientType getClientType() {
            return TestClientType.CLIENT_ONE_TYPE;
        }

        public void registered(WonderlandClientChannel channel) {
            this.channel = channel;
        }

        public void clientAttached(WonderlandClientSession session) {
            logger.info("Session connected: " + session.getName());
            
            // send message over session channel
            session.send(new TestMessageOne("TestOne"));
            
            // send message over all-clients channel
            channel.send(new TestMessageTwo("TestTwo"));
            
            // now schedule a task to send more messages
            AppContext.getTaskManager().scheduleTask(new SendTask(channel, 
                                                                  session));
        }

        public void messageReceived(WonderlandClientSession session, 
                                    Message message)
        {
            // ignore
        }

        public void clientDetached(WonderlandClientSession session) {
            // ignore
        }
        
        static class SendTask implements Task, Serializable {
            private WonderlandClientSession session;
            private WonderlandClientChannel channel;
            
            public SendTask(WonderlandClientChannel channel, 
                            WonderlandClientSession session) 
            {
                this.channel = channel;
                this.session = session;
            }

            public void run() throws Exception {
                channel.send(new TestMessageOne("TestOneFromTask"));
                session.send(new TestMessageThree("TestThree", 42));
            }   
        }
    }
}
