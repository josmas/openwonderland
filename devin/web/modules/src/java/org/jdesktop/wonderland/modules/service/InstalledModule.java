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
import org.jdesktop.wonderland.modules.file.FileModule;

/**
 * The InstalledModule class represents a module that has been installed and
 * unpacked in the proper system directory of a running Wonderland server. An
 * installed module differs from a module that exists as a JAR file in several
 * ways:
 * <p>
 * 1. It contents are unpacked and expanded on disk.
 * 2. Any JAR that needs to be included in the server-side class path is placed
 *    within a special directory.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class InstalledModule extends FileModule {

    public InstalledModule(File root, String name) {
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
        return file.isDirectory() == true && file.isHidden() == false;
    }
}
