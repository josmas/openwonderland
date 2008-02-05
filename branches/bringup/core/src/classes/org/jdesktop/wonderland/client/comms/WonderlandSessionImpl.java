/**
 * Project Wonderland
 *
 * $RCSfile: LogControl.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2007/10/23 18:27:41 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.comms;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.SessionId;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.comms.ProtocolVersion;
import org.jdesktop.wonderland.common.comms.SessionInternalClientType;
import org.jdesktop.wonderland.common.comms.WonderlandChannelNames;
import org.jdesktop.wonderland.common.comms.WonderlandProtocolVersion;
import org.jdesktop.wonderland.common.comms.messages.AttachClientMessage;
import org.jdesktop.wonderland.common.comms.messages.AttachedClientMessage;
import org.jdesktop.wonderland.common.comms.messages.DetachClientMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ExtractMessageException;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageException;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ProtocolSelectionMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * This class provides an extensible base for clients that want to connect
 * to a Wonderland server.  The base client handles logging in to the
 * server, selecting a protocol using the Wonderland protocol selection
 * mechanism, sending data to the server, as well as a listener framework
 * for channel join/leave and messages from the server.
 * <p>
 * Extensions of this listener provide protocol-specific services.  The 
 * WonderlandClient is the main client used by the Wonderland 3D application.
 * 
 * @author kaplanj
 */
public class WonderlandSessionImpl implements WonderlandSession {
    /** logger */
    private static final Logger logger =
            Logger.getLogger(WonderlandSessionImpl.class.getName());
    
    /** the default client type, used for handling data over the session
        channel */
    private static final ClientType INTERNAL_CLIENT_TYPE = 
            SessionInternalClientType.SESSION_INTERNAL_CLIENT_TYPE;
    
    /** the prefix for client-specific channels */
    private static final String CLIENT_CHANNEL_PREFIX =
            WonderlandChannelNames.WONDERLAND_PREFIX + ".Client.";
    
    /** the current status */
    private Status status;
    
    /** the server to connect to */
    private WonderlandServerInfo server;
    
    /** the current login attempt */
    private LoginAttempt currentLogin;
    
    /** the connected client */
    private SimpleClient simpleClient;
   
    /** listeners to notify when we join a channel */
    private List<ChannelJoinedListener> channelJoinedListeners;
    
    /** attached clients */
    private Map<ClientType, ClientRecord> clients;
    private Map<Short, ClientRecord> clientsByID;
    
    /**
     * Create a new client to log in to the given server
     * @param server the server to connect to
     */
    public WonderlandSessionImpl(WonderlandServerInfo server) {
        this.server = server;
        
        // initial status
        status = Status.DISCONNECTED;
       
        // initialize listeners
        channelJoinedListeners = 
                new CopyOnWriteArrayList<ChannelJoinedListener>();
    
        // initialize list of clients
        clients = Collections.synchronizedMap(
                new HashMap<ClientType, ClientRecord>());
        clientsByID = Collections.synchronizedMap(
                new HashMap<Short, ClientRecord>());
        
        // add the internal client, which handles traffic over the session
        // channel
        SessionInternalClient internal = new SessionInternalClient();
        ClientRecord internalRecord = addClientRecord(internal);
        setClientID(internalRecord, 
                    SessionInternalClientType.SESSION_INTERNAL_CLIENT_ID);
        
        // the internal client is always attached
        internal.attached(this);
        
        // add the internal listener, which handles joining the client-specific
        // channels
        ChannelJoinedListener cjl = new SessionInternalChannelJoinedListener();
        addChannelJoinedListener(cjl);
    }
    
    public WonderlandServerInfo getServerInfo() {
        return server;
    }
    
    public synchronized Status getStatus() {
        return status;
    }
   
    /**
     * Set the status of this client.
     * @param status the current status of the client
     */
    protected void setStatus(Status status) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(getName() + " set status " + status);
        }
        
        boolean changed = false;
        
        synchronized (this) {
            // see if the status changed
            if (this.status != status) {
                changed = true;
                this.status = status;
            }
        }
        
        // notify listeners
        if (changed) {
            WonderlandSessionFactory.fireClientStatusChanged(this, status);
        }
    }
    
    public SimpleClient getSimpleClient() {
        return simpleClient;
    }

    public void login(LoginParameters loginParams) 
        throws LoginFailureException
    {
        logger.fine(getName() + " start login attempt for " +
                    loginParams.getUserName());
        
        // make sure there is no login in progress
        synchronized (this) {
            if (status != Status.DISCONNECTED) {
                throw new LoginFailureException("Login already in progress");
            }
            
            startLogin(loginParams);
        }
        
        simpleClient = new SimpleClient(new WonderlandClientListener());
        
        Properties connectProperties = new Properties();
        connectProperties.setProperty("host", server.getHostname());
        connectProperties.setProperty("port", Integer.toString(server.getSgsPort()));
        
        try {
            simpleClient.login(connectProperties);
        
            // wait for the login
            LoginResult result = getCurrentLogin().waitForLogin();
            if (!result.success) {
                throw result.exception;
            }
        } catch (IOException ioe) {
            throw new LoginFailureException(ioe);
        } catch (InterruptedException ie) {
            throw new LoginFailureException(ie);
        }
        
        logger.fine(getName() + " login succeeded as " + 
                    loginParams.getUserName());
    }
    
    public void disconnect() {
        getSimpleClient().logout(true);
    }
    
    public void attach(final WonderlandClient client) 
            throws AttachFailureException 
    {
        logger.fine(getName() + " attach client " + client);
        
        // check our status to make sure we are connected
        if (getStatus() != Status.CONNECTED) {
            throw new AttachFailureException("Session not connected");
        }
        
        final ClientRecord record;
        
        // check if there is already a client registered (or registering) for
        // this client type
        synchronized (clients) {
            if (getClientRecord(client.getClientType()) != null) {
                throw new AttachFailureException("Duplicate attach for " +
                        "client type " + client.getClientType());
            }
            
            // Add a client record.  Adding a client record at this early
            // guarantees that there will only be one registration for the
            // given type in progress at any time. If the attach fails
            // for any reason, we have to make sure to clean this record
            // up before we exit
            record = addClientRecord(client);
        }
        
        // send a request to the server to attach the given client type
        Message attachMessage = new AttachClientMessage(client.getClientType());
        
        // Create a listener to handle the response.  We cannot do this
        // using just sendAndWait() because the response has to be
        // processed immediately, otherwise messages received immediately
        // after the response to this message will not be handled
        // properly.
        AttachResponseListener listener = new AttachResponseListener(record);
        
        // whether or not the attach attempt succeeded
        boolean success = false;
        
        try {
            getInternalClient().send(attachMessage, listener);
            listener.waitForResponse();
            
            // check for success -- if we didn't succeed for any reason,
            // throw an exception
            success = listener.isSuccess();
            if (!success) {
                // throw the relevant exception
                throw listener.getException();
            }
                
            logger.fine(getName() + " attached succeeded for " + client);

        } catch (InterruptedException ie) {
            throw new AttachFailureException("Interrupted", ie);
        } finally {
            // clean up the client record if the attach failed
            if (!success) {
                removeClientRecord(client);
            }
        }
    }

    public WonderlandClient getClient(ClientType type) {
        return getClient(type, WonderlandClient.class);
    }
    
    public <T extends WonderlandClient> T getClient(ClientType type, 
                                                    Class<T> clazz) 
    {
        ClientRecord record = getClientRecord(type);
        if (record == null) {
            return null;
        }
        
        return clazz.cast(record.getClient());
    }

    public Collection<WonderlandClient> getClients() {
        List<WonderlandClient> out = 
                new ArrayList<WonderlandClient>(clients.size());
        
        synchronized (clients) {
            for (ClientRecord record : clients.values()) {
                out.add(record.getClient());
            }
        }
        
        return out;
    }

    public void detach(WonderlandClient client) {
        logger.fine(getName() + " detach " + client);
        
        // get the client record
        ClientRecord record = getClientRecord(client);
        if (record == null) {
            // ignore
            logger.warning(getName() + " trying to detach a client which is " +
                           " not attached: " + client);
            return;
        }
        
        // send a message
        getInternalClient().send(
             new DetachClientMessage(record.getClientID()));
    
        // update the client
        client.detached();
    }
    
    public void send(WonderlandClient client, Message message) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(getName() + " sending message " + message + 
                          " to client " + client);
        }
        
        // only the default client may send when the state is not
        // CONNECTED
        if (client != getInternalClient() && getStatus() != Status.CONNECTED) {
            throw new IllegalStateException("Session not connected");
        }
        
        // make sure the client is attached before we send
        if (client.getStatus() != WonderlandClient.Status.ATTACHED) {
            throw new IllegalStateException("Client not attached");
        }
        
        // get the record for the given client
        ClientRecord record = getClientRecord(client);
        if (record == null) {
            throw new IllegalStateException(
                    "Client " + client.getClientType() + " not attached.");
        }
        
        // send the message to the server
        try {
            // prepend the client id onto the message
            byte[] messageBytes = message.getBytes();
            int len = messageBytes.length + 2;
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
            DataOutputStream out = new DataOutputStream(baos);
            
            out.writeShort(record.getClientID());
            out.write(messageBytes);
            out.close();
            
            // send the combined message
            simpleClient.send(baos.toByteArray());
        } catch (IOException ioe) {
            throw new MessageException(ioe);
        }
    }
   
    public void addChannelJoinedListener(ChannelJoinedListener listener) {
        channelJoinedListeners.add(listener);
    }
    
    public void removeChannelJoinedListener(ChannelJoinedListener listener) {
        channelJoinedListeners.remove(listener);
    }
    
    /**
     * Fire when a new channel is joined
     * @param channel the channel that was joined
     */
    protected ClientChannelListener fireChannelJoined(ClientChannel channel) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(getName() + " join channel " + channel.getName());
        }
        
        // go through the list of chanel joined listeners
        // and find the first one to respond
        for (ChannelJoinedListener listener : channelJoinedListeners) {
            ClientChannelListener ccl = listener.joinedChannel(this, channel);
            if (ccl != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Listener for channel " + channel.getName() + 
                                  " is " + ccl);
                }
                
                return ccl;
            }
        }
        
        // no listener found
        logger.warning(getName() + "no channel listener for " + 
                       channel.getName());
        return null;
    }
    
    /**
     * Fire when a message is received over the session channel
     * @param data the message that was received
     */
    protected void fireSessionMessageReceived(byte[] data) { 
        // get the client ID this is addressed to
        short clientID;
        
        try {
            DataInputStream in = new DataInputStream(
                    new ByteArrayInputStream(data));
            clientID = in.readShort();
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to read clientID from data!");
        }
        
        // handle it with the selected client
        ClientRecord record = getClientRecord(clientID);
        if (record == null) {
            throw new IllegalStateException("Message to unknown client: " +
                                            clientID);
        }
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(getName() + " received session message for handler " + 
                          record.getClient().getClientType());
        }
        
        record.receivedMessage(data, 2, data.length - 2);
    }
      
    /**
     * Get the name of the protocol to connect with
     * @return the name of the protocol to connect with
     */
    protected String getProtocolName() {
        return WonderlandProtocolVersion.PROTOCOL_NAME;
    }
    
    /**
     * Get the version of the protocol to connect with
     * @return the version of the protocol to connect with
     */
    protected ProtocolVersion getProtocolVersion() {
        return WonderlandProtocolVersion.VERSION;
    }
    
    /**
     * Get the default client for handling traffic over the session channel
     * @return the default client
     */
    protected SessionInternalClient getInternalClient() {
        return (SessionInternalClient) getClient(INTERNAL_CLIENT_TYPE);
    }
    
    /**
     * Add a client record to the map
     * @param client the client to add a record for
     * @return the newly added record
     */
    protected ClientRecord addClientRecord(WonderlandClient client) {
        logger.fine(getName() + " adding record for client " + client);
        ClientRecord record = new ClientRecord(client);
        clients.put(client.getClientType(), record);
        return record;
    }
    
    /**
     * Set the client id of a given client
     * @param record the record to set the id for
     * @param clientID the id to set
     */
    protected void setClientID(ClientRecord record, short clientID) {
        logger.fine(getName() + " setting client ID for " + record.toString() +
                    " " + clientID);
        
        synchronized (clients) {
            record.setClientID(clientID);
            clientsByID.put(Short.valueOf(clientID), record);
        }
    }
    
    /**
     * Get the client record for a given client
     * @param client the client to get a record for
     * @return the ClientRecord for the given client, or null if the given
     * client is not attached to this session
     */
    protected ClientRecord getClientRecord(WonderlandClient client) {
        ClientRecord record = clients.get(client.getClientType());
        
        // If the record exists, also make sure it matches the current client.
        // If a different client is registered with the given client type, 
        // return null as well.
        if (record == null || record.getClient() != client) {
            return null;
        }
        
        return record;
    }
    
    /**
     * Get the client record for a given client
     * @param type the type of client to get a record for
     * @return the ClientRecord for the given client, or null if the given
     * client is not attached to this session
     */
    protected ClientRecord getClientRecord(ClientType type) {
        return clients.get(type);
    }
    
    /**
     * Get the client record with the given id
     * @param clientID the client to get a record for
     * @return the ClientRecord for the given client, or null if the given
     * client is not attached to this session
     */
    protected ClientRecord getClientRecord(short clientID) {
        return clientsByID.get(Short.valueOf(clientID));
    }
    
    /**
     * Remove a client record
     * @param client the client to remove a record for
     */
    protected void removeClientRecord(WonderlandClient client) {
        logger.fine(getName() + "Removing record for client " + client);
        
        ClientRecord record = getClientRecord(client);
        if (record != null) {
            synchronized (clients) {
                clients.remove(client.getClientType());
                clientsByID.remove(Short.valueOf(record.getClientID()));
            }
        }
    }
    
    /**
     * Start a new login attempt.
     * @param params the login parameters to login with
     */
    private synchronized void startLogin(LoginParameters params) {
        setStatus(Status.CONNECTING);
        currentLogin = new LoginAttempt(params);
    }
    
    /**
     * Get the current login attempt
     * @return the current login attempt, or null if there is no
     * current attempt
     */
    private synchronized LoginAttempt getCurrentLogin() {
        return currentLogin;
    }
    
    /**
     * Finish the login.  This destroys the current login attempt, and
     * sets the status to the given value.
     * @param status the new status
     */
    private synchronized void finishLogin(Status status) {
        setStatus(status);
        currentLogin = null;
    }
    
 
    /**
     * Get a user-printable name for this session
     * @return a name for this session
     */
    protected String getName() {
        return "WonderlandSession{server: " + getServerInfo().getHostname() +
               ":" + getServerInfo().getSgsPort() + "}";
    }
    
    @Override
    public String toString() {
        return getName() + " status: " + getStatus();
    }
    
    /**
     * Wonderland client listener
     */
    class WonderlandClientListener implements SimpleClientListener {

        /**
         * {@inheritDoc}
         */
        public PasswordAuthentication getPasswordAuthentication() {
            // This is called to get the user name and authentication data (eg password)
            // to be authenticated server side.
            LoginParameters loginParams = getCurrentLogin().getLoginParameters();
            return new PasswordAuthentication(loginParams.getUserName(),
                                              loginParams.getPassword());
        }

        /**
         * {@inheritDoc}
         */
        public synchronized void loggedIn() {
            logger.fine(getName() + " logged in");
            getCurrentLogin().setLoginSuccess();
        }

        /**
         * {@inheritDoc}
         */
        public synchronized void loginFailed(String reason) {
            logger.fine(getName() + " login failed: " + reason);
            getCurrentLogin().setFailure(reason);
        }

        /**
         * {@inheritDoc}
         */
        public void disconnected(boolean graceful, String reason) {
            logger.fine(getName() + " disconnected");
            synchronized (this) {
                // are we in the process of logging in?
                if (getCurrentLogin() != null) {
                    getCurrentLogin().setFailure(reason);
                } else {
                    setStatus(Status.DISCONNECTED);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        public ClientChannelListener joinedChannel(ClientChannel channel) {
           return fireChannelJoined(channel);
        }

        /**
         * {@inheritDoc}
         */
        public void receivedMessage(byte[] data) {
            fireSessionMessageReceived(data);
        }

        /**
         * {@inheritDoc}
         */
        public void reconnecting() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /**
         * {@inheritDoc}
         */
        public void reconnected() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    /**
     * An attempt to log in
     */
    class LoginAttempt {
        // parameters to log in with
        private LoginParameters params;
        
        // whether the login is complete
        private boolean loginComplete;
        
        // whether the login succeeded
        private boolean loginSuccess;
        
        // the exception if the login failed
        private LoginFailureException loginException;
        
        /**
         * Create a new login attempt
         * @param params the login parameters
         */
        public LoginAttempt(LoginParameters params) {
            this.params = params;
            
            loginComplete = false;
            loginSuccess = true;
        }
        
        /**
         * Get the login parameters
         * @return the login parameters
         */
        public LoginParameters getLoginParameters() {
            return params;
        }
        
        /**
         * Set a successful result for the login phase.  This will initiate the 
         * protocol selection phase.
         */
        public synchronized void setLoginSuccess() {
           ProtocolSelectionMessage psm = 
                   new ProtocolSelectionMessage(getProtocolName(),
                                                getProtocolVersion());
           ResponseListener rl = new OKErrorResponseListener() {
                @Override
                public void onSuccess(MessageID messageID) {
                    setProtocolSuccess();
                }

                @Override
                public void onFailure(MessageID messageID, String message,
                                      Throwable cause) 
                {
                    setFailure(message, cause);
                }
            };
            
            // send the message using the default client
            getInternalClient().send(psm, rl);
        }
        
        /**
         * Set success in the protocol selection phase.
         */
        public synchronized void setProtocolSuccess() {
            loginComplete = true;
            loginSuccess = true;
            finishLogin(Status.CONNECTED);
            notifyAll();
        }
        
        /**
         * Set a failed result
         * @param reason the reason for failure
         */
        public synchronized void setFailure(String reason) {
            setFailure(reason, null);
        }
        
        /**
         * Set a failed result
         * @param reason the reason for failure
         * @param cause the underlying cause of the failure
         */
        public synchronized void setFailure(String reason, Throwable cause) {
            loginComplete = true;
            loginSuccess = false;
            loginException = new LoginFailureException(reason, cause);
            finishLogin(Status.DISCONNECTED);
            notifyAll();
        }
               
        /**
         * Get the result of logging in.  This method blocks until the
         * login and protocol selection succeeds or fails.
         * @return true if everything works, or false if not
         */
        public synchronized LoginResult waitForLogin() 
            throws InterruptedException
        {
            while (!loginComplete) {
                wait();
            }
            
            return new LoginResult(loginSuccess, loginException);
        }
    }
    
    /**
     * The result of a login attempt
     */
    class LoginResult {
        boolean success;
        LoginFailureException exception;
    
        public LoginResult(boolean success, LoginFailureException exception) {
            this.success = success;
            this.exception = exception;
        }
    }
    
    /**
     * The record for an attached client
     */
    protected class ClientRecord implements ClientChannelListener {
        /** the client that attached */
        private WonderlandClient client;
        
        /** the id of this client, as assigned by the server */
        private short clientID;
        
        public ClientRecord(WonderlandClient client) {
            this.client = client;
        }
        
        /**
         * Get the client associated with this record
         * @return the associated client
         */
        public WonderlandClient getClient() {
            return client;
        }
        
        /**
         * Get the clientID for this client as sent by the server.  When
         * the client attaches a given protocol, the server assigns an ID
         * that must be pre-pended to outgoing messages so the server
         * can determine which client they are intended for. 
         * @return the id of this client
         */
        protected synchronized short getClientID() {
            return clientID;
        }
         
        /**
         * Set the client ID associated with this record
         * @param clientID the client id to set
         */
        protected synchronized void setClientID(short clientID) {
            this.clientID = clientID;
        }
        
        /**
         * Called when we receieve a message on this client's channel.
         * This method just calls 
         * <code>receivedMessage(data, 0, data.length)</code>
         * @param channel the channel the data was received on
         * @param sessionId the id of the session this channel is associated
         * with
         * @param data the data of the message
         */
        public void receivedMessage(ClientChannel channel, SessionId sessionId, 
                                    byte[] data) 
        {
            receivedMessage(data, 0, data.length);
        }
        
        /**
         * Called when we receieve a message on this client's channel or
         * over the session channel.
         * Deserialize the message and pass it on to the client.  If the
         * message is a response to a message we sent earlier, notify
         * any pending response listeners
         * @param data the data of the message
         * @param offset the offset in the byte buffer of the start of the data
         * @param len the length of the data
         */
        public void receivedMessage(byte[] data, int offset, int len) {
            try {
                // extract the message and pass it off to the client
                handleMessage(Message.extract(data, offset, len));
            } catch (ExtractMessageException eme) {
                logger.log(Level.WARNING, "Error extracting message from server",
                        eme);

                // if possible, send a reply to the client
                if (eme.getMessageID() != null) {
                    // send a fake error message
                    handleMessage(new ErrorMessage(eme.getMessageID(),
                                                   eme.getMessage(),
                                                   eme.getCause()));
                }
            } catch (Exception ex) {
                // what to do?
                logger.log(Level.WARNING, "Error extracting message from server",
                        ex);
            }
        }

        /**
         * Handle a message
         * @param message the message to handle
         */
        protected void handleMessage(Message message) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(getName() + " client " + this + 
                              " received message " + message);
            }
            
            // send to the client
            getClient().messageReceived(message);
        }
        
        /**
         * Notification that we have left the channel associated with this
         * client.  Notify the client that it is now disconnected.
         * @param channel the channel that we left
         */
        public void leftChannel(ClientChannel channel) {
            logger.fine(getName() + " left channel " + channel.getName());
            
            getClient().detached();
            
            // remove the client record
            removeClientRecord(client);
        }
        
        @Override
        public String toString() {
            return getClient().toString();
        }
    }
    
    /**
     * Handle traffic over the session channel
     */
    protected static class SessionInternalClient extends BaseClient {
        public ClientType getClientType() {
            // only used internally
            return INTERNAL_CLIENT_TYPE;
        }

        public void handleMessage(Message message) {
            // unhandled session messages?
            logger.warning("Unhandled message: " + message);
        }   
    }
    
    /**
     * Handle joining internal channels
     */
    class SessionInternalChannelJoinedListener implements ChannelJoinedListener {

        /**
         * This listener looks for channels named Wonderland.Client.Type, where
         * Type is the client type of the channel.  When the channel is
         * found, this will return the ClientRecord, which acts as the
         * ClientChannelListener for the client
         * 
         * @param session the session that joined the channel
         * @param channel the channel that was joined
         * @return the ClientRecord for the given client, or null if
         * no client exists for the given type
         */
        public ClientChannelListener joinedChannel(WonderlandSession session, 
                                                   ClientChannel channel)
        {
            ClientChannelListener listener = null;
            
            // check the channel name to see if it is in the right form
            String channelName = channel.getName();
            
            if (channelName.startsWith(CLIENT_CHANNEL_PREFIX)) {
                String clientTypeName = 
                        channelName.substring(CLIENT_CHANNEL_PREFIX.length());
                ClientType clientType = new ClientType(clientTypeName);
                
                // look up the given client type
                listener = getClientRecord(clientType);
            }
            
            return listener;
        }
    }
    
    /**
     * Listen for responses to the attach() message.
     */
    class AttachResponseListener extends WaitResponseListener {
        /** the record to update on success */
        private ClientRecord record;
        
        /** whether or not we succeeded */
        private boolean success = false;
        
        /** the exception if we failed */
        private AttachFailureException exception;
        
        public AttachResponseListener(ClientRecord record) {
            this.record = record;
        }
        
        @Override
        public void responseReceived(ResponseMessage response) {
            if (response instanceof AttachedClientMessage) {
                AttachedClientMessage acm = (AttachedClientMessage) response;

                // set the client id
                setClientID(record, acm.getClientID());
                
                // notify the client that we are now attached
                record.getClient().attached(WonderlandSessionImpl.this);
                
                // success
                setSuccess(true);
            } else if (response instanceof ErrorMessage) {
                // error -- throw an exception
                ErrorMessage e = (ErrorMessage) response;
                setException(new AttachFailureException(e.getErrorMessage(),
                                                        e.getErrorCause()));
            } else {
                // bad situation
                setException(new AttachFailureException("Unexpected response " +
                                                        "type: " + response));
            }
            
            super.responseReceived(response);
        }
        
        public synchronized boolean isSuccess() {
            return success;
        }
        
        private synchronized void setSuccess(boolean success) {
            this.success = success;
        }
        
        public synchronized AttachFailureException getException() {
            return exception;
        }
        
        public synchronized void setException(AttachFailureException exception) {
            this.exception = exception;
        }
    }
}
