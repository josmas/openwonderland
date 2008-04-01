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
package org.jdesktop.wonderland.server.comms;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.HandlerType;
import org.jdesktop.wonderland.common.comms.SessionInternalHandlerType;
import org.jdesktop.wonderland.common.comms.messages.AttachClientMessage;
import org.jdesktop.wonderland.common.comms.messages.AttachedClientMessage;
import org.jdesktop.wonderland.common.comms.messages.DetachClientMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ExtractMessageException;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.MessagePacker;
import org.jdesktop.wonderland.common.messages.MessagePacker.PackerException;
import org.jdesktop.wonderland.common.messages.MessagePacker.ReceivedMessage;

/**
 * This is the default session listener is used by Wonderland clients.
 * Clients can select this listener by specifiying "wonderland_client" in
 * the protocol selection message.
 * <p>
 * The WonderlandSessionListener supports an extensible set of handlers.
 * Hanlders can be registered to service for specific message types.  These
 * messages will be reported for all sessions connected to the handler.
 * Note that handlers are not dynamic: handlers must be registered before
 * the first session is created.  Handlers added after a session is created
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
            SessionInternalHandlerType.SESSION_INTERNAL_CLIENT_ID;
    
    /** the session associated with this listener */
    private ManagedReference<ClientSession> sessionRef;
    
    /** a map from the ID we've assigned a client to the handler for that
        client */
    private Map<Short, ClientHandlerRef> handlers;
    
    /** a map from the ID we've assigned a client to the sender for that
        client */
    private Map<Short, WonderlandClientSenderImpl> senders;
    
    /**
     * Create a new instance of WonderlandSessionListener for the given
     * session
     * @param session the session connected to this listener
     */
    public WonderlandSessionListener(ClientSession session) {
        DataManager dm = AppContext.getDataManager();
        sessionRef = dm.createReference(session);
        
        if (logger.isLoggable(Level.FINE)) {
            logger.finest("New session listener for " + session.getName());
        }
        
        // initialize maps
        handlers = new TreeMap<Short, ClientHandlerRef>();
        senders = new TreeMap<Short, WonderlandClientSenderImpl>();
                
        // add internal handler
        ClientHandlerRef internalRef = getHandlerStore().getHandlerRef(
                    SessionInternalHandlerType.SESSION_INTERNAL_CLIENT_TYPE);
        ((SessionInternalHandler) internalRef.get()).setListener(this);
        handlers.put(SESSION_INTERNAL_CLIENT_ID, internalRef);
    }
        
    /**
     * Initialize the session listener
     */
    public static void initialize() {
        logger.fine("Initialize WonderlandSessionListener");
        
        DataManager dm = AppContext.getDataManager();
        
        // create store for registered handlers
        dm.setBinding(HandlerStore.DS_KEY, new HandlerStore());
    
        // register the internal handler
        SessionInternalHandler internal = new SessionInternalHandler();
        registerClientHandler(internal);
    }
    
    /**
     * Called when the listener receives a message.  If the wrapped session
     * has not yet been defined, look for ProtocolSelectionMessages, otherwise
     * simply forward the data to the delegate session
     * @param data the message data
     */
    public void receivedMessage(ByteBuffer data) {
        try {
            // extract the message and client id
            ReceivedMessage recv = MessagePacker.unpack(data);
            Message m = recv.getMessage();
            short clientID = recv.getClientID();
            
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
            
            // get the WonderlandClientSender to pass in
            WonderlandClientSender sender = senders.get(clientID);
            
            // call the handler
            handler.messageReceived(sender, getSession(), m);
            
        } catch (PackerException eme) {
            logger.log(Level.WARNING, "Error extracting message from client", 
                       eme);
            
            // if possible, send a reply to the client
            if (eme.getMessageID() != null) {
                sendError(eme.getMessageID(), eme.getClientID(), eme);
            }
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
        store.register(handler);
                
        // let Darkstar know this is an update        
        AppContext.getDataManager().markForUpdate(store);
        
        // notify the handler
        handler.registered(store.getSender(handler.getClientType()));
    }
    
    /**
     * Unregister a client handler that was previously registered
     * @param handler the handler to unregister
     */
    public static void unregisterClientHandler(ClientHandler handler) {
        logger.fine("Unregister client handler for type " + 
                    handler.getClientType());
    
        getHandlerStore().unregister(handler);
    }
    
    /**
     * Get the handler for the given type
     * @param clientType the type of client to get a handler for
     * @return the handler for the given type, or null if no handler
     * is registered for the given type
     */
    public static ClientHandler getClientHandler(HandlerType clientType) {
        ClientHandlerRef ref = getHandlerStore().getHandlerRef(clientType);
        if (ref == null) {
            return null;
        }
        
        return ref.get();
    }
    
    /**
     * Get all client handlers
     * @return the set of all client handlers
     */
    public static Set<ClientHandler> getClientHandlers() {
        return getHandlerStore().getHandlers();
    }
    
    /**
     * Get a sender that can be used to send messages to all clients
     * of a given HandlerType
     * @see org.jdesktop.wonderland.server.comms.CommsManager#getSender(HandlerType)
     * 
     * @param type the type of client to get a channel to
     * @return a sender for sending to all clients of the given type
     * @throws IllegalStateException if no handler is registered for the given
     * type
     */
    public static WonderlandClientSender getSender(HandlerType type) {
        WonderlandClientSender sender = getHandlerStore().getSender(type);
        if (sender == null) {
            throw new IllegalStateException("No handler registered for " + type);
        }
        
        return sender;
    }

    /**
     * Get the session this listener represents.
     * @return the session connected to this listener
     */
    protected ClientSession getSession() {
        return sessionRef.get();
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
    protected void handleAttach(MessageID messageID, HandlerType type) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Session " + getSession().getName() + " attach " +
                        "client type " + type);
        }
        
        ClientSession session = getSession();
        
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
        WonderlandClientSenderImpl sender = getHandlerStore().getSender(type);
        short clientID = sender.getClientID();
        
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
        
        // add this session to the sender
        sender.addSession(session);
        
        // Save the WonderlandChannel that we can use to communicate 
        // with this cell.  Store the channel locally since it is used in
        // every call to messageReceived()
        senders.put(clientID, sender);
        
        // notify the handler
        ref.get().clientAttached(sender, session);
    }
    
    /**
     * Handle a detach request
     * @param clientID the id of the client to detach
     */
    protected void handleDetach(short clientID) {
//        logger.warning("DETACHING "+clientID+"  session "+getSession());
        ClientHandler handler = getHandler(Short.valueOf(clientID));
        if (handler == null) {
            logger.fine("Detach unknown client ID " + clientID);
            return;
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Session " + getSession().getName() + " detach " +
                        "client type " + handler.getClientType());
        }
        
        // remove this client from the sender
        WonderlandClientSenderImpl sender = senders.remove(clientID);
        if (sender==null)
            logger.warning("NULL Sender, is this expected ?");
        else
            sender.removeSession(getSession());
        
        // remove the handler from the map
        removeHandler(Short.valueOf(clientID));
        
        // notify the handler
        handler.clientDetached(sender, getSession());
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
     * @throws IllegalArgumentException if there is an error serializing
     * the given message
     */
    protected void sendToSession(short clientID, Message message) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Session " + getSession().getName() + " send " +
                          "message " + message + " to client " + clientID);
        }
        
        getSession().send(serializeMessage(message, clientID));
    }
        
    /**
     * Serialize the given message into a ByteBuffer to send to a client of the
     * given type.
     * @param message the message to serialize
     * @param clientID the clientID of the handler to send it to
     * @return a ByteBuffer containing the serialized message
     * @throws IllegalArgumentException if there is an error serializing the
     * given message
     */
    private static ByteBuffer serializeMessage(Message message, short clientID)
    {
        try {
            return MessagePacker.pack(message, clientID);
        } catch (PackerException ioe) {
            throw new IllegalArgumentException("Error serializing " + message,
                                               ioe);
        }    
    }
    
    /**
     * Get the store of registered handlers
     * @return the store of registered handlers
     */
    private static HandlerStore getHandlerStore() {
        return (HandlerStore) AppContext.getDataManager().getBinding(HandlerStore.DS_KEY);
    }
    
    /**
     * A sender that sends data to clients of a particular type.  The sender
     * itself is serializable, but the data it uses (the channel and session
     * set) are ManagedObjects.  Therefore any number of separate senders
     * can point to the same underlying session set and channels.
     * <p>
     * A sender is valid as long as the handler for the type it represents
     * is registered.  If the handler is unregistered, the various methods
     * may throw exceptions or have otherwise undefined behaviors.
     */
    static class WonderlandClientSenderImpl 
            implements WonderlandClientSender, Serializable
    {
        /** the client type */
        private HandlerType type;
        
        /** the set of sessions */
        private ManagedReference<ClientSessionSet> sessionsRef;
        
        /** the underlying channel to send to all sessions */
        private ManagedReference<Channel> channelRef;
        
        /** the client ID for this client */
        private short clientID;
        
        /** 
         * Create a new WonderlandClientChannelImpl
         * @param type the client type
         * @param clientID the client ID to send with
         * @param sessions the set of sessions associated with this sender
         * @param channel the channel to wrap
         */
        public WonderlandClientSenderImpl(HandlerType type, short clientID,
                                          ClientSessionSet sessions,
                                          Channel channel) 
        {
            this.type     = type;
            this.clientID = clientID;
        
            // create references
            DataManager dm = AppContext.getDataManager();
            sessionsRef    = dm.createReference(sessions);
            channelRef     = dm.createReference(channel);
        }
        
        public HandlerType getClientType() {
            return type;
        }

        public Set<ClientSession> getSessions() { 
            Set<ClientSession> out = new HashSet<ClientSession>();
            
            for (ManagedReference<ClientSession> ref : sessionsRef.get()) {
                out.add(ref.get());
            }
            
            return out;
        }

        public boolean hasSessions() {
            return !(sessionsRef.get().isEmpty());
        }

        public void send(Message message) {
            send(channelRef.get(), message);
        }

        public void send(ClientSession session, Message message) {
            session.send(serializeMessage(message, clientID));
        }

        public void send(Set<ClientSession> sessions, Message message) 
        {
            // send to each individual session
            for (ClientSession session : sessions) {
                send(session, message);
            }
        }

        public void send(Channel channel, Message message) {
            channel.send(serializeMessage(message, clientID));
        }
        
        /**
         * Get the clientID for this sender
         * @return the client ID
         */
        private short getClientID() {
            return clientID;
        }
        
        /**
         * Add a new session to this sender.  This affects the global state
         * of all senders of this type.
         * @param session the session to add 
         */
        private void addSession(ClientSession session) {
            channelRef.get().join(session);
           
            DataManager dm = AppContext.getDataManager();
            sessionsRef.get().add(dm.createReference(session));
        }
        
        /**
         * Remove a session from this sender.  This affects the global state
         * of all senders of this type.
         * @param session the session to remove 
         */
        private void removeSession(ClientSession session) {
            channelRef.get().leave(session);
            
            DataManager dm = AppContext.getDataManager();
            sessionsRef.get().remove(dm.createReference(session));
        }
    }
      
    /**
     * Handle internal messages from the WonderlandSession object
     */
    static class SessionInternalHandler implements ClientHandler, Serializable {
        /** the listener to call back to */
        private WonderlandSessionListener listener;
        
        /**
         * Set the session listener
         * @param listener the session listener
         */
        public void setListener(WonderlandSessionListener listener) {
            this.listener = listener;
        }
        
        public HandlerType getClientType() {
            return SessionInternalHandlerType.SESSION_INTERNAL_CLIENT_TYPE;
        }

        public void registered(WonderlandClientSender sender) {
            // ignore
        }
        
        public void clientAttached(WonderlandClientSender sender,
                                   ClientSession session) 
        {
            // ignore
        }
        
        public void clientDetached(WonderlandClientSender sender,
                                   ClientSession session) 
        {
            // ignore
        }

        public void messageReceived(WonderlandClientSender sender,
                                    ClientSession session, 
                                    Message message)
        {
            if (message instanceof AttachClientMessage) {
                AttachClientMessage acm = (AttachClientMessage) message;
                listener.handleAttach(acm.getMessageID(), acm.getClientType());
            } else if (message instanceof DetachClientMessage) {
                DetachClientMessage dcm = (DetachClientMessage) message;
                listener.handleDetach(dcm.getClientID());
            }
        }
    }
    
    /**
     * Store all registered handlers, mapped by HandlerType
     */
    static class HandlerStore implements ManagedObject, Serializable {
        /** the key in the data store */
        private static final String DS_KEY = HandlerStore.class.getName();
        
        /** the handlers, mapped by HandlerType */
        private Map<HandlerType, HandlerRecord> handlers = 
                new HashMap<HandlerType, HandlerRecord>();
        
        /** The next client ID to assign */
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
            
            // figure out the client ID to assign to this handler
            short assignID = this.clientID++;
            if (handler instanceof SessionInternalHandler) {
                // special case -- force ID of internal handler
                assignID = SessionInternalHandlerType.SESSION_INTERNAL_CLIENT_ID;
            }
            
            // create a ClientSessionSet and channel
            DataManager dm = AppContext.getDataManager();
            ChannelManager cm = AppContext.getChannelManager();
            
            ClientSessionSet sessions = new ClientSessionSet();
            Channel channel = cm.createChannel(Delivery.RELIABLE);
                    
            // add to the map
            HandlerRecord record = new HandlerRecord();
            record.ref = ref;
            record.channel  = dm.createReference(channel);
            record.sessions = dm.createReference(sessions);
            record.clientID = assignID;
            handlers.put(handler.getClientType(), record);
            
            return record.clientID;
        }
        
        /**
         * Unregister a handler
         * @param handler the handler to unregister
         */
        public void unregister(ClientHandler handler) {
            HandlerRecord record = handlers.remove(handler.getClientType());
  
            // remove the channel and session store
            DataManager dm = AppContext.getDataManager();
            dm.removeObject(record.channel.get());
            dm.removeObject(record.sessions.get());
            
            // clear the reference, which will remove a managed object
            // handler from the data store
            if (record != null) {
                record.ref.clear();
            }
        }
        
        /**
         * Get all registered handlers
         * @return the set of all registered handlers
         */
        public Set<ClientHandler> getHandlers() {
            Set<ClientHandler> out = new HashSet<ClientHandler>(handlers.size());
            
            synchronized (handlers) {
                for (HandlerRecord record : handlers.values()) {
                    out.add(record.ref.get());
                }
            }
            
            return out;
        }
        
        /**
         * Get a handler for the given client type
         * @param type the client type to look up a handler for
         * @return a handler for the given type, or null if none is
         * registered
         */
        public ClientHandlerRef getHandlerRef(HandlerType type) {
            HandlerRecord record = handlers.get(type);
            if (record == null) {
                return null;
            }
            
            return record.ref;
        }
        
        /**
         * Get the sender for the given client type
         * @param type the client type to get a sender for
         * @return a sender for the given type, or null if the type is
         * not registered
         */
        public WonderlandClientSenderImpl getSender(HandlerType type) {
            HandlerRecord record = handlers.get(type);
            if (record == null) {
                return null;
            }
            
            return new WonderlandClientSenderImpl(type, record.clientID,
                                record.sessions.get(), record.channel.get());
        }
        
        // a handler reference and its associated channel
        class HandlerRecord implements Serializable {
            ClientHandlerRef ref;
            ManagedReference<Channel> channel;
            ManagedReference<ClientSessionSet> sessions;
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
        private ManagedReference<ClientHandler> ref;
        
        public ManagedClientHandlerRef(ClientHandler handler) {
            super (null);
            
            DataManager dm = AppContext.getDataManager();
            ref = dm.createReference(handler);
        }
        
        @Override
        public ClientHandler get() {
            return ref.get();
        }
        
        @Override
        public void clear() {
            ClientHandler handler = get();
            AppContext.getDataManager().removeObject((ManagedObject) handler);
        }
    }
    
    /**
     * A Set of client sessions
     */
    static class ClientSessionSet extends HashSet<ManagedReference<ClientSession>> 
            implements ManagedObject
    {
    }
}
