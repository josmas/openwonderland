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
package org.jdesktop.wonderland.modules.sample.client;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.sample.common.SampleCellServerState;

/**
 * The cell factory for the sample cell.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
@CellFactory
public class SampleCellFactory implements CellFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/sample/client/resources/Bundle");

    public String[] getExtensions() {
        return new String[]{};
    }

    public <T extends CellServerState> T getDefaultCellServerState(
            Properties props) {
        SampleCellServerState state = new SampleCellServerState();
        state.setShapeType("BOX");

        // Give the hint for the bounding volume for initial Cell placement
        BoundingBox box = new BoundingBox(Vector3f.ZERO, 2, 2, 2);
        BoundingVolumeHint hint = new BoundingVolumeHint(true, box);
        state.setBoundingVolumeHint(hint);

        return (T) state;
    }

    public String getDisplayName() {
        return BUNDLE.getString("Sample_Cell");
    }

    public Image getPreviewImage() {
        URL url = SampleCellFactory.class.getResource(
                "resources/sample_preview.jpg");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
