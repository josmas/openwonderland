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
 * unpacked in the proper system directory of a running Wonderland server.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class InstalledModule extends FileModule {

    /**
     * Constructor, takes the installed module file object. Throws IOException
     * upon general I/O error reading the added module.
     * 
     * @param file The File object of the installed module
     * @throw IOException Upon general I/O error
     */
    public InstalledModule(File file) {
        super(file);
        
    }
    
    /**
     * Constructor, takes the base directory and name of the module as
     * arguments.
     * 
     * @param root The base directory of the module
     * @param name The unique name of the module
     */
    public InstalledModule(File root, String name) {
        super(new File(root, name));
    }
    
    /**
     * Returns true if the module exists in the given directory with the
     * given unique name, false if not.
     * 
     * @param dir The directory in which the module may exist
     * @param uniqueName The name of the module
     * @return True if the module exists, false if not
     */
    public static boolean isExists(File dir, String uniqueName) {
        File file = new File(dir, uniqueName);
        return file.exists() == true && file.isDirectory() == true && file.isHidden() == false;
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
