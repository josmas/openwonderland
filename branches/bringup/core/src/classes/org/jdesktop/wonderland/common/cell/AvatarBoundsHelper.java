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
package org.jdesktop.wonderland.common.cell;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.Geometry;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

/**
 * Support methods for dealing with avatar bounds
 * 
 * @author paulby
 */
public class AvatarBoundsHelper {
    
    private static float AVATAR_CELL_SIZE = 10f;
    private static float PROXIMITY_SIZE = 200f;      // Radius
    
    // TODO use the back clip property from the view preferences for this
    // value.  Make sure to keep in sync on client and server!
    public static float MAX_VIEW_DISTANCE = 300f;    // Maxiumum distance user can see
        
    /**
     * Return the bounds of the avatar cell.
     */
    public static Bounds getCellBounds(Tuple3f center) {
        Bounds tileBounds = new BoundingBox(
                new Point3d(-AVATAR_CELL_SIZE/2+center.x, -AVATAR_CELL_SIZE/2+center.y, -AVATAR_CELL_SIZE/2+center.z), 
                new Point3d(AVATAR_CELL_SIZE/2+center.x, AVATAR_CELL_SIZE/2+center.y, AVATAR_CELL_SIZE/2+center.z));
        return tileBounds;
        
    }
    
    /**
     * Returns the proximity bounds for the avatar with
     * specified center
     */
    public static BoundingSphere getProximityBounds(Tuple3f center) {
        Point3d p3d = new Point3d(center);
        
        return new BoundingSphere(p3d, PROXIMITY_SIZE);
    }
    
    /**
     * Returns the view frustum bounds. This is computed from the values
     * provided in the last computeViewPlatformTransform call
     */
    public static Bounds getFrustumBounds(Transform3D avatarT3D) {
        Point3d p = new Point3d(0f,0f,-MAX_VIEW_DISTANCE/2f*0.95f);
        avatarT3D.transform(p);
                
        return new BoundingSphere(p, MAX_VIEW_DISTANCE/2);
    }
    
    /**
     * Compute and return the View Platform Transform3D give the users position, direction and up
     */
    public static Transform3D computeViewPlatformTransform(Point3f userPosition, Vector3f direction, Vector3f up) {        
        Matrix4f mat = new Matrix4f();
        Vector3f axisX = new Vector3f();
        Vector3f axisY = new Vector3f();
        Vector3f axisZ = new Vector3f(direction);
        axisZ.negate();
        axisZ.normalize();

        axisX.cross(up, axisZ);
        axisX.normalize();

        axisY.cross(axisZ, axisX);
        axisY.normalize();
        
        mat.m00 = axisX.x;
        mat.m10 = axisX.y;
        mat.m20 = axisX.z;
        mat.m30 = 0.0f;

        mat.m01 = axisY.x;
        mat.m11 = axisY.y;
        mat.m21 = axisY.z;
        mat.m31 = 0.0f;

        mat.m02 = axisZ.x;
        mat.m12 = axisZ.y;
        mat.m22 = axisZ.z;
        mat.m32 = 0.0f;
        
        mat.m03 = userPosition.x;
        mat.m13 = userPosition.y;
        mat.m23 = userPosition.z;
        mat.m33 = 1.0f;

        return new Transform3D(mat);
    }

//    private static Shape3D getCellBoundsShape() {
//        Appearance app = new Appearance();
//        PolygonAttributes polyAttr = new PolygonAttributes();
//        polyAttr.setPolygonMode(PolygonAttributes.POLYGON_LINE);
//        app.setPolygonAttributes(polyAttr);
//        Shape3D shape = new Shape3D(createSquare(AVATAR_CELL_SIZE, new Color3f(1f,1f,1f)), app);
//        
//        return shape;
//    }
//    
//    public static Shape3D getPromximityBoundsShape() {
//        Appearance app = new Appearance();
//        PolygonAttributes polyAttr = new PolygonAttributes();
//        polyAttr.setPolygonMode(PolygonAttributes.POLYGON_LINE);
//        app.setPolygonAttributes(polyAttr);
//        Shape3D shape = new Shape3D(createCircle(PROXIMITY_SIZE, new Color3f(1f,1f,1f)), app);
//        
//        return shape;
//    }
//    
//    private static Geometry createSquare(float size, Color3f color) {
//        QuadArray quad = new QuadArray(4, QuadArray.COORDINATES | QuadArray.COLOR_3);
//        float height = 0.5f;
//        
//        float coords[] = new float[] {
//            -size/2, height, -size/2,
//            -size/2, height, size/2,
//            size/2, height, size/2,
//            size/2, height, -size/2,
//        };
//        
//        quad.setCoordinates(0, coords);
//        for (int i=0; i<coords.length/3; i++)
//            quad.setColor(i, color);
//        
//        return quad;
//    }    
//    
//    private static Geometry createCircle(float radius, Color3f color) {
//        int divisions = 10;
//        LineStripArray line = new LineStripArray(divisions+1, LineArray.COORDINATES | LineArray.COLOR_3, new int[] {divisions+1} );
//        float height = 0.5f;
//        
//        double step = (Math.PI/divisions*2);
//        for(int i=0; i<divisions/2; i++) {
//            double angle = step*i;
//            float x = (float)Math.cos(angle)*radius;
//            float z = (float)Math.sin(angle)*radius;
//            line.setCoordinate(i, new float[] { x, height, z});
//            line.setColor(i, color);
//            
//            line.setCoordinate(i+divisions/2, new float[] { -x, height, -z});
//            line.setColor(i+divisions/2, color);
//        }
//        
//        float coords[] = new float[3];
//        line.getCoordinate(0,coords);
//        line.setCoordinate(divisions, coords);
//        line.setColor(divisions,color);
//        
//        return line;
//    }
    
}
