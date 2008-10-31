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
import org.jdesktop.wonderland.common.messages.Message;

/**
 * Message sent to edit the cell hierarchy.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class CellEditMessage extends Message {
    /** The ID of the cell of the parent */
    private CellID parentCellID;

    /** The message type */
    private EditType editType;
   
    /** The uri of the asset for the new cell */
    private String assetURI;
    
    /** Enumeration of kinds of editing */
    public enum EditType { CREATE_CELL };
    
    /**
     * Create a new cell message to the given cellID of the parent.
     * 
     * @param parentID the id of the parent cell
     */
    public CellEditMessage(CellID parentCellID) {
        this.parentCellID = parentCellID;
        this.editType = EditType.CREATE_CELL;
    }
    
    /**
     * Get the ID of the cell of the parent
     * 
     * @return the parent cellID
     */
    public CellID getParentCellID() {
        return this.parentCellID;
    }
    
    public String getAssetURI() {
        return assetURI;
    }

    public void setAssetURI(String assetURI) {
        this.assetURI = assetURI;
    }

    public EditType getEditTpe() {
        return editType;
    }

    public void setEditTpe(EditType editType) {
        this.editType = editType;
    }
}
