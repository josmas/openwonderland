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

import org.jdesktop.wonderland.common.cell.state.CellServerState;

/**
 * A CellFactory class is responsible for generating the necessary information
 * to generate a new cell. This includes:
 * <ol>
 * <li>A default cell setup class.
 * <li>A display name and image to be used in a palette of cell types.
 * <li>A list of file extensions which can be rendered by this cell type.
 * </ol>
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface CellFactory {

    /**
     * Returns an array of extension file types supported by this cell. The
     * cell can handle media with these extensions. The file extensions are
     * case insensitive.
     * 
     * @return An array of supported file extension (e.g. 'jpg', 'dae')
     */
    public String[] getExtensions();
    
    /**
     * Returns a default cell setup class for this cell type.
     * 
     * @return A cell setup class with default values
     */
    public <T extends CellServerState> T getDefaultCellSetup();
    
    /**
     * Returns the information necessary to display the cell in the world
     * assembler palette.
     * 
     * @return A CellPaletteInfo class
     */
    public CellPaletteInfo getCellPaletteInfo();
}
