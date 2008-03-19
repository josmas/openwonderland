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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public class CellHierarchyUnloadMessage extends CellHierarchyMessage {
    
    /**
     * Creates a new instance of CellHierarchyMessage 
     *
     * 
     * @param cellClassName Fully qualified classname for cell
     */
    public CellHierarchyUnloadMessage(CellID cellID) {
        this.cellID = cellID;
        msgType = ActionType.UNLOAD_CELL;
    }
    
    public CellHierarchyUnloadMessage() {
        msgType = ActionType.UNLOAD_CELL;
    }
}
