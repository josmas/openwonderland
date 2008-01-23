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
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.ProtocolVersion;
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
 */@ExperimentalAPI
public abstract class BaseClient implements SimpleClientListener {
    /** logger */
    private static final Logger logger =
            Logger.getLogger(BaseClient.class.getName());
    
    /** possible states of the client */
    public enum Status { DISCONNECTED, CONNECTING, CONNECTED };
    
    /** the current status */
    private Status status;
    
    /** the server to connect to */
    private WonderlandServerInfo server;
    
    /** the current login attempt */
    private LoginAttempt currentLogin;
    
    /** the connected client */
    private SimpleClient simpleClient;
   
    /** outstanding message listeners, mapped by id */
    private Map<MessageID, ResponseListener> responseListeners;
    
    /** factories to generate a listener when we join a channel */
    private List<ChannelListenerFactory> channelListenerFactories;
    
    /** listeners for session messages */
    private Set<ListenerRecord> sessionListeners;
    
    /** client lifecycle listeners */
    private static Set<ClientLifecycleListener> lifecycleListeners =
            new CopyOnWriteArraySet<ClientLifecycleListener>();
    
    /**
     * Create a new client to log in to the given server
     * @param server the server to connect to
     */
    public BaseClient(WonderlandServerInfo server) {
        this.server = server;
        
        status = Status.DISCONNECTED;
        
        // initialize listener collections
        responseListeners = Collections.synchronizedMap(
                                    new HashMap<MessageID, ResponseListener>());
        channelListenerFactories = 
                new CopyOnWriteArrayList<ChannelListenerFactory>();
        sessionListeners = new CopyOnWriteArraySet<ListenerRecord>();
        
        // notify lifecycle listeners
        fireNewClient(this);
    }
    
    /**
     * Get the server this client is connected to
     * @return the server
     */
    public WonderlandServerInfo getServerInfo() {
        return server;
    }
    
    /**
     * Get the status of this client.
     * @return the current status of the client
     */
    public synchronized Status getStatus() {
        return status;
    }
    
    /**
     * Set the status of this client.
     * @param status the current status of the client
     */
    protected void setStatus(Status status) {
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
            fireClientStatusChanged(this, status);
        }
    }
    
    /**
     * Get the simple client connected to the Darkstar server.  This will
     * only be valid after the login method succeeds.
     * @return the darkstar client
     */
    public SimpleClient getClient() {
        return simpleClient;
    }
    
    /**
     * Log in to the server.  This will connect to the given server,
     * and wait for the login to succeed.  If the login succeeds, 
     * negotiate a protocol using the Wonderland protocol negotiation
     * mechanism.  This method blocks until both the login and protocol
     * selection have been verified by the server.
     * <p>
     * If this method returns normally, then the login and protocol selection
     * succeeded.  If it throws a LoginFailureException, the login has failed.
     * <p>
     * @param loginParams the parameters required for login
     * @throws LoginFailureException if the login fails
     */
    public void login(LoginParameters loginParams) 
        throws LoginFailureException
    {
        // make sure there is no login in progress
        synchronized (this) {
            if (status != Status.DISCONNECTED) {
                throw new LoginFailureException("Login already in progress");
            }
            
            startLogin(loginParams);
        }
        
        simpleClient = new SimpleClient(this);
        
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
    }
    
    /**
     * Send a message to the server over the session channel.  Identical
     * to calling send(message, null).
     * @param message the message to send
     * @throws MessageException if there is an error getting the bytes
     * for the message
     */
    public void send(Message message) {
        send(message, null);
    }
    
    /**
     * Send a message to the server over the session channel and provide a
     * listener.  The given listener will be notified when the response
     * is received.
     * @param message the message to send
     * @param listener the message response listener to notify when a 
     * response is received
     * @throws MessageException if there is an error getting the bytes
     * for the message
     */
    public void send(Message message, ResponseListener listener) {
        if (listener != null) {
            responseListeners.put(message.getMessageID(), listener);
        }
        
        try {
            simpleClient.send(message.getBytes());
        } catch (IOException ioe) {
            throw new MessageException(ioe);
        }
    }
    
    /**
     * Send a message to the server and wait for the response.  The given 
     * response listener will be notified when the response is received. This
     * method blocks until the response is received.
     * @param message the message to send
     * @param listener the message response listener to notify when a response
     * is received
     * @throws MessageException if there is an error getting the bytes
     * for the message
     * @throws InterruptedException if the wait is interrupted
     */
    public void sendAndWait(Message message, WaitResponseListener listener)
        throws InterruptedException
    {
        send(message, listener);
        listener.wait();
    }
    
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
        getCurrentLogin().setLoginSuccess();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void loginFailed(String reason) {
        getCurrentLogin().setFailure(reason);
    }

    /**
     * {@inheritDoc}
     */
    public void disconnected(boolean graceful, String reason) {
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
        // find the first factory that will create a listener
        for (ChannelListenerFactory factory : channelListenerFactories) {
            ClientChannelListener listener = factory.createListener(this, 
                                                                    channel);
            if (listener != null) {
                return listener;
            }
        }
        
        // no listener for the given channel
        logger.warning("No listener factory for channel " + channel.getName());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void receivedMessage(byte[] data) {
        try {
            // extract the message
            Message m = Message.extract(data);
            
            // see if we are waiting for a response
            if (m instanceof ResponseMessage) {
                notifyResponseListener((ResponseMessage) m);
            }
            
            // send to other listeners
            fireSessionMessageReceived(m);
        } catch (ExtractMessageException eme) {
            logger.log(Level.WARNING, "Error extracting message from server", 
                       eme);
            
            // if possible, send a reply to the client
            if (eme.getMessageID() != null) {
                // Was it a response we were waiting for?  
                // If so, fake an error message
                notifyResponseListener(new ErrorMessage(eme.getMessageID(),
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

    /**
     * Add a lifecycle listener.  This will receive messages for all
     * clients that are created or change status
     * @param listener the listener to add
     */
    public static void addLifecycleListener(ClientLifecycleListener listener) {
        lifecycleListeners.add(listener);
    }
    
    /**
     * Remove a lifecycle listener.
     * @param listener the listener to remove
     */
    public static void removeLifecycleListener(ClientLifecycleListener listener) {
        lifecycleListeners.remove(listener);
    }
    
    /**
     * Register a factory that will be used to create a listener when we join a
     * new channel.
     * @param factory the factory to register
     */
    public void registerChannelListenerFactory(ChannelListenerFactory factory) {
        channelListenerFactories.add(factory);
    }
    
    /**
     * Register a listener that will be notified of session messages from
     * the server
     * @param messageClass the class of message to listen for
     * @param listener the listener to add
     */
    public void addSessionMessageListener(Class<? extends Message> messageClass,
                                          SessionMessageListener listener)
    {
        sessionListeners.add(new ListenerRecord(messageClass, listener));
    }
    
    /**
     * Remove the given listener.  This will remove the given listener from
     * listening for all messages of the given type or any classes that are
     * assignable from the given type.
     * @param listener the listener to remove
     */
    public void removeSessionMessageListener(Class<? extends Message> messageClass,
                                             SessionMessageListener listener)
    {
        for (Iterator<ListenerRecord> i = sessionListeners.iterator();
             i.hasNext();)
        {
            ListenerRecord record = i.next();
            if (record.messageClass.isAssignableFrom(messageClass)) {
                i.remove();
            }
        }
    }
    
    /**
     * Notify any registered lifecycle listeners that a new client was created
     * @param client the client that was created
     */
    protected static void fireNewClient(BaseClient client) {
        for (ClientLifecycleListener listener : lifecycleListeners) {
            listener.clientCreated(client);
        }
    }
    
    /**
     * Notify any registered lifecycle listeners of a status change
     * @param client the client that changed
     * @param status the new status
     */
    protected static void fireClientStatusChanged(BaseClient client,
                                                  BaseClient.Status status) 
    {
        for (ClientLifecycleListener listener : lifecycleListeners) {
            listener.clientStatusChanged(client, status);
        }
    }
    
    /**
     * Fire when a message is received over the session channel
     * @param message the message that was received
     */
    protected void fireSessionMessageReceived(Message message) {
        for (ListenerRecord rec : sessionListeners) {
            if (rec.messageClass.isAssignableFrom(message.getClass())) {
                rec.messageListener.messageReceived(this, message);
            }
        }
    }
    
    /**
     * Send a message to any registered response listeners for the message.
     * @param response the response message
     */
    protected void notifyResponseListener(ResponseMessage response) {
        ResponseListener listener = 
                responseListeners.remove(response.getMessageID());
        
        if (listener != null) {  
            listener.responseReceived(response);
        }
    }
    
    /**
     * Get the name of the protocol to connect with
     * @return the name of the protocol to connect with
     */
    protected abstract String getProtocolName();
    
    /**
     * Get the version of the protocol to connect with
     * @return the version of the protocol to connect with
     */
    protected abstract ProtocolVersion getProtocolVersion();
    
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
                public void onFailure(MessageID messageID, String message, Throwable cause) {
                    setFailure(message, cause);
                }
            };
            
            send(psm, rl);
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
     * A listener for messages of the given type
     */
    class ListenerRecord {
        Class<? extends Message> messageClass;
        SessionMessageListener messageListener;
    
        public ListenerRecord(Class<? extends Message> messageClass,
                              SessionMessageListener messageListener)
        {
            this.messageClass = messageClass;
            this.messageListener = messageListener;
        }
    }
}
