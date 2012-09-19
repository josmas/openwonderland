/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.comms.session;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClientListener;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;

/**
 * Wonderland client listener
 */
class WonderlandClientListener implements SimpleClientListener {
    private final WonderlandSessionImpl session;

    private static final Logger logger = Logger.getLogger(WonderlandClientListener.class.getName());
    
    WonderlandClientListener(final WonderlandSessionImpl session) {
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    public PasswordAuthentication getPasswordAuthentication() {
        // This is called to get the user name and authentication data (eg password)
        // to be authenticated server side.
        LoginParameters loginParams = session.getCurrentLogin().getLoginParameters();
        return new PasswordAuthentication(loginParams.getUserName(), loginParams.getPassword());
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void loggedIn() {
        logger.fine(session.getName() + " logged in");
        session.getCurrentLogin().setLoginSuccess();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void loginFailed(String reason) {
        logger.fine(session.getName() + " login failed: " + reason);
        session.getCurrentLogin().setFailure(reason);
    }

    /**
     * {@inheritDoc}
     */
    public void disconnected(boolean graceful, String reason) {
        logger.fine(session.getName() + " disconnected, reason: " + reason);
        synchronized (this) {
            // are we in the process of logging in?
            if (session.getCurrentLogin() != null) {
                session.getCurrentLogin().setFailure(reason);
            } else {
                session.setStatus(Status.DISCONNECTED);
            }
        }
    }

    public ClientChannelListener joinedChannel(ClientChannel channel) {
        logger.fine("Client joined channel " + channel.getName());
        return new ClientChannelListener() {
            public void receivedMessage(ClientChannel channel, ByteBuffer data) {
                logger.finest("Received " + data.remaining() + " bytes on channel " + channel.getName());
                session.fireSessionMessageReceived(data);
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
        session.fireSessionMessageReceived(data);
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
