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
package org.jdesktop.wonderland.server.spatial.impl;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.server.spatial.SpatialCell;
import org.jdesktop.wonderland.server.spatial.Universe;

/**
 *
 * @author paulby
 */
public class UniverseImpl implements Universe {

    private SpaceManager spaceManager = new SpaceManagerGridImpl();

    public void addSpatialCell(SpatialCell cell) {
        SpatialCellImpl cellImpl = (SpatialCellImpl) cell;
        
        cellImpl.setRoot(cell);
        
        cellImpl.acquireRootReadLock();
//        System.out.println("Getting spaces for "+cell.getWorldBounds());
        Iterable<Space> it = spaceManager.getEnclosingSpace(cell.getWorldBounds());
        for(Space s : it) {
            s.addRootSpatialCell(cellImpl);
        }
        cellImpl.releaseRootReadLock();
    }

    public void addViewSpatialCell(SpatialCell cell) {
        addSpatialCell(cell);
        ViewCache cache = new ViewCache((SpatialCellImpl)cell, spaceManager);
    }

    public SpatialCell createSpatialCell(CellID id, boolean view) {
        if (view) {
            return new ViewCellImpl(id, spaceManager);
        } else
            return new SpatialCellImpl(id);
    }
    

}
