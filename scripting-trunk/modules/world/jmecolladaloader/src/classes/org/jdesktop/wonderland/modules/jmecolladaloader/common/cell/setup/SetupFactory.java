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
package org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.setup;

import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.cell.setup.spi.CellExtensionTypeSPI;

/**
 *
 * @author paulby
 */
public class SetupFactory implements CellExtensionTypeSPI {

    public String[] getSupportedExtensions() {
        return new String[] {"dae"};
    }

    public BasicCellSetup getCellSetup(String extension, String uri) {
        JMEColladaCellSetup ret = new JMEColladaCellSetup();
        ret.setModel(uri);
        return ret;
    }

}
