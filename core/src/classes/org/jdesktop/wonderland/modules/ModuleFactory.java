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

package org.jdesktop.wonderland.modules;

import java.io.File;
import org.jdesktop.wonderland.modules.archive.ArchiveModuleUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipFile;
import org.jdesktop.wonderland.modules.archive.ArchiveModule;
import org.jdesktop.wonderland.modules.file.FileModule;
import org.jdesktop.wonderland.modules.file.FileModuleUtil;
import org.jdesktop.wonderland.modules.memory.MemoryModule;

/**
 * The ModuleFactory class creates instances of Module classes. Methods in this
 * class are the only way to create these instances.
 * <p>
 * All methods on this class are static.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleFactory {
    /**
     * Opens an existing module given its archive file. This method reads the
     * module from the input stream and returns a Module object. This method
     * ignores invalid entries within the module, and potentially can return an
     * empty (but non-null) Module if the archive is not properly formatted.
     * <p>
     * @param zipFile The archive file
     */
    public static final Module open(ZipFile zipFile) {
        /* Fetch and parse the three XML files: info, requires, and repository */
        ModuleInfo info = ArchiveModuleUtil.parseModuleInfo(zipFile);
        if (info == null) {
            // print error message XXX
            return null;
        }
        ModuleRequires requires = ArchiveModuleUtil.parseModuleRequires(zipFile);
        ModuleRepository repository = ArchiveModuleUtil.parseModuleRepository(zipFile);
                
        /* Parse the module artwork */
        HashMap<String, ModuleArtResource> artwork = ArchiveModuleUtil.parseModuleArt(zipFile);

        /* Create a new module based upon what has been parsed */
        Module module = new ArchiveModule(zipFile);
        module.setModuleInfo(info);
        module.setModuleRequires(requires);
        module.setModuleRepository(repository);
        module.setModuleArtwork(artwork);
        
        return module;
    }
    
    /**
     * Opens an existing module given its directory on disk.
     * @param args
     * @throws java.io.IOException
     */
    public static final Module open(File file) {
        /* Fetch and parse the three XML files: info, requires, and repository */
        ModuleInfo info = FileModuleUtil.parseModuleInfo(file);
        if (info == null) {
            // print error message XXX
            return null;
        }
        
        ModuleRequires requires = FileModuleUtil.parseModuleRequires(file);
        ModuleRepository repository = FileModuleUtil.parseModuleRepository(file);
                
        /* Parse the module artwork */
        HashMap<String, ModuleArtResource> artwork = FileModuleUtil.parseModuleArt(file);

        /* Create a new module based upon what has been parsed */
        Module module = new FileModule(file);
        module.setModuleInfo(info);
        module.setModuleRequires(requires);
        module.setModuleRepository(repository);
        module.setModuleArtwork(artwork);

        return module;
    }
    
    /**
     * Create a new module in memory.
     */
    public static final Module create() {
        return new MemoryModule();
    }
    
    public static final void main(String args[]) throws IOException {
        ZipFile zipFile = new ZipFile("/Users/jordanslott/module/module-wlm.jar");
        ModuleFactory.open(zipFile);
        
        File file = new File("/Users/jordanslott/module");
        ModuleFactory.open(file);
    }
}
