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
package org.jdesktop.wonderland.modules.xremwin.client;

import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.xremwin.common.cell.AppCellXrwServerState;

/**
 * A cell factory which launches arbitrary X11 apps.
 * 
 * @author deronj
 */
@CellFactory
public class RunXAppCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    // TODO: jordan: how does he handle a null return?
    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {

        // Show dialog to query values 
        RunXAppDialog dialog = new RunXAppDialog(JmeClientMain.getFrame().getFrame(), true);
        dialog.setLocationRelativeTo(JmeClientMain.getFrame().getFrame());
        dialog.setVisible(true);

        if (!dialog.succeeded()) {
            return null;
        }

        AppCellXrwServerState serverState = new AppCellXrwServerState();
        serverState.setAppName(dialog.getAppName());
        serverState.setCommand(dialog.getCommand());
        serverState.setLaunchLocation("server");

        return (T) serverState;
    }

    public String getDisplayName() {
        return "Run X11 App";
    }

    public Image getPreviewImage() {
        // TODO
        return null;
    }
}
