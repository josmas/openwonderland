/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.client.cell.properties.spi;

import javax.swing.JPanel;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

/**
 * An interface implemented by cells that allow their properties to be
 * edited in a dialog.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public interface CellPropertiesSPI {
    /**
     * Returns the Class of the cell's server-side state object
     *
     * @return The server-side cell state Class
     */
    public Class getServerCellStateClass();

    /**
     * Returns a human-readable display name of the panel. This name will be
     * used to identify the panel amongst other panel in the edit dialog.
     *
     * @return The name of the configuration panel
     */
    public String getDisplayName();

    /**
     * Returns a panel to be used in the properties editing dialog.
     *
     * @param editor The editor for all of the cell's properties
     * @return A JPanel
     */
    public JPanel getPropertiesJPanel(CellPropertiesEditor editor);

    /**
     * Given a server state class, updates the elements in the configuration panel
     * to relect the values in the state class.
     *
     * @param cellServerState Use values in this state class to update the GUI panel
     */
    public <T extends CellServerState> void updateGUI(T cellServerState);

    /**
     * Given a server state class, updates the values in the class based upon the
     * currently set values in the GUI configuration panel.
     *
     * @param cellServerState Update the state class with values set in the GUI panel
     */
    public <T extends CellServerState> void getCellServerState(T cellServerState);
}
