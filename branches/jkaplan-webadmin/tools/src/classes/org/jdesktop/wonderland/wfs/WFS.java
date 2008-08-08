/**
 * Project Looking Glass
 *
 * $RCSfile: WFS.java,v $
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
 * $Revision: 1.3.8.2 $
 * $Date: 2008/04/08 10:44:30 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.wfs;

import java.beans.ExceptionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.cells.BasicCellSetup;
import org.jdesktop.wonderland.wfs.event.WFSEvent;
import org.jdesktop.wonderland.wfs.event.WFSListener;

/**
 * <h3>The Wonderland File System (WFS)</h3>
 * 
 * The WFS class represents a Wonderland File System (WFS). A WFS is a series
 * of directories and files (cells) that describe a world or a portion of a
 * world. A WFS may also contain a collection of resources that are exported
 * for use.
 * <p>
 * An instance of the WFS class is created through methods on the WFSFactory
 * class. Instances of WFS (or any of its subclasses) should never be created
 * directly.
 * <p>
 * The WFS class and its associated interfaces (WFSCell and WFSCellDirectory)
 * enable the reading and writing of WFSs. These sets of abstractions are meant
 * to be low-level: they are simply Java class representations of the directory
 * and file concepts defined by WFS.
 *
 * <h3>Reading and Writing Model</h3>
 *
 * A WFS is only read in when asked (i.e. the structure of the WFS is discovered
 * progressively; it is not read all at once into memory). Once read, the WFS
 * structure resides in memory. The underlying file system may be forceably
 * re-read; otherwise changes made to the file system on the operation system
 * level will not be seen in this software layer.
 * <p>
 * The structure and content of the WFS that resides in memory may be updated.
 * programmatically. Changes do not take effect to the underyling medium until
 * explictly done so. Updating the WFS to its underlying medium (e.g. disk) can
 * happen in one of several ways: only the configuration parameters of a cell
 * may be updated, or a cell and all of its children may be updated (representing
 * a subtree of the WFS), or the entire WFS tree. Updates to the medium are
 * done with the minimum amount of disruption to any existing structure--new
 * cells are added, while old cells are removed (versus obliterating an existing
 * structure and rewriting from scratch).
 *
 * <h3>Threading Issues</h3>
 * 
 * No methods on these classes are multi-thread safe, nor does this API prevent
 * multiple users from interfering with each other's edits on a WFS. It is
 * the responsibility of high-level software to coordinate access amongst
 * multiple users to a single WFS. Also, if an external party modifies the
 * underlying WFS (e.g. operating system manipulations of the disk file system),
 * then no guarantees are made on the correctness of actions taken with this
 * API.
 * 
 * <h3>Supported Underlying Mediums</h3>
 *
 * A WFS may exist as a hierarchy of files and directories on a disk file
 * system, or a new one may be created on disk. These WFSs are created by giving
 * a URL with the 'file:' protocol. The WFSFactory.open(URL) method opens an
 * existing WFS on disk, while the WFSFactory.create(URL) creates a new WFS on
 * disk. This API make no guarantees for correctness if changes to the directory
 * structure is made on the operating system level. This API supports both
 * reading and writing for disk file systems.
 * <p>
 * A WFS may also exist as a jar file that encodes the proper directory and
 * file structure. The jar file may be located on a disk locally, or it may
 * be fetched from the network. The WFSFactory.open(URL) method opens a WFS
 * encoded as a jar file, whether on disk locally (with the 'file:' protocol)
 * or over the network (with the 'http:' protocol). See the ArchiveWFS class
 * for more details on the format of the URL in these cases. This API supports
 * only reading of jar files at this time.
 * <p>
 * In the final case, the WFS resides entirely in memory and may be serlialized
 * at once to an output stream, encoding as a jar file. This usage is typically
 * if one wishes to create a WFS from scratch and write it out over a network
 * connection.
 *
 * <h3>Support for Multi-Threaded Access</h3>
 * 
 * This API is meant to be used in situations where multiple threads may be
 * attempting to both read and write the WFS. Synchronization is based upon
 * the concept that one thread may "own" an entire WFS tree and may read and
 * write while it owns the tree. If no thread owns the tree, then anyone may
 * read.
 * <p>
 * This locking scheme was implemented for a couple of reasons. First, updates
 * to a WFS typically happen over a series of updates such as creating new
 * cells, and deleting or modifying existing cells. Second, locking the entire
 * WFS and tree simplifies what is locked.
 * <p>
 * It is expected that threads that lock a WFS only do so to update the WFS
 * tree quickly and then release the lock. It is up to the calling thread to
 * release the lock, especially under exceptional conditions it may encounter.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class WFS implements ExceptionListener {
    
    /* The error logger */
    private static final Logger logger = Logger.getLogger(WFS.class.getName());
    
    /* Prefix names for WFS components */
    public static final String CELL_DIRECTORY_SUFFIX = "-wld";
    public static final String CELL_FILE_SUFFIX = "-wlc.xml";
    public static final String WFS_DIRECTORY_SUFFIX = "-wfs";
    
    /* Support URL protocols for file systems */
    public static final String FILE_PROTOCOL = "file";
    public static final String JAR_PROTOCOL  = "jar";

    /* The directory object associated with the root of the file system */
    protected WFSRootDirectory directory = null;
    
    /*
     * The single owner of this WFS tree. To be able to update or write to
     * the tree, threads must aquire this mutex first.
     */
    private ReentrantReadWriteLock ownerLock = new ReentrantReadWriteLock();
     
    /* A list of cell event listeners */
    private EventListenerList eventListenerList = new EventListenerList();
    
    /**
     * Creates a new instance of WFS. This constructor should not be called--
     * one of the factory methods on the WFSFactory class should be used
     * instead.
     */
    public WFS() {
    }
    
    /**
     * Close any open resource associated with the WFS
     */
    public void close() {
        // Do nothing, to be overridden perhaps
    }
   
    /**
     * Attempt to acquire the owner lock for this WFS, returning immediately
     * if the lock is not available. Returns true if the ownership was
     * successfully acquired, false if not.
     * 
     * @return True if ownership is acquired, false if not.
     */
    public boolean tryAcquireOwnership() {
        return this.ownerLock.writeLock().tryLock();
    }
    
    /**
     * Attempt to acquire the owner lock for this WFS, blocking until the lock
     * is available.
     * 
     * @throw InterruptedException If the thread is interrupted while trying
     */
    public void acquireOwnership() throws java.lang.InterruptedException {
        this.ownerLock.writeLock().lock();
    }
    
    /**
     * Release the ownership lock. If the lock is not owned by this thread,
     * then do nothing
     */
    public void release() {
        /*
         * A few words are in order: since every time a thread acquires the lock,
         * a count is increment in the lock. Upon release, the count is
         * decremented. In order to completely release the lock, the caller
         * needs to call as many releases() as it did acquires(). To make things
         * simpler, this method calls however many unlock() calls as is required
         * the release the lock.
         */
        if (this.ownerLock.isWriteLockedByCurrentThread() == true) {
            while (this.ownerLock.getWriteHoldCount() > 0) {
               this.ownerLock.writeLock().unlock();
            }
        }
    }
    
    /**
     * Checks whether the current thread has ownership of the WFS, otherwise
     * throw an IllegalStateException (a runtime exception so it does not need
     * to be declared for each method that calls this method.
     * 
     * @throw IllegalStateException If the thread is not the owner
     */
    public void checkOwnership() {
        if (this.ownerLock.isWriteLockedByCurrentThread() == false) {
            throw new IllegalStateException("Thread does not current hold ownership of WFS.");
        }
    }
    
    /**
     * Returns the root cell directory class representing of the WFS.
     * 
     * @return The directory containing the children in the root of the WFS
     */
    public WFSRootDirectory getRootDirectory() {
        /*
         * The root directory class object is not something that is going to
         * change, so there is no need to protect this with a read lock.
         */
        return this.directory;
    }
    
    /**
     * Returns the logger for the WFS package
     */
    public static Logger getLogger() {
        return WFS.logger;
    }
    
    /**
     * Writes the entire WFS to the underlying medium, including the meta-
     * information contains within the root directory, the cells containing
     * within the root directory, and any child directories.
     * <p>
     * @throw JAXBException Upon error writing to XML
     * @throw IOException Upon a general I/O error.
     */
    public void write() throws IOException, JAXBException, WFSCellNotLoadedException {
        /* Make sure the thread has write permissions */
        this.checkOwnership();
        
        this.getRootDirectory().write();
    }
    
    /**
     * Returns the read lock associated with this WFS. This method is not part
     * of the public API, it is meant only for the implementation.
     * 
     * @return The read lock for the WFS
     */
    protected Lock getReadLock() {
        return this.ownerLock.readLock();
    }
    
    /**
     * Writes an entire WFS to an output stream, encoding as a jar file. The
     * side effect of this method is to load the entire WFS if it has not
     * already been loaded into memory.
     * 
     * @param os The output stream to write to
     * @throw IOException Upon general I/O error
     * @throw JAXBException Upon error serializing to XML on disk
     */
    public void writeTo(OutputStream os) throws IOException, JAXBException {
        /* We just need a read lock for this, since we aren't changing anything */
        this.getReadLock().lock();
        
        try {
            /* Create the output stream for the jar file */
            JarOutputStream jos = new JarOutputStream(os);

            /* Fetch the root directory and write out the directory */
            WFSRootDirectory rootDir = this.getRootDirectory();
            String pathName = rootDir.getPathName();
            JarEntry je = new JarEntry(pathName + "/");
            jos.putNextEntry(je);

            /* Fetch the version and aliases classes and write them out */
            WFSAliases aliases = rootDir.getAliases();
            if (aliases != null) {
                je = new JarEntry(pathName + "/" + WFSRootDirectory.ALIASES);
                jos.putNextEntry(je);
                aliases.encode(new OutputStreamWriter(jos));
            }

            WFSVersion version = rootDir.getVersion();
            if (version != null) {
                je = new JarEntry(pathName + "/" + WFSRootDirectory.VERSION);
                jos.putNextEntry(je);
                version.encode(new OutputStreamWriter(jos));
            }

            /*
             * Write all of the individua cells in the root directory out.
             * Recursively call to write out the rest of the WFS tree.
             */
            WFSCell cells[] = rootDir.getCells();
            for (WFSCell cell : cells) {
                try {
                    this.writeCellTo(cell, jos);
                } catch (IOException excp) {
                    // ignore, log error
                } catch (JAXBException excp) {
                    // ignore, log error
                }
            }

            /* Close the stream and return */
            jos.close();
        } finally {
            this.getReadLock().unlock();
        }
    }

       /**
     * Adds a listener to this cell, for events such as cell attribute update,
     * cell children added, cell children removed, and cell removed. Note that
     * this only delivers events for this cell, and does not deliver events
     * for changes to child cells recursively down the hierarchy.
     * 
     * @param listener The new cell listener
     */
    public void addWFSListener(WFSListener listener) {
        this.eventListenerList.add(WFSListener.class, listener);
    }

    /**
     * Removes a listener from this cell. If the listener does not exist, this
     * method does nothing
     * 
     * @param listener The cell listener to remove
     */
    public void removeWFSListener(WFSListener listener) {
        this.eventListenerList.remove(WFSListener.class, listener);
    }
    
    /**
     * Notify all of the registered listeners that a cell's attribute files
     * has been updated. This method notifies the listeners each in their
     * own thread.
     */
    public void fireCellAttributeUpdate(WFSCell cell) {
        /*
         * Fetch an array of pairs of { class type, listener object }, is never
         * null.
         */
        final Object[] listeners = this.eventListenerList.getListenerList();
        final WFSEvent event = new WFSEvent(cell);
        
        /*
         * Loop through each listener, from tail to head, create an event for
         * each and fire the event in its own thread.
         */
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == WFSListener.class) {
                /* Spawn a new thread for each of the listeners */
                final int j = i;
                (new Thread() {
                    @Override
                    public void run() {
                        ((WFSListener) listeners[j + 1]).cellAttributeUpdate(event);
                    }
                }).start();
            }
        }
    }
    
    /**
     * Notify all of the registered listeners that a cell has added children.
     * This method notifies the listeners each in their own thread.
     */
    public void fireCellChildrenAdded(WFSCell cell) {
        /*
         * Fetch an array of pairs of { class type, listener object }, is never
         * null.
         */
        final Object[] listeners = this.eventListenerList.getListenerList();
        final WFSEvent event = new WFSEvent(cell);
        
        /*
         * Loop through each listener, from tail to head, create an event for
         * each and fire the event in its own thread.
         */
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == WFSListener.class) {
                /* Spawn a new thread for each of the listeners */
                final int j = i;
                (new Thread() {
                    @Override
                    public void run() {
                        ((WFSListener) listeners[j + 1]).cellChildrenAdded(event);
                    }
                }).start();
            }
        }
    }
    
    /**
     * Notify all of the registered listeners that a cell has removed children.
     * This method notifies the listeners each in their own thread.
     */
    public void fireCellChildrenRemoved(WFSCell cell) {
        /*
         * Fetch an array of pairs of { class type, listener object }, is never
         * null.
         */
        final Object[] listeners = this.eventListenerList.getListenerList();
        final WFSEvent event = new WFSEvent(cell);
        
        /*
         * Loop through each listener, from tail to head, create an event for
         * each and fire the event in its own thread.
         */
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == WFSListener.class) {
                /* Spawn a new thread for each of the listeners */
                final int j = i;
                (new Thread() {
                    @Override
                    public void run() {
                        ((WFSListener) listeners[j + 1]).cellChildrenRemoved(event);
                    }
                }).start();
            }
        }
    }
    
    /**
     * Notify all of the registered listeners that a cell has been removed.
     * This method notifies the listeners each in their own thread.
     */
    public void fireCellRemoved(WFSCell cell) {
        /*
         * Fetch an array of pairs of { class type, listener object }, is never
         * null.
         */
        final Object[] listeners = this.eventListenerList.getListenerList();
        final WFSEvent event = new WFSEvent(cell);
        
        /*
         * Loop through each listener, from tail to head, create an event for
         * each and fire the event in its own thread.
         */
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == WFSListener.class) {
                /* Spawn a new thread for each of the listeners */
                final int j = i;
                (new Thread() {
                    @Override
                    public void run() {
                        ((WFSListener) listeners[j + 1]).cellRemoved(event);
                    }
                }).start();
            }
        }
    }
    
    /**
     * Handles when an exception is thrown by the XML decoding mechanism, to
     * supress any warnings it may print.
     */
    public void exceptionThrown(Exception e) {
        WFS.getLogger().log(Level.WARNING, "Error writing XML: " + e);
    }
    
    /**
     * Takes a WFSCell class and writes its contents to the given jar output
     * stream, and recursively calls to write out any child directory it may
     * have.
     */
    private void writeCellTo(WFSCell cell, JarOutputStream jos) throws IOException, JAXBException {
        /* First fetch the cell setup class, this may result in an read exception */
        BasicCellSetup cellSetup = null;
        try {
            cellSetup = cell.getCellSetup(); 
        } catch (InvalidWFSCellException excp) {
            throw new IOException("Unable to read cell setup: " + cell);
        }

        /* If there is a cell class, then write it out to the stream */
        if (cellSetup != null) {
            /*
             * Write the entry out to the output stream
             */
            JarEntry je = new JarEntry(cell.getPathName());
            jos.putNextEntry(je);
            cellSetup.encode(new OutputStreamWriter(jos));

            /*
             * Recursively write out any children
             */
            if (cell.getCellDirectory() != null) {
                this.writeDirectoryTo(cell.getCellDirectory(), jos);
            }          
        }
    }
    
    /**
     * Takes a WFSCellDirectory class and writes its contents to the given
     * jar output stream. It recursively calls itself until the entire WFS
     * tree has been written.
     */
    private void writeDirectoryTo(WFSCellDirectory directory, JarOutputStream jos) throws IOException {
        /* First create the directory */
        JarEntry je = new JarEntry(directory.getPathName() + "/");
        jos.putNextEntry(je);
        
        /*
         * Write all of the individual cells in the directory out.
         * Recursively call to write out the rest of the WFS tree.
         */
        WFSCell cells[] = directory.getCells();
        for (WFSCell cell : cells) {
            try {
                this.writeCellTo(cell, jos);
            } catch (java.io.IOException excp) {
                // ignore, log error
            } catch (javax.xml.bind.JAXBException excp) {
                // ignore, log error
            }
        }
    }
}
