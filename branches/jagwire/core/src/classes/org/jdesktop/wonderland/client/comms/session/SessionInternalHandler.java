/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.comms.session;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.comms.SessionInternalConnectionType;
import org.jdesktop.wonderland.common.comms.messages.SessionInitializationMessage;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * Handle traffic over the session channel
 */
public class SessionInternalHandler extends BaseConnection {
    private WonderlandSessionImpl session;
    private SessionInitializationMessage initMessage;

    
    private static final Logger logger = Logger.getLogger(SessionInternalHandler.class.getName());
    
    public SessionInternalHandler(WonderlandSessionImpl session) {
        this.session = session;
        // notify anyone waiting for initialization messages if the
        // status becomes disconnected.
        session.addSessionStatusListener(new SessionStatusListener() {
            public void sessionStatusChanged(WonderlandSession session, WonderlandSession.Status status) {
                if (status == WonderlandSession.Status.DISCONNECTED) {
                    synchronized (SessionInternalHandler.this) {
                        SessionInternalHandler.this.notifyAll();
                    }
                }
            }
        });
    }

    public ConnectionType getConnectionType() {
        // only used internally
        return SessionInternalConnectionType.SESSION_INTERNAL_CLIENT_TYPE;
    }

    public void handleMessage(Message message) {
        if (message instanceof SessionInitializationMessage) {
            synchronized (this) {
                this.initMessage = (SessionInitializationMessage) message;
                notifyAll();
            }
        } else {
            // unhandled session messages?
            logger.warning("Unhandled message: " + message);
        }
    }

    /**
     * Wait for a session initialization message to be sent to the internal
     * handler.  This method will return the most recent initialization
     * message received, or block if no message has been received.
     * <p>
     * When a client disconnects, the message will be reset to null.
     * Clients waiting at that point will be woken with a null response.
     * @return the most recent message, or null if the client is
     * disconnected.
     */
    public synchronized SessionInitializationMessage waitForInitialization() throws InterruptedException {
        while (initMessage == null && session.getStatus() != WonderlandSession.Status.DISCONNECTED) {
            wait();
        }
        return initMessage;
    }
    
}
