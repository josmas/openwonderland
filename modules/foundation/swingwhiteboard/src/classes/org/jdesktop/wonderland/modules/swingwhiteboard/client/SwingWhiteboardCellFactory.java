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
package org.jdesktop.wonderland.modules.swingwhiteboard.client;

import org.jdesktop.wonderland.common.cell.state.CellServerState;
import java.awt.Image;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.SwingWhiteboardCellServerState;

/**
 * The cell factory for the swing whiteboard
 */
public class SwingWhiteboardCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState() {
        // Create a setup with some default values
        SwingWhiteboardCellServerState cellServerState = new SwingWhiteboardCellServerState();

        return (T) cellServerState;
    }

    public String getDisplayName() {
        return "Swing Whiteboard";
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
