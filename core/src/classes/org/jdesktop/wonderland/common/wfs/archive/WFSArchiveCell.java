/**
 * Project Looking Glass
 *
 * $RCSfile: WFSArchiveCell.java,v $
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
 * $Date: 2007/10/19 12:39:31 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.common.wfs.archive;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.wfs.InvalidWFSCellException;
import org.jdesktop.wonderland.common.wfs.WFS;
import org.jdesktop.wonderland.common.wfs.WFSCell;
import org.jdesktop.wonderland.server.setup.CellMOSetup;
import org.jdesktop.wonderland.server.setup.InvalidCellMOSetupException;

/**
 * The WFSCell class represents an XML file on disk corresponding to the
 * definition of Wonderland cell. The cells are assumed to conform to any
 * naming conventions that identify them as cells.
 * <p>
 * @author jslott
 */
public class WFSArchiveCell implements ExceptionListener, WFSCell {
    /** logger */
    private static final Logger logger =
            Logger.getLogger(WFSArchiveCell.class.getName());
    
    /* The manifest file that permits access to JAR file entries */
    private ArchiveManifest manifest;
    
    /* The name of the cell, without the standard suffix */
    private String cellName;
    
    /* The canonical, unique path name of the cell file in the WFS */
    private String canonicalName;
    
    /** Creates a new instance of WFSCell */
    public WFSArchiveCell(ArchiveManifest manifest, String canonicalName, String cell) {
        this.manifest = manifest;
        
        /* Parse out the name of the cell, assuming it has the proper suffix */
        try {
            int index          = cell.indexOf(WFS.CELL_FILE_SUFFIX);
            this.cellName      = cell.substring(0, index);
            this.canonicalName = canonicalName;
        } catch (java.lang.IndexOutOfBoundsException excp) {
            WFS.getLogger().log(Level.SEVERE, "A WFSCell class was created with an invalid file");
            WFS.getLogger().log(Level.SEVERE, "file name: " + canonicalName);
            this.cellName = null;
            this.canonicalName = null;
        }
    }
    
    /**
     * Returns the name of the cell, without the standard suffix
     */
    public String getCellName() {
        return this.cellName;
    }
       
    /**
     * Returns the manifest object of the JAR file
     */
    public ArchiveManifest getArchiveManifest() {
        return this.manifest;
    }
    
    /**
     * Returns the time (in milliseconds since the epoch) this file was last
     * modified.
     */
    public long getLastModified() {
        /* Fetch the entry, if it does not exist, return -1 */
        String   path  = this.getCanonicalName();
        JarEntry entry = this.getArchiveManifest().getJarEntry(path);
        return (entry != null) ? entry.getTime() : -1;
    }
    
    /**
     * Returns the canonical name for this cell
     */
    public String getCanonicalName() {
        return this.canonicalName;
    }
    
    /**
     * Returns the instance of a subclass of the CellProperties class that is
     * decoded from the XML file representation.
     *
     * @throw FileNotFoundException If the file cannot be read
     * @throw InvalidWFSCellException If the cell in the file is invalid
     */
    public <T extends CellMOSetup> T decode() throws FileNotFoundException, InvalidWFSCellException {
        T setup = null;
        
        /*
         * Decode the XML file from disk and return the Properties class read
         */
        try {
            String      fname = this.getCanonicalName();
            System.out.println("READING CELL: " + fname);
            InputStream ais   = this.manifest.getEntryInputStream(fname);
            XMLDecoder  d     = new XMLDecoder(new BufferedInputStream(ais));
            d.setExceptionListener(this);
            setup = (T) d.readObject();
            d.close();
        } catch (java.lang.Exception excp) {
            /* Ignore any exception here and just set the class to null */
            System.out.println(excp.toString());
            setup = null;
        }
        
        /* If the object returned is invalid, check if null */
        if (setup == null) {
            throw new InvalidWFSCellException("Invalid Cell from file: " + this.getCellName());
        }
        
        /* Invoke the validate method to make sure all of the properties are consistent */
        try {
            setup.validate();
            return setup;
        } catch (InvalidCellMOSetupException icgse) {
            throw new InvalidWFSCellException(icgse);
        }
    }
    
    /**
     * Handles when an exception is thrown by the XML decoding mechanism, to
     * supress any warnings it may print.
     */
    public void exceptionThrown(Exception e) {
        // log a warning
        logger.log(Level.WARNING, "Error processing WFS properties " + 
                   canonicalName, e);
    }
}
