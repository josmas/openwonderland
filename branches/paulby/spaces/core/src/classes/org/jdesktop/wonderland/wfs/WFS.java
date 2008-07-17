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
package org.jdesktop.wonderland.wfs;

import java.beans.ExceptionListener;
import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.setup.BasicCellMOSetup;

/**
 * <h3>The Wonderland File System (WFS)</h3>
 * 
 * The WFS class represents a Wonderland File System (WFS). A WFS is a series
 * of directories and files (cells) that describe a world or a portion of a
 * world. A WFS may also contain a collection of resources that are exported
 * for use.
 * <p>
 * An instance of the WFS class is created through methods on the WFSFactory
 * class. Instances of WFS (or any of its subclasses) should never be created
 * directly.
 * <p>
 * The WFS class and its associated interfaces (WFSCell and WFSCellDirectory)
 * enable the reading and writing of WFSs. These sets of abstractions are meant
 * to be low-level: they are simply Java class representations of the directory
 * and file concepts defined by WFS.
 *
 * <h3>Reading and Writing Model</h3>
 *
 * A WFS is only read in when asked (i.e. the structure of the WFS is discovered
 * progressively; it is not read all at once into memory). Once read, the WFS
 * structure resides in memory. The underlying file system may be forceably
 * re-read; otherwise changes made to the file system on the operation system
 * level will not be seen in this software layer.
 * <p>
 * The structure and content of the WFS that resides in memory may be updated.
 * programmatically. Changes do not take effect to the underyling medium until
 * explictly done so. Updating the WFS to its underlying medium (e.g. disk) can
 * happen in one of several ways: only the configuration parameters of a cell
 * may be updated, or a cell and all of its children may be updated (representing
 * a subtree of the WFS), or the entire WFS tree. Updates to the medium are
 * done with the minimum amount of disruption to any existing structure--new
 * cells are added, while old cells are removed (versus obliterating an existing
 * structure and rewriting from scratch).
 *
 * <h3>Threading Issues</h3>
 * 
 * No methods on these classes are multi-thread safe, nor does this API prevent
 * multiple users from interfering with each other's edits on a WFS. It is
 * the responsibility of high-level software to coordinate access amongst
 * multiple users to a single WFS. Also, if an external party modifies the
 * underlying WFS (e.g. operating system manipulations of the disk file system),
 * then no guarantees are made on the correctness of actions taken with this
 * API.
 * 
 * <h3>Supported Underlying Mediums</h3>
 *
 * A WFS may exist as a hierarchy of files and directories on a disk file
 * system, or a new one may be created on disk. These WFSs are created by giving
 * a URL with the 'file:' protocol. The WFSFactory.open(URL) method opens an
 * existing WFS on disk, while the WFSFactory.create(URL) creates a new WFS on
 * disk. This API make no guarantees for correctness if changes to the directory
 * structure is made on the operating system level. This API supports both
 * reading and writing for disk file systems.
 * <p>
 * A WFS may also exist as a jar file that encodes the proper directory and
 * file structure. The jar file may be located on a disk locally, or it may
 * be fetched from the network. The WFSFactory.open(URL) method opens a WFS
 * encoded as a jar file, whether on disk locally (with the 'file:' protocol)
 * or over the network (with the 'http:' protocol). See the ArchiveWFS class
 * for more details on the format of the URL in these cases. This API supports
 * only reading of jar files at this time.
 * <p>
 * In the final case, the WFS resides entirely in memory and may be serlialized
 * at once to an output stream, encoding as a jar file. This usage is typically
 * if one wishes to create a WFS from scratch and write it out over a network
 * connection.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class WFS implements ExceptionListener {
    
    /* The error logger */
    private static final Logger logger = Logger.getLogger("wonderland.wfs");
    
    /* Prefix names for WFS components */
    public static final String CELL_DIRECTORY_SUFFIX = "-wld";
    public static final String CELL_FILE_SUFFIX = "-wlc.xml";
    public static final String WFS_DIRECTORY_SUFFIX = "-wfs";
    
    /* Support URL protocols for file systems */
    public static final String FILE_PROTOCOL = "file";
    public static final String JAR_PROTOCOL  = "jar";
 
    /**
     * Creates a new instance of WFS. This constructor should not be called--
     * one of the factory methods on the WFSFactory class should be used
     * instead.
     */
    public WFS() {
    }
    
    /**
     * Close any open resource associated with the WFS
     */
    public void close() {
        // Do nothing, to be overridden perhaps
    }
   
    /**
     * Returns the root cell directory class representing of the WFS.
     * 
     * @return The directory containing the children in the root of the WFS
     */
    public abstract WFSRootDirectory getRootDirectory();
    
    /**
     * Returns the logger for the WFS package
     */
    public static Logger getLogger() {
        return WFS.logger;
    }
    
    /**
     * Writes the entire WFS to the underlying medium, including the meta-
     * information contains within the root directory, the cells containing
     * within the root directory, and any child directories.
     * <p>
     * @throw IOException Upon a general I/O error.
     */
    public void write() throws IOException {
        this.getRootDirectory().write();
    }
    
    /**
     * Writes an entire WFS to an output stream, encoding as a jar file. The
     * side effect of this method is to load the entire WFS if it has not
     * already been loaded into memory.
     * 
     * @param os The output stream to write to
     * @throw IOException Upon general I/O error
     */
    public void writeTo(OutputStream os) throws IOException {
        /* Create the output stream for the jar file */
        JarOutputStream jos = new JarOutputStream(os);
        
        /* Fetch the root directory and write out the directory */
        WFSRootDirectory rootDir = this.getRootDirectory();
        JarEntry         je      = new JarEntry(rootDir.getPathName() + "/");
        jos.putNextEntry(je);
        
        /* Fetch the version and aliases classes and write them out */
        WFSAliases aliases = rootDir.getAliases();
        if (aliases != null) {
            je = new JarEntry(rootDir.getPathName() + "/" + WFSRootDirectory.ALIASES);
            jos.putNextEntry(je);
            aliases.encode(jos);
        }
        
        WFSVersion version = rootDir.getVersion();
        if (version != null) {
            je = new JarEntry(rootDir.getPathName() + "/" + WFSRootDirectory.VERSION);
            jos.putNextEntry(je);
            version.encode(jos);
        }
        
        /*
         * Write all of the individua cells in the root directory out.
         * Recursively call to write out the rest of the WFS tree.
         */
        WFSCell cells[] = rootDir.getCells();
        for (WFSCell cell : cells) {
            try {
                this.writeCellTo(cell, jos);
            } catch (IOException excp) {
                // ignore
            }
        }
        
        /* Close the stream and return */
        jos.close();
    }
    
    /**
     * Handles when an exception is thrown by the XML decoding mechanism, to
     * supress any warnings it may print.
     */
    public void exceptionThrown(Exception e) {
        WFS.getLogger().log(Level.WARNING, "Error writing XML: " + e);
    }
    
    /**
     * Takes a WFSCell class and writes its contents to the given jar output
     * stream, and recursively calls to write out any child directory it may
     * have.
     */
    private void writeCellTo(WFSCell cell, JarOutputStream jos) throws IOException {
        /* First fetch the cell setup class, this may result in an read exception */
        BasicCellMOSetup cellSetup = null;
        try {
            cellSetup = cell.getCellSetup(); 
        } catch (InvalidWFSCellException excp) {
            throw new IOException("Unable to read cell setup: " + cell);
        }

        /* If there is a cell class, then write it out to the stream */
        if (cellSetup != null) {
            /*
             * Write the entry out to the output stream
             */
            JarEntry je = new JarEntry(cell.getPathName());
            jos.putNextEntry(je);
            XMLEncoder e = new XMLEncoder(jos);
            e.setExceptionListener(this);
            e.writeObject(cellSetup);
            e.close();
            
            /*
             * Recursively write out any children
             */
            if (cell.getCellDirectory() != null) {
                this.writeDirectoryTo(cell.getCellDirectory(), jos);
            }          
        }
    }
    
    /**
     * Takes a WFSCellDirectory class and writes its contents to the given
     * jar output stream. It recursively calls itself until the entire WFS
     * tree has been written.
     */
    private void writeDirectoryTo(WFSCellDirectory directory, JarOutputStream jos) throws IOException {
        /* First create the directory */
        JarEntry je = new JarEntry(directory.getPathName() + "/");
        jos.putNextEntry(je);
        
        /*
         * Write all of the individual cells in the directory out.
         * Recursively call to write out the rest of the WFS tree.
         */
        WFSCell cells[] = directory.getCells();
        for (WFSCell cell : cells) {
            try {
                this.writeCellTo(cell, jos);
            } catch (java.io.IOException excp) {
                // ignore
            }
        }
    }

}
