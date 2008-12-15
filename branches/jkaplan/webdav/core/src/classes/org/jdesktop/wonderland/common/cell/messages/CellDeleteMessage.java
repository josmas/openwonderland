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

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Message sent to delete a cell hierarchy.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class CellDeleteMessage extends CellEditMessage {
    /** The ID of the cell to delete */
    private CellID cellID;
    
    /**
     * Create a new cell delete message for the given cellID.
     * 
     * @param cellID the id of the cell
     */
    public CellDeleteMessage(CellID cellID) {
        super(EditType.DELETE_CELL);
        this.cellID = cellID;
    }
    
    /**
     * Get the ID of the cell of the parent
     * 
     * @return the parent cellID
     */
    public CellID getCellID() {
        return this.cellID;
    }
}
