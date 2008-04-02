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
import com.sun.sgs.app.AppContext;
import java.util.Collection;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.server.cell.RevalidatePerformanceMonitor;
import org.jdesktop.wonderland.server.cell.*;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * An implementation of BoundsManager based on a darkstar service.
 * 
 * @author paulby
 */
@InternalAPI
public class ServiceCellDescriptionManager extends BoundsManager {

    /**
     * Create the Manager.
     * @param cell
     */
    public ServiceCellDescriptionManager() {
    }
    
    /**
     * Return the description of the specified cell
     * @param cellID
     * @return
     */
    private CellDescriptionImpl get(CellID cellID) {
        CellDescriptionManager mgr = AppContext.getManager(CellDescriptionManager.class);
        return mgr.getCellMirrorImpl(cellID);
    }
    
    /**
     * Returns the local bounds transformed into VW coordinates. These bounds
     * do not include the subgraph bounds. This call is only valid for live
     * cells
     * 
     * @return
     */
    public BoundingVolume getCachedVWBounds(CellID cellID) {
        return get(cellID).getCachedVWBounds();
    }
    
    /**
     * Return a computed bounds for this cell in World coordinates that 
     * encapsulates the bounds of this cell and all it's children.
     * 
     * The bounds returned by this call are computed periodically so changes
     * to the local bounds of this node or any of it's children may not be 
     * immediately reflected in this bounds.
     * 
     * @return
     */
    public BoundingVolume getComputedWorldBounds(CellID cellID) {
        return get(cellID).getComputedWorldBounds();        
    }
    
    @Override
    public BoundingVolume getLocalBounds(CellID cellID) {
        return get(cellID).getLocalBounds();
    }

    @Override
    public CellTransform getLocalToVWorld(CellID cellID) {
        return get(cellID).getLocalToVWorld();
    }
    
    @Override
    public void createBounds(CellMO cell) {
        CellDescriptionManager mgr = AppContext.getManager(CellDescriptionManager.class);
        mgr.putCellMirrorImpl(new CellDescriptionImpl(cell));
    }

    @Override
    public void removeBounds(CellMO cell) {
        CellDescriptionManager mgr = AppContext.getManager(CellDescriptionManager.class);
        mgr.removeCellMirrorImpl(cell.getCellID());
    }

    @Override
    public void cellTransformChanged(CellID cellID, CellTransform transform) {
        CellDescriptionManager mgr = AppContext.getManager(CellDescriptionManager.class);
        mgr.cellTransformChanged(cellID, transform);
    }

    @Override
    public void cellBoundsChanged(CellID cellID, BoundingVolume bounds) {
        CellDescriptionManager mgr = AppContext.getManager(CellDescriptionManager.class);
        mgr.cellBoundsChanged(cellID, bounds);
    }

    @Override
    public Collection<CellDescription> getVisibleCells(CellID rootCell, BoundingVolume bounds, RevalidatePerformanceMonitor perfMonitor) {
        CellDescriptionManager mgr = AppContext.getManager(CellDescriptionManager.class);
        return mgr.getVisibleCells(rootCell, bounds, perfMonitor);
    }

    @Override
    public void cellChildrenChanged(CellID parent, CellID child, boolean childAdded) {
        AppContext.getManager(CellDescriptionManager.class).cellChildrenChanged(parent, child, childAdded);
    }

    @Override
    public void cellContentsChanged(CellID cellID) {
        AppContext.getManager(CellDescriptionManager.class).cellContentsChanged(cellID);
    }

    @Override
    public CellDescription getCellMirror(CellID cellID) {
        CellDescriptionManager mgr = AppContext.getManager(CellDescriptionManager.class);
        return mgr.getCellMirrorImpl(cellID);
    }
}
