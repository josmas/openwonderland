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

import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ExtractMessageException;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.OKMessage;
import org.jdesktop.wonderland.common.messages.ProtocolSelectionMessage;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.CommunicationsProtocol;

/**
 * This core session listener implements the basic Wonderland protocol
 * selection mechanism.  When a new client connects, they request a protcol
 * using a ProtocolSelectionMessage.  This listener handles the protcol
 * selection message, either by sending an error or instantiating the listener
 * associated with the given protocol type.
 * <p>
 * Once the session type has been successfully selected, this listener
 * simply acts as a wrapper, passing all request on to the delegated
 * listener.
 *
 * @author jkaplan
 */
class ProtocolSessionListener
        implements ClientSessionListener, Serializable {
    
    /** a logger */
    private static final Logger logger =
            Logger.getLogger(ProtocolSessionListener.class.getName());
    
    /** the session associated with this listener */
    private ClientSession session;
    
    /** the wrapped session, or null if no wrapped session exists yet */
    private ClientSessionListener wrapped;
    
    /**
     * Create a new instance of WonderlandSessionListener for the given
     * session
     * @param session the session connected to this listener
     */
    public ProtocolSessionListener(ClientSession session) {
        this.session = session;
    }

    /**
     * Called when the listener receives a message.  If the wrapped session
     * has not yet been defined, look for ProtocolSelectionMessages, otherwise
     * simply forward the data to the delegate session
     * @param data the message data
     */
    public void receivedMessage(byte[] data) {
        
        // if there is a wrapped session, simply forward the data to it
        if (wrapped != null) {
            wrapped.receivedMessage(data);
            return;
        }
        
        // no wrapped session -- look for a ProtocolSelectionMessage
        try {
            Message m = Message.extract(data);
            
            // check the message type
            if (!(m instanceof ProtocolSelectionMessage)) {
                sendError(m, "Only ProtcolSelectionMessage allowed");
                return;
            }
            
            ProtocolSelectionMessage psm = (ProtocolSelectionMessage) m;
            CommsManager cm = WonderlandContext.getCommsManager();

            // see if we have a protocol to match the request
            CommunicationsProtocol cp = cm.getProtocol(psm.getProtocolName());
            if (cp == null) {
                sendError(m, "Protocol " + psm.getProtocolName() + " not found");
                return;
            }
            
            // see if the versions match
            if (!cp.getVersion().isCompatible(psm.getProtocolVersion())) {
                sendError(m, "Client version incompatible with server " + 
                             "version " + cp.getVersion());
            }
            
            logger.info("Session " + session.getName() + " connected with " +
                        "protocol " + cp.getName());
            
            // all set -- set the wrapped session
            wrapped = cp.createSessionListener(session, psm.getProtocolVersion());
            
            WonderlandContext.getUserManager().login(session, this);
            
            // send an OK message
            getSession().send(new OKMessage(psm.getMessageID()).getBytes());
        } catch (ExtractMessageException eme) {
            sendError(eme.getMessageID(), null, eme);
        } catch (Exception ex) {
            // TODO: react better?
            getSession().disconnect();
        }
    }

    /**
     * Called when the delegate session is disconnected
     * @param forced true if the disconnect was forced
     */
    public void disconnected(boolean forced) {
        if (wrapped != null) {
            wrapped.disconnected(forced);
            WonderlandContext.getUserManager().logout(session, this);
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
    protected void sendError(Message source, String error) {
        sendError(source.getMessageID(), error, null);
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
}
