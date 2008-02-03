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
package org.jdesktop.wonderland.client.cell;

import com.jme.bounding.BoundingVolume;
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
import org.jdesktop.wonderland.common.cell.CellCacheClientType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.comms.WonderlandChannelNames;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * Client that handles Cell cache information
 * @author jkaplan
 */
@ExperimentalAPI
public class CellCacheClient extends BaseClient {
    /** the prefix for cell channels */
    private static final String CELL_CHANNEL_PREFIX =
            WonderlandChannelNames.AVATAR_CACHE_PREFIX + ".";
    
    /** a channel joined listener */
    private ChannelJoinedListener listener = new CellCacheChannelJoinedListener();
    
    private static final Logger logger = Logger.getLogger(CellCacheClient.class.getName());
    
    private ArrayList<CellCacheMessageListener> listeners = new ArrayList();
    
    /**
     * Get the type of client
     * @return CellClientType.CELL_CLIENT_TYPE
     */
    public ClientType getClientType() {
        return CellCacheClientType.CELL_CLIENT_TYPE;
    }

    /**
     * Add a listener for cell cache actions. This should be called during setup
     * not once the system is running
     * @param listener
     */
    public void addListener(CellCacheMessageListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Send a cell message to a specific cell on the server
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#send(WonderlandClient, Message)
     * 
     * @param message the cell message to send
     */
    public void send(CellMessage message) {
        getSession().send(this, message);
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
    public void send(CellMessage message, ResponseListener listener) {
        getSession().send(this, message, listener);
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
    public ResponseMessage sendAndWait(CellMessage message)
        throws InterruptedException
    {
        return getSession().sendAndWait(this, message);
    }
    
    /**
     * Handle a message from the server
     * @param message the message to handle
     */
    public void messageReceived(Message message) {
        if (!(message instanceof CellHierarchyMessage))
            throw new RuntimeException("Unexpected message type "+message.getClass().getName());
        
        CellHierarchyMessage msg = (CellHierarchyMessage)message;
        switch(msg.getActionType()) {
            case LOAD_CELL :
                for(CellCacheMessageListener l : listeners) {
                    l.loadCell(msg.getCellID(),
                                msg.getCellClassName(),
                                msg.getLocalBounds(),
                                msg.getParentID(),
                                msg.getCellChannelName(),
                                msg.getCellTransform(),
                                msg.getSetupData());
                }
                break;
            case MOVE_CELL :
                for(CellCacheMessageListener l : listeners) {
                    l.moveCell(msg.getCellID(),
                            msg.getCellTransform());
                }
                 break;
            case SET_WORLD_ROOT :
                for(CellCacheMessageListener l : listeners) {
                    l.setRootCell(msg.getCellID());
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
    class CellCacheChannelJoinedListener implements ChannelJoinedListener {
        public ClientChannelListener joinedChannel(WonderlandSession session, 
                                                   ClientChannel channel)
        {
            String channelName = channel.getName();
            if (channelName.startsWith(CELL_CHANNEL_PREFIX)) {
                String id = channelName.substring(CELL_CHANNEL_PREFIX.length());
                
                // TODO: get cell, associate cell channel with cell 
            }
            
            return null;
        }
    }
   
    /**
     * Listener interface for cell cache action messages
     */
    public static interface CellCacheMessageListener {
        /**
         * Load the cell and prepare it for use
         * @param cellID
         * @param className
         * @param computedWorldBounds
         * @param parentCellID
         * @param channelName
         * @param cellTransform
         * @param setup
         */
        public void loadCell(CellID cellID, 
                               String className, 
                               BoundingVolume localBounds,
                               CellID parentCellID,
                               String channelName,
                               CellTransform cellTransform,
                               CellSetup setup);
        
        /**
         * Unload the cell. This removes the cell from memory but will leave
         * cell data cached on the client
         * @param cellID
         */
        public void unloadCell(CellID cellID);
        
        /**
         * Delete the cell and all its content from the client
         * @param cellID
         */
        public void deleteCell(CellID cellID);
        
        public void setRootCell(CellID cellID);
       
        public void moveCell(CellID cellID,
                             CellTransform cellTransform);
    }
}
