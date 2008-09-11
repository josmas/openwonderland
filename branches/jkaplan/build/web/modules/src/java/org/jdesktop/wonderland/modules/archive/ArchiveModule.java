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
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
    /* The URL file in which the archive is contained */
    private URL url = null;
    
    /* The manifest of the archive */
    private ArchiveManifest manifest = null;
    
    /* The chunk size to write out while expanding archive files to disk */
    private static final int CHUNK_SIZE = (8 * 1024);
            
    /**
     * Constructor, takes a URL to the archive file. Throws IOException upon
     * general I/O error reading the archive module
     * 
     * @param url The URL of the archive module file
     * @throw IOException Upon general I/O error
     */
    public ArchiveModule(URL url) throws IOException {
        super();
        this.url = url;
        this.manifest = new ArchiveManifest(url);
    }
    
    /**
     * Opens the module by reading its contents.
     */
    @Override
    public void open() {
        /* Fetch and parse the three XML files: info, requires, and repository */
        ModuleInfo info = ArchiveModuleUtil.parseModuleInfo(this.manifest);
        if (info == null) {
            System.out.println("cannot parse module info");
            // print error message XXX
        }
        ModuleRequires requires = ArchiveModuleUtil.parseModuleRequires(this.manifest);
        ModuleRepository repository = ArchiveModuleUtil.parseModuleRepository(this.manifest);
        HashMap<String, ModuleArtResource> artwork = ArchiveModuleUtil.parseModuleArt(this.manifest);
        ModuleChecksums checksums = ArchiveModuleUtil.parseModuleChecksums(this.manifest);
        HashMap<String, String> wfs = ArchiveModuleUtil.parseModuleWFS(this.manifest);
        HashMap<String, ModulePlugin> plugins = ArchiveModuleUtil.parseModulePlugins(this.manifest);


        /* Create a new module based upon what has been parsed */
        this.setModuleInfo(info);
        this.setModuleRequires(requires);
        this.setModuleRepository(repository);
        this.setModuleChecksums(checksums);
        this.setModuleArtwork(artwork);
        this.setModuleWFSs(wfs);
        this.setModulePlugins(plugins);
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
}
