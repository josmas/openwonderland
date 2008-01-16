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
package org.jdesktop.wonderland.client.comms;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.SessionId;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.ProtocolVersion;
import org.jdesktop.wonderland.common.comms.WonderlandChannelNames;
import org.jdesktop.wonderland.common.comms.WonderlandProtocolVersion;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * This class provides the client side instance of a particular Wonderland
 * server. All interaction with a server are handled by this class.
 * 
 * @author kaplanj
 */
@ExperimentalAPI
public class WonderlandClient extends BaseClient {
    /** a logger */
    private static final Logger logger =
            Logger.getLogger(WonderlandClient.class.getName());
    
    /** the name of the standard all-clients channel */
    private static final String ALL_CLIENTS_CHANNEL = 
            WonderlandChannelNames.WONDERLAND_PREFIX + ".ALL_CLIENTS";
    
    /**
     * {@inheritDoc}
     */
    public WonderlandClient(WonderlandServerInfo server) {
        super (server);
        
        // add default channel listeners
        registerChannelListenerFactory(new DefaultChannelListenerFactory());   
    }
    
    /**
     * Return the Wonderland protocol name
     * @return the name of the Wonderland client protcol
     */
    @Override
    protected String getProtocolName() {
        return WonderlandProtocolVersion.PROTOCOL_NAME;
    }

    /**
     * Return the Wonderland protocol version
     * @return the version of the Wonderland protocol
     */
    @Override
    protected ProtocolVersion getProtocolVersion() {
        return WonderlandProtocolVersion.VERSION;
    }
   
    /**
     * Handle built-in channels
     */
    class DefaultChannelListenerFactory implements ChannelListenerFactory {
        /**
         * {@inheritDoc}
         */
        public ClientChannelListener createListener(BaseClient client,
                                                    ClientChannel channel)
        {
            if (channel.getName().equals(ALL_CLIENTS_CHANNEL)) { 
                return new AllClientsChannelListener();
            } else {
                return null;
            }
        }
    }
    
    /**
     * Pass messages from the all-client channel in to the session message
     * listeners.
     */
    class AllClientsChannelListener implements ClientChannelListener {
        /**
         * {@inheritDoc}
         */
        public void receivedMessage(ClientChannel channel, SessionId session, 
                                    byte[] data) 
        {
            try {
                // extract the message
                Message message = Message.extract(data);
            
                // notify listeners
                fireSessionMessageReceived(message);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error extracting message from server", 
                           ex);
            }
        }
            
        /**
         * {@inheritDoc}
         */
        public void leftChannel(ClientChannel channel) {
            // ignore
        }
    }
}
