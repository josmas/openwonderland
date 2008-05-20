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
package org.jdesktop.wonderland.server.cell;

import com.jme.bounding.BoundingVolume;
import java.util.Collection;
import org.jdesktop.wonderland.common.PrivateAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.bounds.ServiceCellDescriptionManager;

/**
 * Provides an abstraction for the cell bounds computation engine allowing
 * various implementation options.
 * 
 * @author paulby
 */
@PrivateAPI
public abstract class BoundsManager {

    private static final BoundsManager handler = new ServiceCellDescriptionManager();
    
    /**
     * Return the singleton bounds handler
     * 
     * @return
     */
    public static BoundsManager get() {
        return handler;
    }
    
      /**
     * Return the local bounds of the specified cell
     * 
     * @param cellID
     * @return
     */
    public abstract BoundingVolume getLocalBounds(CellID cellID);

    /**
     * Return the bounds of the cell in virtual world coordinates. This bounds
     * does not necessarily encapsulate all the child bounds.
     * @param cellID
     * @return
     */
    public abstract BoundingVolume getCachedVWBounds(CellID cellID);

    /**
     * Return the computed World Bounds of the specified cell. The computed
     * world bounds encapsulate the cell and all it's children.
     * 
     * @param cellID
     * @return
     */
    public abstract BoundingVolume getComputedWorldBounds(CellID cellID);

    /**
     * Get the local to VWorld transform of this cells origin.
     * @param cellID
     * @return
     */
    public abstract CellTransform getLocalToVWorld(CellID cellID);
    
    /**
     * Instruct the handler to start managing a cell
     * @param cell
     */
    public abstract void createBounds(CellMO cell);
    
    /**
     * Instruct the handler to stop managing a cell
     * @param cell
     */
    public abstract void removeBounds(CellMO cell);
    
    /**
     * Notify handler that the transform of this cell has changed. This will
     * cause recomputation of the transform data.
     * 
     * @param parent the parent cell
     * @param child the child cell
     */
    public abstract void cellTransformChanged(CellID cellID, CellTransform transform);
    
    /**
     * Notify handler that the set of children of this cell has changed. This will
     * cause recomputation of the transform and/or bounds data.
     * 
     * @param parent the parent cell
     * @param child the child cell
     * @param childAdded true if a child was added, false if removed
     */
    public abstract void cellChildrenChanged(CellID parent, CellID child, boolean childAdded);
    
    /**
     * Notify handler that cell bounds have been changed
     * 
     * @param cellID
     * @param bounds
     */
    public abstract void cellBoundsChanged(CellID cellID, BoundingVolume bounds);

    /**
     * Notify handler that the contents of the cell has changed, this
     * will cause change notifications to be sent from each AvatarCellCacheMO
     * that contains this cell during the next revalidation cycle
     * 
     * @param cellID
     */
    public abstract void cellContentsChanged(CellID cellID);
    
    public abstract Collection<CellDescription> getVisibleCells(CellID rootCell, BoundingVolume bounds, RevalidatePerformanceMonitor perfMonitor);
 
    /**
     * 
     * @param rootCell
     * @param bounds
     * @param perfMonitor
     * @param cellClass  Report only cells of this claass
     * @param reportSubclasses If true reports cells of cellClass and subclasses of cellClass, if false
     * returns only cells of cellClass.
     * @return
     */
    public Collection<CellDescription> getVisibleCells(CellID rootCell, BoundingVolume bounds, RevalidatePerformanceMonitor perfMonitor, Class cellClass, boolean reportSubclasses) {
        throw new RuntimeException("Not Implemented");
    }
 
    /**
     * Return the CellDescription corresponding to the given CellID.  The cell
     * must have been previously added to the tree using {@code createBounds()}.
     * @param cellID
     * @return
     */
    public abstract CellDescription getCellMirror(CellID cellID);
}
