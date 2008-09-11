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
import org.jdesktop.wonderland.modules.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.HashMap;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.modules.ModuleArtResource;
import org.jdesktop.wonderland.modules.service.ModuleManager;

/**
 * The ModuleFileUtil class contains a collection is utility routines to help
 * parse the module on disk.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class FileModuleUtil {

    /**
     * Returns the basic module info, given the module file root. Returns
     * null if the module's info file does not exist within the module.
     * <p>
     * @param root The module's root directory
     * @return The module's info, null if it does not exist
     */
    public static ModuleInfo parseModuleInfo(File root) {
        try {
            /* Fetch the entry, return null if it does not exist */
            File entry = new File(root, Module.MODULE_INFO);
            return ModuleInfo.decode(new FileReader(entry));
        } catch (java.io.IOException excp) {
            // print stack trace
            return null;
        } catch (java.lang.ClassCastException excp) {
            // print stack trace
            return null;
        } catch (java.lang.ArrayIndexOutOfBoundsException excp) {
            // print stack trace
            return null;
        } catch (javax.xml.bind.JAXBException excp) {
            // print stack trace
            return null;
        }
    }
    
    /**
     * Returns the module dependency info, given the module file root.
     * Returns null if the module's dependency info does not exist within
     * the module.
     * <p>
     * @param root The module's root directory
     * @return The module's info, null if it does not exist
     */
    public static ModuleRequires parseModuleRequires(File root) {
        try {
            /* Fetch the entry, return null if it does not exist */
            File entry = new File(root, Module.MODULE_REQUIRES);
            InputStream is = new FileInputStream(entry);
            return ModuleRequires.decode(is);
        } catch (java.io.IOException excp) {
            // print stack trace
            return null;
        } catch (java.lang.ClassCastException excp) {
            // print stack trace
            return null;
        } catch (java.lang.ArrayIndexOutOfBoundsException excp) {
            // print stack trace
            return null;
        } catch (javax.xml.bind.JAXBException excp) {
            // print stack trace
            return null;
        }
    }
    
    /**
     * Returns the module's repository information, given the module file root.
     * Returns null if the module's repository information does not exist within
     * the module.
     * <p>
     * @param root The module's root directory
     * @return The module's info, null if it does not exist
     */
    public static ModuleRepository parseModuleRepository(File root) {
        try {
            /* Fetch the entry, return null if it does not exist */
            File entry = new File(root, Module.MODULE_REPOSITORY);
            InputStream is = new FileInputStream(entry);
            return ModuleRepository.decode(new InputStreamReader(is));
        } catch (java.io.IOException excp) {
            // print stack trace
            return null;
        } catch (java.lang.ClassCastException excp) {
            // print stack trace
            return null;
        } catch (java.lang.ArrayIndexOutOfBoundsException excp) {
            // print stack trace
            return null;
        } catch (javax.xml.bind.JAXBException excp) {
            // print stack trace
            return null;
        }
    }
    
    /**
     * Parses the module's WFS contained within it, given the modile file root.
     * Returns a collection of WFS objects that represent each file system. If
     * no Wonderland file systems exist, this method returns an empty collection.
     * 
     * @param root The module's root directory
     * @return A HashMap of the module's WFSs.
     */
    public static HashMap<String, String> parseModuleWFS(File root) {
        /* Find the "wfs/" subdirectory and list just the topmost entries */
        File wfsFile = new File(root, Module.MODULE_WFS);
        if (wfsFile.exists() == false || wfsFile.isDirectory() == false) {
            // print error message
            return null;
        }
        HashMap<String, String> wfsMap = new HashMap<String, String>();
        
        /* List all of the files, take only directories ending in -wfs */
        File[] files = wfsFile.listFiles();
        for (File file : files) {
            /* If a directory, then recursively descend and append */
            String name = file.getName();
            if (file.isDirectory() == true && name.endsWith("-wfs") == true) {
                /* Strip off the name of the wfs from the path */
                try {
                    name = name.substring(0, name.length() - 4);
                    wfsMap.put(name, file.toURL().toExternalForm());
                } catch (MalformedURLException excp) {
                    // log an error and continue
                }
            } 
        }
        return wfsMap;
    }
    
    /**
     * Parses the module's checksum file for its resources
     *
     * @param root The module's root directory
     * @return A collection of resource checksums
     */
    public static ModuleChecksums parseModuleChecksums(File root) {
        /* Find the "art/" subdirectory and the checksum file */
        File file = new File(root, Module.MODULE_CHECKSUMS);
        try {
            return ModuleChecksums.decode(new FileReader(file));
        } catch (Exception excp) {
            // log an error and continue
            ModuleManager.getLogger().warning(excp.toString());
        }
        return null;
    }
    
    /**
     * Parses the module's artwork, given the module file root. Returns a
     * collection of module artwork resource entries with the paths beneath the
     * art/ subdirectory, beginning with the name following the "/". If no
     * entries exist, this method returns an empty collection.
     *
     * @param root The module's root directory
     * @return A HashMap of the module's artwork entries.
     */
    public static HashMap<String, ModuleArtResource> parseModuleArt(File root) {
        /* Find the "art/" subdirectory and recursively list */
        File artFile = new File(root, Module.MODULE_ART);
        if (artFile.exists() == false || artFile.isDirectory() == false) {
            // print error message
            return null;
        }
        return FileModuleUtil.listModuleArt(artFile, artFile);
    }
    
    /**
     * Takes a directory and returns a Map containing all of the files beneath
     * the directory, making a recursive search through the directory structure.
     * <p>
     * The path name returned are with respect to the root directory passed to
     * the first call of this method.
     * <p>
     * @param root The root directory of the search
     * @param dir The directory to recursively enumerate
     * @return A set containing all of the files beneath the directory
     */
    private static HashMap<String, ModuleArtResource> listModuleArt(File root, File dir) {
        HashMap<String, ModuleArtResource> hashMap = new HashMap<String, ModuleArtResource>();
        String rootPathName = root.getAbsolutePath();
        
        /*
         * List all of the files. For those that are directories, recusrively
         * call this method and add the results to what we find here. For all
         * ordinary files, simple add the entry.
         */
        File[] files = dir.listFiles();
        for (File file : files) {
            /* If a directory, then recursively descend and append */
            if (file.isDirectory() == true && file.isHidden() == false) {
                HashMap<String, ModuleArtResource> rHashMap = FileModuleUtil.listModuleArt(root, file);
                hashMap.putAll(rHashMap);
            }
            else if (file.isFile() == true && file.isHidden() == false) {
                /*
                 * If a normal, non-hidden file, then add to the set. The name
                 * we use is relative to the root, so we strip that off the
                 * front first
                 */
                String name = file.getAbsolutePath().substring(rootPathName.length() + 1);
                hashMap.put(name, new ModuleArtResource(name));
            }
        }
        return hashMap;
    }

    
    /**
     * Takes directory and returns a Map containing all of the plugins in the
     * module. Returns an empty map if no plugins are found.
     * 
     * @param root The module's root directory
     * @return A HashMap of plugin entries
     */
    public static HashMap<String, ModulePlugin> parseModulePlugins(File root) {
        /* Where to put all of the plugins we find */
        HashMap<String, ModulePlugin> plugins = new HashMap();
        
        /* Filter for only files ending in .jar */
        JarFileFilter filter = new JarFileFilter();
        
        /*
         * List all of the files. Take only those files that are actual files
         * and are not hidden. For each, fetch the name of the JAR files in
         * each of the client/, server/, common/ subdirectories
         */
        File dir = new File(root, Module.MODULE_PLUGINS);
        File[] files = dir.listFiles();
        if (files == null) {
            return plugins;
        }
        
        for (File file : files) {
            /*
             * If a directory, then fetch all of its jar entries in client/,
             * common/, and server/.
             */
            if (file.isDirectory() == true && file.isHidden() == false) {
                /* Create file objects for each of the subdirectories */
                String name = file.getName();
                File clientFile = new File(file, ModulePlugin.CLIENT_JAR);
                File serverFile = new File(file, ModulePlugin.SERVER_JAR);
                File commonFile = new File(file, ModulePlugin.COMMON_JAR);
                
                /* List each of the JAR files */
                String[] client = clientFile.list(filter);
                String[] server = serverFile.list(filter);
                String[] common = commonFile.list(filter);
                
                /* Make sure each are not null */
                client = (client != null) ? client : new String[] {};
                server = (server != null) ? server : new String[] {};
                common = (common != null) ? common : new String[] {};
                
                /* Create the ModulePlugin object, add, and continue */
                ModulePlugin p = new ModulePlugin(name, client, server, common);
                plugins.put(name, p);
            }
        }
        return plugins;
    }
}
