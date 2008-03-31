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
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.EntityMessage;

/**
 * A cell that can move
 * 
 * @author paulby
 */
public class EntityCell extends Cell implements ChannelCell {
    private CellHandler cellClient;
    
    private static Logger logger = Logger.getLogger(EntityCell.class.getName());
    private ArrayList<CellMoveListener> serverMoveListeners = null;
    
    public EntityCell(CellID cellID) {
        super(cellID);
        cellClient = CellHandler.getCellClient();
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
    
    public interface CellMoveListener {
        public void cellMoved(CellTransform transform);
    }
    
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

    /**
     * @{inheritDoc}
     */
    public void send(CellMessage message) {
        ChannelCellAdapter.send(message);
    }
    
}
