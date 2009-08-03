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

package org.jdesktop.wonderland.modules.sample.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import org.jdesktop.wonderland.client.cell.registry.CellPaletteInfo;

/**
 * The palette information for the sample cell.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class SampleCellPaletteInfo implements CellPaletteInfo {

    public String getDisplayName() {
        return "Sample Cell";
    }

    public Image getPreviewImage() {
        URL url = SampleCellPaletteInfo.class.getResource("resources/sample_preview.jpg");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}