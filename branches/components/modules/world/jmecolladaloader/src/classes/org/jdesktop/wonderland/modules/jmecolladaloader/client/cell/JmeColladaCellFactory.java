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

package org.jdesktop.wonderland.modules.jmecolladaloader.client.cell;

import java.util.Set;
import org.jdesktop.wonderland.client.media.cell.CellConfigPanel;
import org.jdesktop.wonderland.client.media.cell.CellFactory;
import org.jdesktop.wonderland.client.media.cell.CellPaletteInfo;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.setup.JMEColladaCellSetup;

/**
 * A factory that generates a setup class for the kmz loader
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class JmeColladaCellFactory implements CellFactory {

    public String[] getExtensions() {
        return new String[] { "kmz" };
    }

    public int getDetailLevels() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<CellConfigPanel> getCellConfigPanels(int level) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends BasicCellSetup> T getDefaultCellSetup() {
        return (T) new JMEColladaCellSetup();
    }

    public CellPaletteInfo getCellPaletteInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
