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

import com.sun.sgs.app.ManagedReference;
import java.util.Iterator;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.jdesktop.j3d.utils.math.Math3D;
import org.jdesktop.wonderland.common.cell.MultipleParentException;

/**
 * @author paulby
 */
public class GroupCellMO extends CellMO implements CellContainerInterface {

    public GroupCellMO(BoundingBox bounds, Matrix4d cellOrigin) {
        super();
        setLocalBounds(bounds);
        setTransform(cellOrigin);
    }

    private static Matrix4d calcBoxCenter(BoundingBox bounds) {
        Matrix4d ret;
        Point3d upper = new Point3d();
        Point3d lower = new Point3d();
        bounds.getUpper(upper);
        bounds.getLower(lower);

        Vector3d cellCenter = new Vector3d();
        cellCenter.x = lower.x + (upper.x-lower.x)/2;
        cellCenter.y = lower.y + (upper.y-lower.y)/2;
        cellCenter.z = lower.z + (upper.z-lower.z)/2;

        ret = new Matrix4d();
        ret.setIdentity();
        ret.setTranslation(cellCenter);

        return ret;
    }

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
    public CellMO insertCellInHierarchy(CellMO insertChild, Bounds childVWBounds) throws MultipleParentException {
        CellMO parent = null;
        
        if (!boundsContainsPoint(getCachedVWBounds(), getBoundsCenter(childVWBounds))) {
            // Center of child bounds is not within our bounds so we should
            // not be the parent
            return null;
        }
        
        // Try our children first
        Iterator<ManagedReference> it = getAllChildrenRefs();
        while(it.hasNext() && parent==null) {
            CellMO child = it.next().get(CellMO.class);
            if (child instanceof CellContainerInterface)
                parent = ((CellContainerInterface)child).insertCellInHierarchy(insertChild, childVWBounds);
        }
        
        
        if (parent==null) {
            addChild(insertChild);
            parent = this;
        }
        
        return parent;
    }
    
    /**
     * Returns true if the bounds contains the point
     * 
     * @param bounds
     * @param point
     * @return
     */
    public static boolean boundsContainsPoint(Bounds bounds, Point3d point) {
        if (bounds instanceof BoundingBox) {
            return boundsContainsPoint((BoundingBox)bounds, point);
        } else if (bounds instanceof BoundingSphere) {
            return boundsContainsPoint((BoundingSphere)bounds, point);
        } else {
            throw new IllegalArgumentException("Unsupported Bounds class "+bounds.getClass().getName());
        }
    }
    
    /**
     * Returns true if the bounds contains the point
     * 
     * @param bounds
     * @param point
     * @return
     */
    public static boolean boundsContainsPoint(BoundingBox bounds, Point3d point) {
        Point3d upper = new Point3d();
        Point3d lower = new Point3d();
        ((BoundingBox)bounds).getUpper(upper);
        ((BoundingBox)bounds).getUpper(lower);
        
        if (point.x<lower.x || point.x>upper.x) return false;
        if (point.y<lower.y || point.y>upper.y) return false;
        if (point.z<lower.z || point.z>upper.z) return false;
        
        return true;
    }
    
    /**
     * Returns true if the bounds contains the point
     * 
     * @param bounds
     * @param point
     * @return
     */
    public static boolean boundsContainsPoint(BoundingSphere bounds, Point3d point) {
        Point3d center = new Point3d();
        bounds.getCenter(center);
        
        Vector3d diff = new Vector3d();
        diff.sub(center, point);
        
        double distSq = diff.lengthSquared();
        if (distSq > (bounds.getRadius()*bounds.getRadius()))
            return false;
        
        return true;
    }
    
    /**
     * Returns the center of the bounds
     * 
     * @param bounds
     * @return
     */
    public static Point3d getBoundsCenter(Bounds bounds) {
        Point3d center = new Point3d();
        
        if (bounds instanceof BoundingSphere) {
            ((BoundingSphere)bounds).getCenter(center);
        } else if (bounds instanceof BoundingBox) {
            Point3d upper = new Point3d();
            Point3d lower = new Point3d();
            ((BoundingBox)bounds).getUpper(upper);
            ((BoundingBox)bounds).getUpper(lower);
            center.x = upper.x - lower.x;
            center.y = upper.y - lower.y;
            center.z = upper.z - lower.z;
        } else {
            throw new IllegalArgumentException("Unsupported Bounds class "+bounds.getClass().getName());
        }
        
        return center;
    }
}
