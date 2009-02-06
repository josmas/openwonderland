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
package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.ConeOfSilenceComponentServerState;

/**
 * A factory to register the cone of silence component.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellComponentFactory
public class ConeOfSilenceComponentFactory implements CellComponentFactorySPI {

    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {
        return (T)new ConeOfSilenceComponentServerState();
    }

    public String getDisplayName() {
        return "Cone of Silence";
    }

    public String getDescription() {
        return "Surround any cell with a Cone of Silence";
    }
}
