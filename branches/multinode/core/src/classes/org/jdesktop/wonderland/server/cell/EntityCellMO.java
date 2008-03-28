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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.CellHandlerType;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.EntityMessage;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * For cells that are expected to move frequently
 * 
 * TODO - Should MoveListener notifications be scheduled in their
 * own task ? Also do we need a listener that allows veto of a move request, don't
 * think so instead I suggest we would subclass to add veto capability
 * @author paulby
 */
public class EntityCellMO extends CellMO {

    private ArrayList<ManagedReference<CellMoveListener>> listeners = null;
    
    // cache the sender for sending to CellClients
    private WonderlandClientSender cellSender;
    
    @Override
    public void setTransform(CellTransform transform) {
        super.setTransform(transform);
        
        // Notify listeners
        if (listeners!=null) {
            for(ManagedReference<CellMoveListener> listenerRef : listeners)
                listenerRef.getForUpdate().cellMoved(this, transform);
        }

        if (isLive() && getCellChannel() != null) {
            cellSender.send(getCellChannel(), 
                            EntityMessage.newMovedMessage(cellID, transform));
        }
    }
    
    @Override
    protected void openChannel() {
        defaultOpenChannel();
        
        // cache the sender for sending to cell clients.  This saves a
        // Darkstar lookup for every cell we want to send to.
        cellSender = WonderlandContext.getCommsManager().getSender(CellHandlerType.CLIENT_TYPE);
    }
    
    @Override
    public String getClientCellClassName() {
        return "org.jdesktop.wonderland.client.cell.EntityCell";
    }
    
    /**
     * Add a CellMoveListener. This listener is notified when the setTransform 
     * method is called. super.setTransform is called first, so the cell transform
     * will have been updated before the listener is called.
     * 
     * @param listener
     */
//    public void addCellMoveListener(CellMoveListener listener) {
//        if (listeners==null)
//            listeners = new ArrayList<ManagedReference>();
//        
//        listeners.add(AppContext.getDataManager().createReference(listener));
//    }
    
    /**
     * Remove the CellMoveListener
     * @param listener
     */
    public void removeCellMoveListener(CellMoveListener listener) {
        if (listeners!=null)
            listeners.remove(AppContext.getDataManager().createReference(listener));
    }
       
    public interface CellMoveListener extends ManagedObject {
        public void cellMoved(EntityCellMO cell, CellTransform transform);
    }

}
