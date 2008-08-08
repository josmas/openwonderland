/**
 * Project Looking Glass
 *
 * $RCSfile: WFSCellDirectory.java,v $
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
 * $Revision: 1.2.8.2 $
 * $Date: 2008/04/08 10:44:30 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.wfs;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.wfs.delegate.CellDelegate;
import org.jdesktop.wonderland.wfs.delegate.DirectoryDelegate;

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
public class WFSCellDirectory {
    /* The cell associated with this directory */
    private WFSCell associatedCell = null;
       
    /* The path name of the cell (with the suffix) */
    private String pathName = null;
    
    /*
     * A Hashmap of cell names (keys) and WFSCell objects (values) contained
     * within this directory. This hashmap is initially null, indicating that
     * the directory has not been read yet.
     */
    protected HashMap<String, WFSCell> children = null;
    
    /* The implementation specific delegate for directories */
    protected DirectoryDelegate delegate = null;
    
    /* A weak reference to the main WFS object */
    protected WeakReference<WFS> wfsRef = null;
    
    /**
     * Constructor, takes the implementation-specific directory delegate
     */
    public WFSCellDirectory(WFS wfs, WFSCell associatedCell, DirectoryDelegate delegate) {
        this.wfsRef = new WeakReference(wfs);
        this.associatedCell = associatedCell;
        this.delegate = delegate;
    
        /* Compute what the path name of this directory should be */
        try {
            String cellPathWithSuffix = associatedCell.getPathName();
            int index = cellPathWithSuffix.indexOf(WFS.CELL_FILE_SUFFIX);
            String cellPath = cellPathWithSuffix.substring(0, index);
            this.pathName = cellPath + WFS.CELL_DIRECTORY_SUFFIX + "/";
        } catch (java.lang.IndexOutOfBoundsException excp) {
            WFS.getLogger().log(Level.SEVERE, "A WFSCellDirectory class was created with an invalid cell");
            WFS.getLogger().log(Level.SEVERE, "cell path: " + associatedCell.getPathName());
        }
    }
    
    /**
     * Constructor, which takes no parent directory (if this is the root
     * directory
     */
    public WFSCellDirectory(WFS wfs, DirectoryDelegate delegate) {
        this.wfsRef = new WeakReference(wfs);
        this.associatedCell = null;
        this.delegate = delegate;
        this.pathName = "";
    }
    
    /**
     * Returns an array of string representing the cell names in the current
     * directory. Returns an empty array if no names exist.
     * 
     * @return An array of cell names contained within this directory
     */
    public String[] getCellNames() {
        /*
         * First attempt to read in the cells first. This simply returns if the
         * children have already been created. We don't need to explicitly
         * acquire a read lock because the loadChildCells() method does so.
         */
        this.loadChildCells();
        return this.children.keySet().toArray(new String[]{});
    }
          
    /**
     * Returns a cell given its name, returns null if it does not exist.
     * 
     * @return The cell given its name, null if it does not exist
     */
    public WFSCell getCellByName(String cellName) {
        /*
         * First attempt to read in the cells first. This simply returns if the
         * children have already been created. We don't need to explicitly
         * acquire a read lock because the loadChildCells() method does so.
         */
        this.loadChildCells();
        return this.children.get(cellName);
    }
    
    /**
     * Returns an array the WFSCell class representing all of the cells in the
     * current directory.
     * 
     * @return An array of cells containing within this directory
     */
    public WFSCell[] getCells() {
        /*
         * First attempt to read in the cells first. This simply returns if the
         * children have already been created. We don't need to explicitly
         * acquire a read lock because the loadChildCells() method does so.
         */
        this.loadChildCells();
        return this.children.values().toArray(new WFSCell[] {});
    }

    /**
     * Returns the cell associated with this directory. This cell is the parent
     * of all the cells contained within the directory.
     * 
     * @return The associated cell
     */
    public WFSCell getAssociatedCell() {
        /*
         * There is no need to protect this with a read lock. Even though a
         * cell can move around within a file system, this is implemented as
         * removing a cell and adding it somewhere else. The 'associatedCell'
         * member variable is invariant in the WFSDirectory class.
         */
        return this.associatedCell;
    }

    /**
     * Adds a cell to this directory. Takes the name of the cell and its
     * properties; a new WFSCell class is returned.
     * 
     * @param cellName The name of the new cell to add
     * @param cellSetup The properties of the cell
     * @return The class representing the new cell
     * @throw IOException Upon I/O error when adding the new cell
     */
    public WFSCell addCell(String cellName) {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        /*
         * First attempt to read in the cells first. This simply returns if the
         * children have already been created. We don't need to explicitly
         * acquire a read lock because: (1) we are in a write lock, (2) the
         * loadChildCells() method does so.
         */
        this.loadChildCells();
        
        /* Call the implementation to create the cell */
        CellDelegate cellDelegate = this.delegate.createCellDelegate(cellName);
        WFSCell cell = new WFSCell(this.wfsRef.get(), cellName, this, cellDelegate);
        this.children.put(cellName, cell);
        
        /* Fire events to indicate a new cell has been added */
        this.wfsRef.get().fireCellChildrenAdded(this.getAssociatedCell());
        return cell;
    }
        
    /**
     * Removes a cell from this directory, if it exists. If it does not exist,
     * this method does nothing.
     * 
     * @param cell The cell to remove
     * @throw IOException Upon I/O error when removing the cell
     */
    public void removeCell(WFSCell cell) {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        /*
         * First attempt to read in the cells first. This simply returns if the
         * children have already been created. We don't need to explicitly
         * acquire a read lock because: (1) we are in a write lock, (2) the
         * loadChildCells() method does so.
         */
        this.loadChildCells();

        /* Remove the cell from the hashmap, ignore if it does not exist */
        this.children.remove(cell.getCellName());
        
        /* Fire events to indicate a cell has been removed -- check to see that is really has been removed! XXX*/
        this.wfsRef.get().fireCellChildrenRemoved(this.getAssociatedCell());
        this.wfsRef.get().fireCellRemoved(cell);
    }
    
    /**
     * Writes all of the cells in this directory to the underlying medium. The
     * list of cells must first be loaded (e.g. by calling getCells()), otherwise
     * a WFSCellNotLoadedException is throw.
     * 
     * @throw IOException Upon general I/O error
     * @throw JAXBException Upon error writing to XML
     * @throw WFSCellNotLoadedException If not all of the cells have been loaded
     */
    public void write() throws IOException, JAXBException, WFSCellNotLoadedException {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
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
                WFS.getLogger().warning("Unable to write cell to WFS: " + cell);
            } catch (InvalidWFSCellException excp) {
                // log some error and continue
                WFS.getLogger().warning("Unable to write cell to WFS: " + cell);
            } catch (javax.xml.bind.JAXBException excp) {
                // log some error and continue
                WFS.getLogger().warning("Unable to write cell to WFS: " + cell);
            }
        }
        
        /*
         * We now need to cleanup any existing files or directories that are
         * no longer present. We ask the implementation of the cell directory
         * to do this.
         */
        this.delegate.cleanupDirectory(this.children);
    }
    
    /**
     * Returns the unique path name of this cell directory, including all of
     * the naming convention suffixes.
     * 
     * @return The unique path name
     */
    protected String getPathName() {
        return this.pathName;
    }
    
    /**
     * This utility method fetches the names of the cell from the WFS. Any
     * method that returns or updates the list of child cells calls this
     * method first, which does nothing if the child cells have already been
     * loaded. This method simplifies multithreaded issues by putting the
     * loading in once place.
     */
    private void loadChildCells() {
        /*
         * The synchronization issues with this method are complex. First, this
         * method will acquire the read lock. That will allow methods to read
         * the cells (e.g. getCellNames()) to complete without any other thread
         * interfering. But it will also allow methods that write to the list
         * of children (e.g. addChild()) to first read in the list of children,
         * since a writer can acquire a read lock.
         * 
         * We also want to protect against multiple read threads from calling
         * this method at the same time. Therefore, we will also protect the
         * 'children' member variable so that only one thread can call this
         * thread at a time.
         */
        this.wfsRef.get().getReadLock().lock();
        
        try {
            synchronized(this.children) {
                /*
                 * If the hashmap is not null, then simply return. We need to
                 * make this check inside the synchronized keyword because the
                 * creation of the hashmap and filling the hashmap happen in
                 * multiple steps below.
                 */
                if (this.children != null) {
                    return;
                }

                /*
                 * Call the delegate to actually load the cells. The delegate will
                 * return an array of String cell names. We need to parse off the
                 * -wlc.xml extension, and create new cells
                 */
                this.children = new HashMap<String, WFSCell>();
                String fileNames[] = this.delegate.loadCellNames();
                for (String fileName : fileNames) {
                    String cellName = WFSCell.stripCellFileSuffix(fileName);
                    CellDelegate cellDelegate = this.delegate.createCellDelegate(cellName);
                    WFSCell cell = new WFSCell(this.wfsRef.get(), cellName, this, cellDelegate);
                    this.children.put(cellName, cell);
                }
            }
        } finally {
            this.wfsRef.get().getReadLock().unlock();
        }
    }
}
