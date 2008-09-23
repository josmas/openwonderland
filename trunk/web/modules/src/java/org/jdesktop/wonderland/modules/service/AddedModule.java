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
import java.io.IOException;
import org.jdesktop.wonderland.modules.archive.ArchiveModule;

/**
 * TBD
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AddedModule extends ArchiveModule {

    /**
     * Constructor, takes the added module file object. Throws IOException upon
     * general I/O error reading the added module.
     * 
     * @param file The File object of the added module
     * @throw IOException Upon general I/O error
     */
    public AddedModule(File file) throws IOException {
        super(file);
    }
    
    /**
     * Constructor, takes the added module directory and its name (without the
     * .jar extension). Throws IOException upon general I/O error reading the
     * added module.
     * 
     * @param root The directory in which the archive module is contained
     * @param name The name of the module (witnout the .jar extension)
     * @throw IOException Upon general I/O error reading the module
     */
    public AddedModule(File root, String name) throws IOException {
        super(new File(root, name + ".jar"));
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
        File file = new File(dir, uniqueName + ".jar");
        return file.exists() == true && file.isDirectory() == false && file.isHidden() == false;
    }
    
    /**
     * Returns true if the given file is potentially a valid module, false if
     * not.
     * 
     * @param file The File to check if it is a potentially valid module
     * @return True If the file is potentially valid, false if not.
     */
    public static boolean isValidFile(File file) {
        return file.getName().endsWith(".jar") && file.isDirectory() == false && file.isHidden() == false;
    }
}
