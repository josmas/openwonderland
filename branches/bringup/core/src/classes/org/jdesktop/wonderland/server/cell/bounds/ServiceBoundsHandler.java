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

import com.sun.sgs.app.AppContext;
import java.util.Iterator;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.UserPerformanceMonitor;
import org.jdesktop.wonderland.server.cell.*;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 *
 * @author paulby
 */
public class ServiceBoundsHandler extends BoundsHandler {

    /**
     * Create Handler for specified cell
     * @param cell
     */
    public ServiceBoundsHandler() {
    }
    
    private CellMirror get(CellID cellID) {
        BoundsManager mgr = AppContext.getManager(BoundsManager.class);
        return mgr.getCellBounds(cellID);
    }
    
    /**
     * Returns the local bounds transformed into VW coordinates. These bounds
     * do not include the subgraph bounds. This call is only valid for live
     * cells
     * 
     * @return
     */
    public Bounds getCachedVWBounds(CellID cellID) {
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
    public Bounds getComputedWorldBounds(CellID cellID) {
        return get(cellID).getComputedWorldBounds();        
    }
    
    /**
     * Set the computed world bounds of this cell
     * 
     * @param bounds
     */
    public void setComputedWorldBounds(CellID cellID, Bounds bounds) {
        get(cellID).setComputedWorldBounds(bounds);
    }

    @Override
    public Bounds getLocalBounds(CellID cellID) {
        return get(cellID).getLocalBounds();
    }

    @Override
    public void setLocalBounds(CellID cellID, Bounds bounds) {
        get(cellID).setLocalBounds(bounds);
    }

    @Override
    public void setLocalToVworld(CellID cellID, Matrix4d transform) {
        get(cellID).setLocalToVWorld(transform);
    }

    @Override
    public void addChild(CellMO parent, CellMO child) throws MultipleParentException {
        get(parent.getCellID()).addChild(get(child.getCellID()));
    }

    @Override
    public void removeChild(CellMO parent, CellMO child) {
        get(parent.getCellID()).removeChild(get(child.getCellID()));
    }

    @Override
    public void createBounds(CellMO cell) {
        BoundsManager mgr = AppContext.getManager(BoundsManager.class);
        mgr.putCellBounds(new CellMirror(cell));
    }

    @Override
    public void removeBounds(CellMO cell) {
        BoundsManager mgr = AppContext.getManager(BoundsManager.class);
        mgr.removeCellBounds(cell.getCellID());
    }

    @Override
    public void cellTransformChanged(CellID cellID, Matrix4d transform) {
        BoundsManager mgr = AppContext.getManager(BoundsManager.class);
        mgr.cellTransformChanged(cellID, transform);
    }

    @Override
    public void cellBoundsChanged(CellID cellID, Bounds bounds) {
        BoundsManager mgr = AppContext.getManager(BoundsManager.class);
        mgr.cellBoundsChanged(cellID, bounds);
    }

    @Override
    public Iterator<CellID> getVisibleCells(CellID rootCell, Bounds bounds, UserPerformanceMonitor perfMonitor) {
        BoundsManager mgr = AppContext.getManager(BoundsManager.class);
        return mgr.getVisibleCells(rootCell, bounds, perfMonitor);
    }

    @Override
    public void cellChildrenChanged(CellID parent, CellID child, boolean childAdded) {
        AppContext.getManager(BoundsManager.class).childrenChanged(parent, child, childAdded);
    }

}
