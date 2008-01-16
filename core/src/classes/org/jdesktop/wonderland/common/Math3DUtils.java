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
package org.jdesktop.wonderland.common;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Utilities for dealing with 3D Math
 * 
 * @author paulby
 */
public class Math3DUtils {

    /**
     * Creates a bounding box with the specified center and size.
     */
    public static BoundingBox createBoundingBox(Vector3d center, float size) {
        BoundingBox cellBounds = new BoundingBox(new Point3d(center.x-size/2f, center.y-size/2f, center.z-size/2f), new Point3d(center.x+size/2f, center.y+size/2f, center.z+size/2f));
        return cellBounds;
    }
    
    /**
     * Creates a bounding box with the specified center and dimensions.
     */
    public static BoundingBox createBoundingBox(Vector3d center, float xDim, float yDim, float zDim) {
        BoundingBox cellBounds = new BoundingBox(new Point3d(center.x-xDim/2f, center.y-yDim/2f, center.z-zDim/2f), new Point3d(center.x+xDim/2f, center.y+yDim/2f, center.z+zDim/2f));
        return cellBounds;
    }
    
    /**
     * Creates a bounding sphere with the specified center and size.
     */
    public static BoundingSphere createBoundingSphere(Vector3d center, float radius) {
        return new BoundingSphere(new Point3d(center), radius);
    }
    
    /**
     * Creates a Matrix4d with a translation to center
     */
    public static Matrix4d createOriginM4d(Vector3d center) {
        Matrix3d rot = new Matrix3d();
        rot.setIdentity();
        return new Matrix4d(rot, center, 1);
    }
    
    /**
     * Create a Matrix4D with a translation to center, a rotation in the
     * y axis and a scale
     */
    public static Matrix4d createOriginM4d(Vector3d center, double angle,
        double scale) {
        Matrix3d rot = new Matrix3d();
        rot.rotY(angle);
        return new Matrix4d(rot, center, scale);
    }
      
}
