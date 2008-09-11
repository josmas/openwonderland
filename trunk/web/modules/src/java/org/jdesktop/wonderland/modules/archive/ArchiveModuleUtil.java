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

package org.jdesktop.wonderland.modules.archive;

import org.jdesktop.wonderland.modules.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.jdesktop.wonderland.utils.ArchiveManifest;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSFactory;

/**
 * The ModuleArchiveUtil class contains a collection is utility routines to help
 * parse the module archive file.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ArchiveModuleUtil {

    /**
     * Returns the basic module info, given the module archive file manifest.
     * Returns null if the module's info file does not exist within the archive.
     * <p>
     * @param manifest The archive file manifest
     * @return The module's info, null if it does not exist
     */
    public static ModuleInfo parseModuleInfo(ArchiveManifest manifest) {
        try {
            /* Fetch the input stream, parse and return */
            InputStream is = manifest.getEntryInputStream(Module.MODULE_INFO);
            if (is == null) {
                return null;
            }
            return ModuleInfo.decode(new InputStreamReader(is));
        } catch (java.lang.IllegalStateException excp) {
            // print stack trace
            return null;
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
     * Returns the module dependency info, given the module archive manifest.
     * Returns null if the module's dependency info does not exist within
     * the archive.
     * <p>
     * @param manifest The archive file manifest
     * @return The module's info, null if it does not exist
     */
    public static ModuleRequires parseModuleRequires(ArchiveManifest manifest) {
        try {
            /* Fetch the input stream, parse and return */
            InputStream is = manifest.getEntryInputStream(Module.MODULE_REQUIRES);
            if (is == null) {
                return null;
            }
            return ModuleRequires.decode(is);
        } catch (java.lang.IllegalStateException excp) {
            System.out.println(excp.toString());
            // print stack trace
            return null;
        } catch (java.io.IOException excp) {
            System.out.println(excp.toString());
            // print stack trace
            return null;
        } catch (java.lang.ClassCastException excp) {
            System.out.println(excp.toString());
            // print stack trace
            return null;
        } catch (java.lang.ArrayIndexOutOfBoundsException excp) {
            System.out.println(excp.toString());
            // print stack trace
            return null;
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
            // print stack trace
            return null;
        }
    }
    
    /**
     * Returns the module's repository information, given the module archive
     * manifest. Returns null if the module's repository information does not
     * exist within the archive.
     * <p>
     * @param zipFile The archive file
     * @return The module's info, null if it does not exist
     */
    public static ModuleRepository parseModuleRepository(ArchiveManifest manifest) {
        try {
            /* Fetch the input stream, parse and return */
            InputStream is = manifest.getEntryInputStream(Module.MODULE_REPOSITORY);
            if (is == null) {
                return null;
            }
            return ModuleRepository.decode(new InputStreamReader(is));
        } catch (java.lang.IllegalStateException excp) {
            // print stack trace
            return null;
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
     * Parses the module's artwork, given the module archive manifest. Returns a
     * collection of module artwork resource entries with the paths beneath the
     * art/ subdirectory,beginning with the name following the "/". If no
     * entries exist, this method returns an empty collection.
     * <p>
     * This method distinguishes directory entries in the archive file from
     * individual files by checking to see if there is a trailing "/" in the
     * entry (and if so, assumes it is a directory).
     * <p>
     * @param manifest The archive file manifest
     * @return A HashSet of the module's artwork entries.
     */
    public static HashMap<String, ModuleArtResource> parseModuleArt(ArchiveManifest manifest) {
        try {
            /* Create a hash set to store the entries, get the entries */
            HashMap<String, ModuleArtResource> hashMap = new HashMap<String, ModuleArtResource>();
            Iterator<String> it = manifest.getEntries().listIterator();
            
            /*
             * Loop through each entry and see if its name begins with "art/"
             * does not end with "/". If so, take the name, minus the beginning
             * "art/" part.
             */
            while (it.hasNext() == true) {
                String name = it.next();
                
                /* See if the name begins with "art/" */
                if (name.startsWith(Module.MODULE_ART + "/") == false) {
                    continue;
                }
                
                /* See if the name ends with "/" */
                if (name.endsWith("/") == true) {
                    continue;
                }
                
                /* Add it to the list */
                ModuleArtResource resource = new ModuleArtResource(name);
                hashMap.put(name, resource);
            }
            return hashMap;
        } catch (java.lang.IllegalStateException excp) {
            // print stack trace
            return null;
        }
    }

    /**
     * Parses the module's checksum file for the resource checksums, given
     * the archive manifest.
     *
     * @param manifest The archive file manifest
     * @return A collection of resource checksums
     */
    public static ModuleChecksums parseModuleChecksums(ArchiveManifest manifest) {
        try {
            /* Fetch the input stream, parse and return */
            InputStream is = manifest.getEntryInputStream(Module.MODULE_CHECKSUMS);
            if (is == null) {
                return null;
            }
            return ModuleChecksums.decode(new InputStreamReader(is));
        } catch (java.lang.IllegalStateException excp) {
            System.out.println(excp.toString());
            // print stack trace
            return null;
        } catch (java.io.IOException excp) {
            System.out.println(excp.toString());
            // print stack trace
            return null;
        } catch (java.lang.ClassCastException excp) {
            System.out.println(excp.toString());
            // print stack trace
            return null;
        } catch (java.lang.ArrayIndexOutOfBoundsException excp) {
            System.out.println(excp.toString());
            // print stack trace
            return null;
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
            // print stack trace
            return null;
        }
    }
    
    /**
     * Returns a HashMap of all WFSs contained in the archive module, given
     * the archive manifest.
     * 
     * @param manifest The archive file manifest
     * @return A HashSet of the module's WFSs
     */
    public static HashMap<String, WFS> parseModuleWFS(ArchiveManifest manifest) {
        /* Create the hash map to store all of the WFS objects */
        HashMap<String, WFS> wfsMap = new HashMap<String, WFS>();
        
        /*
         * Fetch all of the directories beneath the "wfs/" directory. See if
         * it is a valid WFS and add it to the hashmap.
         */
        LinkedList<String> listEntries = manifest.getEntries(Module.MODULE_WFS);
        Iterator<String> it = listEntries.listIterator();
        while (it.hasNext() == true) {
            /*
             * Attempt to create the WFS. We do this by forming a URL of the
             * module archive file followed by the path of the wfs within
             * the archive. Creation of the WFS object will tell us whether
             * it is valid or not
             */
            String entryName = it.next();
            WFS wfs = null;
            try {
                URL url = new URL(manifest.getURL().toExternalForm() + entryName);
                wfs = WFSFactory.create(url);
                wfsMap.put(wfs.getName(), wfs);
            } catch (Exception excp) {
                // print log warning
                continue;
            }
        }
        return wfsMap;
    }
    
    /**
     * Returns a HashMap of all plugins contained in the archive module, given
     * the archive manifest.
     * 
     * @param manifest The archive file manifest
     * @return A HashSet of the module's plugins
     */
    public static HashMap<String, ModulePlugin> parseModulePlugins(ArchiveManifest manifest) {
        /* Where to put all of the plugins we find */
        HashMap<String, ModulePlugin> plugins = new HashMap();
        
        /*
         * Fetch all of the directories beneath the "plugins/" directory. See if
         * it is a valid plugin and add it to the hashmap.
         */
        LinkedList<String> listEntries = manifest.getEntries(Module.MODULE_PLUGINS);
        Iterator<String> it = listEntries.listIterator();
        while (it.hasNext() == true) {
            /*
             * Beneath each, see if there is a common/, client/, and server/
             * subdirectories, and in each, fetch the names of the JAR files
             * in each.
             */
            String moduleName = it.next();
            String baseDir = Module.MODULE_PLUGINS + "/" + moduleName + "/";
            LinkedList<String> commonJARs = manifest.getEntries(baseDir + ModulePlugin.COMMON_JAR);
            LinkedList<String> clientJARs = manifest.getEntries(baseDir + ModulePlugin.CLIENT_JAR);
            LinkedList<String> serverJARs = manifest.getEntries(baseDir + ModulePlugin.SERVER_JAR);
            
            /* Convert each list to an array string */
            String[] common = commonJARs.toArray(new String[] {});
            String[] client = clientJARs.toArray(new String[] {});
            String[] server = serverJARs.toArray(new String[] {});
            
            /* Create the ModulePlugin object, add, and continue */
            ModulePlugin p = new ModulePlugin(client, server, common);
            plugins.put(moduleName, p);
        }
        return plugins;
    }
}
