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
package org.jdesktop.wonderland.wfs.memory;

import java.io.FileNotFoundException;
import org.jdesktop.wonderland.server.setup.BasicCellMOSetup;
import org.jdesktop.wonderland.wfs.InvalidWFSCellException;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.wfs.WFSCellDirectory;


/**
 * The WFSMemoryCell class represents a WFS cell that resides in memory.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSMemoryCell extends WFSCell {
    /*
     * The directory of child cells. This object is null until the directory
     * exist.
     */
    private WFSCellDirectory cellDirectory = null;

    /* The cell's setup object, null if it has not yet been read */
    private BasicCellMOSetup cellSetup = null;
    
    /** Creates a new instance of WFSMemoryCell */
    public WFSMemoryCell(String pathName, String cellName, WFSCellDirectory parentCellDirectory) {
        super(parentCellDirectory);
        this.setCellName(cellName);
        this.setPathName(pathName + "/" + cellName + WFS.CELL_FILE_SUFFIX);
        this.setCanonicalName(pathName + "/" + cellName);
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
     * Calculate the last modified date of the file this cell represents -- for
     * cells in memory, this is always zero.
     * 
     * @return The time this file was last modified, always zero.
     */
    @Override
    public long getLastModified() {
        return 0;
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
        this.cellDirectory = new WFSMemoryCellDirectory(this);
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
        if (this.cellSetup != null) {
            Class clazz = this.cellSetup.getClass();
            return (T) clazz.cast(this.cellSetup);
        }
        return null;
    }
    
    /**
     * Updates the cell's properties in memory.
     * 
     * @param cellSetup The cell properties class
     * @throw InvalidWFSCellException If the cell properties is invalid
     */
    public <T extends BasicCellMOSetup> void setCellSetup(T cellSetup) throws InvalidWFSCellException {
        this.cellSetup = cellSetup;
    }
    

    /**
     * Updates the contents of the cell to the underlying medium.
     */
    public void writeCell() {
        // Not supported, single nothing to write to!
        throw new UnsupportedOperationException("Not supported. Please use WFS.writeTo()");
    }
    
    /**
     * Updates the contents of the cell to the underlying medium, and recursively
     * for all of its children cells.
     */
    public void write() {
        // Not supported, single nothing to write to!
        throw new UnsupportedOperationException("Not supported. Please use WFS.writeTo()");
    }
}
