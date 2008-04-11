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
package org.jdesktop.wonderland.common.cell.messages;

import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * A message indicating a cell has moved.
 * 
 * @author paulby
 */
@InternalAPI
public class CellHierarchyMoveMessage extends CellHierarchyMessage {
    
    private BoundingVolume localBounds;
    private CellTransform cellTransform;
    
    /**
     * Creates a new instance of CellHierarchyMessage 
     *
     * 
     * @param cellClassName Fully qualified classname for cell
     */
    public CellHierarchyMoveMessage(BoundingVolume localBounds, 
                                CellID cellID, 
                                CellTransform cellTransform) {
        this.msgType = ActionType.MOVE_CELL;
        this.cellID = cellID;
        this.localBounds = localBounds;
        this.cellTransform = cellTransform;
    }
    
    public CellHierarchyMoveMessage() {
        msgType = ActionType.MOVE_CELL;
    }
}
