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
package org.jdesktop.wonderland.wfs.archive;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import org.jdesktop.wonderland.server.setup.BasicCellMOSetup;
import org.jdesktop.wonderland.wfs.InvalidWFSCellException;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.wfs.WFSCellDirectory;



/**
 * The WFSArchiveCell class represents a cell within an archive (jar) file. This
 * class extends the WFSCell class.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSArchiveCell extends WFSCell implements ExceptionListener {
    
    /* The manifest file that permits access to JAR file entries */
    private ArchiveManifest manifest;
     
    /*
     * The directory of child cells. This object is null until the directory
     * exist.
     */
    private WFSCellDirectory cellDirectory = null;

    /* The cell's setup object, null if it has not yet been read */
    private BasicCellMOSetup cellSetup = null;
    
    /**
     * Creates a new instance of WFSArchive cell class. This constructor takes
     * an instance of the JAR manifest, the canonical path name of the directory
     * in which it is contained and the name of the cell (with the '-wlc.xml'
     * extension.
     */
    public WFSArchiveCell(ArchiveManifest manifest, String pathName,
            String cellName, WFSCellDirectory parentCellDirectory) {
        
        super(parentCellDirectory);
        this.manifest = manifest;
        
        /* Parse out the name of the cell, assuming it has the proper suffix */
        try {
            int index = cellName.indexOf(WFS.CELL_FILE_SUFFIX);
            this.setCellName(cellName.substring(0, index));
            this.setPathName(pathName + "/" + cellName);
            this.setCanonicalName(pathName + "/" + this.getCellName());
        } catch (java.lang.IndexOutOfBoundsException excp) {
            WFS.getLogger().log(Level.SEVERE, "A WFSCell class was created with an invalid file");
            WFS.getLogger().log(Level.SEVERE, "file name: " + this.getCanonicalName());
        }
        
        /*
         * Check to see if an associated child directory exists, and create the
         * object if it does.
         */
        WFSArchiveCellDirectory archiveCellDirectory = new WFSArchiveCellDirectory(this, manifest);
        if (archiveCellDirectory.exists() == true) {
            this.cellDirectory = archiveCellDirectory;
        }
    }

    /**
     * Returns the directory containing any children of this cell, null if no
     * such directory exists.
     * 
     * @return The child directory for this cell, null if none
     */
    public WFSCellDirectory getCellDirectory() {
        return this.cellDirectory;
    }
    
    /**
     * Calculate the last modified date of the file this cell represents
     * @return the time this file was last modified
     */
    @Override
    public long getLastModified() {
        /* Fetch the entry, if it does not exist, return -1 */
        String   path  = this.getPathName();
        JarEntry entry = this.getArchiveManifest().getJarEntry(path);
        return (entry != null) ? entry.getTime() : -1;
    }
    
    /**
     * Creates a directory that will contain children of this cell. Depending
     * upon the type of WFS, this routine may either update a file system
     * immediately, or simply store the update in memory. Returns the object
     * representing the new directory.
     * 
     * @return A WFSCellDirectory object representing the new directory
     * @throw IOException Upon an exception creating the new directory
     */
    public WFSCellDirectory createCellDirectory() {
        /*
         * Simply create the object -- the actual directory does not get
         * created until a write() happens.
         */
        this.cellDirectory = new WFSArchiveCellDirectory(this, this.getArchiveManifest());
        return this.cellDirectory;
    }
    
    /**
     * Removes the directory containing all of the children. If this directory
     * does not exist, then this method does nothing.
     */
    public void removeCellDirectory() {
         /* Simply set the object to null -- to be removed upon a write() */
        this.cellDirectory = null;
    }
 
       /**
     * Returns the instance of a subclass of the BasisCellSetup class that is
     * decoded from the XML file representation.
     *
     * @return A class representing the cell's properties
     * @throw FileNotFoundException If the file cannot be read
     * @throw InvalidWFSCellException If the cell in the file is invalid
     */
    public <T extends BasicCellMOSetup> T getCellSetup() throws FileNotFoundException, InvalidWFSCellException {
        /* If the cell's setup has already been read, then return it */
        if (this.cellSetup != null) {
            Class clazz = this.cellSetup.getClass();
            return (T)clazz.cast(this.cellSetup);
        }
        
        /* Otherwise, read it in from disk and return it */
        T newCellSetup = (T)this.decode();
        this.cellSetup = newCellSetup;
        this.clearCellSetupUpdate();
        return newCellSetup;
    }
    
    /**
     * Updates the cell's properties in memory.
     * 
     * @param cellSetup The cell properties class
     * @throw InvalidWFSCellException If the cell properties is invalid
     */
    public <T extends BasicCellMOSetup> void setCellSetup(T cellSetup) throws InvalidWFSCellException {
        /*
         * Sets the cell setup class and indicates the cell to be udpated upon
         * the next write
         */
        this.cellSetup = cellSetup;
        this.setCellSetupUpdate();
    }
    

    /**
     * Updates the contents of the cell to the underlying medium.
     */
    public void writeCell() {
        // Not yet supported
        throw new UnsupportedOperationException("Not yet supported.");
    }
    
    /**
     * Updates the contents of the cell to the underlying medium, and recursively
     * for all of its children cells
     */
    public void write() {
        // Not yet supported
        throw new UnsupportedOperationException("Not yet supported.");
    }

    /**
     * Returns the instance of a subclass of the CellProperties class that is
     * decoded from the XML file representation.
     *
     * @throw FileNotFoundException If the file cannot be read
     * @throw InvalidWFSCellException If the cell in the file is invalid
     */
    private <T extends BasicCellMOSetup> T decode() throws FileNotFoundException, InvalidWFSCellException {
        T setup = null;
        
        /*
         * Decode the XML file from disk and return the Properties class read
         */
        try {
            String      fname = this.getPathName();
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
    private <T extends BasicCellMOSetup> void encode(T cellSetup) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Handles when an exception is thrown by the XML decoding mechanism, to
     * supress any warnings it may print.
     */
    public void exceptionThrown(Exception e) {
        WFS.getLogger().log(Level.WARNING, "Error processing WFS properties " +  this.getCellName(), e);
    }
           
    /**
     * Returns the manifest object of the JAR file
     */
    private ArchiveManifest getArchiveManifest() {
        return this.manifest;
    }
}
