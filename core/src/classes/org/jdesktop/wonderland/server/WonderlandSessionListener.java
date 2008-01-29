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
import org.jdesktop.wonderland.server.comms.ClientSender;

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
            
            // handle the message
            ClientSender sender = new ClientSenderImpl(clientID.shortValue());
            handler.messageReceived(sender, m);
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
        logger.fine("Session " + getSession().getName() + " disconnected");
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
    }
    
    /**
     * Get all clients connected via the given WonderlandClient
     * @see org.jdesktop.wonderland.server.comms.CommsManager#getClients(ClientType)
     * 
     * @param clientType the type of client to get sessions for
     * @return a set of all client sessions connected via a client of the given
     * type, or an empty set if no clients are connected via the given type
     */
    public static Set<ClientSession> getClients(ClientType clientType) {
        Channel channel = getHandlerStore().getChannel(clientType);
        if (channel == null) {
            return Collections.emptySet();
        }
        
        return channel.getSessions();
    }

    /**
     * Send a message to all clients connected via the given WonderlandClient
     * @see org.jdesktop.wonderland.server.comms.CommsManager#send(ClientType, Message)
     * 
     * @param clientType the type of client to send to
     * @param message the message to send
     * @throws IllegalStateException if the given client type is not registered
     */
    public static void send(ClientType clientType, Message message) {
        Channel channel = getHandlerStore().getChannel(clientType);
        if (channel == null) {
            throw new IllegalStateException("No handler for client type: " +
                                            clientType);
        }
        
        channel.send(message.getBytes());
    }

    /**
     * Send a message to specific clients connected via the given
     * WonderlandClient
     * @see org.jdesktop.wonderland.server.comms.CommsManager#send(ClientType, Set<ClientSession>, Message)
     * 
     * @param clientType the type of client to send to
     * @param sessions the session to send to
     * @param message the message to send
     * @throws IllegalStateException if the given client type is not registered
     */
    public static void send(ClientType clientType, Set<ClientSession> sessions, 
                            Message message) 
    {
        Channel channel = getHandlerStore().getChannel(clientType);
        if (channel == null) {
            throw new IllegalStateException("No handler for client type: " +
                                            clientType);
        }
        
        channel.send(sessions, message.getBytes());
    }

     /**
     * Send a message to a specific clients connected via the given
     * WonderlandClient
     * @see org.jdesktop.wonderland.server.comms.CommsManager#send(ClientType, Set<ClientSession>, Message)
     * 
     * @param clientType the type of client to send to
     * @param session the session to send to
     * @param message the message to send
     * @throws IllegalStateException if the given client type is not registered
     */
    public static void send(ClientType clientType, ClientSession session, 
                            Message message) 
    {
        Channel channel = getHandlerStore().getChannel(clientType);
        if (channel == null) {
            throw new IllegalStateException("No handler for client type: " +
                                            clientType);
        }
        
        channel.send(session, message.getBytes());
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
        ClientSender sender = new ClientSenderImpl(clientID);
        ref.get().clientAttached(sender);
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
        
        // notify the handler
        handler.clientDetached(session);
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
        
        byte[] messageData = message.getBytes();
        int len = messageData.length + 2;
        
        // pre-pend the client id
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
        DataOutputStream out = new DataOutputStream(baos);
        
        try {
            out.writeShort(clientID);
            out.write(messageData);
            out.close();
            
            getSession().send(baos.toByteArray());
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Unable to write message " + message + 
                       " to " + clientID, ioe);
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
     * Implementation of ClientSender
     */
    class ClientSenderImpl implements ClientSender {
        private short clientID;
        
        ClientSenderImpl(short clientID) {
            this.clientID = clientID;
        }

        public void send(Message message) {
            sendToSession(clientID, message);
        }

        public void sendError(MessageID messageID, String error) {
            WonderlandSessionListener.this.sendError(messageID, clientID, error);
        }

        public void sendError(MessageID messageID, Throwable cause) {
            WonderlandSessionListener.this.sendError(messageID, clientID, cause);
        }

        public void sendError(MessageID messageID, String error, Throwable cause) {
            WonderlandSessionListener.this.sendError(messageID, clientID, error, cause);
        }

        public ClientSession getSession() {
            return session;
        }
    }
    
    /**
     * Handle internal messages from the WonderlandSession object
     */
    class SessionInternalHandler implements ClientHandler, Serializable {

        public ClientType getClientType() {
            return SessionInternalClientType.SESSION_INTERNAL_CLIENT_TYPE;
        }

        public void clientAttached(ClientSender sender) {
            // ignore
        }
        
        public void clientDetached(ClientSession session) {
            // ignore
        }

        public void messageReceived(ClientSender sender, Message message) {
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
         */
        public void register(ClientHandler handler) {
            
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
        }
        
        /**
         * Unregister a handler
         * @param handler the handler to unregister
         */
        public void unregister(ClientHandler handler) {
            handlers.remove(handler.getClientType());
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
    }
}
