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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
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
import org.jdesktop.wonderland.utils.ArchiveManifest;

/**
 * The ArchiveModule class extends the Module abstract base class and represents
 * all modules that are contained within either a JAR or ZIP archive.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ArchiveModule extends Module {

    /* The File representing the module */
    private File root = null;
    
    /* The manifest of the archive */
    private ArchiveManifest manifest = null;
    
    /* The chunk size to write out while expanding archive files to disk */
    private static final int CHUNK_SIZE = (8 * 1024);
            
    /**
     * Constructor, takes File of the module jar. Throws IOException upon
     * general I/O error reading the archive module.
     * 
     * @param file The archive module File object
     * @throw IOException Upon general I/O exception readin the module
     */
    public ArchiveModule(File file) throws IOException {
        super();
        this.root = file;
        this.manifest = new ArchiveManifest(this.root);
    }

    /**
     * Returns the name of the module given its file object.
     * 
     * @param file The file pointing to the module
     * @return The name of the module
     */
    public static String getModuleName(File file) {
        /* Return the name minus the .jar ending */
        String name = file.getName();
        if (name.endsWith(".jar") == true) {
            return name.substring(0, name.length() - 4);
        }
        return name;
    }
    
    /**
     * Returns an input stream for the given resource, null upon error.
     * <p>
     * @param resource A resource contained within the archive
     * @return An input stream to the resource
     */
    public InputStream getInputStreamForResource(ModuleResource resource) {
        try {
            return this.manifest.getEntryInputStream(resource.getPathName());
        } catch (java.lang.IllegalStateException excp) {
            // print stack trace
            return null;
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
            return this.manifest.getEntryInputStream(path);
        } catch (java.io.IOException excp) {
            // print stack trace
            return null;
        }
    }
    
    /**
     * Takes a base directory (which must exist and be readable) and expands
     * the contents of the archive module into that directory. Upon error,
     * throws IOException
     * 
     * @param root The base directory in which the module is expanded
     * @throw IOException Upon error
     */
    public void expand(File root) throws IOException {
        /*
         * Loop through each entry, fetchs its input stream, and write to an
         * output stream for the file.
         */
        LinkedList<String> entryList = this.manifest.getEntries();
        Iterator<String> it = entryList.listIterator();
        while (it.hasNext() == true) {
            /* Fetch the next entry, its name is the relative file name */
            String entryName = it.next();
            InputStream is = this.manifest.getEntryInputStream(entryName);
            
            /* Don't expand anything that beings with META-INF */
            if (entryName.startsWith("META-INF") == true) {
                continue;
            }
            
            /* Ignore if it is a directory, then create it */
            if (this.manifest.isDirectory(entryName) == true) {
                File file = new File(root, entryName);
                file.mkdirs();
                continue;
            }
            
            /* Write out to a file in 'root' */
            File file = new File(root, entryName);
            FileOutputStream os = new FileOutputStream(file);
            byte[] b = new byte[ArchiveModule.CHUNK_SIZE];
            while (true) {
                int len = is.read(b);
                if (len == -1) {
                    break;
                }
                os.write(b, 0, len);
            }
            os.close();
        }
    }

    @Override
    public ModuleInfo getModuleInfo() {
        try {
            /* Fetch the input stream, parse and return */
            InputStream is = manifest.getEntryInputStream(Module.MODULE_INFO);
            if (is == null) {
                return null;
            }
            return ModuleInfo.decode(new InputStreamReader(is));
        } catch (java.lang.Exception excp) {
            // print stack trace
            return null;
        }
    }

    @Override
    public ModuleRequires getModuleRequires() {
        try {
            /* Fetch the input stream, parse and return */
            InputStream is = manifest.getEntryInputStream(Module.MODULE_REQUIRES);
            if (is == null) {
                return null;
            }
            return ModuleRequires.decode(new InputStreamReader(is));
        } catch (java.lang.Exception excp) {
            // print stack trace
            return null;
        }
    }

    @Override
    public ModuleRepository getModuleRepository() {
        try {
            /* Fetch the input stream, parse and return */
            InputStream is = manifest.getEntryInputStream(Module.MODULE_REPOSITORY);
            if (is == null) {
                return null;
            }
            return ModuleRepository.decode(new InputStreamReader(is));
        } catch (java.lang.Exception excp) {
            // print stack trace
            return null;
        }
    }
    
    @Override
    public ModuleChecksums getModuleChecksums() {
        try {
            /* Fetch the input stream, parse and return */
            InputStream is = manifest.getEntryInputStream(Module.MODULE_CHECKSUMS);
            if (is == null) {
                return null;
            }
            return ModuleChecksums.decode(new InputStreamReader(is));
        } catch (java.lang.Exception excp) {
            // print stack trace
            return null;
        }
    
    }
    
    @Override
    public Collection<String> getModuleArtResources() {
        /* Create a linked list to store the entries, get the entries */
        LinkedList<String> list = new LinkedList<String>();
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
            list.addLast(name);
        }
        return list;
    }

    @Override
    public ModuleArtResource getModuleArtResource(String path) {
        return new ModuleArtResource(path);
    }

    @Override
    public Collection<String> getModuleWFSs() {
        /* Create a linked list to store the entries, get the entries */
        LinkedList<String> list = new LinkedList<String>();
        Iterator<String> it = manifest.getEntries().listIterator();

        /*
         * Loop through each entry and see if its name begins with "art/"
         * does not end with "/". If so, take the name, minus the beginning
         * "art/" part.
         */
        while (it.hasNext() == true) {
            String name = it.next();

            /* See if the name begins with "art/" */
            if (name.startsWith(Module.MODULE_WFS + "/") == false) {
                continue;
            }

            /* See if the name ends with "/" */
            if (name.endsWith("-wfs/") == true) {
                continue;
            }

            /* Add it to the list */
            name = name.substring(0, name.length() - 4);
            list.addLast(name);
        }
        return list;
    }

    @Override
    public Collection<String> getModulePlugins() {
        /* Create a linked list to store the entries, get the entries */
        LinkedList<String> list = new LinkedList<String>();
        Iterator<String> it = manifest.getEntries().listIterator();

        /*
         * Loop through each entry and see if its name begins with "art/"
         * does not end with "/". If so, take the name, minus the beginning
         * "art/" part.
         */
        while (it.hasNext() == true) {
            String name = it.next();

            /* See if the name begins with "art/" */
            if (name.startsWith(Module.MODULE_PLUGINS + "/") == false) {
                continue;
            }

            /* See if the name ends with "/" */
            if (name.endsWith("-wf/") == true) {
                continue;
            }

            /* Add it to the list */
            list.addLast(name);
        }
        return list;
    }

    @Override
    public ModulePlugin getModulePlugin(String name) {
        /* Fetch the entry for the plugin directory, see if it exists */
        LinkedList<String> listEntries = manifest.getEntries(Module.MODULE_PLUGINS + "/" + name);
        if (listEntries.isEmpty() == true) {
            return null;
        }

        /* Check for the common/, client/, and server/ directories */
        String baseDir = Module.MODULE_PLUGINS + "/" + name + "/";
        LinkedList<String> commonJARs = manifest.getEntries(baseDir + ModulePlugin.COMMON_JAR);
        LinkedList<String> clientJARs = manifest.getEntries(baseDir + ModulePlugin.CLIENT_JAR);
        LinkedList<String> serverJARs = manifest.getEntries(baseDir + ModulePlugin.SERVER_JAR);

        /* Convert each list to an array string */
        String[] common = commonJARs.toArray(new String[]{});
        String[] client = clientJARs.toArray(new String[]{});
        String[] server = serverJARs.toArray(new String[]{});

        /* Create the ModulePlugin object, add, and continue */
        return new ModulePlugin(name, client, server, common);
    }

    @Override
    public boolean delete() {
        return FileUtils.deleteQuietly(this.root);
    }

    @Override
    public String getName() {
        /* Return the name minus the .jar ending */
        return ArchiveModule.getModuleName(this.root);
    }
}
