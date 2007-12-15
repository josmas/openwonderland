/**
 * Project Wonderland
 *
 * $RCSfile:$
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
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.server.cell;

import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public class CellManager {

    /**
     * Add the child to the parent
     * 
     * @param child
     * @param parent
     */
    public void addCell(CellMO child, CellMO parent) {
        throw new RuntimeException("Not Implemented");
    }
    
    /**
     * Remove cell from the world
     * 
     * @param cell
     */
    public void removeCell(CellMO cell) {        
        throw new RuntimeException("Not Implemented");
    }
    
    /**
     * Return the cell with the given ID, or null if the id is invalid
     * 
     * @param cellID
     * @return
     */
    public CellMO getCell(CellID cellID) {
        throw new RuntimeException("Not Implemented");        
    }
    
    /**
     *  Traverse all trees and return the set of cells which are within
     * the specified bounds and are of the give Class 
     * 
     * @param b
     * @param cellClasses
     * @return
     */
    public cellClass[], CellID[] getCells(Bounds b, Class[] cellClasses) {
        
    }
    
    /**
     *  Cell has moved, revalidate the user cell caches for those
     * users that are close
     * @param cell
     */
    public void revalidate(CellMO cell) {
        
    }
}
