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
import org.jdesktop.wonderland.PrivateAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.RevalidatePerformanceMonitor;
import org.jdesktop.wonderland.server.cell.CellMirror;

/**
 *
 * @author paulby
 */
@PrivateAPI
public interface BoundsManager {

    public void cellTransformChanged(CellID cellID, CellTransform transform);
    public void cellBoundsChanged(CellID cellID, BoundingVolume bounds);
    public CellDescriptionImpl getCellMirrorImpl(CellID cellID);
    public void putCellMirrorImpl(CellDescriptionImpl cellBounds);
    public void removeCellMirrorImpl(CellID cellID);
    public Collection<CellMirror> getVisibleCells(CellID rootCell, BoundingVolume bounds, RevalidatePerformanceMonitor perfMonitor);
    public void cellChildrenChanged(CellID parent, CellID child, boolean childAdded);
    public void cellContentsChanged(CellID cellID);
}
