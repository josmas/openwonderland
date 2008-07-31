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
 * $Revision: 1.3.8.4 $
 * $Date: 2008/04/08 10:44:29 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.wfs.archive;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.JarEntry;
import org.jdesktop.wonderland.cells.BasicCellSetup;
import org.jdesktop.wonderland.wfs.InvalidWFSCellException;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.wfs.delegate.CellDelegate;
import org.jdesktop.wonderland.wfs.delegate.DirectoryDelegate;

/**
 * The WFSArchiveCell class represents a cell within an archive (jar) file. This
 * class extends the WFSCell class.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ArchiveCellDelegate implements CellDelegate {
    
    /* The manifest file that permits access to JAR file entries */
    private ArchiveManifest manifest = null;
    
    /* The full pathname inside the maniest of the cell */
    private String pathName = null;
    
    /**
     * Creates a new instance of WFSArchive cell class. This constructor takes
     * an instance of the JAR manifest, the canonical path name of the directory
     * in which it is contained and the name of the cell (with the '-wlc.xml'
     * extension.
     */
    public ArchiveCellDelegate(ArchiveManifest manifest, String pathName) {        
        this.manifest = manifest;
        this.pathName = pathName;
    }
    
    /**
     * Calculate the last modified date of the file this cell represents
     * @return the time this file was last modified
     */
    public long getLastModified() {
        /* Fetch the entry, if it does not exist, return -1 */
        JarEntry entry = this.getArchiveManifest().getJarEntry(this.pathName);
        return (entry != null) ? entry.getTime() : -1;
    }
    
    /**
     * Creates a new directory delegate for the cell directory containing the
     * child cells of this cell.
     * 
     * @return A new directory delegate.
     */
    public DirectoryDelegate createDirectoryDelegate() {
        /*
         * Simply create the object -- the actual directory does not get
         * created until a write() happens.
         */
        return new ArchiveDirectoryDelegate(this.getArchiveManifest(), pathName);
    }

    /**
     * Returns the instance of a subclass of the CellProperties class that is
     * decoded from the XML file representation.
     *
     * @throw FileNotFoundException If the file cannot be read
     * @throw InvalidWFSCellException If the cell in the file is invalid
     */
    public <T extends BasicCellSetup> T decode() throws FileNotFoundException, InvalidWFSCellException {
        T setup = null;
        
        /*
         * Decode the XML file from disk and return the Properties class read
         */
        try {
            String fname = this.pathName;
            InputStream ais = this.manifest.getEntryInputStream(fname);
            setup = (T)BasicCellSetup.decode(new InputStreamReader(ais));
        } catch (java.lang.Exception excp) {
            /* Ignore any exception here and just set the class to null */
            System.out.println(excp.toString());
            setup = null;
        }
        
        /* If the object returned is invalid, check if null */
        if (setup == null) {
            throw new InvalidWFSCellException("Invalid Cell from file: " + this.pathName);
        }
        return setup;
    }

    /**
     * Updates the cell's properties. Depending upon the type of the WFS, this
     * method may immediately serialize the cell properties to disk or store
     * them for later serialization.
     * 
     * @param cellSetup The cell properties class
     * @throw IOException Upon general I/O error
     * @throw InvalidWFSCellException If the cell properties is invalid
     */
    public <T extends BasicCellSetup> void encode(T cellSetup) {
        throw new UnsupportedOperationException("Writing a cell to an archive is not supported");
    }

    /**
     * Returns true if the cell directory associated with this cell exists,
     * false if not.
     * 
     * @return True if the cell's directory exists, false if not
     */
    public boolean cellDirectoryExists() {
        /* Parse off the cell file suffix. See if it exists in the manifest */
        String path = WFSCell.stripCellFileSuffix(this.pathName) + WFS.CELL_DIRECTORY_SUFFIX;
        return this.manifest.isValidEntry(path);
    }
    
    /**
     * Creates the cell's directory on the medium, if it does not exist
     */
    public void createCellDirectory() {
        throw new UnsupportedOperationException("Writing a cell to an archive is not supported");
    }
    
    /**
     * Removes the cell's directory on the medium, if it exists.
     */
    public void removeCellDirectory() {
        throw new UnsupportedOperationException("Writing a cell to an archive is not supported");
    }
    
    /**
     * Returns the manifest object of the JAR file
     */
    private ArchiveManifest getArchiveManifest() {
        return this.manifest;
    }
}
