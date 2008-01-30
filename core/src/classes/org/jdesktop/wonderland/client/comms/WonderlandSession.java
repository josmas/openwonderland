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

import com.sun.sgs.client.simple.SimpleClient;
import java.util.Collection;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * The WonderlandSession is the base class for communicating with a given
 * Wonderland server.  The session encapsulates the work of login and
 * maintaining the state of the connection with the given server.
 * <p>
 * 
 * @author jkaplan
 */
@ExperimentalAPI
public interface WonderlandSession {
    /** possible states of the client */
    @ExperimentalAPI
    public enum Status { DISCONNECTED, CONNECTING, CONNECTED };
    
    /**
     * Get the server this client is connected to
     * @return the server
     */
    public WonderlandServerInfo getServerInfo();
    
    /**
     * Get the status of this client.
     * @return the current status of the client
     */
    public Status getStatus();
    
    /**
     * Get the simple client connected to the Darkstar server.  This will
     * only be valid after the login method succeeds.
     * @return the darkstar client
     */
    public SimpleClient getSimpleClient();
    
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
        throws LoginFailureException;
    
    /**
     * Disconnect from the server.
     */
    public void disconnect();
    
    /**
     * Attach a new client to this session.  When a client is attached to
     * this session, it will interact with the server associated with
     * this session.  
     * <p>
     * Only one client of any given ClientType may be
     * connected to the server at any time.  Attempting to attach a second
     * client of the same type will result in an AttachFailureException.
     * <p>
     * Clients may only be attached when a session is in the CONNECTED state.
     * If the session is in any other state, an AttachFailureException will
     * be thrown.  When a session disconnects, all clients are detached,
     * and must be re-attached to start working again.
     * 
     * @param client the client to attach
     * @throws AttachFailureException of the attachment fails
     */
    public void attach(WonderlandClient client) throws AttachFailureException;
    
    /**
     * Get the attched WonderlandClient for the given ClientType.
     * @param type the client type to get
     * @return the attached client for the given type, or null if no
     * client of the given type is attached
     */
    public WonderlandClient getClient(ClientType type);
    
    /**
     * Get the attched WonderlandClient for the given ClientType.
     * @param type the client type to get
     * @param clazz the class of client to return
     * @return the attached client for the given type, or null if no
     * client of the given type is attached
     * @throws ClassCastException if the client for the given client type
     * is not assignable to the given type
     */
    public <T extends WonderlandClient> T getClient(ClientType type,
                                                    Class<T> clazz);
    
    /**
     * Get all clients attached to this session
     * @return the clients attached to this session
     */
    public Collection<WonderlandClient> getClients();
    
    /**
     * Detach a previously attached client from this session.
     * @param client the client to detach
     */
    public void detach(WonderlandClient client);
    
    /**
     * Send a message to the server over the session channel on behalf of the 
     * given client.  Identical to calling send(message, null).
     * @param client the client that is sending the message
     * @param message the message to send
     * @throws MessageException if there is an error getting the bytes
     * for the message
     */
    public void send(WonderlandClient client, Message message);
    
    /**
     * Send a message to the server over the session channel and provide a
     * listener.  The message is sent on behalf of the given WonderlandClient.
     * The client must be successfully attached to this session in order
     * for the send to work.
     * <p>
     * The given listener will be notified when a response is received. If
     * the listener is not null, it is critical that the server guarantees 
     * that either a ResponseMessage or an ErrorMessage will be sent in response
     * to this message, otherwise this will cause a memory leak.
     * <p>
     * Note that listeners are cleared as soon as the given client detaches
     * from the session, so responses are not guaranteed in this case.
     * 
     * @param client the client that is sending the message
     * @param message the message to send
     * @param listener the message response listener to notify when a 
     * response is received
     * @throws MessageException if there is an error getting the bytes
     * for the message
     */
    public void send(WonderlandClient client, Message message, 
                     ResponseListener listener);
    
    /**
     * Send a message to the server and wait for the response. This
     * method blocks until the response is received.
     * <p>
     * If the given client disconnects before a response is received from the
     * server, this method should throw an InterruptedException.
     * 
     * @param client the client that is sending the message
     * @param message the message to send
     * @return the response to the given message
     * @throws MessageException if there is an error getting the bytes
     * for the message
     * @throws InterruptedException if the wait is interrupted
     */
    public ResponseMessage sendAndWait(WonderlandClient client, Message message)
        throws InterruptedException;
    
    /**
     * Add a listener that will be notified when a new channel is joined.
     * When a new channel is joined, each ChannelJoinedListener is notifed
     * in the order in which they were added.  The first listener that
     * responds with a non-null ClientChannelListener will be used to handle
     * messages on the given channel.
     * 
     * @param listener the listener to register
     */
    public void addChannelJoinedListener(ChannelJoinedListener listener);
    
    /**
     * Remove a ChannelJoinedListener
     */
    public void removeChannelJoinedListener(ChannelJoinedListener listener);
}
