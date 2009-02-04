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
package org.jdesktop.wonderland.modules.microphone.client.cell;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState.FullVolumeArea;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState.ActiveArea;
import com.jme.math.Vector3f;
import java.awt.Image;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;

/**
 * The cell factory for the sample cell.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellFactory
public class MicrophoneCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState() {
        // Create a setup with some default values
        MicrophoneCellServerState cellServerState = new MicrophoneCellServerState();
        cellServerState.setName("MICROPHONE");
        cellServerState.setFullVolumeArea(new FullVolumeArea("BOX", 11.0, 0, 11.0));
	
        Origin origin = new Origin(new Vector3f(0F, 0F, 0F));

	cellServerState.setActiveArea(new ActiveArea(origin, "BOX", 2., 0., 2.));

        Logger.getLogger(MicrophoneCellFactory.class.getName()).warning("MICROPHONE!!!!");
        return (T) cellServerState;
    }

    public String getDisplayName() {
        return "Microphone";
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
