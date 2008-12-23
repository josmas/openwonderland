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

package org.jdesktop.wonderland.client.cell.registry;

import java.awt.Image;



/**
 * The CellPaletteInfo class provides information necessary to list the cell
 * type in the world assembly palette of cell types.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface CellPaletteInfo {

    /**
     * Returns the human-readable display name of the cell type.
     * 
     * @return The name of the cell type
     */
    public String getDisplayName();
    
    /**
     * Returns an image preview of the cell type.
     * 
     * @return An image of the cell type
     */
    public Image getPreviewImage();
}
