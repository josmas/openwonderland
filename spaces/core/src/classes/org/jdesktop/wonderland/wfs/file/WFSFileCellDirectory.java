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

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import org.jdesktop.wonderland.wfs.InvalidWFSCellException;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.wfs.WFSCellDirectory;
import org.jdesktop.wonderland.wfs.WFSCellNotLoadedException;

/**
 * The WFSFileCellDirectory is a directory on disk that contains child cells
 * within a WFS. This class extends the WFSCellDirectory abstract base class.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSFileCellDirectory extends WFSCellDirectory {
    /* The File of the referring WFS Directory */
    private File file = null;
    
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
    public WFSFileCellDirectory(WFSCell associatedCell) {
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
        
        /* Create a file object representing the directory */
        this.file = new File(this.getPathName());
    }
    
    /**
     * Creates a new instance of WFSCellDirectory, takes the File of the
     * directory. This constructor is meant for WFSRootCellDirectory classes
     * that do not have a cell associated with them
     */
    public WFSFileCellDirectory(File file) {
        super(null);
        this.file = file;
        this.setPathName(file.getPath());
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
        
        /* List all of the files in the current directory subject to the filter */
        File[] files = this.getFile().listFiles(FileWFS.CELL_FILE_FILTER);
        
        WFS.getLogger().log(Level.INFO, "WFS File List: " + files.length +
            " files found in " + this.getFile().getName());
        
        /* For each entry, create the hashmap and fill it in */
        this.children = new HashMap<String, WFSCell>();
        for (File currentFile : files) {
            WFSCell cell = new WFSFileCell(currentFile, this);
            this.children.put(cell.getCellName(), cell);
            WFS.getLogger().log(Level.INFO, "File filter found " + currentFile);
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
        /* If the directory does not yet exist, we simply create the hashmap */
        if (this.exists() == false && this.children == null) {
            this.children = new HashMap<String, WFSCell>();
        }
        else if (this.children == null) {
            /* If the hashmap is null, we must load in the names first */
            this.getCellNames();
        }

        /*
         * Create a new WFSFileCell object, which takes a File object. We should
         * be able to create the File object even if the actual file does not
         * yet exist.
         */
        String      path    = this.getPathName() + "/" + cellName + WFS.CELL_FILE_SUFFIX;
        File        newFile = new File(path);
        WFSFileCell cell    = new WFSFileCell(newFile, this);
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
       /* If the directory does not yet exist, we simply create the hashmap */
        if (this.exists() == false && this.children == null) {
            this.children = new HashMap<String, WFSCell>();
        }
        else if (this.children == null) {
            /* If the hashmap is null, we must load in the names first */
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
        /* Create the directory if it does not yet exist */
        if (this.exists() == false) {
            this.mkdir();
        }
        
        /* If the hashmap is null, throw an exception */
        if (this.children == null) {
            throw new WFSCellNotLoadedException();
        }

        /*
         * Iterate through all of the cells and write them out. It is ok if
         * not all of them have been parsed yet, simply fail gracefully and
         * continue where possible.
         */
        for (WFSCell cell : this.children.values()) {
            try {
                cell.write();
            } catch (java.io.IOException excp) {
                // log some error and continue
                WFS.getLogger().log(Level.WARNING, "Unable to write cell to WFS: " + cell);
            } catch (InvalidWFSCellException excp) {
                // log some error and continue
                WFS.getLogger().log(Level.WARNING, "Unable to write cell to WFS: " + cell);
            }
        }
        
        /*
         * Finally, delete any cells that exist within the directory, but are
         * not present in the current list of cells.
         */
        File[] files = this.getFile().listFiles(FileWFS.CELL_FILE_FILTER);
        for (File oldFile : files) {
            /*
             * Parse out the cell name and see if it exists, delete if not.
             */
            int    index    = oldFile.getName().indexOf(WFS.CELL_FILE_SUFFIX);
            String cellName = oldFile.getName().substring(0, index);
            if (this.children.containsKey(cellName) == false) {
                /*
                 * Ignore an errors upon deletion.
                 * XXX Perhaps we should report upon this
                 */
                oldFile.delete();
                
                /*
                 * Also check to see if an associated cell directory exists
                 * and delete it too.
                 */
                File dir = new File(cellName + WFS.CELL_DIRECTORY_SUFFIX + "/");
                if (dir.exists() == true && dir.isDirectory() == true) {
                    dir.delete();
                }
            }
        }
    }
    
    /**
     * Returns the File object associated with this WFS directory.
     */
    protected File getFile() {
        return this.file;
    }
    
    /**
     * Returns true if the directory actually exists on disk and is a directory
     */
    protected boolean exists() {
        return (this.file.exists() == true && this.file.isDirectory() == true);
    }
    
    /**
     * Delete the actual directory if it does exist
     */
    protected boolean delete() {
        return WFSFileUtils.deleteDirectory(this.file);
    }
    
    /**
     * Create the directory.
     */
    protected boolean mkdir() {
        return this.file.mkdir();
    }
}
