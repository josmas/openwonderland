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

package org.jdesktop.wonderland.common.cell.setup;

import java.util.Iterator;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.setup.spi.CellExtensionTypeSPI;
import sun.misc.Service;

/**
 * The CellExtensionTypeFactory class generates a new cell setup class given
 * the file name extension of an asset and the asset's uri. 
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellExtensionTypeFactory {
    
    /* The Logger for this class */
    private static Logger logger = Logger.getLogger(CellExtensionTypeFactory.class.getName());
     
    /**
     * Returns an instance of the setup class given the extension type and
     * media uri.
     * 
     * @param extension The extension of the asset (e.g. "dae", "jpg")
     * @param uri The full uri of the asset
     * @return A new setup class for the cell that renders the asset
     */
    public static BasicCellSetup getCellSetup(String extension, String uri) {
        CellExtensionTypeSPI spi = getExtensionTypeProvider(extension);
        return spi.getCellSetup(extension, uri);
    }
    
    /**
     * Finds the service provide class (that implements CellExtensionTypeSPI)
     * that supports the given extension. This method returns the first one it
     * finds.
     */
    private static CellExtensionTypeSPI getExtensionTypeProvider(String extension) {
        Iterator<CellExtensionTypeSPI> it = Service.providers(CellExtensionTypeSPI.class);
        while (it.hasNext() == true) {
            CellExtensionTypeSPI spi = it.next();
            String[] exts = spi.getSupportedExtensions();
            if (exts != null) {
                for (String ext : exts)
                   if (ext.equals(extension) == true) {
                       return spi;
                   }
            }
        }
        return null;
    }
}
