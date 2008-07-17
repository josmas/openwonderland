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

import java.io.IOException;

/**
 * The WFSCellDirectory abstract class represents the directory within the WFS
 * that stores child cells. (If the cell is named 'cell-wlc.xml' within the
 * WFS, this directory is correspondingly named 'cell-wld/' although these
 * details are hidden by the WFS API.) 
 * <p>
 * The list of child cells are obtained via two method calls: getCellNames()
 * and getCells() method. The list of cells are not read until either one of
 * these is invoked;
 *
 * The list of child cells may be updated via the addCell() and removeCell()
 * methods. This updates the list of children in memory and is not written by
 * to the underlying medium until explicitly told to do so. These methods
 * first load all of the cells, if not already done so. This may be a time-
 * consuming task, so users of this API are strongly encouraged to call the
 * getCells() method themselves at a time of their choosing.
 *
 * <h3>Writring</h3>
 * 
 * The directory on the underlying medium is updated via the write() method.
 * Each cell must have been first loaded -- either by the getCells() or the
 * getCellNames() methods -- otherwise, this method does nothing. If all cells
 * have been loaded, the method simply calls the write() method on each cell.
 * <p>
 * Note: Each individual cell is not written until its setup is de-serialized.
 * A cell to write() on this class may result in some cell's setup parameters
 * being written, others not.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class WFSCellDirectory {
    /* The cell associated with this directory */
    private WFSCell associatedCell = null;
       
    /* The path name of the cell (with the suffix) */
    private String pathNameWithSuffix = null;
    
    /** Constructor, takes the cell associated with this directory */
    protected WFSCellDirectory(WFSCell associatedCell) {
        this.associatedCell = associatedCell;
    }
    
    /**
     * Returns an array of string representing the cell names in the current
     * directory.
     * 
     * @return An array of cell names contained within this directory
     */
    public abstract String[] getCellNames();
    
    /**
     * Returns a cell given its name, returns null if it does not exist.
     * 
     * @return The cell given its name, null if it does not exist
     */
    public abstract WFSCell getCellByName(String cellName);
    
    /**
     * Returns an array the WFSCell class representing all of the cells in the
     * current directory.
     * 
     * @return An array of cells containing within this directory
     */
    public abstract WFSCell[] getCells();

    /**
     * Returns the cell associated with this directory. This cell is the parent
     * of all the cells contained within the directory.
     * 
     * @return The associated cell
     */
    public WFSCell getAssociatedCell() {
        return this.associatedCell;
    }

    /**
     * Adds a cell to this directory. Depending upon the type of WFS created,
     * this method either immediately updates the underlying WFS on disk or
     * it updates the WFS in memory. Takes the name of the cell and its
     * properties; a new WFSCell class is returned.
     * 
     * @param cellName The name of the new cell to add
     * @param cellSetup The properties of the cell
     * @return The class representing the new cell
     * @throw IOException Upon I/O error when adding the new cell
     */
    public abstract WFSCell addCell(String cellName);
    
    /**
     * Removes a cell from this directory, if it exists. If it does not exist,
     * this method does nothing. Depending upon the type of WFS created, this
     * method either immediately updates the underyling WFS on disk or it
     * updates the WFS in memory.
     * 
     * @param cell The cell to remove
     * @throw IOException Upon I/O error when removing the cell
     */
    public abstract void removeCell(WFSCell cell);
    
    /**
     * Writes all of the cells in this directory to the underlying medium. The
     * list of cells must first be loaded (e.g. by calling getCells()), otherwise
     * a WFSCellNotLoadedException is throw.
     * 
     * @throw IOException Upon general I/O error
     * @throw WFSCellNotLoadedException If not all of the cells have been loaded
     */
    public abstract void write() throws IOException, WFSCellNotLoadedException;
    
        
    /**
     * Sets the full path name (with the '-wld' suffix) of this directory within
     * the WFS. This method is meant only for subclasses; it is not part of the
     * public API.
     */
    public void setPathName(String pathNameWithSuffix) {
        this.pathNameWithSuffix = pathNameWithSuffix;
    }
    
    /**
     * Returns the path name (with the '-wld' suffix) of this directory within
     * the WFS. This method is meant only for subclasses; it is not part of the
     * public API.
     */
    public String getPathName() {
        return this.pathNameWithSuffix;
    }
}
