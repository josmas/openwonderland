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

import java.util.HashMap;
import java.util.logging.Level;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.wfs.WFSCellDirectory;

/**
 * The WFSMemoryCellDirectory is a directory that contains child cells
 * within a WFS. This class extends the WFSCellDirectory abstract base class.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSMemoryCellDirectory extends WFSCellDirectory {
    /*
     * A Hashmap of cell names (keys) and WFSCell objects (values) contained
     * within this directory. This hashmap is initially null, indicating that
     * the directory has not been read yet.
     */
    private HashMap<String, WFSCell> children = new HashMap<String, WFSCell>();
    
    /**
     * Creates a new instance of WFSCellDirectory, takes the File of the referring
     * directory as an argument
     */
    public WFSMemoryCellDirectory(WFSCell associatedCell) {
        super(associatedCell);

        /* Compute what the path name of this directory should be */
        try {
            String cellPathWithSuffix = associatedCell.getPathName();
            int    index              = cellPathWithSuffix.indexOf(WFS.CELL_FILE_SUFFIX);
            String cellPath           = cellPathWithSuffix.substring(0, index);
            this.setPathName(cellPath + WFS.CELL_DIRECTORY_SUFFIX + "/");
        } catch (java.lang.IndexOutOfBoundsException excp) {
            WFS.getLogger().log(Level.SEVERE, "A WFSCellDirectory class was created with an invalid cell");
            WFS.getLogger().log(Level.SEVERE, "cell path: " + associatedCell.getPathName());
        }
    }
    
    /**
     * Creates a new instance of WFSCellDirectory, takes the File of the
     * directory. This constructor is meant for WFSRootCellDirectory classes
     * that do not have a cell associated with them
     */
    public WFSMemoryCellDirectory(String pathName) {
        super(null);
        this.setPathName(pathName);
    }

    /**
     * Returns an array of string representing the cell names in the current
     * directory.
     * 
     * @return An array of cell names contained within this directory
     */
    public String[] getCellNames() {
        return this.children.keySet().toArray(new String[] {});
    }
    
    /**
     * Returns a cell given its name, returns null if it does not exist.
     * 
     * @return The cell given its name, null if it does not exist
     */
    public WFSCell getCellByName(String cellName) {
        return this.children.get(cellName);
    }
    
    /**
     * Returns an array the WFSCell class representing all of the cells in the
     * current directory.
     * 
     * @return An array of cells containing within this directory
     */
    public WFSCell[] getCells() {
        return this.children.values().toArray(new WFSCell[] {});
    }

    /**
     * Adds a cell to this directory. Depending upon the type of WFS created,
     * this method either immediately updates the underlying WFS on disk or
     * it updates the WFS in memory. Takes the name of the cell and its
     * properties; a new WFSCell class is returned. If the cell already exists,
     * then this method overwrites the existing cell.
     * 
     * @param cellName The name of the new cell to add
     * @return The class representing the new cell
     */
    public WFSCell addCell(String cellName) {
        /*
         * Create a new WFSMemoryCell object, which takes a File object. We should
         * be able to create the File object even if the actual file does not
         * yet exist.
         */
        WFSMemoryCell cell = new WFSMemoryCell(this.getPathName(), cellName, this);
        this.children.put(cellName, cell);
        return cell;
    }
    
    /**
     * Removes a cell from this directory, if it exists. If it does not exist,
     * this method does nothing. Depending upon the type of WFS created, this
     * method either immediately updates the underyling WFS on disk or it
     * updates the WFS in memory.
     * 
     * @param cell The cell to remove
     * @throw IOException Upon I/O error when removing the cell
     */
    public void removeCell(WFSCell cell) {
        /* Remove the cell from the hashmap, ignore if it does not exist */
        this.children.remove(cell.getCellName());
    }
    
    /**
     * Writes all of the cells in this directory to the underlying medium. The
     * list of cells must first be loaded (e.g. by calling getCells()), otherwise
     * a WFSCellNotLoadedException is throw.
     */
    public void write() {
        // Not supported, single nothing to write to!
        throw new UnsupportedOperationException("Not supported. Please use WFS.writeTo()");
    }
}
