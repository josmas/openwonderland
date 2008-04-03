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
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;
import java.util.ArrayList;
import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.EntityMessage;
import org.jdesktop.wonderland.common.cell.messages.EntityMessageResponse;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * For cells that are expected to move frequently
 * 
 * TODO - Do we need a listener that allows veto of a move request, don't
 * think so instead I suggest we would subclass to add veto capability
 * 
 * @author paulby
 */
public class EntityCellMO extends CellMO implements ChannelCellMO {

    private ArrayList<ManagedReference<CellMoveListener>> listeners = null;
    
    // cache the sender for sending to CellClients
    private WonderlandClientSender cellSender;
    
    @Override
    public void setTransform(CellTransform transform) {
        super.setTransform(transform);
        
        // Notify listeners
        if (listeners!=null) {
            notifyMoveListeners(transform);
        }

        if (isLive() && cellChannelRef != null) {
            cellSender.send(cellChannelRef.get(), 
                            EntityMessage.newMovedMessage(cellID, transform));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void openChannel() {
        defaultOpenChannel();
        
        // cache the sender for sending to cell clients.  This saves a
        // Darkstar lookup for every cell we want to send to.
        cellSender = WonderlandContext.getCommsManager().getSender(CellChannelConnectionType.CLIENT_TYPE);
    }
    
    /**
     * {@inheritDoc}
     */
    public void closeChannel() {
        defaultCloseChannel();
        cellSender=null;
    }  
    
    @Override
    public String getClientCellClassName(ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.client.cell.EntityCell";
    }
    
    public void messageReceived(WonderlandClientSender sender, 
                                ClientSession session,
                                CellMessage message ) {
        if (!(message instanceof EntityMessage)) {
            logger.severe("Incorrect message type "+message.getClass().getName());
        }
        
        EntityMessage ent = (EntityMessage)message;
        switch(ent.getActionType()) {
            case MOVE_REQUEST :
                setTransform(new CellTransform(ent.getRotation(), ent.getTranslation()));
                
                // Only need to send a response if the move can not be completed as requested
                sender.send(EntityMessageResponse.newMoveModifiedMessage(ent.getMessageID(), ent.getTranslation(), ent.getRotation()));
                break;
            case MOVED :
                logger.severe("Server should never receive MOVED messages");
                break;
        }
    }
    
    /**
     * Add a CellMoveListener. This listener is notified when the setTransform 
     * method is called. super.setTransform is called first, so the cell transform
     * will have been updated before the listener is called.
     * 
     * @param listener
     */
    public void addCellMoveListener(CellMoveListener listener) {
        if (listeners==null)
            listeners = new ArrayList<ManagedReference<CellMoveListener>>();
        
        listeners.add(AppContext.getDataManager().createReference(listener));
    }
    
    /**
     * Remove the CellMoveListener
     * @param listener
     */
    public void removeCellMoveListener(CellMoveListener listener) {
        if (listeners!=null)
            listeners.remove(AppContext.getDataManager().createReference(listener));
    }

    /**
     * Notify Listeners that this Entity has moved. Each listener is notified
     * in a separate task.
     * @param transform
     */
    private void notifyMoveListeners(final CellTransform transform) {
        TaskManager tm = AppContext.getTaskManager();
        
        for(final ManagedReference<CellMoveListener> listenerRef : listeners) {
            tm.scheduleTask(new Task() {

                public void run() throws Exception {
                    listenerRef.get().cellMoved(EntityCellMO.this, transform);
                }

            });
            
        }
    }
       
    /**
     * Listener inteface for cell movement
     */
    public interface CellMoveListener extends ManagedObject {
        public void cellMoved(EntityCellMO cell, CellTransform transform);
    }



}
