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
package org.jdesktop.wonderland.server.cell.bounds;

import com.jme.bounding.BoundingVolume;
import java.util.Collection;
import org.jdesktop.wonderland.common.PrivateAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.CellDescription;
import org.jdesktop.wonderland.server.cell.RevalidatePerformanceMonitor;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 *
 * @author paulby
 */
@PrivateAPI
public interface CellDescriptionManager {
    /**
     * Get the cached cell description associated with the given id.
     * @param cellID the id of the cell to get
     * @return the cached cell description, or null if the description
     * does not exist in the cache
     */
    public CellDescriptionImpl getCellDescription(CellID cellID);
    
    /**
     * Add a cell description for the given cell to the cache.
     * @param cell the cell to create a description for
     * @return the created cell description object
     */
    public CellDescriptionImpl addCellDescription(CellMO cell);
    
    /**
     * Remove a cell description for the given cell from the cache
     * @param cellID the id of the cell to remove
     * @return the description that was removed, or null if there was
     * no description for the given cell
     */
    public CellDescriptionImpl removeCellDescription(CellID cellID);

    /**
     * Walk the cell tree starting at the given root node, and return all
     * cells that intersect with the given bounds
     * @param rootCell the root of the tree to walk
     * @param bounds the bounds to test for intersection
     * @param perfMonitor monitors peformance
     * @return a collection of all cells that intersect the given bounds
     */
    public Collection<CellDescription> 
            getVisibleCells(CellID rootCell, BoundingVolume bounds, 
                            RevalidatePerformanceMonitor perfMonitor);

    
    /**
     * Notification that the transform for the given cell has changed
     * @param cellID the id of the cell that changed
     * @param transform the new transform
     */
    public void cellTransformChanged(CellID cellID, CellTransform transform);
    
    /**
     * Notification that the bounds for the given cell has changed
     * @param cellID the id of the cell that changed
     * @param bounds the updated bounds
     */
    public void cellBoundsChanged(CellID cellID, BoundingVolume bounds);
    
    /**
     * Notification that the bounds for the given cell has changed
     * @param cellID the id of the cell that changed
     * @param child the id of the child that changed
     * @param childAdded true if the child was added, or false if it was removed
     */
    public void cellChildrenChanged(CellID parent, CellID child, 
                                    boolean childAdded);
    
    /**
     * Notification that the contents of a cell have changed
     * @param cellID the id of the cell that changed
     */
    public void cellContentsChanged(CellID cellID);
}
