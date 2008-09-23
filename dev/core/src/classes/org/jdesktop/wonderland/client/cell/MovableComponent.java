/**
 * Project Wonderland
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
 * $State$
 */
package org.jdesktop.wonderland.client.cell;

import java.util.ArrayList;
import org.jdesktop.wonderland.common.cell.CellTransform;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessageResponse;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A component that provides cell movement
 * 
 * @author paulby
 */
@ExperimentalAPI
public class MovableComponent extends CellComponent {
    
    private static Logger logger = Logger.getLogger(MovableComponent.class.getName());
    private ArrayList<CellMoveListener> serverMoveListeners = null;
    private ChannelComponent channelComp;
    
    public enum CellMoveSource { LOCAL, REMOTE }; // Do we need BOTH as well ?
    
    private ChannelComponent.ComponentMessageReceiver msgReceiver;
    
    public MovableComponent(Cell cell) {
        super(cell);
        channelComp = cell.getComponent(ChannelComponent.class);
    }
    
    
    @Override
    public void setStatus(CellStatus status) {
         switch(status) {
            case DISK :
                if (msgReceiver!=null) {
                    channelComp.removeMessageReceiver(MovableMessage.class);
                    msgReceiver = null;
                }
                break;
             case BOUNDS : {
                 if (msgReceiver==null) {
                    msgReceiver = new ChannelComponent.ComponentMessageReceiver() {

                        public void messageReceived(CellMessage message) {
            //                System.out.println("messageReceived "+message);
                            MovableMessage mov = (MovableMessage)message;
                            serverMoveRequest(new CellTransform(mov.getRotation(), mov.getTranslation()));
                        }
                    };                    
                    channelComp.addMessageReceiver(MovableMessage.class, msgReceiver);
                 }
             }
        }
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
    
//        CellManager.getCellManager().notifyCellMoved(cell, false);  
        
        // make sure we are connected to the server
        if (channelComp == null || 
                channelComp.getStatus() != ClientConnection.Status.CONNECTED)
        {
            logger.warning("Cell channel not connected when moving cell " +
                           cell.getCellID());
            return;
        }
        
        // TODO at the moment this request does not apply a local change, rather
        // it relies on the MOVED message sent by the server as a result of this
        // request to actually move the local cell. Obviously needs updating
        // so a local change is applied.
        
        // TODO throttle sends, we should only send so many times a second.
        if (listener!=null) {
            channelComp.send(
                MovableMessage.newMoveRequestMessage(cell.getCellID(), 
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
            channelComp.send(
                MovableMessage.newMoveRequestMessage(cell.getCellID(), 
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
        cell.setLocalTransform(transform);
    }
    
    /**
     * Called when a message arrives from the server requesting that the
     * cell be moved.
     * @param transform
     */
    protected void serverMoveRequest(CellTransform transform) {
        cell.setLocalTransform(transform);
//        if (cell.getTransform()!=null)
//            System.out.println("serverMoveRequest "+cell.getTransform().getTranslation(null)+"  "+cell);
//        else
//            System.out.println("serverMoveRequest with null transform "+cell.getName());
//        CellManager.getCellManager().notifyCellMoved(cell, true);
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

}
