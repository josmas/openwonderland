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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jdesktop.wonderland.checksum.RepositoryChecksums;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleArtResource;
import org.jdesktop.wonderland.modules.ModuleInfo;
import org.jdesktop.wonderland.modules.ModuleRepository;
import org.jdesktop.wonderland.modules.ModuleRequires;
import org.jdesktop.wonderland.modules.ModuleResource;

/**
 * The ArchiveModule class extends the Module abstract base class and represents
 * all modules that are contained within either a JAR or ZIP archive.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ArchiveModule extends Module {
    /* The module's archive file in which it is contained */
    private ZipFile zipFile = null;
    
    /* The chunk size to write out while expanding archive files to disk */
    private static final int CHUNK_SIZE = (8 * 1024);
            
    /** Default constructor, takes a reference to the archive file */
    public ArchiveModule(ZipFile zipFile) {
        super();
        this.zipFile = zipFile;
    }
    
    /**
     * Returns the archive file associated with this module
     */
    public ZipFile getZipFile() {
        return this.zipFile;
    }
    
    /**
     * Opens the module by reading its contents.
     */
    @Override
    public void open() {
        /* Fetch and parse the three XML files: info, requires, and repository */
        ModuleInfo info = ArchiveModuleUtil.parseModuleInfo(this.zipFile);
        if (info == null) {
            System.out.println("cannot parse module info");
            // print error message XXX
        }
        ModuleRequires requires = ArchiveModuleUtil.parseModuleRequires(this.zipFile);
        ModuleRepository repository = ArchiveModuleUtil.parseModuleRepository(this.zipFile);
        HashMap<String, ModuleArtResource> artwork = ArchiveModuleUtil.parseModuleArt(this.zipFile);
        RepositoryChecksums checksums = ArchiveModuleUtil.parseModuleChecksums(this.zipFile);

        /* Create a new module based upon what has been parsed */
        this.setModuleInfo(info);
        this.setModuleRequires(requires);
        this.setModuleRepository(repository);
        this.setModuleChecksums(checksums);
        this.setModuleArtwork(artwork);
    }
    
    /**
     * Returns an input stream for the given resource, null upon error.
     * <p>
     * @param resource A resource contained within the archive
     * @return An input stream to the resource
     */
    public InputStream getInputStream(ModuleResource resource) {
        try {
            ZipEntry entry = this.zipFile.getEntry(resource.getPathName());
            return this.zipFile.getInputStream(entry);
        } catch (java.lang.IllegalStateException excp) {
            // print stack trace
            return null;
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
        Enumeration<? extends ZipEntry> entries = this.zipFile.entries();
        while (entries.hasMoreElements() == true) {
            /* Fetch the next entry, its name is the relative file name */
            ZipEntry entry = (ZipEntry)entries.nextElement();
            String name = entry.getName();
            InputStream is = this.zipFile.getInputStream(entry);
            
            /* Don't expand anything that beings with META-INF */
            if (name.startsWith("META-INF") == true) {
                continue;
            }
            
            /* Ignore if it is a directory, then create it */
            if (entry.isDirectory() == true) {
                File file = new File(root, name);
                file.mkdirs();
                continue;
            }
            
            /* Write out to a file in 'root' */
            File file = new File(root, name);
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
