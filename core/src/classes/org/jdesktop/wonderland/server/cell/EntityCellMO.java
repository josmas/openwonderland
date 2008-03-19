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
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.util.ArrayList;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMoveMessage;

/**
 * For cells that are expected to move frequently
 * 
 * @author paulby
 */
@ExperimentalAPI
public class EntityCellMO extends CellMO {

    private ArrayList<ManagedReference> listeners = null;
    
    @Override
    public void setTransform(CellTransform transform) {
        super.setTransform(transform);
        
        // Notify listeners
        if (listeners!=null) {
            for(ManagedReference listenerRef : listeners)
                listenerRef.getForUpdate(CellMoveListener.class).cellMoved(this, transform);
        }
        
//        if (cellChannel!=null)
//            cellChannel.send(new CellMoveMessage(transform).getBytes());
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
            listeners = new ArrayList<ManagedReference>();
        
        listeners.add(AppContext.getDataManager().createReference(listener));
    }
    
    /**
     * Remove the CellMoveListener
     * 
     * @param listener
     */
    public void removeCellMoveListener(CellMoveListener listener) {
        if (listeners!=null)
            listeners.remove(AppContext.getDataManager().createReference(listener));
    }
       
    public interface CellMoveListener extends ManagedObject {
        public void cellMoved(EntityCellMO cell, CellTransform transform);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    protected void openChannel() {
        // Moveable Cells always have a channel. CellTransform updates (moves)
        // are sent on this channel.
        openCellChannel();
    }
}
