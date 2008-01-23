/**
 * Project Looking Glass
 *
 * $RCSfile: WFSArchiveCellDirectory.java,v $
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
 * $Revision: 1.2 $
 * $Date: 2007/10/17 17:11:14 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.server.utils.wfs.archive;

import java.io.File;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Level;
import org.jdesktop.wonderland.server.utils.wfs.NoSuchWFSDirectory;
import org.jdesktop.wonderland.server.utils.wfs.WFS;
import org.jdesktop.wonderland.server.utils.wfs.WFSCell;
import org.jdesktop.wonderland.server.utils.wfs.WFSCellDirectory;
import org.jdesktop.wonderland.server.utils.wfs.file.FileWFS;
import org.jdesktop.wonderland.server.utils.wfs.file.WFSFileCell;

/**
 * The WFSArchiveCellDirectory is a directory within a Wonderland File System (WFS)
 * that corresponds to a cell and corresponds to an entry within a JAR file.
 * A WFSArchiveCellDirectory may contain cells and cell directories.
 * <p>
 * @author jslott
 */
public class WFSArchiveCellDirectory implements WFSCellDirectory {
    /* The manifest file that permits access to JAR file entries */
    private ArchiveManifest manifest;

    /* The name of this directory */
    private String dir;
    
    /*
     * The canonical name of the parent cell associated with this directory. For
     * example if there is a hierarchy: my-wfs/cell-wld/subcell-wld/my-wlc.xml
     * then the WFSCellDirectory class representing the subcell-wld/ directory
     * will have my-wfs/cell-wlc as the name of the canonical parent cell.
     */
    private String canonicalParent;
    
    /**
     * Creates a new instance of WFSArchiveCellDirectory, takes the manifest
     * that permits access to the JAR file entrys, the name of the directory,
     * and the canonical name of the parent cell of this directory.
     *
     * @param manifest The archive's manifest and contents
     * @param canonicalParent A unique name of the parent cell
     */
    public WFSArchiveCellDirectory(ArchiveManifest manifest, String dir, String canonicalParent) {
        this.manifest        = manifest;
        this.dir             = dir;
        this.canonicalParent = canonicalParent;
    }
    
    /**
     * Returns a canonical, unique name for the parent associated with this
     * directory of cells
     */
    public String getCanonicalParent() {
        return this.canonicalParent;
    }
    
    /**
     * Returns the manifest object of the JAR file
     */
    public ArchiveManifest getArchiveManifest() {
        return this.manifest;
    }
    
    /**
     * Returns the pathname of the directory
     */
    public String getPathName() {
        return this.dir;
     }
 
    /**
     * Returns the WFSCellDirectory class representing the directory containing
     * the children of the given cell name. Throws NoSuchWFSDirectory if the
     * directory does not exist.
     *
     * @param cellName The name of the cell
     * @param canonicalParent The canonical name for the parent of the new directory
     * @throw NoSuchWFSDirectory Thrown if the cell does not have a child directory
     */
    public WFSCellDirectory getCellDirectory(String cellName, String canonicalParent) throws NoSuchWFSDirectory {
        /*
         * In this current directory, return a sub-directory contained within
         * this cell directory. We first check to see if the entry exists in
         * the JAR file.
         */
        ArchiveManifest manifest = this.getArchiveManifest();
        String          name     = this.getPathName() + "/" + cellName + WFS.CELL_DIRECTORY_SUFFIX;
        
        if (manifest.isValidEntry(name) == false) {
            throw new NoSuchWFSDirectory("The WFS Directory does not exist: " + name);
        }
        return new WFSArchiveCellDirectory(manifest, name, canonicalParent);
    }
    
    /**
     * Returns an array the WFSCell class representing all of the cells in the
     * current directory.
     */
    public WFSCell[] getCells() {
        /*
         * Fetch the entry in the manifest corresponding to this directory,
         * subject to the filter for the cell file suffix.
         */
        ArchiveManifest    manifest = this.getArchiveManifest();
        String             dir      = this.getPathName();
        LinkedList<String> entries  = manifest.getCellEntries(dir);
        
        WFS.getLogger().log(Level.INFO, "WFS File List: " + entries.size() +
            " files found in " + dir);
        
        /*
         * Copy into an array of WFSCell classes, but check to make sure the
         * cells satisfy the naming convention.
         */
        Vector<WFSCell> cells = new Vector<WFSCell>();
        for (String entry : entries) {
            if (entry.endsWith(WFS.CELL_FILE_SUFFIX) == true) {
                WFS.getLogger().log(Level.INFO, "Cell found " + entry);
                cells.add(new WFSArchiveCell(manifest, dir + "/" + entry, entry));
            }
        }
        return cells.toArray(new WFSCell[] {});
    }
}
