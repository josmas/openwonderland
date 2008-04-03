/**
 * Project Wonderland
 *
 * $Id$
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
 */
package org.jdesktop.wonderland.client.cell;

import java.util.ArrayList;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.EntityMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A cell that can move
 * 
 * @author paulby
 */
@ExperimentalAPI
public class EntityCell extends Cell implements ChannelCell {
    private CellChannelConnection cellChannelConnection;
    
    private static Logger logger = Logger.getLogger(EntityCell.class.getName());
    private ArrayList<CellMoveListener> serverMoveListeners = null;
    
    public EntityCell(CellID cellID) {
        super(cellID);
        cellChannelConnection = CellChannelConnection.getCellChannelHandler();
    }
    
    /**
     * A request from this client to move the cell. The cell we be moved locally
     * and the requested change sent to the server. If the server denies the move
     * the cell will be moved to a server provided location. The server will
     * notify all other clients using this cell of the new location.
     * 
     * @param transform
     */
    public void localMoveRequest(CellTransform transform) {
        // TODO Need listener for move failure, both within this class and
        // potentially for external developers
        EntityManager.getEntityManager().notifyEntityMoved(this, false);        
        
        // TODO at the moment this request does not apply a local change, rather
        // it relies on the MOVED message sent by the server as a result of this
        // request to actually move the local entity. Obviously needs updating
        // so a local change is applied.
        
        // TODO throttle sends, we should only send so many times a second.
        cellChannelConnection.send(
                EntityMessage.newMoveRequestMessage(getCellID(), 
                                                    transform.getTranslation(null), 
                                                    transform.getRotation(null)),
                new ResponseListener() {

                    public void responseReceived(ResponseMessage response) {
                        logger.warning("Not Implemented - Entity Move request got response from server");
                    }
                });
    }
    
    /**
     * Called when a message arrives from the server requesting that the
     * cell be moved.
     * @param transform
     */
    protected void serverMoveRequest(CellTransform transform) {
        setTransform(transform);
        EntityManager.getEntityManager().notifyEntityMoved(this, true);
        notifyServerCellMoveListeners(transform);
    }
    
    
    
    /**
     * Listen for move events from the server
     * @param listener
     */
    public void addServerCellMoveListener(CellMoveListener listener) {
        if (serverMoveListeners==null) {
            serverMoveListeners = new ArrayList();
        }
        serverMoveListeners.add(listener);
    }
    
    /**
     * Remove the server move listener.
     * @param listener
     */
    public void removeServerCellMoveListener(CellMoveListener listener) {
        if (serverMoveListeners!=null) {
            serverMoveListeners.remove(listener);
        }
    }
    
    /**
     * Notify any serverMoveListeners that the cell has moved
     * 
     * @param transform
     */
    private void notifyServerCellMoveListeners(CellTransform transform) {
        if (serverMoveListeners==null)
            return;
        
        for(CellMoveListener listener : serverMoveListeners) {
            listener.cellMoved(transform);
        }
    }
    
    @ExperimentalAPI
    public interface CellMoveListener {
        public void cellMoved(CellTransform transform);
    }
    
    @ExperimentalAPI
    public interface CellMoveRefusedListener {
        /**
         * Notification from the server that the requested move was
         * not possible. The cell should be positioned with the actualTransform
         * transform.
         * 
         * @param requestedTransform
         * @param reason
         * @param actualTransform
         */
        public void moveRefused(CellTransform requestedTransform, int reason, CellTransform actualTransform);
    }

    /**
     * @{inheritDoc}
     */
    public void handleMessage(CellMessage message) {
        if (message instanceof EntityMessage) {
            EntityMessage msg = (EntityMessage)message;
            switch(msg.getActionType()) {
                case MOVED :
                    serverMoveRequest(new CellTransform(msg.getRotation(), msg.getTranslation()));
                    break;
                default :
                    logger.warning("Received unimplemented message "+message.getClass().getName());
            }
        }
    }

}
