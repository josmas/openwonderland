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
 * Message sent to add a cell hierarchy. This (for now) specifically assumes a
 * cell that is based upon some model (asset uri).
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class CellCreateMessage extends CellEditMessage {
    /** The ID of the cell of the parent */
    private CellID parentCellID;
   
    /** The uri of the asset for the new cell */
    private String assetURI;
    
    /**
     * Create a new cell message to the given cellID of the parent and uri of
     * asset to associated with the new cell.
     * 
     * @param parentID the id of the parent cell
     */
    public CellCreateMessage(CellID parentCellID, String assetURI) {
        super(EditType.CREATE_CELL);
        this.parentCellID = parentCellID;
        this.assetURI = assetURI;
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
}
