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
package org.jdesktop.wonderland.wfs.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import org.jdesktop.wonderland.wfs.InvalidWFSException;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSAliases;
import org.jdesktop.wonderland.wfs.WFSRootDirectory;
import org.jdesktop.wonderland.wfs.WFSVersion;


/**
 * The FileWFS class extends the WFS abstract class and represents a Wonderland
 * File System that resides on disk.
 * <p>
 * @author jslott
 */
public class FileWFS extends WFS {
    /* The location of the file system, and a referring File object */
    private URI  uri  = null;
    private File root = null;
    
    /* The directory object associated with the root of the file system */
    private WFSRootDirectory directory = null;
    
    /* File and directory filters for reading Wonderland file system */
    public static final FileFilter CELL_FILE_FILTER = new CellFileFilter();
    
    /**
     * Creates a new instance of WFS given the URI of the file system.
     * <p>
     * @param uri The URI of the location of the file system
     * @throw SecurityException If a URI protocol type is not supported
     * @throw FileNotFoundException If the URI does not exist
     * @throw InvalidWFSException If the URI does not contain a valid WFS
     * @throw IOException If the URI cannot be opened and/or read.
     */
    public FileWFS(String path, boolean create) throws SecurityException, FileNotFoundException, IOException, InvalidWFSException {
        super();
        this.uri = new File(path).toURI();
        
        /*
         * Test whether the URI has a null scheme (in which case it is assumed to
         * be 'file' type or whether it is of type file. XXX
         */
        if (uri.getScheme() != null && uri.getScheme().equals("file") != true) {
            throw new SecurityException("Invalid URI scheme type: " + uri.getScheme());
        }
        
        /* Attempt to open the URI, throwing a FileNotFoundException if bad */
        this.root = new File(uri);
        
        /*
         * Make sure the File name conforms to the '<name>-wfs' format. If not,
         * throw an InvalidWFSException.
         */
        if (this.root.getName().endsWith(WFS.WFS_DIRECTORY_SUFFIX) == false) {
            throw new InvalidWFSException("WFS URI has an invalid name: " + uri.toString());
        }

        /* Check to see if it exists and it is a directory */
        if (create == false && (this.root.exists() == false || this.root.isDirectory() == false)) {
            throw new InvalidWFSException("WFS URI is not a directory: " + uri.toString());
        }
        else if (create == true && this.root.exists() == true) {
            /* If we wish to create it, but it already exists */
            throw new IOException("WFS URI already exists: " + uri.toString());
        }
        else if (create == true) {
            /* Then create it! */
            if (this.root.mkdirs() == false) {
                throw new IOException("Unable to create WFS: " + uri.toString());
            }
        }
        this.directory = new WFSFileRootDirectory(this.root);
        
        /* At this point, if we are created a new WFS, then simply return */
        if (create == true) {
            return;
        }
        
        /*
         * Attempt to open and read the aliases.xml file as a WFSAliases class, if
         * it exists. If it does not exist, then simply have no mapping and fail
         * by writing an error to the log.
         */
        try {
            File afile   = new File(this.root, WFSRootDirectory.ALIASES);
            this.directory.setAliases(WFSAliases.decode(new FileInputStream(afile)));
        } catch (FileNotFoundException excp) {
            WFS.getLogger().log(Level.WARNING, "Invalid/Nonexistent aliases.xml file in WFS: " + uri.toString());
        }
        
        /*
         * Attempt to open and read the version.xml file as a WFSVersion class, if
         * it exists. If it does not exist, then simply assign the current version
         * of the software and log a message
         */
        try {
            File vfile   = new File(this.root, WFSRootDirectory.VERSION);
            this.directory.setVersion(WFSVersion.decode(new FileInputStream(vfile)));
        } catch (FileNotFoundException excp) {
            WFS.getLogger().log(Level.WARNING, "Invalid/Nonexistent version.xml file in WFS: " + uri.toString());
        }
    }
    
    /**
     * Returns the root cell directory class representing of the WFS.
     * 
     * @return The directory containing the children in the root of the WFS
     */
    public WFSRootDirectory getRootDirectory() {
        return this.directory;
    }
        
    /**
     * Writes the entire WFS to the underlying medium, including the meta-
     * information contains within the root directory, the cells containing
     * within the root directory, and any child directories.
     * <p>
     * @throw IOException Upon a general I/O error.
     */
    public void write() throws IOException {
        /* Delegate to the root directory for writing */
        this.directory.write();
    }
}
