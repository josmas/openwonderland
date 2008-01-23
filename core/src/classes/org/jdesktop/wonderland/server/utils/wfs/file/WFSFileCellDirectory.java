/**
 * Project Looking Glass
 *
 * $RCSfile: WFSFileCellDirectory.java,v $
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
 * $Date: 2007/10/17 17:11:07 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.server.utils.wfs.file;

import java.io.File;
import java.util.logging.Level;
import org.jdesktop.wonderland.server.utils.wfs.NoSuchWFSDirectory;
import org.jdesktop.wonderland.server.utils.wfs.WFS;
import org.jdesktop.wonderland.server.utils.wfs.WFSCell;
import org.jdesktop.wonderland.server.utils.wfs.WFSCellDirectory;

/**
 * The WFSCellDirectory is a directory within a Wonderland File System (WFS)
 * that corresponds to a cell. A WFSCellDirectory may contain cells and cell
 * directories.
 * <p>
 * @author jslott
 */
public class WFSFileCellDirectory implements WFSCellDirectory {
    /* The File of the referring WFS Directory */
    private File file = null;
    
    /* The canonical name of the parent cell associated with this directory */
    private String canonicalParent;
    
    /**
     * Creates a new instance of WFSCellDirectory, takes the File of the referring
     * directory as an argument
     */
    public WFSFileCellDirectory(File directory, String canonicalParent) {
        this.file            = directory;
        this.canonicalParent = canonicalParent;
    }
    
    /**
     * Returns the File associated with this WFS file
     */
    public File getFile() {
        return this.file;
    }
    
    /**
     * Returns a canonical, unique name for the parent associated with this
     * directory of cells
     */
    public String getCanonicalParent() {
        return this.canonicalParent;
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
        /* Check if the directory exists */
        File dir = new File(this.getFile(), cellName + WFS.CELL_DIRECTORY_SUFFIX);
        if (dir.exists() == false || dir.isDirectory() == false) {
            throw new NoSuchWFSDirectory("The WFS Directory does not exist: " + dir.toString());
        }
        return new WFSFileCellDirectory(dir, canonicalParent);
    }
    
    /**
     * Returns an array the WFSCell class representing all of the cells in the currnet
     * directory
     */
    public WFSCell[] getCells() {
        /* List all of the files in the current directory subject to the filter */
        File[] files = this.getFile().listFiles(FileWFS.CELL_FILE_FILTER);
        
        WFS.getLogger().log(Level.INFO, "WFS File List: " + files.length +
            " files found in " + this.getFile().getName());
        
        /* Copy into an array of WFSCell classes */
        WFSCell[] cells = new WFSCell[files.length];
        for (int i = 0; i < files.length; i++) {
            WFS.getLogger().log(Level.INFO, "File filter found " + files[i]);
            cells[i] = new WFSFileCell(files[i]);
        }
        return cells;
    }
}
