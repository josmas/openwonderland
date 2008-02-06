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
package org.jdesktop.wonderland.client.tools;

import org.jdesktop.wonderland.client.cell.*;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.client.comms.AttachFailureException;
import org.jdesktop.wonderland.client.comms.BaseClient;
import org.jdesktop.wonderland.client.comms.ChannelJoinedListener;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.cell.AvatarClientType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.AvatarMessage;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * The AvatarClient handles avatar communication
 * @author paulby
 */
@ExperimentalAPI
public class AvatarClient extends BaseClient {
    
    /** a channel joined listener */
    private ChannelJoinedListener listener = new AvatarClientChannelJoinedListener();
    
    private static final Logger logger = Logger.getLogger(AvatarClient.class.getName());
    
    private ArrayList<AvatarMessageListener> listeners = new ArrayList();
    
    /**
     * Get the type of client
     * @return CellClientType.CELL_CLIENT_TYPE
     */
    public ClientType getClientType() {
        return AvatarClientType.CLIENT_TYPE;
    }

    /**
     * Add a listener for avatar actions. This should be called during setup
     * not once the system is running
     * @param listener
     */
    public void addListener(AvatarMessageListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Send a cell message to a specific cell on the server
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#send(WonderlandClient, Message)
     * 
     * @param message the cell message to send
     */
    public void send(AvatarMessage message) {
        super.send(message);
    }
    
    /**
     * Send a cell messag to a specific cell on the server with the given
     * listener.
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#send(WonderlandClient, Message, ResponseListener)
     * 
     * @param message the message to send
     * @param listener the response listener to notify when a response
     * is received.
     */
    public void send(AvatarMessage message, ResponseListener listener) {
        super.send(message, listener);
    }
    
    /**
     * Send a cell messag to a specific cell on the server and wait for a 
     * response.
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#sendAndWait(WonderlandClient, Message)
     * 
     * @param message the message to send
     * @throws InterruptedException if there is a problem sending a message
     * to the given cell
     */
    public ResponseMessage sendAndWait(AvatarMessage message)
        throws InterruptedException
    {
        return super.sendAndWait(message);
    }
    
    /**
     * Handle a message from the server
     * @param message the message to handle
     */
    public void handleMessage(Message message) {
        if (!(message instanceof AvatarMessage))
            throw new RuntimeException("Unexpected message type "+message.getClass().getName());
        
        AvatarMessage msg = (AvatarMessage)message;
        switch(msg.getActionType()) {
            case MOVED :
                for(AvatarMessageListener l : listeners) {
                    l.avatarMoved(msg.getLocation(), msg.getOrientation());
                }
                break;
            default :
                logger.warning("Message type not implemented "+msg.getActionType());
        }
    }

    @Override
    public void attach(WonderlandSession session) 
            throws AttachFailureException 
    {
        // register a channel listener
        session.addChannelJoinedListener(listener);
        
        // now attach to the session
        try {
            super.attach(session);
        } catch (AttachFailureException afe) {
            // unregister the listener and re-throw the exception
            session.removeChannelJoinedListener(listener);
            throw afe;
        } catch (RuntimeException re) {
            // unregister the listener and re-throw the exception
            session.removeChannelJoinedListener(listener);
            throw re; 
        }
    }

    @Override
    public void detached() {
        // unregister the listener
        getSession().removeChannelJoinedListener(listener);
        
        // remove any action listeners
        listeners.clear();
    }
    
    /**
     * Map cell channels to the cell they match
     */
    class AvatarClientChannelJoinedListener implements ChannelJoinedListener {
        public ClientChannelListener joinedChannel(WonderlandSession session, 
                                                   ClientChannel channel)
        {
            logger.warning("AvatarClient channel join "+session);
            
            return null;
        }
    }
   
    /**
     * Listener interface for cell cache action messages
     */
    public static interface AvatarMessageListener {
        /**
         * The avatar has moved (in world coordinates)
         * 
         * @param location
         * @param velocity
         */
        public void avatarMoved(Vector3f location, Quaternion orientation);
        
        /**
         * Just an example
         * @param gesture
         */
        public void avatarGesture(int gesture);
    }
}
