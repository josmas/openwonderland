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

package org.jdesktop.wonderland.modules.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import org.apache.commons.io.FileUtils;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleArtResource;
import org.jdesktop.wonderland.modules.ModuleChecksums;
import org.jdesktop.wonderland.modules.ModuleInfo;
import org.jdesktop.wonderland.modules.ModulePlugin;
import org.jdesktop.wonderland.modules.ModuleRepository;
import org.jdesktop.wonderland.modules.ModuleRequires;
import org.jdesktop.wonderland.modules.ModuleResource;

/**
 * The FileModule class extends the Module abstract base class and represents
 * all modules that exist as a collection of directories and files on disk
 * representing the structure of the module.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class FileModule extends Module {
    /* The root of the module directory structure on disk */
    private File root = null;

    
    /** Default constructor, takes a reference to the module directory root */
    public FileModule(File root, String name) {
        super();
        this.root = new File(root, name);
    }
    
    /**
     * Returns the file root for the module.
     * 
     * @return The File root for the module
     */
    public File getRoot() {
        return this.root;
    }
    
    /**
     * Returns the name of the module given its file object.
     * 
     * @param file The file pointing to the module
     * @return The name of the module
     */
    public static String getModuleName(File file) {
        /* Just return the name of the file */
        return file.getName();
    }
    
    /**
     * Returns an input stream for the given resource, null upon error.
     *
     * @param resource A resource contained within the archive
     * @return An input stream to the resource
     */
    public InputStream getInputStreamForResource(ModuleResource resource) {
        try {
            String resourcePath = Module.MODULE_ART + "/" + resource.getPathName();
            File entry = new File(this.root, resourcePath);
            return new FileInputStream(entry);
        } catch (java.io.IOException excp) {
            // print stack trace
            return null;
        }
    }
    
    /**
     * Returns an input stream for the given JAR file from a plugin, null
     * upon error
     * 
     * @param name The name of the plugin
     * @param jar The name of the jar file
     * @param type The type of the jar file (CLIENT, SERVER, COMMON)
     */
    public InputStream getInputStreamForPlugin(String name, String jar, String type) {
        try {
            String path = Module.MODULE_PLUGINS + "/" + name + "/" + type + jar;
            File entry = new File(this.root, path);
            return new FileInputStream(entry);
        } catch (java.io.IOException excp) {
            // print stack trace
            return null;
        }
    }

    @Override
    public ModuleInfo getModuleInfo() {
        try {
            /* Fetch the entry, return null if it does not exist */
            File entry = new File(root, Module.MODULE_INFO);
            return ModuleInfo.decode(new FileReader(entry));
        } catch (java.lang.Exception excp) {
            // print stack trace
            return null;
        }
    }

    @Override
    public ModuleRequires getModuleRequires() {
         try {
            /* Fetch the entry, return null if it does not exist */
            File entry = new File(root, Module.MODULE_REQUIRES);
            return ModuleRequires.decode(new FileReader(entry));
        } catch (java.lang.Exception excp) {
            // print stack trace
            return null;
        }
    }

    @Override
    public ModuleRepository getModuleRepository() {
         try {
            /* Fetch the entry, return null if it does not exist */
            File entry = new File(root, Module.MODULE_REPOSITORY);
            return ModuleRepository.decode(new FileReader(entry));
        } catch (java.lang.Exception excp) {
            // print stack trace
            return null;
        }
    }

    
    @Override
    public ModuleChecksums getModuleChecksums() {
        try {
            /* Fetch the entry, return null if it does not exist */
            File entry = new File(root, Module.MODULE_CHECKSUMS);
            return ModuleChecksums.decode(new FileReader(entry));
        } catch (java.lang.Exception excp) {
            // print stack trace
            return null;
        }
    }
    
    @Override
    public Collection<String> getModuleArtResources() {
        /* Find the "art/" subdirectory and recursively list */
        File artFile = new File(root, Module.MODULE_ART);
        if (artFile.exists() == false || artFile.isDirectory() == false) {
            // print error message
            return null;
        }
        return FileModuleUtil.listModuleArt(artFile, artFile);
    }

    @Override
    public ModuleArtResource getModuleArtResource(String path) {
        File file = new File(this.root, Module.MODULE_ART + "/" + path);
        if (file.exists() == true && file.isFile() == true && file.canRead() == true) {
            return new ModuleArtResource(path);
        }
        return null;
    }

    @Override
    public Collection<String> getModuleWFSs() {
        /* Find the "wfs/" subdirectory and list just the topmost entries */
        File wfsFile = new File(root, Module.MODULE_WFS);
        if (wfsFile.exists() == false || wfsFile.isDirectory() == false) {
            // print error message
            return null;
        }
        LinkedList<String> wfsList = new LinkedList<String>();
        
        /* List all of the files, take only directories ending in -wfs */
        File[] files = wfsFile.listFiles();
        for (File file : files) {
            /* If a directory, then recursively descend and append */
            String name = file.getName();
            if (file.isDirectory() == true && file.isHidden() == false && name.endsWith("-wfs") == true) {
                /* Strip off the name of the wfs from the path */
                name = name.substring(0, name.length() - 4);
                wfsList.addLast(name);
            } 
        }
        return wfsList;
    }

    @Override
    public Collection<String> getModulePlugins() {
         /* Find the "plugins/" subdirectory and list just the topmost entries */
        File pluginFile = new File(root, Module.MODULE_PLUGINS);
        if (pluginFile.exists() == false || pluginFile.isDirectory() == false) {
            // print error message
            return null;
        }
        LinkedList<String> pluginList = new LinkedList<String>();
        
        /* List all of the files, take only directories ending in -wfs */
        File[] files = pluginFile.listFiles();
        for (File file : files) {
            /* If a directory and is not hidden then is it ok */
            if (file.isDirectory() == true && file.isHidden() == false) {
              pluginList.addLast(file.getName());
            } 
        }
        return pluginList;
    }

    @Override
    public ModulePlugin getModulePlugin(String name) {
        /* Filter for only files ending in .jar */
        JarFileFilter filter = new JarFileFilter();
        
        /* Construct the directory in which the module resides, check it exists */
        File file = new File(this.root, Module.MODULE_PLUGINS + "/" + name);
        if (file.exists() == false || file.canRead() == false || file.isDirectory() == false) {
            return null;
        }
        
        /* Fetch all of its jar entries in client/, common/, and server/. */
        File clientFile = new File(file, ModulePlugin.CLIENT_JAR);
        File serverFile = new File(file, ModulePlugin.SERVER_JAR);
        File commonFile = new File(file, ModulePlugin.COMMON_JAR);

        /* List each of the JAR files */
        String[] client = clientFile.list(filter);
        String[] server = serverFile.list(filter);
        String[] common = commonFile.list(filter);

        /* Make sure each are not null */
        client = (client != null) ? client : new String[]{};
        server = (server != null) ? server : new String[]{};
        common = (common != null) ? common : new String[]{};

        /* Create the ModulePlugin object, add, and continue */
        return new ModulePlugin(name, client, server, common);
    }

    @Override
    public boolean delete() {
        return FileUtils.deleteQuietly(this.root);
    }

    @Override
    public String getName() {
        /* Just return the name of the directory */
        return FileModule.getModuleName(this.root);
    }
}
