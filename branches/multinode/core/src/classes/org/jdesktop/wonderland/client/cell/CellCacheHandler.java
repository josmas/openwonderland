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
import java.util.ArrayList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.client.comms.BaseHandler;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.cell.CellCacheHandlerType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.comms.HandlerType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * Handler for Cell cache information
 * @author jkaplan
 */
@ExperimentalAPI
public class CellCacheHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(CellCacheHandler.class.getName());
    
    private ArrayList<CellCacheMessageListener> listeners = new ArrayList();
    
    /**
     * Get the type of client
     * @return CellClientType.CELL_CLIENT_TYPE
     */
    public HandlerType getHandlerType() {
        return CellCacheHandlerType.CLIENT_TYPE;
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
    public void send(CellHierarchyMessage message) {
        super.send(message);
    }
    
    /**
     * Send a message to the server CellCacheHandler
     * 
     * @param message the message to send
     * @param listener the response listener to notify when a response
     * is received.
     */
    public void send(CellHierarchyMessage message, ResponseListener listener) {
        super.send(message, listener);
    }
    
    /**
     * Send a message to the server and wait for a 
     * response.
     * 
     * @param message the message to send
     * @throws InterruptedException if there is a problem sending a message
     * to the given cell
     */
    public ResponseMessage sendAndWait(CellHierarchyMessage message)
        throws InterruptedException
    {
        return super.sendAndWait(message);
    }
    
    /**
     * Handle a message from the server
     * @param message the message to handle
     */
    public void handleMessage(Message message) {
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
            case UNLOAD_CELL :
                for(CellCacheMessageListener l : listeners) {
                    l.unloadCell(msg.getCellID());
                }
                break;
                
//            case LOAD_CLIENT_AVATAR :
//                for(CellCacheMessageListener l : listeners) {
//                    l.loadClientAvatar(msg.getCellID(),
//                                msg.getCellClassName(),
//                                msg.getLocalBounds(),
//                                msg.getParentID(),
//                                msg.getCellChannelName(),
//                                msg.getCellTransform(),
//                                msg.getSetupData());
//                }
//                
            default :
                logger.warning("Message type not implemented "+msg.getActionType());
        }
    }

    @Override
    public void detached() {
        // remove any action listeners
        listeners.clear();
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
         * @param cellTransform
         * @param setup
         */
        public void loadCell(CellID cellID, 
                               String className, 
                               BoundingVolume localBounds,
                               CellID parentCellID,
                               CellTransform cellTransform,
                               CellSetup setup);
        /**
         * Load the avatar for this client
         * @param cellID
         * @param className
         * @param localBounds
         * @param parentCellID
         * @param cellTransform
         * @param setup
         */
//        public void loadClientAvatar(CellID cellID, 
//                               String className, 
//                               BoundingVolume localBounds,
//                               CellID parentCellID,
//                               CellTransform cellTransform,
//                               CellSetup setup);
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
