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
 * $Revision$
 * $Date$
 * $State: Exp $
 */

package org.jdesktop.wonderland.server.utils.wfs;


/**
 * The WFSCellDirectory interface represents a virtual directory, whether it
 * be on disk, in an archive file, or over the network, that represents a
 * hierarchical container for child cells.
 *
 * @author jslott
 */
public interface WFSCellDirectory {
    /**
     * Returns a canonical, unique name for the parent associated with this
     * directory of cells
     */
    public String getCanonicalParent();
    
    /**
     * Returns the WFSCellDirectory class representing the directory containing
     * the children of the given cell name. Throws NoSuchWFSDirectory if the
     * directory does not exist.
     *
     * @param cellName The name of the cell
     * @param canonicalParent The canonical name for the parent of the new directory
     * @throw NoSuchWFSDirectory Thrown if the cell does not have a child directory
     */
    public WFSCellDirectory getCellDirectory(String cellName, String canonicalParent) throws NoSuchWFSDirectory;
    
    /**
     * Returns an array the WFSCell class representing all of the cells in the currnet
     * directory
     */
    public WFSCell[] getCells();
    
}
