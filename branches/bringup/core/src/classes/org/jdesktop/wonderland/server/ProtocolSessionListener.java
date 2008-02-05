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
package org.jdesktop.wonderland.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.comms.SessionInternalClientType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ExtractMessageException;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.OKMessage;
import org.jdesktop.wonderland.common.messages.ProtocolSelectionMessage;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.CommunicationsProtocol;

/**
 * This core session listener implements the basic Wonderland protocol
 * selection mechanism.  When a new client connects, they request a protcol
 * using a ProtocolSelectionMessage.  This listener handles the protcol
 * selection message, either by sending an error or instantiating the listener
 * associated with the given protocol type.
 * <p>
 * Once the session type has been successfully selected, this listener
 * simply acts as a wrapper, passing all request on to the delegated
 * listener.
 * <p>
 * TODO: these messages should be defined in binary and not as Java objects
 * to allow connections from non-Java clients.
 *
 * @author jkaplan
 */
public class ProtocolSessionListener
        implements ClientSessionListener, Serializable {
    
    /** a logger */
    private static final Logger logger =
            Logger.getLogger(ProtocolSessionListener.class.getName());
    
    /** the session associated with this listener */
    private ClientSession session;
    
    /** the protocol in use by this client */
    private CommunicationsProtocol protocol;
    
    /** the wrapped session, or null if no wrapped session exists yet */
    private ClientSessionListener wrapped;
    
    /**
     * Create a new instance of WonderlandSessionListener for the given
     * session
     * @param session the session connected to this listener
     */
    public ProtocolSessionListener(ClientSession session) {
        this.session = session;
    }
    
    /**
     * Initialize the session listener
     */
    public static void initialize() {
        DataManager dm = AppContext.getDataManager();
        
        // create map from protocols to clients
        dm.setBinding(ProtocolClientMap.DS_KEY, new ProtocolClientMap());
    }

    /**
     * Called when the listener receives a message.  If the wrapped session
     * has not yet been defined, look for ProtocolSelectionMessages, otherwise
     * simply forward the data to the delegate session
     * @param data the message data
     */
    public void receivedMessage(byte[] data) {
        
        // if there is a wrapped session, simply forward the data to it
        if (wrapped != null) {
            wrapped.receivedMessage(data);
            return;
        }
        
        // no wrapped session -- look for a ProtocolSelectionMessage
        try {
            // the message contains a client identifier in the first
            // 2 bytes, so ignore those
            Message m = Message.extract(data, 2, data.length - 2);
            
            // check the message type
            if (!(m instanceof ProtocolSelectionMessage)) {
                sendError(m, "Only ProtcolSelectionMessage allowed");
                return;
            }
            
            ProtocolSelectionMessage psm = (ProtocolSelectionMessage) m;
            CommsManager cm = WonderlandContext.getCommsManager();

            // see if we have a protocol to match the request
            CommunicationsProtocol cp = cm.getProtocol(psm.getProtocolName());
            if (cp == null) {
                sendError(m, "Protocol " + psm.getProtocolName() + " not found");
                return;
            }
            
            // see if the versions match
            if (!cp.getVersion().isCompatible(psm.getProtocolVersion())) {
                sendError(m, "Client version incompatible with server " + 
                             "version " + cp.getVersion());
            }
            
            logger.info("Session " + session.getName() + " connected with " +
                        "protocol " + cp.getName());
            
            // all set -- set the wrapped session
            wrapped = cp.createSessionListener(session, psm.getProtocolVersion());
            
            // TODO: is this the right thing to do, or should we only
            // do this automatically for the Wonderland protocol?
            WonderlandContext.getUserManager().login(session, this);
            
            // record the client connection
            this.protocol = cp;
            recordConnect(cp, session);
            
            // send an OK message
            sendToSession(new OKMessage(psm.getMessageID()));
        } catch (ExtractMessageException eme) {
            sendError(eme.getMessageID(), null, eme);
        }
    }

    /**
     * Called when the delegate session is disconnected
     * @param forced true if the disconnect was forced
     */
    public void disconnected(boolean forced) {
        if (wrapped != null) {
            wrapped.disconnected(forced);
            
            // TODO: is this the right thing to do, or should we only
            // do this automatically from the Wonderland protocol?
            WonderlandContext.getUserManager().logout(session, this);
        }
        
        // record client disconnect
        if (protocol != null) {
            recordDisconnect(protocol, session);
        }
    }
    
    /**
     * Get all clients using the given protocol
     * @param protocol the protocol to get clients for
     * @return a set of all clients connected with that protocol, or null
     * if no clients are connected via the protocol
     */
    public static Set<ClientSession> getClients(CommunicationsProtocol protocol)
    {
        return getProtocolClientMap().get(protocol);
    }
    
    /**
     * Get the session this listener represents.
     * @return the session connected to this listener
     */
    protected ClientSession getSession() {
        return session;
    }
    
    /**
     * Send an error to the session
     * @param message the source message
     * @param error the error to send
     */
    protected void sendError(Message source, String error) {
        sendError(source.getMessageID(), error, null);
    }
    
    /**
     * Send an error to the session
     * @param messageID the messageID of the original error
     * @param error the error message
     * @param cause the underlying exception
     */
    protected void sendError(MessageID messageID, String error, 
                             Throwable cause)
    {
        sendToSession(new ErrorMessage(messageID, error, cause));
    }
    
    /**
     * Send a message to the session
     * @param message the message to send
     */
    protected void sendToSession(Message message) {
        byte[] messageData = message.getBytes();
        int len = messageData.length + 2;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
        DataOutputStream out = new DataOutputStream(baos);
        
        try {
            out.writeShort(SessionInternalClientType.SESSION_INTERNAL_CLIENT_ID);
            out.write(messageData);
            out.close();
            
            session.send(baos.toByteArray());
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Unable to send message " + message, ioe);
        }
    }
    
    /**
     * Record a client of the given type connecting
     * @param protocol the protocol the session connected with
     * @param session the session that connected
     */
    protected void recordConnect(CommunicationsProtocol protocol,
                                 ClientSession session)
    {
        ProtocolClientMap pcm = getProtocolClientMap();
        
        DataManager dm = AppContext.getDataManager();
        dm.markForUpdate(pcm);
        
        pcm.add(protocol, session);
    }
    
    /**
     * Record a client of the given type disconnecting
     * @param protocol the protocol the session connected with
     * @param session the session that connected
     */
    protected void recordDisconnect(CommunicationsProtocol protocol,
                                    ClientSession session)
    {
        ProtocolClientMap pcm = getProtocolClientMap();
        
        DataManager dm = AppContext.getDataManager();
        dm.markForUpdate(pcm);
        
        pcm.remove(protocol, session);
    }
      
    /**
     * Get the protocol client map, which maps from protocols to clients
     * using that protocol
     * @return the ProtocolClientMap
     */
    protected static ProtocolClientMap getProtocolClientMap() {
        return AppContext.getDataManager().getBinding(ProtocolClientMap.DS_KEY, 
                                                      ProtocolClientMap.class);
    }
    
    /**
     * A record of clients connected with the given protocol
     */
    protected static class ProtocolClientMap
            implements ManagedObject, Serializable
    {
        /** the key in the datastore */
        private static final String DS_KEY = ProtocolClientMap.class.getName();
        
        /** mapping from protocol to clients */
        private Map<CommunicationsProtocol, ManagedReference> clientMap = 
                new HashMap<CommunicationsProtocol, ManagedReference>();
        
        /**
         * Add a session to a communications protocol
         * @param protocol the communications protocol
         * @param session the client session associated with the given protocol
         */
        public void add(CommunicationsProtocol protocol, ClientSession session) {
            ManagedReference ref = clientMap.get(protocol);
            if (ref == null) {
                ProtocolClientSet sessions = new ProtocolClientSet();
                ref = AppContext.getDataManager().createReference(sessions);
                clientMap.put(protocol, ref);
            }
            
            ProtocolClientSet sessions = ref.getForUpdate(ProtocolClientSet.class);
            sessions.add(session);
        }
        
        /**
         * Remove a session from a communications protocol
         * @param protocol the communications protocol
         * @param session the client session associated with the given protocol
         */
        public void remove(CommunicationsProtocol protocol, ClientSession session) {
            ManagedReference ref = clientMap.get(protocol);
            if (ref != null) {
                ProtocolClientSet sessions = ref.getForUpdate(ProtocolClientSet.class);
                sessions.remove(session);
                
                if (sessions.isEmpty()) {
                    clientMap.remove(protocol);
                }
            }
        }
        
        /**
         * Get all sessions associated with the given protocol
         * @param protocol the protocol
         * @return the set of client sessions associated with the given
         * protocol, or an empty set if no sessions are associated with
         * the protocol
         */
        public Set<ClientSession> get(CommunicationsProtocol protocol) {
            ManagedReference ref = clientMap.get(protocol);
            if (ref == null) {
                return Collections.emptySet();
            }
            
            // return a copy of the set
            return new HashSet(ref.get(ProtocolClientSet.class));
        }
    }
    
    static class ProtocolClientSet extends HashSet<ClientSession>
            implements ManagedObject, Serializable
    {
    }
}
