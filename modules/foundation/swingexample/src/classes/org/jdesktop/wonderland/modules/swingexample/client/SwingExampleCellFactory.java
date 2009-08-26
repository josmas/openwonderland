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
package org.jdesktop.wonderland.modules.swingexample.client;

import java.awt.Image;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.appbase.client.swing.SwingCellFactoryUtils;
import org.jdesktop.wonderland.modules.swingexample.common.cell.SwingExampleCellServerState;

/**
 * The cell factory for the Swing Example.
 * 
 * @author Paul Byrne, deronj
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
@CellFactory
public class SwingExampleCellFactory implements CellFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/swingexample/client/Bundle");

    public String[] getExtensions() {
        return new String[]{};
    }

    public <T extends CellServerState> T getDefaultCellServerState(
            Properties props) {
        SwingExampleCellServerState state = new SwingExampleCellServerState();

        // Minor Optimization
        SwingCellFactoryUtils.skipSystemInitialPlacement(state);

        return (T) state;
    }

    public String getDisplayName() {
        return BUNDLE.getString("Swing_Example");
    }

    public Image getPreviewImage() {
        return null;
    }
}
