/**
 * Project Looking Glass
 *
 * $RCSfile: FileWFS.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.3 $
 * $Date: 2007/10/23 21:34:33 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.server.utils.wfs.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import org.jdesktop.wonderland.server.utils.wfs.InvalidWFSException;
import org.jdesktop.wonderland.server.utils.wfs.WFS;
import org.jdesktop.wonderland.server.utils.wfs.WFSAliases;
import org.jdesktop.wonderland.server.utils.wfs.WFSCellDirectory;
import org.jdesktop.wonderland.server.utils.wfs.WFSVersion;


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
    
    /* The version of the file system, parsed from the top-level directory */
    private WFSVersion version = null;
    
    /* The mapping of URIs to locations within this file system */
    private WFSAliases aliases = null;
    
    /* The directory object associated with the root of the file system */
    private WFSCellDirectory directory = null;
    
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
    public FileWFS(String path) throws SecurityException, FileNotFoundException, IOException, InvalidWFSException {
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
         * Make sure the File is a directory and that its name conforms to the
         * '<name>-wfs' format. If not, throw an InvalidWFSException.
         */
        if (this.root.isDirectory() == false) {
            throw new InvalidWFSException("WFS URI is not a directory: " + uri.toString());
        }
        
        if (this.root.getName().endsWith(WFS.WFS_DIRECTORY_SUFFIX) == false) {
            throw new InvalidWFSException("WFS URI has an invalid name: " + uri.toString());
        }
        this.directory = new WFSFileCellDirectory(this.root, this.root.getCanonicalPath());
        
        /*
         * Attempt to open and read the aliases.xml file as a WFSAliases class, if
         * it exists. If it does not exist, then simply have no mapping and fail
         * by writing an error to the log.
         */
        try {
            File afile   = new File(this.root, WFS.ALIASES);
            this.aliases = WFSAliases.decode(new FileInputStream(afile));
        } catch (FileNotFoundException excp) {
            WFS.getLogger().log(Level.WARNING, "Invalid/Nonexistent aliases.xml file in WFS: " + uri.toString());
        }
        
        /*
         * Attempt to open and read the version.xml file as a WFSVersion class, if
         * it exists. If it does not exist, then simply assign the current version
         * of the software and log a message
         */
        try {
            File vfile   = new File(this.root, WFS.VERSION);
            this.version = WFSVersion.decode(new FileInputStream(vfile));
        } catch (FileNotFoundException excp) {
            WFS.getLogger().log(Level.WARNING, "Invalid/Nonexistent version.xml file in WFS: " + uri.toString());
            this.version = new WFSVersion(0, 0);
        }
    }
    
    /**
     * Returns the version of the file system
     * @return The version of the WFS.
     */
    public WFSVersion getVersion() {
        return this.version;
    }
    
    /**
     * Returns the WFS URI aliases for this filesystem
     */
    public WFSAliases getAliases() {
        return this.aliases;
    }
    
    /**
     * Returns the Cell directory class representing this root
     */
    public WFSCellDirectory getCellDirectory() {
        return this.directory;
    }
    
    public static final void main(String[] args) {
        try {
            FileWFS wfs = new FileWFS(new URI(args[0]).getPath());
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
        }
    }
}
