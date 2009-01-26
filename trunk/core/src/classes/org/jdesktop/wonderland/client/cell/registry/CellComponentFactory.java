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

import javax.swing.JPanel;

/**
 * A CellComponentFactory class is responsible for generating the necessary
 * information to generate a new cell component. This includes:
 * <p>
 * <ol>
 * <li>A configuration GUI panel to allow a user to edit the setup data for a
 * cell component.
 * <li>The Class of the cell component setup class.
 * <li>A display name and description to be used in the list of cell components.
 * </ol>
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface CellComponentFactory {
    
    /**
     * Returns a ConfigPanel object that help configure the cell component-
     * specific parameters.
     * 
     * @return A configuration panel component
     */
    public JPanel getConfigPanel();

    /**
     * Returns the class of the cell component setup class
     *
     * @return The cell component setup Class
     */
    public Class getCellComponentSetupClass();
    
    /**
     * Returns the display name of the component.
     * 
     * @return The human-readable name of the cell component
     */
    public String getDisplayName();

    /**
     * Returns a description of the component.
     *
     * @return A description of the cell component
     */
    public String getDescription();
}
