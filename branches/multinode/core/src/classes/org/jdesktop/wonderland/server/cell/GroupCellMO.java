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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.ManagedReference;
import java.util.Iterator;
import org.jdesktop.wonderland.PrivateAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;

/**
 * @author paulby
 */
@PrivateAPI
public class GroupCellMO extends CellMO implements CellContainerInterface {

    public GroupCellMO(BoundingBox bounds, CellTransform cellOrigin) {
        super();
        setLocalBounds(bounds);
        setTransform(cellOrigin);
    }

//    private static Matrix4d calcBoxCenter(BoundingBox bounds) {
//        Matrix4d ret;
//        Point3d upper = new Point3d();
//        Point3d lower = new Point3d();
//        bounds.getUpper(upper);
//        bounds.getLower(lower);
//
//        Vector3d cellCenter = new Vector3d();
//        cellCenter.x = lower.x + (upper.x-lower.x)/2;
//        cellCenter.y = lower.y + (upper.y-lower.y)/2;
//        cellCenter.z = lower.z + (upper.z-lower.z)/2;
//
//        ret = new Matrix4d();
//        ret.setIdentity();
//        ret.setTranslation(cellCenter);
//
//        return ret;
//    }

    @Override
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.darkstar.client.cell.GroupCell";
    }

//    public CellSetup getSetupData() {
//        return null;
//    }

//    @Override
//    public void addChildCell(ManagedReference childRef) {
//        super.addChildCell(childRef);
//        System.out.println("Child count "+childCells.size());
//    }

    /**
     * {@inheritDoc}
     */
    public CellMO insertCellInHierarchy(CellMO insertChild, BoundingVolume childVWBounds) throws MultipleParentException {
        CellMO parent = null;
        
        if (!getCachedVWBounds().contains(childVWBounds.getCenter())) {
            // Center of child bounds is not within our bounds so we should
            // not be the parent
            return null;
        }
        
        // Try our children first
        Iterator<ManagedReference<CellMO>> it = getAllChildrenRefs().iterator();
        while(it.hasNext() && parent==null) {
            CellMO child = it.next().get();
            if (child instanceof CellContainerInterface)
                parent = ((CellContainerInterface)child).insertCellInHierarchy(insertChild, childVWBounds);
        }
        
        
        if (parent==null) {
            addChild(insertChild);
            parent = this;
        }
        
        return parent;
    }
    
}
