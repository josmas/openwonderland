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

package org.jdesktop.wonderland.client.media.cell;

import java.util.Set;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;

/**
 * A CellFactory class is responsible for generating the necessary information
 * to generate a new cell. This includes:
 * <p>
 * <ol>
 * <li>A set of configuration GUI panels to allow a user to edit the setup
 * data for a cell.
 * <li>A default cell setup class.
 * <li>A display name, category name, and image to be used in a palette of
 * cell types.
 * </ol>
 * <p>
 * The set of GUI configuration panels are organized into a series of levels,
 * of increasing complexity of configuration. The 0th level is the most basic
 * level. This class can define an artibtrary number of levels, as returns by
 * the getDetailLevels().
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface CellFactory {

    /**
     * Returns an array of extension file types supported by this cell factory.
     * 
     * @return An array of supported file extension (e.g. 'jpg', 'dae')
     */
    public String[] getExtensions();
    
    /**
     * Returns the maximum number of layers of details.
     * 
     * @return The number of layers of configuration panels
     */
    public int getDetailLevels();
    
    /**
     * Returns a Set of CellConfigPanel objects given the level of detail, which
     * must be between 0 and getDetailLevels() - 1.
     * 
     * @param level The level of detail of component
     * @return A set of configuration panel components at that level
     * @throw IndexOutOfBoundsException If level < 0 or level > getDetailsLevel() - 1
     */
    public Set<CellConfigPanel> getCellConfigPanels(int level);
    
    /**
     * Returns a default cell setup class for this cell type.
     * 
     * @return A cell setup class with default values
     */
    public <T extends BasicCellSetup> T getDefaultCellSetup();
    
    /**
     * Returns the information necessary to display the cell in the world
     * assembler palette.
     * 
     * @return A CellPaletteInfo class
     */
    public CellPaletteInfo getCellPaletteInfo();
}
