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
package org.jdesktop.wonderland.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionId;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.comms.SessionInternalClientType;
import org.jdesktop.wonderland.common.comms.WonderlandChannelNames;
import org.jdesktop.wonderland.common.comms.messages.AttachClientMessage;
import org.jdesktop.wonderland.common.comms.messages.AttachedClientMessage;
import org.jdesktop.wonderland.common.comms.messages.DetachClientMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ExtractMessageException;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.server.comms.ClientHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientChannel;
import org.jdesktop.wonderland.server.comms.WonderlandClientSession;

/**
 * This is the default session listener is used by Wonderland clients.
 * Clients can select this listener by specifiying "wonderland_client" in
 * the protocol selection message.
 * <p>
 * The WonderlandSessionListener supports an extensible set of listeners.
 * Listeners can be registered to listen for specific message types.  These
 * messages will be reported for all sessions connected to the listener.
 * Note that listeners are not dynamic: listeners must be registered before
 * the first session is created.  Listeners added after a session is created
 * will not be used in this session.
 * <p>
 * Other convenience methods exist to send messages to all clients.
 *
 * @author jkaplan
 */
@ExperimentalAPI
public class WonderlandSessionListener
        implements ClientSessionListener, Serializable {
    
    /** a logger */
    private static final Logger logger =
            Logger.getLogger(WonderlandSessionListener.class.getName());
    
    /** client ID of the internal session handler */
    private static final short SESSION_INTERNAL_CLIENT_ID =
            SessionInternalClientType.SESSION_INTERNAL_CLIENT_ID;
    
    /** the session associated with this listener */
    private ClientSession session;
    
    /** a map from the ID we've assigned a client to the handler for that
        client */
    private Map<Short, ClientHandlerRef> handlers;
    
    /**
     * Create a new instance of WonderlandSessionListener for the given
     * session
     * @param session the session connected to this listener
     */
    public WonderlandSessionListener(ClientSession session) {
        this.session = session;
        
        if (logger.isLoggable(Level.FINE)) {
            logger.finest("New session listener for " + session.getName());
        }
        
        // initialize maps
        handlers = new HashMap<Short, ClientHandlerRef>();
        
        // add internal handler
        handlers.put(Short.valueOf(SESSION_INTERNAL_CLIENT_ID), 
                     new ClientHandlerRef(new SessionInternalHandler()));
    }
        
    /**
     * Initialize the session listener
     */
    public static void initialize() {
        logger.fine("Initialize WonderlandSessionListener");
        
        DataManager dm = AppContext.getDataManager();
        
        // create store for registered handlers
        dm.setBinding(HandlerStore.DS_KEY, new HandlerStore());
    }
    
    /**
     * Called when the listener receives a message.  If the wrapped session
     * has not yet been defined, look for ProtocolSelectionMessages, otherwise
     * simply forward the data to the delegate session
     * @param data the message data
     */
    public void receivedMessage(byte[] data) {
        try {
            // extract the client id
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            Short clientID = Short.valueOf(dis.readShort());
            
            // extract the message
            Message m = Message.extract(data, 2, data.length - 2);
            
            // find the handler
            ClientHandler handler = getHandler(clientID);
            if (handler == null) {
                logger.fine("Session " + getSession().getName() + 
                            " unknown handler ID: " + clientID);
                sendError(m.getMessageID(), clientID,
                          "Unknown handler ID: " + clientID);
                return;
            }
            
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Session " + getSession().getName() + 
                              " received message " + m + 
                              " for client ID" + clientID + 
                              " handled by " + handler.getClientType());
            }
            
            // create a new WonderlandClientSession to pass in
            WonderlandClientSession wonderlandSession = 
                    new WonderlandClientSessionImpl(handler.getClientType(),
                                                    clientID.shortValue(),
                                                    getSession());
            handler.messageReceived(wonderlandSession, m);
        } catch (ExtractMessageException eme) {
            logger.log(Level.WARNING, "Error extracting message from client", 
                       eme);
            
            // if possible, send a reply to the client
            if (eme.getMessageID() != null) {
                sendError(eme.getMessageID(), SESSION_INTERNAL_CLIENT_ID, eme);
            }
        } catch (Exception ex) {
            // what to do?
            logger.log(Level.WARNING, "Error extracting message from client",
                       ex);
        }
    }

    /**
     * Called when the delegate session is disconnected
     * @param forced true if the disconnect was forced
     */
    public void disconnected(boolean forced) {
        logger.warning("Session " + getSession().getName() + " disconnected");
        
        // Detach all handlers. Convert IDs to an array first because
        // the map is modified in the handleDetach method, which causes
        // a concurrent modification exception if we are iterating directly
        // over the key set
        Short[] clientIDs = handlers.keySet().toArray(new Short[0]);
        for (Short clientID : clientIDs) {
            handleDetach(clientID.shortValue());
        }
        
        // clear the list
        handlers.clear();
    }
     
    /**
     * Register a handler that will handle connections from a particular
     * WonderlandClient type.
     * @see org.jdesktop.wonderland.server.comms.CommsManager#registerClientHandler(ClientHandler)
     *
     * @param handler the handler to register
     */
    public static void registerClientHandler(ClientHandler handler) {
        logger.fine("Register client handler for type " + 
                    handler.getClientType());
        
        HandlerStore store = getHandlerStore();
        short clientID = store.register(handler);
                
        // get the channel for this hander
        Channel channel = store.getChannel(handler.getClientType());
        
        // let Darkstar know this is an update        
        AppContext.getDataManager().markForUpdate(store);
        
        // notify the handler
        WonderlandClientChannel wonderlandChannel =
                new WonderlandClientChannelImpl(handler.getClientType(),
                                                clientID, channel);
        handler.registered(wonderlandChannel);
    }
    
    /**
     * Get all clients connected via the given WonderlandClient
     * @see org.jdesktop.wonderland.server.comms.CommsManager#getClients(ClientType)
     * 
     * @param type the type of client to get sessions for
     * @return a set of all client sessions connected via a client of the given
     * type, or an empty set if no clients are connected via the given type
     * @throws IllegalStateException if the given type is not registered
     */
    public static Set<WonderlandClientSession> getClients(ClientType type) {
        Channel channel = getHandlerStore().getChannel(type);
        if (channel == null) {
            throw new IllegalStateException("Client type not registered: " + 
                                            type);
        }
        short clientID = getHandlerStore().getClientID(type);
                
        // wrap the sessions
        Set<WonderlandClientSession> out = 
                 new HashSet<WonderlandClientSession>();
        for (ClientSession session : channel.getSessions()) {
            out.add(new WonderlandClientSessionImpl(type, clientID, session));
        }
            
        return out;
    }
    
    /**
     * Get a channel that can be used to send messages to all clients
     * of a given ClientType
     * @see org.jdesktop.wonderland.server.comms.CommsManager#getChannel(ClientType)
     * 
     * @param type the type of client to get a channel to
     * @return a channel for sending to all clients of the given type
     * @throws IllegalStateException if no handler is registered for the given
     * type
     */
    public static WonderlandClientChannel getChannel(ClientType type) {
        Channel channel = getHandlerStore().getChannel(type);
        if (channel == null) {
            throw new IllegalStateException("Client type not registered: " + 
                                            type);
        }
        short clientID = getHandlerStore().getClientID(type);
                
        return new WonderlandClientChannelImpl(type, clientID, channel);
    }

    /**
     * Get the session this listener represents.
     * @return the session connected to this listener
     */
    protected ClientSession getSession() {
        return session;
    }
    
    /**
     * Get a client handler by client ID
     * @param clientID the id of the client to get
     * @return the handler for the given ID, or null if there is no 
     * handler for the given ID
     */
    protected ClientHandler getHandler(Short clientID) {
        ClientHandlerRef ref = handlers.get(clientID);
        if (ref == null) {
            return null;
        }
        
        return ref.get();
    }
    
    /**
     * Remove the client handler with the given clientID
     * @param clientID the id of the client to remove
     * @return the removed handler for the given ID, or null if there is no 
     * handler for the given ID
     */
    protected ClientHandler removeHandler(Short clientID) {
        ClientHandlerRef ref = handlers.remove(clientID);
        if (ref == null) {
            return null;
        }
        
        return ref.get();
    }
    
    /**
     * Handle an attach request
     * @param messageID the ID of the message to respond to
     * @param type the type of client to attach
     */
    protected void handleAttach(MessageID messageID, ClientType type) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Session " + getSession().getName() + " attach " +
                        "client type " + type);
        }
        
        // get the handler for this type
        ClientHandlerRef ref = getHandlerStore().getHandlerRef(type);
        if (ref == null) {
            logger.fine("Session " + session.getName() + " no handler for " +
                        "client type " + type);
            sendError(messageID, SESSION_INTERNAL_CLIENT_ID,
                      "No handler for " + type);
            return;
        }
        
        // get the ID for this type
        short clientID = getHandlerStore().getClientID(type);
        
        // make sure this isn't a duplicate join
        if (handlers.containsKey(Short.valueOf(clientID))) {
            logger.fine("Session " + session.getName() + " duplicate client " +
                        "for type " + type);
            sendError(messageID, SESSION_INTERNAL_CLIENT_ID,
                          "Duplicate client for " + type);
            return;
        }
        
        // add handler to our list
        handlers.put(Short.valueOf(clientID), ref);
        
        // send response message
        Message resp = new AttachedClientMessage(messageID, clientID);
        sendToSession(SESSION_INTERNAL_CLIENT_ID, resp);
        
        // add the client to the handler's channel
        Channel channel = getHandlerStore().getChannel(type);
        channel.join(session, null);
        
        // notify the handler
        WonderlandClientSession wonderlandSession =
                new WonderlandClientSessionImpl(type, clientID, getSession());
        ref.get().clientAttached(wonderlandSession);
    }
    
    /**
     * Handle a detach request
     * @param clientID the id of the client to detach
     */
    protected void handleDetach(short clientID) {
        ClientHandler handler = getHandler(Short.valueOf(clientID));
        if (handler == null) {
            logger.fine("Detach unknown client ID " + clientID);
            return;
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Session " + getSession().getName() + " detach " +
                        "client type " + handler.getClientType());
        }
        
        // remove this client from the handler channel
        Channel channel = getHandlerStore().getChannel(handler.getClientType());
        if (channel != null) {
            channel.leave(session);
        }
        
        // remove the handler from the map
        removeHandler(Short.valueOf(clientID));
        
        // notify the handler
        WonderlandClientSession wonderlandSession =
                new WonderlandClientSessionImpl(handler.getClientType(), 
                                                clientID, getSession());
        handler.clientDetached(wonderlandSession);
    }
    
    /**
     * Send an error to the session
     * @param messageID the source message's ID
     * @param clientID the client ID to send to
     * @param error the error to send
     */
    protected void sendError(MessageID messageID, short clientID, String error)
    {
        sendError(messageID, clientID, error, null);
    }
    
    /**
     * Send an error to the session
     * @param messageID the source message's ID
     * @param clientID the client ID to send to
     * @param cause the cause of the error
     */
    protected void sendError(MessageID messageID, short clientID, 
                             Throwable cause)
    {
        sendError(messageID, clientID, null, cause);
    }
    
    /**
     * Send an error to the session
     * @param messageID the messageID of the original error
     * @param clientID the client ID to send to
     * @param error the error message
     * @param cause the underlying exception
     */
    protected void sendError(MessageID messageID, short clientID, 
                             String error, Throwable cause)
    {
        ErrorMessage msg = new ErrorMessage(messageID, error, cause);
        sendToSession(clientID, msg);
    }
   
    /**
     * Send a message to the session channel using the given client ID.
     * @param clientID the client ID to use when sending
     * @param message the message to send
     */
    protected void sendToSession(short clientID, Message message) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Session " + getSession().getName() + " send " +
                          "message " + message + " to client " + clientID);
        }
        
        sendToSession(getSession(), clientID, message.getBytes());
    }
    
    /**
     * Send a message to the given session channel using the given client ID.
     * This will pre-pend the client id onto the given data and send it
     * over the session channel.
     * @param session the session to send to
     * @param clientID the client ID to use when sending
     * @param data the data to send
     */
    protected static void sendToSession(ClientSession session, short clientID, 
                                        byte[] data) {
    
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Session " + session.getName() + " send " +
                          data.length + " bytes to client " + clientID);
        }
        
        int len = data.length + 2;
        
        // pre-pend the client id
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
        DataOutputStream out = new DataOutputStream(baos);
        
        try {
            out.writeShort(clientID);
            out.write(data);
            out.close();
            
            session.send(baos.toByteArray());
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Unable to end to " + clientID, ioe);
        }
    }
    
    /**
     * Get the store of registered handlers
     * @return the store of registered handlers
     */
    private static HandlerStore getHandlerStore() {
        return AppContext.getDataManager().getBinding(HandlerStore.DS_KEY,
                                                      HandlerStore.class);
    }
    
    /**
     * Wrap Darkstar channel into a WonderlandClientChannel.  This is used
     * by ClientHandlers to send to the particular client.
     */
    static class WonderlandClientChannelImpl 
            implements WonderlandClientChannel, Serializable
    {
        /** the client type */
        private ClientType type;
        
        /** the underlying channel */
        private Channel channel;
        
        /** the client ID for this client */
        private short clientID;
        
        /** 
         * Create a new WonderlandClientChannelImpl
         * @param type the client type
         * @param clientID the client ID to send with
         * @param channel the channel to wrap
         */
        public WonderlandClientChannelImpl(ClientType type, short clientID,
                                           Channel channel) 
        {
            this.type = type;
            this.channel = channel;
            this.clientID = clientID;
        }
        
        public ClientType getClientType() {
            return type;
        }

        public Set<WonderlandClientSession> getSessions() {
            Set<WonderlandClientSession> out = 
                    new HashSet<WonderlandClientSession>();
            
            // wrap the sessions
            for (ClientSession session : channel.getSessions()) {
                out.add(new WonderlandClientSessionImpl(type, clientID, session));
            }
            
            return out;
        }

        public String getName() {
            return channel.getName();
        }

        public Delivery getDeliveryRequirement() {
            return channel.getDeliveryRequirement();
        }

        public boolean hasSessions() {
            return channel.hasSessions();
        }

        public void send(Message message) {
            channel.send(message.getBytes());
        }

        public void send(WonderlandClientSession session, Message message) {
            channel.send(session, message.getBytes());
        }

        public void send(Set<WonderlandClientSession> sessions, Message message) 
        {
            // convert to the right type
            Set<ClientSession> clientSessions = new HashSet<ClientSession>(sessions);
            channel.send(clientSessions, message.getBytes());
        }
    }
    
    /**
     * Wrap a WonderlandSession
     */
    static class WonderlandClientSessionImpl 
            implements WonderlandClientSession, Serializable
    {
        /** the client type */
        private ClientType type;
        
        /** the client id to send with */
        private short clientID;
        
        /** the wrapped session */
        private ClientSession session;
        
        /**
         * Create a new WonderlandClientSessionImpl
         * @param type the type of client
         * @param clientID the client id to send with
         * @param session the session to wrap
         */
        public WonderlandClientSessionImpl(ClientType type, short clientID,
                                           ClientSession session)
        {
            this.type = type;
            this.clientID = clientID;
            this.session = session;
        }
        
        public ClientType getClientType() {
            return type;
        }

        public void send(Message message) {
            send(message.getBytes());
        }

        public void send(byte[] data) {
            sendToSession(session, clientID, data);
        }
        
        public String getName() {
            return session.getName();
        }

        public ClientSessionId getSessionId() {
            return session.getSessionId();
        }

        public boolean isConnected() {
            return session.isConnected();
        }

        public void disconnect() {
            throw new UnsupportedOperationException(
                    "Cannot disconnect WonderlandClientSession");
        }
    }
    
    /**
     * Handle internal messages from the WonderlandSession object
     */
    class SessionInternalHandler implements ClientHandler, Serializable {

        public ClientType getClientType() {
            return SessionInternalClientType.SESSION_INTERNAL_CLIENT_TYPE;
        }

        public void registered(WonderlandClientChannel channel) {
            // ignore
        }
        
        public void clientAttached(WonderlandClientSession session) {
            // ignore
        }
        
        public void clientDetached(WonderlandClientSession session) {
            // ignore
        }

        public void messageReceived(WonderlandClientSession session, 
                                    Message message)
        {
            if (message instanceof AttachClientMessage) {
                AttachClientMessage acm = (AttachClientMessage) message;
                handleAttach(acm.getMessageID(), acm.getClientType());
            } else if (message instanceof DetachClientMessage) {
                DetachClientMessage dcm = (DetachClientMessage) message;
                handleDetach(dcm.getClientID());
            }
        }
    }
    
    /**
     * Store all registered handlers, mapped by ClientType
     */
    private static class HandlerStore implements ManagedObject, Serializable {
        /** the key in the data store */
        private static final String DS_KEY = HandlerStore.class.getName();
        
        /** prefix for client-specific channels */
        private static final String CLIENT_CHANNEL_PREFIX =
                WonderlandChannelNames.WONDERLAND_PREFIX + ".Client.";
    
        
        /** the handlers, mapped by ClientType */
        private Map<ClientType, HandlerRecord> handlers = 
                new HashMap<ClientType, HandlerRecord>();
        
        /** the next client ID to assign */
        private short clientID = 0;
        
        /**
         * Register a new handler type
         * @param handler the handler to register
         * @return the clientID that will be used for this handler
         */
        public short register(ClientHandler handler) {
            
            // check for duplicates
            if (handlers.containsKey(handler.getClientType())) {
                throw new IllegalStateException("Handler for type " + 
                        handler.getClientType() + " already registered.");
            }
            
            // decide the correct type of reference depending on if the
            // handler is a managed object or not
            ClientHandlerRef ref;
            if (handler instanceof ManagedObject) {
                ref = new ManagedClientHandlerRef(handler);
            } else {
                ref = new ClientHandlerRef(handler);
            }
            
            // create the channel
            String channelName = CLIENT_CHANNEL_PREFIX + 
                    handler.getClientType().toString(); 
             
            ChannelManager cm = AppContext.getChannelManager();
            Channel channel = cm.createChannel(channelName, null, 
                                               Delivery.RELIABLE);
            
            // add to the map
            HandlerRecord record = new HandlerRecord();
            record.ref = ref;
            record.channel = channel;
            record.clientID = clientID++;
            handlers.put(handler.getClientType(), record);
            
            return record.clientID;
        }
        
        /**
         * Unregister a handler
         * @param handler the handler to unregister
         */
        public void unregister(ClientHandler handler) {
            HandlerRecord record = handlers.remove(handler.getClientType());
            
            // clear the reference, which will remove a managed object
            // handler from the data store
            if (record != null) {
                record.ref.clear();
            }
        }
        
        /**
         * Get a handler for the given client type
         * @param type the client type to look up a handler for
         * @return a handler for the given type, or null if none is
         * registered
         */
        public ClientHandlerRef getHandlerRef(ClientType type) {
            HandlerRecord record = handlers.get(type);
            if (record == null) {
                return null;
            }
            
            return record.ref;
        }
        
        /**
         * Get the channel for the given client type
         * @param type the client type to look up a handler for
         * @return a channel for the given type, or null if none is registered
         */
        public Channel getChannel(ClientType type) {
            HandlerRecord record = handlers.get(type);
            if (record == null) {
                return null;
            }
            
            return record.channel;
        }
        
        /**
         * Get the clientID for the given client type
         * @param type the client type to look up an ID for
         * @return the clientID for the given type or -1 if none is found
         */
        public short getClientID(ClientType type) {
            HandlerRecord record = handlers.get(type);
            if (record == null) {
                return -1;
            }
            
            return record.clientID;
        }
        
        // a handler reference and its associated channel
        class HandlerRecord implements Serializable {
            ClientHandlerRef ref;
            Channel channel;
            short clientID;
        }
    }
    
    /**
     * A reference to a regular client handler
     */
    static class ClientHandlerRef implements Serializable {
        private ClientHandler handler;
       
        public ClientHandlerRef(ClientHandler handler) {
            this.handler = handler;
        }
        
        public ClientHandler get() {
            return handler;
        }
        
        public void clear() {
            handler = null;
        }
    }
    
    /**
     * A reference to a managed client handler
     */
    static class ManagedClientHandlerRef extends ClientHandlerRef 
            implements Serializable 
    {
        private ManagedReference ref;
        
        public ManagedClientHandlerRef(ClientHandler handler) {
            super (null);
            
            DataManager dm = AppContext.getDataManager();
            ref = dm.createReference((ManagedObject) handler);
        }
        
        @Override
        public ClientHandler get() {
            return ref.get(ClientHandler.class);
        }
        
        @Override
        public void clear() {
            ClientHandler handler = get();
            AppContext.getDataManager().removeObject((ManagedObject) handler);
        }
    }
}
