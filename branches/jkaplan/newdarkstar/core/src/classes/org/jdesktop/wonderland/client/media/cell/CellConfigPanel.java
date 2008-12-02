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

import javax.swing.JPanel;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;

/**
 * The CellConfigPanel interface represents a panel that lets users edit some
 * aspect of a cell's setup information.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface CellConfigPanel {

    /**
     * Returns a human-readable display name of the panel. This name will be
     * used to identify the panel amongst other panel in the edit dialog.
     * 
     * @return The name of the configuration panel
     */
    public String getDisplayName();
    
    /**
     * Returns the Swing panel that displays a collection of GUI controls that
     * lets users configure some aspect of a cell's setup information.
     * 
     * @return The configuration panel
     */
    public JPanel getConfigJPanel();
    
    /**
     * Given a setup class, updates the elements in the configuration panel
     * to relect the values in the setup class.
     * 
     * @param setup Use values in this setup class to update the GUI panel
     */
    public <T extends BasicCellSetup> void updateGUI(T setup);
    
    /**
     * Given a setup class, updates the values in the class based upon the
     * currently set values in the GUI configuration panel.
     * 
     * @param setup Update the setup class with values set in the GUI panel
     */
    public <T extends BasicCellSetup> void updateCellSetup(T setup);
}
