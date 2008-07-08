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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The ModuleArchiveUtil class contains a collection is utility routines to help
 * parse the module archive file.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ArchiveModuleUtil {

    
    /**
     * Returns the basic module info, given the module archive file. Returns
     * null if the module's info file does not exist within the archive.
     * <p>
     * @param zipFile The archive file
     * @return The module's info, null if it does not exist
     */
    public static ModuleInfo parseModuleInfo(ZipFile zipFile) {
        try {
            /* Fetch the entry, return null if it does not exist */
            ZipEntry entry = zipFile.getEntry(Module.MODULE_INFO);
            if (entry == null) {
                return null;
            }
            
            /* Fetch the input stream, parse and return */
            InputStream is = zipFile.getInputStream(entry);
            return ModuleInfo.decode(is);
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
        }
    }
    
    /**
     * Returns the module dependency info, given the module archive file.
     * Returns null if the module's dependency info does not exist within
     * the archive.
     * <p>
     * @param zipFile The archive file
     * @return The module's info, null if it does not exist
     */
    public static ModuleRequires parseModuleRequires(ZipFile zipFile) {
        try {
            /* Fetch the entry, return null if it does not exist */
            ZipEntry entry = zipFile.getEntry(Module.MODULE_REQUIRES);
            if (entry == null) {
                return null;
            }
            
            /* Fetch the input stream, parse and return */
            InputStream is = zipFile.getInputStream(entry);
            return ModuleRequires.decode(is);
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
        }
    }
    
    /**
     * Returns the module's repository information, given the module archive
     * file. Returns null if the module's repository information does not
     * exist within the archive.
     * <p>
     * @param zipFile The archive file
     * @return The module's info, null if it does not exist
     */
    public static ModuleRepository parseModuleRepository(ZipFile zipFile) {
        try {
            /* Fetch the entry, return null if it does not exist */
            ZipEntry entry = zipFile.getEntry(Module.MODULE_REPOSITORY);
            if (entry == null) {
                return null;
            }
            
            /* Fetch the input stream, parse and return */
            InputStream is = zipFile.getInputStream(entry);
            return ModuleRepository.decode(is);
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
        }
    }
    
    /**
     * Parses the module's artwork, given the module archive file. Returns a
     * collection of module artwork resource entries with the paths beneath the
     * art/ subdirectory,beginning with the name following the "/". If no
     * entries exist, this method returns an empty collection.
     * <p>
     * This method distinguishes directory entries in the archive file from
     * individual files by checking to see if there is a trailing "/" in the
     * entry (and if so, assumes it is a directory).
     * <p>
     * @param zipFile The archive file
     * @return A HashSet of the module's artwork entries.
     */
    public static HashMap<String, ModuleArtResource> parseModuleArt(ZipFile zipFile) {
        try {
            /* Create a hash set to store the entries, get the entries */
            HashMap<String, ModuleArtResource> hashMap = new HashMap<String, ModuleArtResource>();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            
            /*
             * Loop through each entry and see if its name begins with "art/"
             * does not end with "/". If so, take the name, minus the beginning
             * "art/" part.
             */
            while (entries.hasMoreElements() == true) {
                ZipEntry entry = entries.nextElement();
                String   name  = entry.getName();
                
                /* See if the name begins with "art/" */
                if (name.startsWith(Module.MODULE_ART) == false) {
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
}
