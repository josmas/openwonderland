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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.wfs.WFSCellDirectory;
import org.jdesktop.wonderland.wfs.WFSCellNotLoadedException;

/**
 * The WFSArchiveCellDirectory is a directory within a Wonderland File System
 * (WFS) that corresponds to a cell and corresponds to an entry within a JAR
 * file. A WFSArchiveCellDirectory may contain cells and cell directories.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSArchiveCellDirectory extends WFSCellDirectory {
    /* The manifest file that permits access to JAR file entries */
    private ArchiveManifest manifest;

    /*
     * A Hashmap of cell names (keys) and WFSCell objects (values) contained
     * within this directory. This hashmap is initially null, indicating that
     * the directory has not been read yet.
     */
    private HashMap<String, WFSCell> children = null;
    
    /**
     * Creates a new instance of WFSCellDirectory, takes the File of the referring
     * directory as an argument
     */
    public WFSArchiveCellDirectory(WFSCell associatedCell, ArchiveManifest manifest) {
        super(associatedCell);

        /* Compute what the path name of this directory should be */
        try {
            String cellPathWithSuffix = associatedCell.getPathName();
            int    index              = cellPathWithSuffix.indexOf(WFS.CELL_FILE_SUFFIX);
            String cellPath           = cellPathWithSuffix.substring(0, index);
            this.setPathName(cellPath + WFS.CELL_DIRECTORY_SUFFIX);
        } catch (java.lang.IndexOutOfBoundsException excp) {
            WFS.getLogger().log(Level.SEVERE, "A WFSCellDirectory class was created with an invalid cell");
            WFS.getLogger().log(Level.SEVERE, "cell path: " + associatedCell.getPathName());
        }
        this.manifest = manifest;
    }

    /**
     * Constructor meant for root directories, takes the manifest and the root
     * path as arguments
     */
    public WFSArchiveCellDirectory(ArchiveManifest manifest, String pathName) {
        super(null);
        this.manifest = manifest;
        this.setPathName(pathName);
    }
    
    /**
     * Returns an array of string representing the cell names in the current
     * directory.
     * 
     * @return An array of cell names contained within this directory
     */
    public String[] getCellNames() {
        /* If the hashmap is not null, then simply return it */
        if (this.children != null) {
            return this.children.keySet().toArray(new String[] {});
        }
        
        /*
         * Fetch the entry in the manifest corresponding to this directory,
         * subject to the filter for the cell file suffix.
         */
        ArchiveManifest    manifest = this.getArchiveManifest();
        String             pathName = this.getPathName();
        LinkedList<String> entries  = manifest.getCellEntries(pathName);
        
        WFS.getLogger().log(Level.INFO, "WFS File List: " + entries.size() +
            " files found in " + pathName);
        
        /* For each entry, create the hashmap and fill it in */
        this.children = new HashMap<String, WFSCell>();
        for (String entry : entries) {
            if (entry.endsWith(WFS.CELL_FILE_SUFFIX) == true) {
                WFS.getLogger().log(Level.INFO, "Cell found " + entry);
                WFSCell cell = new WFSArchiveCell(manifest, pathName, entry, this);
                this.children.put(cell.getCellName(), cell);
            }
        }
        return this.children.keySet().toArray(new String[] {});   
    }
    
    /**
     * Returns a cell given its name, returns null if it does not exist.
     * 
     * @return The cell given its name, null if it does not exist
     */
    public WFSCell getCellByName(String cellName) {
        /* If the hashmap is not null, then simply return it */
        if (this.children != null) {
            return this.children.get(cellName);
        }
        
        /* Ask the getCellNames() method to load in the directory and return */
        this.getCellNames();
        return this.children.get(cellName);
    }
    
    /**
     * Returns an array the WFSCell class representing all of the cells in the
     * current directory.
     * 
     * @return An array of cells containing within this directory
     */
    public WFSCell[] getCells() {
        /* If the hashmap is not null, then simply return it */
        if (this.children != null) {
            return this.children.values().toArray(new WFSCell[] {});
        }
        
        /* Ask the getCellNames() method to load in the directory and return */
        this.getCellNames();
        return this.children.values().toArray(new WFSCell[] {});
    }

    /**
     * Adds a cell to this directory. Takes the name of the cell and its
     * properties; a new WFSCell class is returned. If the cell already exists,
     * then this method overwrites the existing cell.
     * 
     * @param cellName The name of the new cell to add
     * @return The class representing the new cell
     */
    public WFSCell addCell(String cellName) {
        /* If the hashmap is null, we must load in the names first */
        if (this.children == null) {
            this.getCellNames();
        }

        /*
         * Create a new WFSArhiveCell object, which takes the path and a new
         * cell name.
         */
        String path = this.getPathName() + "/" + cellName + WFS.CELL_FILE_SUFFIX;
        WFSArchiveCell cell = new WFSArchiveCell(this.getArchiveManifest(),
                this.getPathName(), cellName, this);
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
        /* If the hashmap is null, we must load in the names first */
        if (this.children == null) {
            this.getCellNames();
        }

        /* Remove the cell from the hashmap, ignore if it does not exist */
        this.children.remove(cell.getCellName());
    }
    
    /**
     * Writes all of the cells in this directory to the underlying medium. The
     * list of cells must first be loaded (e.g. by calling getCells()), otherwise
     * a WFSCellNotLoadedException is throw.
     * 
     * @throw WFSCellNotLoadedException If not all of the cells have been loaded
     */
    public void write() throws WFSCellNotLoadedException {
        // Not yet supported
        throw new UnsupportedOperationException("Not yet supported.");
    }
    
    /**
     * Returns the manifest object of the JAR file
     */
    private ArchiveManifest getArchiveManifest() {
        return this.manifest;
    }
    
    /**
     * Returns true if the directory actually exists on disk and is a directory
     */
    protected boolean exists() {
        return manifest.isValidEntry(this.getPathName());
    }
 }
