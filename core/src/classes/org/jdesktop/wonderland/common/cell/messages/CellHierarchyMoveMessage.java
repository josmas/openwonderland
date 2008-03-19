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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.messages.PackHelper;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
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
    
    public void packObject(ObjectOutputStream out) throws IOException {
        cellID.put(out);
        PackHelper.writeBoundingVolume(out, localBounds);
        PackHelper.writeCellTransform(out, cellTransform);
    }
    
    public void unpackObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        cellID = CellID.value(in);
        localBounds = PackHelper.readBoundingVolume(in);
        cellTransform = PackHelper.readCellTransform(in);
    }
}
