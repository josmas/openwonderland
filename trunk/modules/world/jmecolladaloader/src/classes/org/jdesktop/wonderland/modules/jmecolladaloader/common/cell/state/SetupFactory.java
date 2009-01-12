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
package org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state;

import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.spi.CellExtensionTypeSPI;

/**
 *
 * @author paulby
 */
public class SetupFactory implements CellExtensionTypeSPI {

    public String[] getSupportedExtensions() {
        return new String[] {"dae"};
    }

    public CellServerState getCellSetup(String extension, String uri) {
        JMEColladaCellServerState ret = new JMEColladaCellServerState();
        ret.setModel(uri);
        return ret;
    }

}
