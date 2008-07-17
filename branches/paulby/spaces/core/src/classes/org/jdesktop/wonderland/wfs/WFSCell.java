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

import java.io.FileNotFoundException;
import java.io.IOException;
import org.jdesktop.wonderland.server.setup.BasicCellMOSetup;

/**
 * The WFSCell abstract class represents a single cell in a WFS. Each cell is
 * identified by a name that corresponds to the name given the file contained
 * within the WFS (without the '-wlc.xml' extension). Each cell is also given
 * a canonical name that can be used to uniquely identify the cell within the
 * WFS. The cell name (and canonical name) are immutable--to rename a cell, one
 * must create a new WFSCell object and delete the old object.
 * <p>
 * If the WFSCell has children, they reside in the corresponding directory (with
 * the '-wld' extension), a handle to the directory is obtained by calling the
 * getCellDirectory() method. The actual directory is not referenced until this
 * method is called for the first time. 
 * <p>
 * The writeCell() method updates the cell data on the underlying medium. It
 * does not update any of its children. The write() method updates the cell
 * data on the underlying medium and recursively updates all of its children
 * too.
 *
 * <h3>Cell Properties<h3>
 * 
 * The properties of the cell are not read until explictly asked to do so, via
 * the getCellSetup() method. This method returns a subclass of BasicCellSetup.
 * <p>
 * The cell properties object may be set via the setCellSetup() method, although
 * modifying the cell setup class itself will also update a cell's properties.
 * When a WFS is updated on the underlying medium, the cell property class,
 * because it can be a time-consuming operation, is not automatically updated. 
 * (Noly only may the serlialization to XML be time-consuming, but the 'last
 * modified' stamp on the cell gets updates upon each write too, causing the
 * cell to always be reloaded by Wonderland. The 'reload' is a time-consuming
 * process and not necessary if the cell did not change.)
 * <p>
 * The WFS API follows the following algorithm to determine whether to update
 * the cell properties on the underlying medium:
 * <p>
 * 1. If the setCellSetup() method is invoked since the last write, then the
 *    cell properties are written.
 * 2. If the setCellSetupUpdate() method is invoked since the last write, then
 *    the cell properties are written.
 * <p>
 * If a write is performed before the cell property's are explicitly read, then
 * the write method throws WFSCellNotLoadedException, and the write stops.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class WFSCell {
    /* The cell and canonical name of the cell (no suffix) */
    private String cellName = null;
    private String canonicalName = null;
    
    /* The path name of the cell (with the suffix) */
    private String pathNameWithSuffix = null;
    
    /* True if the cell's setup should be written upon the next write() */
    private boolean cellSetupUpdate = false;
    
    /* The parent cell directory that contains this cell */
    private WFSCellDirectory parentCellDirectory = null;
    
    /** Default constructor, should never be called */
    protected WFSCell(WFSCellDirectory parentCellDirectory) {
        this.parentCellDirectory = parentCellDirectory;
    }
    
    /**
     * Returns the parent cell directory, that is, the cell directory in which
     * this cell is contained. If this cell is contained in the root of the
     * WFS, then the root directory is returned.
     * 
     * @return The parent cell directory
     */
    public WFSCellDirectory getParentCellDirectory() {
        return this.parentCellDirectory;
    }
    
    /**
     * Returns the parent cell. If this cell is in the root directory, then
     * this method returns null.
     * 
     * @return The parent cell, null if in the root directory
     */
    public WFSCell getParentCell() {
        return this.parentCellDirectory.getAssociatedCell();
    }
    
    /**
     * Returns the canonical name for this cell, which is guaranteed to unique
     * identify it within the WFS.
     * 
     * @return A unique, canonical name for the cell
     */
    public String getCanonicalName() {
        return this.canonicalName;
    }
    
    /**
     * Sets the canonical name for this cell.
     *
     * @param canonicalName The cell's unique name
     */
    protected void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    /**
     * Returns the name of the cell, without the standard suffix
     * 
     * @return The name of the cell file in the WFS
     */
    public String getCellName() {
        return this.cellName;
    }

    /**
     * Sets the name of the cell
     *
     * @param cellName The name of the cell 
     */
    public void setCellName(String cellName) {
        this.cellName = cellName;
    }
    
    /**
     * Returns the directory containing any children of this cell, null if no
     * such directory exists.
     * 
     * @return The child directory for this cell, null if none
     */
    public abstract WFSCellDirectory getCellDirectory();
    
    /**
     * Creates a directory that will contain children of this cell. Depending
     * upon the type of WFS, this routine may either update a file system
     * immediately, or simply store the update in memory. Returns the object
     * representing the new directory.
     * 
     * @return A WFSCellDirectory object representing the new directory
     */
    public abstract WFSCellDirectory createCellDirectory();

    /**
     * Removes the directory containing all of the children. If this directory
     * does not exist, then this method does nothing.
     */
    public abstract void removeCellDirectory();

    /**
     * Returns the time (in milliseconds since the epoch) this file was last
     * modified.
     * 
     * @return The last time the cell was modified in the WFS
     */
    public abstract long getLastModified();
    
    /**
     * Returns the instance of a subclass of the BasisCellSetup class that is
     * decoded from the XML file representation.
     *
     * @return A class representing the cell's properties
     * @throw FileNotFoundException If the file cannot be read
     * @throw InvalidWFSCellException If the cell in the file is invalid
     */
    public abstract <T extends BasicCellMOSetup> T getCellSetup() throws FileNotFoundException, InvalidWFSCellException;
    
    /**
     * Updates the cell's properties in memory.
     * 
     * @param cellSetup The cell properties class
     * @throw InvalidWFSCellException If the cell properties is invalid
     */
    public abstract <T extends BasicCellMOSetup> void setCellSetup(T cellSetup) throws InvalidWFSCellException;
    
    /**
     * Indicates that this cell's setup class should be updated upon the next
     * write() invocation, even if the setup parameters have not changed.
     */
    public void setCellSetupUpdate() {
        this.cellSetupUpdate = true;
    }
    
    /**
     * Returns true if the cell's setup class should be updated upon the next
     * write() invocation.
     * 
     * @return True to update the cell's setup
     */
    public boolean isCellSetupUpdate() {
        return this.cellSetupUpdate;
    }

    /**
     * Clears the flag to indicate whether the cell should be updated upon the
     * next write. This is not a public method on the API.
     */
    protected void clearCellSetupUpdate() {
        this.cellSetupUpdate = false;
    }
    
    /**
     * Updates the contents of the cell to the underlying medium.
     * 
     * @throw InvalidWFSCellException If the cell's setup is not valid
     * @throw IOException Upon general I/O error
     */
    public abstract void writeCell() throws IOException, InvalidWFSCellException;
    
    /**
     * Updates the contents of the cell to the underlying medium, and recursively
     * for all of its child cell
     * 
     * @throw InvalidWFSCellException If the cell's setup is not valid
     * @throw IOException Upon general I/O error
     */
    public abstract void write() throws IOException, InvalidWFSCellException;
    
    /**
     * Sets the full path name (with the '-wlc.xml' suffix) of this cell within
     * the WFS. This method is meant only for subclasses; it is not part of the
     * public API.
     */
    public void setPathName(String pathNameWithSuffix) {
        this.pathNameWithSuffix = pathNameWithSuffix;
    }
    
    /**
     * Returns the path name (with the '-wlc.xml' suffix) of this cell within
     * the WFS. This method is meant only for subclasses; it is not part of the
     * public API.
     */
    public String getPathName() {
        return this.pathNameWithSuffix;
    }
}
