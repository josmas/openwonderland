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

/**
 * An interface implemented by cells that allow their properties to be edited
 * by a GUI properties panel. This class must be annotated with @CellProperties
 * and is displayed by a dialog container.
 * <p>
 * The GUI properties panel has a well-defined life-cycle. When it is about to
 * be displayed, the container invokes the refresh() method on this interface.
 * When the "Apply" action is selected by the user, the apply() method is
 * invokved, and when the "Close" action is selected by the user, the close()
 * method is invoked.
 * <p>
 * The specific actions the CellPropertiesSPI class takes when these three
 * methods are invoked is implementation specific. If the refresh() method is
 * invoked, the class should update the state of the GUI according to the state
 * stored in the Cell.
 * <p>
 * It is the responsibility of the class that implements this interface to
 * properly update the state of the Cell when apply() is invoked. It can either
 * interact with the Cell interface directly, or update the state of the cell
 * via methods on CellPropertiesEditor.
 * <p>
 * A CellPropertiesSPI class make also choose to immediately update the values
 * of the Cell when the GUI is changed, and not only when apply() is invoked.
 * <p>
 * When the close() method is called, the CellPropertiesSPI class should revert
 * all values in the state back to the values at the last apply(), if it so
 * chooses.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public interface PropertiesFactorySPI {

    /**
     * Returns a human-readable display name of the panel. This name will be
     * used to identify the panel amongst other panel in the edit dialog.
     *
     * @return The name of the configuration panel
     */
    public String getDisplayName();

    /**
     * Sets the cell properties editor containing this individual property
     * sheet.
     *
     * @param editor A CellProperties Editor object
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor);

    /**
     * Returns a panel to be used in the properties editing dialog.
     *
     * @return A JPanel
     */
    public JPanel getPropertiesJPanel();

    /**
     * Instructs the GUI to refresh its values against the currently set values
     * in the state of the Cell. This method is typically called when the
     * properties panel is first displayed.
     */
    public void refresh();

    /**
     * Applies the values current set in the properties GUI panel to the state
     * of the Cell.
     */
    public void apply();

    /**
     * Tells the properties GUI panel that it is being closed. The panel should
     * revert any intermediate changes it made to the state of the Cell after
     * the last time apply() was invoked.
     */
    public void close();
}
