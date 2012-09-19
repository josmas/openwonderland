/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.comms.session;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * The record for an connected client
 */
class ClientRecord {
    /** the client that connected */
    private ClientConnection client;
    /** the id of this client, as assigned by the server */
    private short clientID;
    private final WonderlandSessionImpl session;

    
    private static final Logger logger = Logger.getLogger(ClientRecord.class.getName());
    
    public ClientRecord(ClientConnection client, final WonderlandSessionImpl session) {
        this.session = session;
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
            logger.finest(session.getName() + " client " + this + " received message " + message);
        }
        // send to the client
        getClient().messageReceived(message);
    }

    @Override
    public String toString() {
        return getClient().toString();
    }
    
}
