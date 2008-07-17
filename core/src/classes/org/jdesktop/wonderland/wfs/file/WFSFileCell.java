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

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import org.jdesktop.wonderland.server.setup.BasicCellMOSetup;
import org.jdesktop.wonderland.wfs.InvalidWFSCellException;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.wfs.WFSCellDirectory;
import org.jdesktop.wonderland.wfs.WFSCellNotLoadedException;


/**
 * The WFSCell class represents an XML file on disk corresponding to the
 * definition of Wonderland cell. This class extends WFSCell.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSFileCell extends WFSCell implements ExceptionListener {
    
    /* The File of the referring WFS Directory */
    private File file = null;
    
    /*
     * The directory of child cells. This object is null until the directory
     * exist.
     */
    private WFSCellDirectory cellDirectory = null;

    /* The cell's setup object, null if it has not yet been read */
    private BasicCellMOSetup cellSetup = null;
    
    /** Creates a new instance of WFSCell */
    public WFSFileCell(File file, WFSCellDirectory parentCellDirectory) {
        super(parentCellDirectory);
        this.file = file;
        
        /* Parse out the name of the cell, assuming it has the proper suffix */
        try {
            int index = file.getName().indexOf(WFS.CELL_FILE_SUFFIX);
            this.setCellName(file.getName().substring(0, index));
            this.setCanonicalName(file.getCanonicalPath());
            this.setPathName(file.getPath());
        } catch (java.lang.IndexOutOfBoundsException excp) {
            WFS.getLogger().log(Level.SEVERE, "A WFSCell class was created with an invalid file");
            WFS.getLogger().log(Level.SEVERE, "file name: " + file.getName());
        } catch (java.io.IOException excp) {
            WFS.getLogger().log(Level.SEVERE, "A WFSCell class was created with an invalid file");
            WFS.getLogger().log(Level.SEVERE, "file name: " + file.getName());
        }
        
        /*
         * Check to see if an associated child directory exists, and create the
         * object if it does.
         */
        WFSFileCellDirectory fileCellDirectory = new WFSFileCellDirectory(this);
        if (fileCellDirectory.exists() == true) {
            this.cellDirectory = fileCellDirectory;
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
     * Creates a directory that will contain children of this cell. Depending
     * upon the type of WFS, this routine may either update a file system
     * immediately, or simply store the update in memory. Returns the object
     * representing the new directory.
     * 
     * @return A WFSCellDirectory object representing the new directory
     */
    public WFSCellDirectory createCellDirectory() {
        /*
         * Simply create the object -- the actual directory does not get
         * created until a write() happens.
         */
        this.cellDirectory = new WFSFileCellDirectory(this);
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
     * Calculate the last modified date of the file this cell represents
     * @return the time this file was last modified
     */
    @Override
    public long getLastModified() {
        return getFile().lastModified();
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
     * 
     * @throw InvalidWFSCellException If the cell's setup is not valid
     * @throw IOException Upon general I/O error
     */
    public void writeCell() throws IOException, InvalidWFSCellException {
        /*
         * Checks to see whether we should update the cell's setup class on
         * disk. If so, then write and clear the update flag. If not, do
         * nothing.
         */
        if (this.isCellSetupUpdate() == true) {
            this.encode(this.cellSetup);
            this.clearCellSetupUpdate();
        }
    }
    
    /**
     * Updates the contents of the cell to the underlying medium, and recursively
     * for all of its children cells
     * 
     * @throw InvalidWFSCellException If the cell's setup is not valid
     * @throw IOException Upon general I/O error
     */
    public void write() throws IOException, InvalidWFSCellException {
        /* Updates the cell's information */
        this.writeCell();
        
        /*
         * If the child cell directory exists, then see if it exists. If not,
         * create it, and ask it to update itself. 
         */
        if (this.cellDirectory != null) {
            if (((WFSFileCellDirectory)this.cellDirectory).exists() == false) {
                ((WFSFileCellDirectory)this.cellDirectory).mkdir();
            }
            
            try {
                this.cellDirectory.write();
            } catch (WFSCellNotLoadedException excp) {
                // log a warning and continue
                WFS.getLogger().log(Level.WARNING, "The WFS directory has not been loaded: " + this.cellDirectory);
            }
        }
        else {
            /*
             * The cell directory should not exist, so remove it if it does
             */
            WFSFileCellDirectory fileCellDirectory = new WFSFileCellDirectory(this);
            fileCellDirectory.delete();
        }
    }

    /**
     * Handles when an exception is thrown by the XML decoding mechanism, to
     * supress any warnings it may print.
     */
    public void exceptionThrown(Exception e) {
        WFS.getLogger().log(Level.WARNING, "Error processing WFS config file " + file, e);
    }
        
    /**
     * Returns the File associated with this WFS file
     */
    private File getFile() {
        return this.file;
    }
        
    /**
     * Returns the instance of a subclass of the CellProperties class that is
     * decoded from the XML file representation.
     *
     * @throw FileNotFoundException If the file cannot be read
     * @throw InvalidWFSCellException If the cell in the file is invalid
     */
    private <T extends BasicCellMOSetup> T decode() throws FileNotFoundException, InvalidWFSCellException {
        /*
         * Decode the XML file from disk and return the Properties class read
         */
        T setup = null;
        try {
            FileInputStream fis     = new FileInputStream(this.getFile());
            XMLDecoder      d       = new XMLDecoder(new BufferedInputStream(fis));
            d.setExceptionListener(this);
            setup = (T) d.readObject();
            d.close();
        } catch (java.lang.Exception excp) {
            /* Ignore any exception here and just set the class to null */
            setup = null;
        }
        
        /* If the object returned is invalid, check if null */
        if (setup == null) {
            throw new InvalidWFSCellException("Invalid Cell from file: " + this.getFile());
        }
        
        /* Invoke the validate method to make sure all of the properties are consistent
        try {
            setup.validate();
            return setup;
        } catch (InvalidCellGLOSetupException icgse) {
            throw new InvalidWFSCellException(icgse);
        }*/
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
    private <T extends BasicCellMOSetup> void encode(T cellSetup) throws IOException, InvalidWFSCellException {
        FileOutputStream fis = new FileOutputStream(this.getFile());
        XMLEncoder       e   = new XMLEncoder(fis);
           
        e.setExceptionListener(this);
        e.writeObject(cellSetup);
        e.close();
    }
}
