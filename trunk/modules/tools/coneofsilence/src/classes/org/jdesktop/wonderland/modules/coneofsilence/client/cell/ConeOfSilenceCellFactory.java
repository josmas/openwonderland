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
package org.jdesktop.wonderland.modules.coneofsilence.client.cell;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.coneofsilence.common.ConeOfSilenceCellServerState;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;

/**
 * The cell factory for the sample cell.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellFactory
public class ConeOfSilenceCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        // Create a setup with some default values
        ConeOfSilenceCellServerState cellServerState = new ConeOfSilenceCellServerState();

	cellServerState.setName("COS");
	cellServerState.setFullVolumeRadius((float) 1.5);

        Vector3f axis = new Vector3f((float) 1, (float) 0, (float) 0);
        /*
         * Try rotating 45 degrees to see what that does.
         */
        //cellServerState.setRotation(new Rotation(axis, (float) Math.PI / 4));

        Logger.getLogger(ConeOfSilenceCellFactory.class.getName()).warning("COS!!!!");
        return (T) cellServerState;
    }

    public String getDisplayName() {
        return "Cone Of Silence";
    }

    public Image getPreviewImage() {
        URL url = ConeOfSilenceCellFactory.class.getResource("resources/coneofsilence_preview.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
