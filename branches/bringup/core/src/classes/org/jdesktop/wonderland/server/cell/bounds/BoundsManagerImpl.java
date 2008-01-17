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

import java.util.Collection;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.server.UserPerformanceMonitor;

/**
 *
 * @author paulby
 */
public class BoundsManagerImpl implements BoundsManager {
    private BoundsService service;
    
    public BoundsManagerImpl(BoundsService service) {
        this.service = service;
    }

    public CellMirror getCellBounds(CellID cellID) {
        return service.getCellBounds(cellID);
    }
    
    public void putCellBounds(CellMirror cellBounds) {
        service.putCellBounds(cellBounds);
    }
    
    public void removeCellBounds(CellID cellID) {
        service.removeCellBounds(cellID);
    }

    public void cellTransformChanged(CellID cellID, Matrix4d transform) {
        service.cellTransformChanged(cellID, transform);
    }

    public void cellBoundsChanged(CellID cellID, Bounds bounds) {
        service.cellBoundsChanged(cellID, bounds);
    }

    public Collection<CellID> getVisibleCells(CellID rootCell, Bounds bounds, UserPerformanceMonitor perfMonitor) {
        return service.getVisibleCells(rootCell, bounds, perfMonitor);
    }

    public void childrenChanged(CellID parent, CellID child, boolean childAdded) {
        service.childrenChanged(parent, child, childAdded);
    }


}
