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
 * The WFSRootDirectory represents the base-level directory for all WFSs. It
 * extends the WFSCellDirectory abstract class, so it contains a list of child
 * cells. It delegates to an instance of WFSCellDirectory
 * <p>
 * The root directory also contains meta-information about the WFS, including
 * the WFS URI aliases and the version. Unlike WFSCellDirectory objects, this
 * class has no cell associated with it, so calls to getAssociatedCell() always
 * return null.
 * <p>
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class WFSRootDirectory extends WFSCellDirectory {
   
    /* The WFSVersion and WFSAliases objects */
    private WFSAliases aliases = null;
    private WFSVersion version = null;
    
    /* The file names for the aliases and version classes */
    public static final String ALIASES = "aliases.xml";
    public static final String VERSION = "version.xml";
    
    /** Constructor that takes no cell as its argument */
    protected WFSRootDirectory() {
        super(null);
    }

    /**
     * Returns the WFS URI aliases for this filesystem.
     * 
     * @return The aliases of the WFS.
     */
    public WFSAliases getAliases() {
        return this.aliases;
    }
    
    /**
     * Sets the WFS URI aliases for this filesystem.
     * 
     * @param aliases The new aliases for this filesytem
     */
    public void setAliases(WFSAliases aliases) {
        this.aliases = aliases;
    }
    
    /**
     * Returns the version of the file system.
     *
     * @return The version of the WFS
     */
    public WFSVersion getVersion() {
        return this.version;
    }
    
    /**
     * Sets the version for this filesystem.
     *
     * @param version The new version for this filesystem
     */
    public void setVersion(WFSVersion version) {
        this.version = version;
    }
    
    /**
     * Writes all of the cells in this directory to the underlying medium. The
     * list of cells must first be loaded (e.g. by calling getCells()), otherwise
     * a WFSCellNotLoadedException is throw.
     * 
     * @throw IOException Upon general I/O error
     * @throw WFSCellNotLoadedException If not all of the cells have been loaded
     */
    public abstract void write() throws IOException;
    
    /**
     * Writes the WFS meta-information (e.g. version, aliases) to the WFS.
     * <p>
     * @throw IOException Upon a general I/O error.
     */
    public abstract void writeMetaData() throws IOException; 
}
