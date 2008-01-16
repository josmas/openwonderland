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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.WonderlandChannelNames;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ExtractMessageException;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.server.comms.ClientConnectionListener;
import org.jdesktop.wonderland.server.comms.ClientMessageListener;

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
    
    /** 
     * A single channel that all clients are connected to. This is a private
     * mechanism that is internal to this listener, and may be extended in
     * the future to handle lots of connected clients.
     */
    private static final String ALL_CLIENTS_CHANNEL = 
            WonderlandChannelNames.WONDERLAND_PREFIX + ".ALL_CLIENTS";
    
    /** the session associated with this listener */
    private ClientSession session;
    
    /** the list of message listeners */
    private List<MessageListenerRecord> messageListeners;
    
    /** the list of connection listeners */
    private List<ClientConnectionListener> connectionListeners;
    
    /**
     * Create a new instance of WonderlandSessionListener for the given
     * session
     * @param session the session connected to this listener
     */
    public WonderlandSessionListener(ClientSession session) {
        this.session = session;
    
        // set up message & connection listeners
        copyListeners();
        
        // join to relevant channels
        joinChannels(session);
        
        // notify connection listeners
        for (ClientConnectionListener listener : connectionListeners) {
            listener.connected(session);
        }
    }
        
    /**
     * Join the client to the default channels on login
     * @param session the session to join to channels
     */
    protected void joinChannels(ClientSession session) {
        ChannelManager cm = AppContext.getChannelManager();
        
        // the all-clients channel
        Channel channel = cm.getChannel(ALL_CLIENTS_CHANNEL);
        channel.join(session, null);
    }
    
    /**
     * Initialize the session listener
     */
    public static void initialize() {
        ChannelManager cm = AppContext.getChannelManager();
        DataManager dm = AppContext.getDataManager();
        
        // create all-users channel
        cm.createChannel(ALL_CLIENTS_CHANNEL, null, Delivery.RELIABLE);
    
        // create listener support
        dm.setBinding(ListenerSupport.BINDING_NAME, new ListenerSupport());
    }
    
    /**
     * Send a message to all clients.  This will send a message to all
     * clients that are connected via the WonderlandSessionListener.
     * @param message the message to send
     */
    public static void sendToAllClients(Message message) {
        ChannelManager cm = AppContext.getChannelManager();
        Channel channel = cm.getChannel(ALL_CLIENTS_CHANNEL);
        channel.send(message.getBytes());
    }
    
    /**
     * Register a message listener.  This listener will listen for messages
     * on all sessions.  The listener will be stored in the Darkstar
     * data store, so it must be either Serializable or a ManagedObject.
     * @param messageClass the class of message to list for
     * @param listener the listener
     */
    public static void registerMessageListener(
            Class<? extends Message> messageClass, 
            ClientMessageListener listener)
    {
        DataManager dm = AppContext.getDataManager();
        ListenerSupport ls = dm.getBinding(ListenerSupport.BINDING_NAME, 
                                           ListenerSupport.class);
        ls.addMessageListener(messageClass, listener);
    }
    
    /**
     * Register a connection listener.  This listener will listen for clients
     * connecting to and disconnecting from the Wonderland server.
     * The listener will be stored in the Darkstar data store, so it must be
     * either Serializable or a ManagedObject.
     * @param listener the listener
     */
    public static void registerConnectionListener(ClientConnectionListener listener)
    {
        DataManager dm = AppContext.getDataManager();
        ListenerSupport ls = dm.getBinding(ListenerSupport.BINDING_NAME, 
                                           ListenerSupport.class);
        ls.addConnectionListener(listener);
    }
    
    /**
     * Called when the listener receives a message.  If the wrapped session
     * has not yet been defined, look for ProtocolSelectionMessages, otherwise
     * simply forward the data to the delegate session
     * @param data the message data
     */
    public void receivedMessage(byte[] data) {
        try {
            // extract the message
            Message m = Message.extract(data);
            
            // send to listeners
            for (MessageListenerRecord record : messageListeners) {
                if (record.getMessageClass().isAssignableFrom(m.getClass())) {
                    record.getListener().messageReceived(m, session);
                }
            }
            
        } catch (ExtractMessageException eme) {
            logger.log(Level.WARNING, "Error extracting message from client", 
                       eme);
            
            // if possible, send a reply to the client
            if (eme.getMessageID() != null) {
                sendError(eme.getMessageID(), eme);
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
        // notify connection listeners
        for (ClientConnectionListener listener : connectionListeners) {
            listener.disconnected(session);
        }
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
    protected void sendError(MessageID messageID, String error) {
        sendError(messageID, error, null);
    }
    
    /**
     * Send an error to the session
     * @param cause the cause of the error
     * @param error the error to send
     */
    protected void sendError(MessageID messageID, Throwable cause) {
        sendError(messageID, null, cause);
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
        ErrorMessage msg = new ErrorMessage(messageID, error, cause);
        getSession().send(msg.getBytes());
    }
    
    /**
     * Copy the list of registered message listeners from the listener
     * manager.
     * @return a copy of the list of message listeners
     */
    private void copyListeners() {
        DataManager dm = AppContext.getDataManager();
        ListenerSupport ls = dm.getBinding(ListenerSupport.BINDING_NAME, 
                                           ListenerSupport.class);
        
        // copy message listeners
        messageListeners = new ArrayList<MessageListenerRecord>(ls.getMessageListeners());
    
        // copy connection listeners
        connectionListeners = new ArrayList<ClientConnectionListener>(ls.getConnectionListeners());
    }
    
    /**
     * Manage listeners for the server.  This managed object stores a 
     * prototype list of listeners.  The list is *copied* into any
     * new SessionListeners that are created.  This means that this
     * object is only used when a new listener is created, not on every
     * message.  It also means that the list of listeners is not dynamic --
     * a given session listener will have exactly the set of listeners that
     * was present when it connected.  For this reason, listeners should
     * be registered during initialization.
     */
    static class ListenerSupport implements ManagedObject, Serializable {
        static final String BINDING_NAME = ListenerSupport.class.getName();
        
        /** message listeners */
        private List<MessageListenerRecord> messageListeners =
                new ArrayList<MessageListenerRecord>();
           
        /** session listeners */
        private List<ClientConnectionListener> connectionListeners =
                new ArrayList<ClientConnectionListener>();
        
        /**
         * Add a message listener
         * @param messageClass the class of message to listen for
         * @param listener the listener to add
         */
        public void addMessageListener(Class<? extends Message> messageClass, 
                                       ClientMessageListener listener) 
        {
            MessageListenerRecord lr;
            
            if (listener instanceof ManagedObject) {
                lr = new ManagedMessageListenerRecord(messageClass, 
                                                      (ManagedObject) listener);
            } else {
                lr = new MessageListenerRecord(messageClass, listener);
            }
            
            messageListeners.add(lr);
        }
        
        /**
         * Add a connection listener
         * @param listener the listener to add
         */
        public void addConnectionListener(ClientConnectionListener listener) {
            if (listener instanceof ManagedObject) {
                listener = 
                        new ManagedConnectionListener((ManagedObject) listener);
            } 
            
            connectionListeners.add(listener);
        }
        
        /**
         * Get the message listeners
         * @return the list of listeners
         */
        public List<MessageListenerRecord> getMessageListeners() {
            return messageListeners;
        }
        
        /**
         * Get the connection listeners
         * @return the list of listeners
         */
        public List<ClientConnectionListener> getConnectionListeners() {
            return connectionListeners;
        }
    }
    
    /**
     * A listener record, including the class to listen for
     */
    static class MessageListenerRecord implements Serializable {
        // the class of message to listen for
        private Class<? extends Message> messageClass;
        
        // the listener
        private ClientMessageListener listener;
        
        public MessageListenerRecord(Class<? extends Message> messageClass, 
                                     ClientMessageListener listener) 
        {
            this.messageClass = messageClass;
            this.listener = listener;
        }
        
        public Class<? extends Message> getMessageClass() {
            return messageClass;
        }
        
        public ClientMessageListener getListener() {
            return listener;
        }
    }
    
    /**
     * A message listener record for a listener which is a managed object
     */
    static class ManagedMessageListenerRecord extends MessageListenerRecord {
        // a reference to the listener record
        private ManagedReference listenerRef;
        
        public ManagedMessageListenerRecord(Class<? extends Message> messageClass,
                                            ManagedObject listener)
        {
            super (messageClass, null);
            this.listenerRef = AppContext.getDataManager().createReference(listener);
        }
        
        @Override
        public ClientMessageListener getListener() {
            return listenerRef.get(ClientMessageListener.class);
        }
    }
    
    /**
     * A session listener for a session which is a managed object
     */
    static class ManagedConnectionListener 
            implements ClientConnectionListener, Serializable
    {
        // a reference to the listener record
        private ManagedReference listenerRef;
        
        public ManagedConnectionListener(ManagedObject listener) {
            this.listenerRef = AppContext.getDataManager().createReference(listener);
        }

        public void connected(ClientSession session) {
            listenerRef.get(ClientConnectionListener.class).connected(session);
        }

        public void disconnected(ClientSession session) {
            listenerRef.get(ClientConnectionListener.class).disconnected(session);
        }
    }
}
