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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Global services for client cells. The manager will only
 * report information on currently loaded Cells.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class CellManager {

    private static CellManager cellManager = null;
    
//    private Set<CellMoveListener> moveListeners=Collections.synchronizedSet(new LinkedHashSet());            
;
    
    private CellManager() {
    }
    
    public static CellManager getCellManager() {
        if (cellManager==null)
            cellManager = new CellManager();
        return cellManager;
    } 
    
    /**
     * Called by the MovableCell to notify the system that it has moved.
     * @param cell
     * @param fromServer
     */
//    void notifyCellMoved(Cell cell, boolean fromServer) {
//        for(CellMoveListener listener : moveListeners)
//            listener.cellMoved(cell, fromServer);
//    }
    
//    /**
//     * Add a listener that will be notified of entity cell movement
//     * @param listener
//     */
//    public synchronized void addCellMoveListener(CellMoveListener listener) {
//        moveListeners.add(listener);
//    }
//    
//    /**
//     * Remove the specified entity cell listener
//     * @param listener
//     */
//    public synchronized void removeCellMoveListener(CellMoveListener listener) {
//        moveListeners.remove(listener);
//    }
    
//    @ExperimentalAPI
//    public interface CellMoveListener {
//        /**
//         * Notification that an MovableCell has moved. 
//         * @param cell the cell that moved
//         * @param fromServer, if true then the move came from the server, otherwise
//         * the move originated on this client
//         */
//        public void cellMoved(Cell cell, boolean fromServer);
//    }
}
