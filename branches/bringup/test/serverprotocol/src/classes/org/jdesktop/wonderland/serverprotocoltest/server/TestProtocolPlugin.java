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
package org.jdesktop.wonderland.serverprotocoltest.server;

import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.CommunicationsProtocol;
import org.jdesktop.wonderland.server.comms.ProtocolVersion;
import org.jdesktop.wonderland.serverprotocoltest.common.TestProtocolVersion;

/**
 * Sample plugin that doesn't do anything
 * @author jkaplan
 */
public class TestProtocolPlugin implements ServerPlugin {
    private static final Logger logger =
            Logger.getLogger(TestProtocolPlugin.class.getName());
    
    public void initialize() {
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerProtocol(new TestProtocol());
    }
    
    public static class TestProtocol 
            implements CommunicationsProtocol, Serializable
    {
        private ProtocolVersion version = new TestProtocolVersion();
        
        public String getName() {
            return TestProtocolVersion.PROTOCOL_NAME;
        }

        public ProtocolVersion getVersion() {
            return version;
        }

        public ClientSessionListener createSessionListener(ClientSession session, 
                                                           ProtocolVersion version) 
        {
            return new TestProtocolSessionListener(session);
        }
    }
    
    public static class TestProtocolSessionListener
            implements ClientSessionListener, Serializable
    {
        private static final Logger logger =
                Logger.getLogger(TestProtocolSessionListener.class.getName());
        
        private ClientSession session;

        private TestProtocolSessionListener(ClientSession session) {
            logger.info("New session for " + session);
            
            this.session = session;
        }
            
        public void receivedMessage(byte[] data) {
            logger.info("Received " + data.length + " bytes from " + session + 
                        ".");
        }

        public void disconnected(boolean forced) {
            logger.info("Session " + session + " disconnected.");
        } 
    }
}
