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

import org.jdesktop.wonderland.client.cell.registry.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.CellPaletteInfo;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.sample.common.SampleCellSetup;

/**
 * The cell factory for the sample cell.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class SampleCellFactory implements CellFactory {

    public String[] getExtensions() {
        return new String[] {};
    }


    public <T extends CellServerState> T getDefaultCellSetup() {
        return (T)new SampleCellSetup();
    }

    public CellPaletteInfo getCellPaletteInfo() {
        return new SampleCellPaletteInfo();
    }

}
