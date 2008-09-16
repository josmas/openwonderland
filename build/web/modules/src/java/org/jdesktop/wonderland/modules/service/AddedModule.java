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

package org.jdesktop.wonderland.modules.service;

import java.io.File;
import org.jdesktop.wonderland.modules.archive.ArchiveModule;

/**
 * TBD
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AddedModule extends ArchiveModule {

    /**
     * Constructor TBD
     * @param root
     */
    public AddedModule(File root, String name) {
        super(root, name);
    }
    
    /**
     * Returns true if the given file is potentially a valid module, false if
     * not.
     * 
     * @param file The File to check if it is a potentially valid module
     * @return True If the file is potentially valid, false if not.
     */
    public static boolean isValidFile(File file) {
        System.out.println("module name: " + file.getName());
        return file.getName().endsWith(".jar");
    }
}
