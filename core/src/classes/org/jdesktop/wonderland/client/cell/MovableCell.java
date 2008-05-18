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
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessageResponse;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A cell that can move
 * 
 * @author paulby
 */
@ExperimentalAPI
public class MovableCell extends Cell implements ChannelCell {
    private CellChannelConnection cellChannelConnection;
    
    private static Logger logger = Logger.getLogger(MovableCell.class.getName());
    private ArrayList<CellMoveListener> serverMoveListeners = null;
    
    public enum CellMoveSource { LOCAL, REMOTE }; // Do we need BOTH as well ?
    
    public MovableCell(CellID cellID) {
        super(cellID);
    }
    
    /**
     * A request from this client to move the cell. The cell we be moved locally
     * and the requested change sent to the server. If the server denies the move
     * the cell will be moved to a server provided location and the listener
     * will be called. The server will
     * notify all other clients of the new location.
     * 
     * @param transform the requrested transform
     * @param listener the listener that will be notified in the event the
     * system modifies this move (due to collision etc).
     */
    public void localMoveRequest(CellTransform transform, 
                                 final CellMoveModifiedListener listener) {
    
        CellManager.getCellManager().notifyCellMoved(this, false);        
        
        // make sure we are connected to the server
        if (cellChannelConnection == null || 
                cellChannelConnection.getStatus() != ClientConnection.Status.CONNECTED)
        {
            logger.warning("Cell channel not connected when moving cell " +
                           getCellID());
            return;
        }
        
        // TODO at the moment this request does not apply a local change, rather
        // it relies on the MOVED message sent by the server as a result of this
        // request to actually move the local cell. Obviously needs updating
        // so a local change is applied.
        
        // TODO throttle sends, we should only send so many times a second.
        if (listener!=null) {
            cellChannelConnection.send(
                MovableMessage.newMoveRequestMessage(getCellID(), 
                                                    transform.getTranslation(null), 
                                                    transform.getRotation(null)),
                new ResponseListener() {

                    public void responseReceived(ResponseMessage response) {
                        MovableMessageResponse msg = (MovableMessageResponse)response;
                        CellTransform requestedTransform = null;
                        CellTransform actualTransform = new CellTransform(msg.getRotation(), msg.getTranslation());
                        int reason = 1;
                        listener.moveModified(requestedTransform, reason, actualTransform);
                    }
                });
        } else {
            cellChannelConnection.send(
                MovableMessage.newMoveRequestMessage(getCellID(), 
                                                    transform.getTranslation(null), 
                                                    transform.getRotation(null)));
        }
    }
    
    /**
     * A request from this client to move the cell. The cell we be moved locally
     * and the requested change sent to the server. If the server denies the move
     * the cell will be moved to a server provided location. The server will
     * notify all other clients of the new location.
     * 
     * @param transform
     */
    public void localMoveRequest(CellTransform transform) {
        localMoveRequest(transform, null);
    }
    
    /**
     * Called when a message arrives from the server requesting that the
     * cell be moved.
     * @param transform
     */
    protected void serverMoveRequest(CellTransform transform) {
        setTransform(transform);
        CellManager.getCellManager().notifyCellMoved(this, true);
        notifyServerCellMoveListeners(transform, CellMoveSource.REMOTE);
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
    private void notifyServerCellMoveListeners(CellTransform transform, CellMoveSource source) {
        if (serverMoveListeners==null)
            return;
        
        for(CellMoveListener listener : serverMoveListeners) {
            listener.cellMoved(transform, source);
        }
    }
    
    @ExperimentalAPI
    public interface CellMoveListener {
        /**
         * Notification that the cell has moved. Source indicates the source of 
         * the move, local is from this client, remote is from the server.
         * 
         * @param transform
         * @param source
         */
        public void cellMoved(CellTransform transform, CellMoveSource source);
    }
    
    @ExperimentalAPI
    public interface CellMoveModifiedListener {
        /**
         * Notification from the server that the requested move was
         * not possible and a modified move took place instead. The cell should be positioned with the actualTransform
         * transform.
         * 
         * @param requestedTransform
         * @param reason
         * @param actualTransform
         */
        public void moveModified(CellTransform requestedTransform, int reason, CellTransform actualTransform);
    }

    /**
     * @{inheritDoc}
     */
    public void handleMessage(CellMessage message) {
        if (message instanceof MovableMessage) {
            MovableMessage msg = (MovableMessage)message;
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
    public void setCellChannelConnection(CellChannelConnection connection) {
        this.cellChannelConnection = connection;
    }
}
