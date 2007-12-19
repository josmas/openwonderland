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
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ExtractMessageException;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.server.comms.ServerMessageListener;

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
    private static final String ALL_CLIENT_CHANNEL = "Wonderland.ALL_CLIENT";
    
    /** the session associated with this listener */
    private ClientSession session;
    
    /**
     * Create a new instance of WonderlandSessionListener for the given
     * session
     * @param session the session connected to this listener
     */
    public WonderlandSessionListener(ClientSession session) {
        this.session = session;
    
        // join to relevant channels
        joinChannels(session);
    }
        
    /**
     * Join the client to the default channels on login
     * @param session the session to join to channels
     */
    protected void joinChannels(ClientSession session) {
        ChannelManager cm = AppContext.getChannelManager();
        
        // the all-clients channel
        Channel channel = cm.getChannel(ALL_CLIENT_CHANNEL);
        channel.join(session, null);
    }
    
    /**
     * Initialize the session listener
     */
    public static void initialize() {
        // create all-users channel
        ChannelManager cm = AppContext.getChannelManager();
        cm.createChannel(ALL_CLIENT_CHANNEL, null, Delivery.RELIABLE);
    }
    
    /**
     * Send a message to all clients.  This will send a message to all
     * clients that are connected via the WonderlandSessionListener.
     * @param message the message to send
     */
    public static void sendToAllClients(Message message) {
        ChannelManager cm = AppContext.getChannelManager();
        Channel channel = cm.getChannel(ALL_CLIENT_CHANNEL);
        channel.send(message.getBytes());
    }
    
    /**
     * Register a listener.  This listener will listen for messages
     * on all sessions.  The listener will be stored in the Darkstar
     * data store, so it must be either Serializable or a ManagedObject.
     * @param messageClass the class of message to list for
     * @param listener the listener
     */
    public static void registerListener(Class<? extends Message> messageClass,
                                        ServerMessageListener listener)
    {
        ListenerSupport.addListener(messageClass, listener);
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
            ListenerSupport.fireMessage(m, session);
            
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
     * Manage listeners for the server.  
     * TODO: a more efficient version of this class.
     */
    static class ListenerSupport {
        private static List<ListenerRecord> listeners =
                new ArrayList<ListenerRecord>();
            
        /**
         * Add a listener
         * @param messageClass the class of message to listen for
         */
        static void addListener(Class<? extends Message> messageClass, 
                                ServerMessageListener listener) 
        {
            ListenerRecord lr;
            
            if (listener instanceof ManagedObject) {
                lr = new ManagedListenerRecord(messageClass, 
                                               (ManagedObject) listener);
            } else {
                lr = new ListenerRecord(messageClass, listener);
            }
            
            listeners.add(lr);
        }
        
        /**
         * Send a message to all listeners.  TODO: make this efficient.
         * @param message the message
         * @param session the session
         */
        static void fireMessage(Message message, ClientSession session) {
            for (ListenerRecord lr : listeners) {
                if (lr.getMessageClass().isAssignableFrom(message.getClass())) {
                    lr.getListener().messageReceived(message, session);
                }
            }
        }
    }
    
    /**
     * A listener record, including the class to listen for
     */
    static class ListenerRecord implements Serializable {
        // the class of message to listen for
        private Class<? extends Message> messageClass;
        
        // the listener
        private ServerMessageListener listener;
        
        public ListenerRecord(Class<? extends Message> messageClass, 
                              ServerMessageListener listener) 
        {
            this.messageClass = messageClass;
            this.listener = listener;
        }
        
        public Class<? extends Message> getMessageClass() {
            return messageClass;
        }
        
        public ServerMessageListener getListener() {
            return listener;
        }
    }
    
    /**
     * A listener record for a listener which is a managed object
     */
    static class ManagedListenerRecord extends ListenerRecord {
        // a reference to the listener record
        private ManagedReference listenerRef;
        
        public ManagedListenerRecord(Class<? extends Message> messageClass,
                                     ManagedObject listener)
        {
            super (messageClass, null);
            this.listenerRef = AppContext.getDataManager().createReference(listener);
        }
        
        @Override
        public ServerMessageListener getListener() {
            return listenerRef.get(ServerMessageListener.class);
        }
    }
}
