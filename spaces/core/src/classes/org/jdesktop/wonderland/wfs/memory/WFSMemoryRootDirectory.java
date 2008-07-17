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

import java.io.IOException;
import org.jdesktop.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.wfs.WFSRootDirectory;

/**
 * The WFSMemoryRootDirectory represents the root directory for all memory-
 * based WFS file systems. It extends WFSRootDirectory.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSMemoryRootDirectory extends WFSRootDirectory {
    
    /* Create a WFSMemoryCellDirectory to use as a delegate */
    private WFSMemoryCellDirectory fileCellDirectory = null;
    
    /** Constructor */
    public WFSMemoryRootDirectory(String pathName) {
        super();
        fileCellDirectory = new WFSMemoryCellDirectory(pathName);
        this.setPathName(pathName);
    }
    
   /**
     * Returns an array of string representing the cell names in the current
     * directory.
     * 
     * @return An array of cell names contained within this directory
     */
    public String[] getCellNames() {
        return this.fileCellDirectory.getCellNames();
    }
    
    /**
     * Returns a cell given its name, returns null if it does not exist.
     * 
     * @return The cell given its name, null if it does not exist
     */
    public WFSCell getCellByName(String cellName) {
        return this.fileCellDirectory.getCellByName(cellName);
    }
    
    /**
     * Returns an array the WFSCell class representing all of the cells in the
     * current directory.
     * 
     * @return An array of cells containing within this directory
     */
    public WFSCell[] getCells() {
        return this.fileCellDirectory.getCells();
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
    public WFSCell addCell(String cellName) {
        return this.fileCellDirectory.addCell(cellName);
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
        this.fileCellDirectory.removeCell(cell);
    }
    
    /**
     * Writes all of the cells in this directory to the underlying medium. The
     * list of cells must first be loaded (e.g. by calling getCells()), otherwise
     * a WFSCellNotLoadedException is throw.
     */
    public void write() {
        // Not supported, since nothing to write to!
        throw new UnsupportedOperationException("Not supported. Please use WFS.writeTo()");
    }
    
    /**
     * Writes the WFS meta-information (e.g. version, aliases) to the WFS.
     */
    public void writeMetaData() throws IOException {
        // Not supported, since nothing to write to!
        throw new UnsupportedOperationException("Not supported. Please use WFS.writeTo()");        
    } 
}
