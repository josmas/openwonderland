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
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.CellDescription;
import org.jdesktop.wonderland.server.cell.RevalidatePerformanceMonitor;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 *
 * @author paulby
 */
@InternalAPI
public class CellDescriptionManagerImpl implements CellDescriptionManager {
    private CellDescriptionService service;
    
    public CellDescriptionManagerImpl(CellDescriptionService service) {
        this.service = service;
    }

    public CellDescriptionImpl getCellDescription(CellID cellID) {
        return service.getCellDescription(cellID);
    }
    
    public CellDescriptionImpl addCellDescription(CellMO cell) {
        return service.addCellDescription(cell);
    }
    
    public CellDescriptionImpl removeCellDescription(CellID cellID) {
        return service.removeCellDescription(cellID);
    }

    public Collection<CellDescription> 
            getVisibleCells(CellID rootCell, BoundingVolume bounds, 
                            RevalidatePerformanceMonitor perfMonitor) {
        return service.getVisibleCells(rootCell, bounds, perfMonitor);
    }
    
    public void cellTransformChanged(CellID cellID, CellTransform transform) {
        service.cellTransformChanged(cellID, transform);
    }

    public void cellBoundsChanged(CellID cellID, BoundingVolume bounds) {
        service.cellBoundsChanged(cellID, bounds);
    }

    public void cellChildrenChanged(CellID parent, CellID child, boolean childAdded) {
        service.cellChildrenChanged(parent, child, childAdded);
    }

    public void cellContentsChanged(CellID cellID) {
        service.cellContentsChanged(cellID);
    }
}
