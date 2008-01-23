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

import java.util.Collection;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import org.jdesktop.wonderland.PrivateAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.UserPerformanceMonitor;
import org.jdesktop.wonderland.server.cell.bounds.ServiceBoundsHandler;

/**
 * Provides an abstraction for the cell bounds computation engine allowing
 * various implementation options.
 * 
 * @author paulby
 */
@PrivateAPI
public abstract class BoundsHandler {

    private static final BoundsHandler handler = new ServiceBoundsHandler();
    
    /**
     * Return the singleton bounds handler
     * 
     * @return
     */
    public static BoundsHandler get() {
        return handler;
    }
    
    /**
     * Return the bounds of the cell in virtual world coordinates. This bounds
     * does not necessarily encapsulate all the child bounds.
     * @param cellID
     * @return
     */
    public abstract Bounds getCachedVWBounds(CellID cellID);

    /**
     * Return the computed World Bounds of the specified cell. The computed
     * world bounds encapsulate the cell and all it's children.
     * 
     * @param cellID
     * @return
     */
    public abstract Bounds getComputedWorldBounds(CellID cellID);

    /**
     * Return the local bounds of the specified cell
     * 
     * @param cellID
     * @return
     */
    public abstract Bounds getLocalBounds(CellID cellID);

    /**
     * Set the local bounds of the specified cell
     * 
     * @param cellID
     * @param bounds
     */
    public abstract void setLocalBounds(CellID cellID, Bounds bounds);

    public abstract void setComputedWorldBounds(CellID cellID, Bounds bounds);

    public abstract void setLocalToVworld(CellID cellID, Matrix4d transform);

    /**
     * Construct the graph by adding the child to the parent cell
     * This call does not recompute the internal transform or bounds data
     * 
     * @param parent
     * @param child
     * @throws org.jdesktop.wonderland.common.cell.MultipleParentException
     */
    public abstract void addChild(CellMO parent, CellMO child) throws MultipleParentException ;
    
    public abstract void removeChild(CellMO parent, CellMO child);
    
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
    
    public abstract void cellTransformChanged(CellID cellID, Matrix4d transform);
    
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
    public abstract void cellBoundsChanged(CellID cellID, Bounds bounds);
    
    public abstract Collection<CellID> getVisibleCells(CellID rootCell, Bounds bounds, UserPerformanceMonitor perfMonitor);
 
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
    public Collection<CellID> getVisibleCells(CellID rootCell, Bounds bounds, UserPerformanceMonitor perfMonitor, Class cellClass, boolean reportSubclasses) {
        throw new RuntimeException("Not Implemented");
    }
 
    
}
