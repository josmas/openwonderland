/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.setup;

import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.cell.setup.spi.CellExtensionTypeSPI;

/**
 *
 * @author jordanslott
 */
public class JMEColladaCellExtensionType implements CellExtensionTypeSPI {

    public String[] getSupportedExtensions() {
        return new String[] { "dae" };
    }

    public BasicCellSetup getCellSetup(String extension, String uri) {
        JMEColladaCellSetup setup = new JMEColladaCellSetup();
        setup.setModel(uri);
        return setup;
    }

}
