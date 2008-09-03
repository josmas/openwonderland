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
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
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
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.comms.ProtocolVersion;
import org.jdesktop.wonderland.common.comms.SessionInternalConnectionType;
import org.jdesktop.wonderland.common.comms.WonderlandProtocolVersion;
import org.jdesktop.wonderland.common.comms.messages.AttachClientMessage;
import org.jdesktop.wonderland.common.comms.messages.AttachedClientMessage;
import org.jdesktop.wonderland.common.comms.messages.DetachClientMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ExtractMessageException;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageException;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.MessagePacker;
import org.jdesktop.wonderland.common.messages.MessagePacker.PackerException;
import org.jdesktop.wonderland.common.messages.MessagePacker.ReceivedMessage;
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
 * ClientConnection is the main client used by the Wonderland 3D application.
 * 
 * @author kaplanj
 */
public class WonderlandSessionImpl implements WonderlandSession {
    /** logger */
    private static final Logger logger =
            Logger.getLogger(WonderlandSessionImpl.class.getName());
    
    /** the default client type, used for handling data over the session
        channel */
    private static final ConnectionType INTERNAL_CLIENT_TYPE = 
            SessionInternalConnectionType.SESSION_INTERNAL_CLIENT_TYPE;
    
    /** the current status */
    private Status status;
    
    /** the server to connect to */
    private WonderlandServerInfo server;
    
    /** the current login attempt */
    private LoginAttempt currentLogin;
    
    /** the connected client */
    private SimpleClient simpleClient;
   
    /** listeners to notify when our status changes */
    private List<SessionStatusListener> sessionStatusListeners;
 
    /** connected clients */
    private Map<ConnectionType, ClientRecord> clients;
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
        sessionStatusListeners =
                new CopyOnWriteArrayList<SessionStatusListener>();
      
        // initialize list of clients
        clients = Collections.synchronizedMap(
                new HashMap<ConnectionType, ClientRecord>());
        clientsByID = Collections.synchronizedMap(
                new HashMap<Short, ClientRecord>());
        
        // add the internal client, which handles traffic over the session
        // channel
        SessionInternalHandler internal = new SessionInternalHandler();
        ClientRecord internalRecord = addClientRecord(internal);
        setClientID(internalRecord, 
                    SessionInternalConnectionType.SESSION_INTERNAL_CLIENT_ID);
        
        // the internal client is always connected
        internal.connected(this);
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
            fireClientStatusChanged(status);
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
    
    public void logout() {
        getSimpleClient().logout(true);
    }
    
    public void connect(final ClientConnection client) 
            throws ConnectionFailureException 
    {
        connect(client, null);
    }
    
    public void connect(final ClientConnection client, 
                        final Properties properties)
            throws ConnectionFailureException
    {
        logger.fine(getName() + " attach client " + client);
        
        // check our status to make sure we are connected
        if (getStatus() != Status.CONNECTED) {
            throw new ConnectionFailureException("Session not connected");
        }
        
        final ClientRecord record;
        
        // check if there is already a client registered (or registering) for
        // this client type
        synchronized (clients) {
            if (getClientRecord(client.getConnectionType()) != null) {
                throw new ConnectionFailureException("Duplicate attach for " +
                        "client type " + client.getConnectionType());
            }
            
            // Add a client record.  Adding a client record at this early
            // guarantees that there will only be one registration for the
            // given type in progress at any time. If the connect fails
            // for any reason, we have to make sure to clean this record
            // up before we exit
            record = addClientRecord(client);
        }
        
        // send a request to the server to connect the given client type
        Message attachMessage = new AttachClientMessage(client.getConnectionType(),
                                                        properties);
        
        // Create a listener to handle the response.  We cannot do this
        // using just sendAndWait() because the response has to be
        // processed immediately, otherwise messages received immediately
        // after the response to this message will not be handled
        // properly.
        AttachResponseListener listener = new AttachResponseListener(record);
        
        // whether or not the connect attempt succeeded
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
            throw new ConnectionFailureException("Interrupted", ie);
        } finally {
            // clean up the client record if the connect failed
            if (!success) {
                removeClientRecord(client);
            }
        }
    }

    public ClientConnection getConnection(ConnectionType type) {
        return getConnection(type, ClientConnection.class);
    }
    
    public <T extends ClientConnection> T getConnection(ConnectionType type, 
                                                    Class<T> clazz) 
    {
        ClientRecord record = getClientRecord(type);
        if (record == null) {
            return null;
        }
        
        return clazz.cast(record.getClient());
    }

    public Collection<ClientConnection> getConnections() {
        List<ClientConnection> out = 
                new ArrayList<ClientConnection>(clients.size());
        
        synchronized (clients) {
            for (ClientRecord record : clients.values()) {
                out.add(record.getClient());
            }
        }
        
        return out;
    }

    public void disconnect(ClientConnection client) {
        logger.fine(getName() + " detach " + client);
        
        // get the client record
        ClientRecord record = removeClientRecord(client);
        if (record == null) {
            // ignore
            logger.warning(getName() + " trying to detach a client which is " +
                           " not attached: " + client);
            return;
        }
        
        // send a message
        getInternalClient().send(
             new DetachClientMessage(record.getClientID()));
        
        // notify the client
        client.disconnected();
    }
    
    public void send(ClientConnection client, Message message) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(getName() + " sending message " + message + 
                          " to client " + client);
        }
        
        // only the default client may send when the state is not
        // CONNECTED
        if (client != getInternalClient() && getStatus() != Status.CONNECTED) {
            throw new IllegalStateException("Session not connected");
        }
        
        // make sure the client is connected before we send
        if (client.getStatus() != ClientConnection.Status.CONNECTED) {
            throw new IllegalStateException("Client not attached");
        }
        
        // get the record for the given client
        ClientRecord record = getClientRecord(client);
        if (record == null) {
            throw new IllegalStateException(
                    "Client " + client.getConnectionType() + " not attached.");
        }
        
        // send the message to the server
        try {
            // send the combined message
            simpleClient.send(MessagePacker.pack(message, record.getClientID()));
        } catch (IOException ioe) {
            throw new MessageException(ioe);
        } catch (PackerException e) {
            throw new MessageException(e);
        }
    }
   
    public void addSessionStatusListener(SessionStatusListener listener) {
        sessionStatusListeners.add(listener);
    }
    
    public void removeSessionStatusListener(SessionStatusListener listener) {
        sessionStatusListeners.remove(listener);
    }
   
    /**
     * Fire when a message is received over the session channel
     * @param data the message that was received
     */
    protected void fireSessionMessageReceived(ByteBuffer data) { 
        Message message;
        short clientID;
        
        try {
            // read the message
            ReceivedMessage recv = MessagePacker.unpack(data);
            
            // all set, just unpack the received message
            message = recv.getMessage();
            clientID = recv.getClientID();
        } catch (PackerException eme) {
            logger.log(Level.WARNING, "Error extracting message from server",
                       eme);

            // if possible, send an error reply to the client
            if (eme.getMessageID() != null) {
                message = new ErrorMessage(eme.getMessageID(),
                                           eme.getMessage(),
                                           eme.getCause());
                clientID = eme.getClientID();
            } else {
                return;
            }
        } catch (Exception ex) {
            // what to do?
            logger.log(Level.WARNING, "Error extracting message from server",
                       ex);
            return;
        }
        
        // handle it with the selected client
        ClientRecord record = getClientRecord(clientID);
        if (record == null) {
            throw new IllegalStateException("Message " + message + 
                                            "to unknown client: " + clientID);
        }
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(getName() + " received session message for handler " + 
                          record.getClient().getConnectionType()+"  message type "+message.getClass().getName());
        }
        
        record.handleMessage(message);
    }
      
    /**
     * Notify any registered status listeners of a status change
     * @param session the session that changed
     * @param status the new status
     */
    protected void fireClientStatusChanged(WonderlandSession.Status status) 
    {
        for (SessionStatusListener listener : sessionStatusListeners) {
            listener.sessionStatusChanged(this, status);
        }
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
    protected SessionInternalHandler getInternalClient() {
        return (SessionInternalHandler) getConnection(INTERNAL_CLIENT_TYPE);
    }
    
    /**
     * Add a client record to the map
     * @param client the client to add a record for
     * @return the newly added record
     */
    protected ClientRecord addClientRecord(ClientConnection client) {
        logger.fine(getName() + " adding record for client " + client);
        ClientRecord record = new ClientRecord(client);
        clients.put(client.getConnectionType(), record);
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
     * client is not connected to this session
     */
    protected ClientRecord getClientRecord(ClientConnection client) {
        ClientRecord record = clients.get(client.getConnectionType());
        
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
     * client is not connected to this session
     */
    protected ClientRecord getClientRecord(ConnectionType type) {
        return clients.get(type);
    }
    
    /**
     * Get the client record with the given id
     * @param clientID the client to get a record for
     * @return the ClientRecord for the given client, or null if the given
     * client is not connected to this session
     */
    protected ClientRecord getClientRecord(short clientID) {
        return clientsByID.get(Short.valueOf(clientID));
    }
    
    /**
     * Remove a client record
     * @param client the client to remove a record for
     * @return the client record that was removed, or null if no record
     * was removed
     */
    protected ClientRecord removeClientRecord(ClientConnection client) {
        logger.fine(getName() + "Removing record for client " + client);
        
        ClientRecord record = getClientRecord(client);
        if (record != null) {
            synchronized (clients) {
                clients.remove(client.getConnectionType());
                clientsByID.remove(Short.valueOf(record.getClientID()));
            }
        }
        
        return record;
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
            logger.fine(getName() + " disconnected, reason: " + reason);
            synchronized (this) {
                // are we in the process of logging in?
                if (getCurrentLogin() != null) {
                    getCurrentLogin().setFailure(reason);
                } else {
                    setStatus(Status.DISCONNECTED);
                }
            }
        }

        public ClientChannelListener joinedChannel(ClientChannel channel) {
            logger.fine("Client joined channel " + channel.getName());
            
            return new ClientChannelListener() {

                public void receivedMessage(ClientChannel channel, 
                                            ByteBuffer data) 
                {
                    logger.finest("Received " + data.remaining() + 
                                  " bytes on channel " + channel.getName());
                    fireSessionMessageReceived(data);
                }

                public void leftChannel(ClientChannel channel) {
                    logger.fine("Left channel " + channel.getName());
                }
            };
        }
        
        /**
         * {@inheritDoc}
         */
        public void receivedMessage(ByteBuffer data) {
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
     * The record for an connected client
     */
    protected class ClientRecord {
        /** the client that connected */
        private ClientConnection client;
        
        /** the id of this client, as assigned by the server */
        private short clientID;
        
        public ClientRecord(ClientConnection client) {
            this.client = client;
        }
        
        /**
         * Get the client associated with this record
         * @return the associated client
         */
        public ClientConnection getClient() {
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
          
        @Override
        public String toString() {
            return getClient().toString();
        }
    }
    
    /**
     * Handle traffic over the session channel
     */
    protected static class SessionInternalHandler extends BaseConnection {
        public ConnectionType getConnectionType() {
            // only used internally
            return INTERNAL_CLIENT_TYPE;
        }

        public void handleMessage(Message message) {
            // unhandled session messages?
            logger.warning("Unhandled message: " + message);
        }   
    }
     
    /**
     * Listen for responses to the connect() message.
     */
    class AttachResponseListener extends WaitResponseListener {
        /** the record to update on success */
        private ClientRecord record;
        
        /** whether or not we succeeded */
        private boolean success = false;
        
        /** the exception if we failed */
        private ConnectionFailureException exception;
        
        public AttachResponseListener(ClientRecord record) {
            this.record = record;
        }
        
        @Override
        public void responseReceived(ResponseMessage response) {
            if (response instanceof AttachedClientMessage) {
                AttachedClientMessage acm = (AttachedClientMessage) response;

                // set the client id
                setClientID(record, acm.getClientID());
                
                // notify the client that we are now connected
                record.getClient().connected(WonderlandSessionImpl.this);
                
                // success
                setSuccess(true);
            } else if (response instanceof ErrorMessage) {
                // error -- throw an exception
                ErrorMessage e = (ErrorMessage) response;
                setException(new ConnectionFailureException(e.getErrorMessage(),
                                                        e.getErrorCause()));
            } else {
                // bad situation
                setException(new ConnectionFailureException("Unexpected response " +
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
        
        public synchronized ConnectionFailureException getException() {
            return exception;
        }
        
        public synchronized void setException(ConnectionFailureException exception) {
            this.exception = exception;
        }
    }
}
