/**
 * Project Looking Glass
 *
 * $RCSfile: WFSRootDirectory.java,v $
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
 * $Revision: 1.1.2.2 $
 * $Date: 2008/04/08 10:44:30 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.wfs;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.wfs.delegate.DirectoryDelegate;

/**
 * The WFSRootDirectory represents the base-level directory for all WFSs. It
 * extends the WFSCellDirectory abstract class, so it contains a list of child
 * cells. It delegates to an instance of WFSCellDirectory
 * <p>
 * The root directory also contains meta-information about the WFS, including
 * the WFS URI aliases and the version. Unlike WFSCellDirectory objects, this
 * class has no cell associated with it, so calls to getAssociatedCell() always
 * return null.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSRootDirectory extends WFSCellDirectory {
   
    /* The WFSVersion and WFSAliases objects */
    private WFSAliases aliases = null;
    private WFSVersion version = null;
    
    /* The file names for the aliases and version classes */
    public static final String ALIASES = "aliases.xml";
    public static final String VERSION = "version.xml";
    
    /** Constructor that takes the directory delegate */
    public WFSRootDirectory(WFS wfs, DirectoryDelegate delegate) {
        super(wfs, delegate);
    }

    /**
     * Returns the WFS URI aliases for this filesystem.
     * 
     * @return The aliases of the WFS.
     */
    public WFSAliases getAliases() {
        /*
         * Protect with a read lock since this can be updated via the public
         * method setAliases().
         */
        this.wfsRef.get().getReadLock().lock();
        try {
            return this.aliases;
        } finally {
            this.wfsRef.get().getReadLock().unlock();
        }
    }
    
    /**
     * Sets the WFS URI aliases for this filesystem.
     * 
     * @param aliases The new aliases for this filesytem
     */
    public void setAliases(WFSAliases aliases) {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        this.aliases = aliases;
    }
    
    /**
     * Returns the version of the file system.
     *
     * @return The version of the WFS
     */
    public WFSVersion getVersion() {
        /*
         * Protect with a read lock since this can be updated via the public
         * method setVersion().
         */
        this.wfsRef.get().getReadLock().lock();
        try {
            return this.version;
        } finally {
            this.wfsRef.get().getReadLock().unlock();
        }
    }
    
    /**
     * Sets the version for this filesystem.
     *
     * @param version The new version for this filesystem
     */
    public void setVersion(WFSVersion version) {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        this.version = version;
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
    @Override
    public void write() throws IOException, JAXBException, WFSCellNotLoadedException {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        /* Write out the meta data */
        this.writeMetaData();
        
        /* Ask the cell directory super class to do the rest */
        super.write();
    }
    
    /**
     * Writes the WFS meta-information (e.g. version, aliases) to the WFS.
     * <p>
     * @throw IOException Upon a general I/O error
     * @throw JAXBException Upon error writing to XML
     */
    public void writeMetaData() throws IOException, JAXBException {
        /* Make sure the thread has write permissions */
        this.wfsRef.get().checkOwnership();
        
        if (this.getVersion() != null) {
            this.getVersion().encode(this.delegate.getWriter(WFSRootDirectory.VERSION));
        }

        if (this.getAliases() != null) {
            this.getAliases().encode(this.delegate.getWriter(WFSRootDirectory.ALIASES));
        }
    }
}
