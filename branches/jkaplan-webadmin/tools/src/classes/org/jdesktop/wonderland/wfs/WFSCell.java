/**
 * Project Looking Glass
 *
 * $RCSfile: WFSCell.java,v $
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
 * $Revision: 1.2.8.4 $
 * $Date: 2008/04/08 10:44:30 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.wfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.cells.BasicCellSetup;
import org.jdesktop.wonderland.wfs.event.WFSEvent;
import org.jdesktop.wonderland.wfs.event.WFSListener;
import org.jdesktop.wonderland.wfs.delegate.CellDelegate;
import org.jdesktop.wonderland.wfs.delegate.DirectoryDelegate;

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
public class WFSCell {
    /* The cell name without any naming convention suffix */ 
    private String cellName = null;
    
    /* The canonical (unique) name of the cell in the WFS (no suffixes) */
    private String canonicalName = null;
    
    /* The unique path name of the cell (including suffixes) */
    private String pathName = null;
    
    /* The directory of child cells, null until the directory exist. */
    protected WFSCellDirectory cellDirectory = null;
 
    /* The implementation specific cell delegate */
    private CellDelegate delegate = null;
    
    /* The cell's setup object, null if it has not yet been read */
    protected BasicCellSetup cellSetup = null;
    
    /* True if the cell's setup should be written upon the next write() */
    private boolean cellSetupUpdate = false;
    
    /* The parent cell directory that contains this cell */
    private WFSCellDirectory parentDirectory = null;

    
    /* A weak reference to the main WFS object */
    private WeakReference<WFS> wfsRef = null;
    
    /** Default constructor, should never be called */
    protected WFSCell(WFS wfs, String cellName, WFSCellDirectory parentDirectory, CellDelegate delegate) {
        this.wfsRef = new WeakReference(wfs);
        this.parentDirectory = parentDirectory;
        this.delegate = delegate;
        this.cellName = cellName;
        
        /*
         * Form the canonical name of the cell as the canonical name of the
         * parent cell, followed by the name of this cell. If there is not
         * parent cell (for the root), then just use the cell name as the
         * canonical name
         */
        WFSCell parentCell = this.parentDirectory.getAssociatedCell();
        if (parentCell != null) {
            this.canonicalName = parentCell.getCanonicalName() + "/" + cellName;
        }
        else {
            this.canonicalName = cellName;
        }
        
        /*
         * Form the path name of the cell as the path name of the parent
         * directory, followed by the name of the cell plus the name
         * convention suffix.
         */
        this.pathName = parentDirectory.getPathName() + cellName + WFS.CELL_FILE_SUFFIX;
        
        /* Create the cellDirectory object if the directory exists */
        if (this.delegate.cellDirectoryExists() == true) {
            DirectoryDelegate dirDelegate = this.delegate.createDirectoryDelegate();
            this.cellDirectory = new WFSCellDirectory(this.wfsRef.get(), this, dirDelegate);
        }
    }
    
    /**
     * Returns the parent cell directory, that is, the cell directory in which
     * this cell is contained. If this cell is contained in the root of the
     * WFS, then the root directory is returned.
     * 
     * @return The parent cell directory
     */
    public WFSCellDirectory getParentCellDirectory() {
        /*
         * There is no need to protect this with a read lock. Even though the
         * parent of a cell can change on the file system-level, that gets
         * manifested as removing a cell and re-adding it under the parent.
         * Therefore, the 'parentDirectory' member variable is invariant in
         * the WFSCell class.
         */
        return this.parentDirectory;
    }
    
    /**
     * Returns the parent cell. If this cell is in the root directory, then
     * this method returns null.
     * 
     * @return The parent cell, null if in the root directory
     */
    public WFSCell getParentCell() {
        /*
         * There is no need to protect this with a read lock. Even though the
         * parent of a cell can change on the file system-level, that gets
         * manifested as removing a cell and re-adding it under the parent.
         * Therefore, the 'parentDirectory' member variable is invariant in
         * the WFSCell class.
         */
        return this.parentDirectory.getAssociatedCell();
    }
    
    /**
     * Returns the canonical name for this cell, which is guaranteed to unique
     * identify it within the WFS.
     * 
     * @return A unique, canonical name for the cell
     */
    public String getCanonicalName() {
        /*
         * There is no need to protect this with a read lock. Even though a
         * cell can move around within a file system, this is implemented as
         * removing a cell and adding it somewhere else. The 'canonicalName'
         * member variable is invariant in the WFSCell class.
         */
        return this.canonicalName;
    }

    /**
     * Returns the name of the cell, without the standard suffix
     * 
     * @return The name of the cell file in the WFS
     */
    public String getCellName() {
        /*
         * The cell name can change via the public method setCellName(), so
         * we must protect this with a read lock
         */
        this.wfsRef.get().getReadLock().lock();
        try {
            return this.cellName;
        } finally {
            this.wfsRef.get().getReadLock().unlock();
        }
    }

    /**
     * Sets the name of the cell
     *
     * @param cellName The name of the cell 
     */
    public void setCellName(String cellName) {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        this.cellName = cellName;
    }

    /**
     * Returns the directory containing any children of this cell, null if no
     * such directory exists.
     * 
     * @return The child directory for this cell, null if none
     */
    public WFSCellDirectory getCellDirectory() {
        /*
         * Protect with a read lock. The cell directory is initially null and
         * is created by the public method createCellDirectory().
         */
        this.wfsRef.get().getReadLock().lock();
        try {
            return this.cellDirectory;
        } finally {
            this.wfsRef.get().getReadLock().unlock();
        }
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
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        /*
         * Simply create the object -- the actual directory does not get
         * created until a write() happens.
         */
        DirectoryDelegate dirDelegate = this.delegate.createDirectoryDelegate();
        this.cellDirectory = new WFSCellDirectory(this.wfsRef.get(), this, dirDelegate);
        return this.cellDirectory;
    }
 
    /**
     * Removes the directory containing all of the children. If this directory
     * does not exist, then this method does nothing.
     */
    public void removeCellDirectory() {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        /* Simply set the object to null -- to be removed upon a write() */
        this.cellDirectory = null;
    }

    /**
     * Returns the time (in milliseconds since the epoch) this file was last
     * modified.
     * 
     * @return The last time the cell was modified in the WFS
     */
    public long getLastModified() {
        /*
         * There is no need to protect this with a read lock. The only way this
         * can change is if the disk file changes.
         */
        return this.delegate.getLastModified();
    }

    /**
     * Returns the instance of a subclass of the BasisCellSetup class that is
     * decoded from the XML file representation.
     *
     * @return A class representing the cell's properties
     * @throw FileNotFoundException If the file cannot be read
     * @throw InvalidWFSCellException If the cell in the file is invalid
     */
    public <T extends BasicCellSetup> T getCellSetup()
            throws FileNotFoundException, InvalidWFSCellException {
        /*
         * The multi-threaded behavior of this method is a bit complicated. On
         * the one-hand, the method should try to acquire the read lock. One
         * the other head, this method can be called simultaneously by multiple
         * threads, and we want to avoid multiple decode() calls. So we will
         * acquire a read lock (that will prevent other writes to the 'cellSetup'
         * member variable) and also synchronize around cellSetup so that more
         * than one thread calling this method won't re-parse the same data.
         */
        
        this.wfsRef.get().getReadLock().lock();
        
        try {
            synchronized (this.cellSetup) {
                /* If the cell's setup has already been read, then return it */
                if (this.cellSetup != null) {
                    Class clazz = this.cellSetup.getClass();
                    return (T) clazz.cast(this.cellSetup);
                }

                /* Otherwise, read it in from disk and return it */
                try {
                    T newCellSetup = (T) this.delegate.decode();
                    this.cellSetup = newCellSetup;
                    this.cellSetupUpdate = false;
                    return newCellSetup;
                } catch (java.lang.UnsupportedOperationException excp) {
                    /* If it is unsupported, just quietly return null */
                    return null;
                }
            }
        } finally {
            this.wfsRef.get().getReadLock().unlock();
        }
    }
    
    /**
     * Updates the cell's properties in memory.
     * 
     * @param cellSetup The cell properties class
     * @throw InvalidWFSCellException If the cell properties is invalid
     */
    public <T extends BasicCellSetup> void setCellSetup(T cellSetup)
            throws InvalidWFSCellException {
        
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        /*
         * Sets the cell setup class and indicates the cell to be udpated upon
         * the next write
         */
        this.cellSetup = cellSetup;
        this.setCellSetupUpdate();
    }
    
    /**
     * Indicates that this cell's setup class should be updated upon the next
     * write() invocation, even if the setup parameters have not changed.
     */
    public void setCellSetupUpdate() {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        /* Indiciate that the cell's attributes should be update, and fire events */
        this.cellSetupUpdate = true;
        this.wfsRef.get().fireCellAttributeUpdate(this);
    }
    
    /**
     * Returns true if the cell's setup class should be updated upon the next
     * write() invocation.
     * 
     * @return True to update the cell's setup
     */
    public boolean isCellSetupUpdate() {
        /*
         * Acquire the read lock since this can be updated via setCellSetupUpdate()
         * or clearCellSetupUpdate().
         */
        this.wfsRef.get().getReadLock().lock();
        try {
            return this.cellSetupUpdate;
        } finally {
            this.wfsRef.get().getReadLock().unlock();
        }
    }

    /**
     * Clears the flag to indicate whether the cell should be updated upon the
     * next write. This is not a public method on the API.
     */
    private void clearCellSetupUpdate() {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        this.cellSetupUpdate = false;
    }
    
    /**
     * Updates the contents of the cell to the underlying medium.
     * 
     * @throw InvalidWFSCellException If the cell's setup is not valid
     * @throw IOException Upon general I/O error
     */
    public void writeCell() throws IOException, InvalidWFSCellException, JAXBException {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        /*
         * Checks to see whether we should update the cell's setup class on
         * disk. If so, then write and clear the update flag. If not, do
         * nothing.
         */
        if (this.isCellSetupUpdate() == true) {
            this.delegate.encode(this.cellSetup);
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
    public void write() throws IOException, InvalidWFSCellException, JAXBException {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        /* Updates the cell's information */
        this.writeCell();
        
        /*
         * Ask the cell's directory to write itself out to disk. If it does
         * not exist, then simply remove it.
         */
        if (this.cellDirectory == null) {
            this.delegate.removeCellDirectory();
            return;
        }
        
        /*
         * Write the directory, creating it if necessary. If the directory has
         * yet to been touched, then its children are not loaded, in which case
         * we do not need to save anything. Catch the WFSCellNotLoadedException
         * and ignore.
         */
        this.delegate.createCellDirectory();
        try {
            this.cellDirectory.write();
        } catch (WFSCellNotLoadedException excp) {
            /* Quietly ignore */
        }
    }
    
 
    /**
     * Returns the unique path name of this cell, including all of the naming
     * convention suffixes.
     * 
     * @return The unique path name
     */
    protected String getPathName() {
        return this.pathName;
    }
    
    /**
     * Given a cell or path name with the cell naming convention suffix (-wlc.xml),
     * parses off the suffix and returns the rest. If the file name does not have
     * the propery suffix, the original name given.
     * 
     * @param fileName The file name (with suffix)
     * @return cellName The cell name (whtout suffix)
     */
    public static String stripCellFileSuffix(String fileName) {
        Logger logger = WFS.getLogger();
        try {
            int index = fileName.indexOf(WFS.CELL_FILE_SUFFIX);
            return fileName.substring(0, index);
        } catch (java.lang.IndexOutOfBoundsException excp) {
            /* Quietly return the original */
        }
        return fileName;
    }
}