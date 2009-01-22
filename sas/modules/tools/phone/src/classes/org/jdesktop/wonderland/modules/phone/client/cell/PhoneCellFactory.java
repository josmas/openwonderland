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
package org.jdesktop.wonderland.modules.phone.client.cell;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState.Rotation;
import org.jdesktop.wonderland.modules.phone.common.PhoneCellServerState;
import com.jme.math.Vector3f;
import java.awt.Image;

/**
 * The cell factory for the sample cell.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class PhoneCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState() {
        // Create a setup with some default values
        PhoneCellServerState cellServerState = new PhoneCellServerState();
        cellServerState.setLocked(true);
        cellServerState.setSimulateCalls(false);
	cellServerState.setPhoneNumber("100");
	cellServerState.setPhoneLocation("Unknown location");
	cellServerState.setZeroVolumeRadius(.2);
	cellServerState.setFullVolumeRadius(.1);

        Vector3f axis = new Vector3f((float) 1, (float) 0, (float) 0);
        /*
         * Try rotating 45 degrees to see what that does.
         */
        cellServerState.setRotation(new Rotation(axis, (float) Math.PI / 4));

        Logger.getLogger(PhoneCellFactory.class.getName()).warning("Virtual Phone!!!!");
        return (T) cellServerState;
    }

    public String getDisplayName() {
        return "Virtual Phone";
    }

    public Image getPreviewImage() {
//        try {
////        URL url = ConeOfSilenceCellPaletteInfo.class.getResource("resources/sample_preview.jpg");
////        Logger.getLogger(ConeOfSilenceCellPaletteInfo.class.getName()).warning("INFO " + url.toString());
//            URL url = new URL("file:///Users/jordanslott/Desktop/sample_preview.jpg");
//            return Toolkit.getDefaultToolkit().createImage(url);
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(ConeOfSilenceCellPaletteInfo.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
        return null;
    }
}
