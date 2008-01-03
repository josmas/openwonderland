/**
 * Project Looking Glass
 *
 * $RCSfile: SerializationHelper.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.4 $
 * $Date: 2007/05/04 23:11:34 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.media.j3d.Bounds;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Matrix4f;

/**
 * 
 * Provides support to serialize various objects
 * 
 * 
 * 
 * @author paulby
 */
public class SerializationHelper {
    
    private static final short NULL = 0;
    private static final short BOX = 1;
    private static final short SPHERE = 2;
    
    /**
     * Handle serialization of Bounds
     */
    public static void writeBoundsObject(Bounds bounds, ObjectOutputStream out) throws IOException {
        if (bounds==null)
            out.writeShort(NULL);
        else if (bounds instanceof BoundingBox) {
            out.writeShort(BOX);
            Point3d p = new Point3d();
            ((BoundingBox)bounds).getUpper(p);
            out.writeDouble(p.x);
            out.writeDouble(p.y);
            out.writeDouble(p.z);
            ((BoundingBox)bounds).getLower(p);
            out.writeDouble(p.x);
            out.writeDouble(p.y);
            out.writeDouble(p.z);
        } else if (bounds instanceof BoundingSphere) {
            out.writeShort(SPHERE);
            Point3d p = new Point3d();
            ((BoundingSphere)bounds).getCenter(p);
            out.writeDouble(p.x);
            out.writeDouble(p.y);
            out.writeDouble(p.z);
            out.writeDouble(((BoundingSphere)bounds).getRadius());
        } else
            throw new RuntimeException("Unsupported Tile Bounds "+bounds.getClass());
        
    }
    
    /**
     * Handle de-serialization of Bounds
     */
    public static Bounds readBoundsObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Bounds bounds;
        switch(in.readShort()) {
            case NULL :
                bounds=null;
                break;
            case BOX :
                Point3d upper = new Point3d();
                upper.x = in.readDouble();
                upper.y = in.readDouble();
                upper.z = in.readDouble();
                Point3d lower = new Point3d();
                lower.x = in.readDouble();
                lower.y = in.readDouble();
                lower.z = in.readDouble();
                bounds = new BoundingBox(lower,upper);
                break;
            case SPHERE :
                Point3d p = new Point3d();
                double radius;
                p.x = in.readDouble();
                p.y = in.readDouble();
                p.z = in.readDouble();
                radius = in.readDouble();
                bounds = new BoundingSphere(p,radius);
                break;
            default :
                throw new RuntimeException("Unrecognized Bounds Class");
        }
        
        return bounds;
    }

    /**
     * Handle serialization of Transform3D
     */
    public static void writeTransform3DObject(Transform3D t3d, ObjectOutputStream out) 
	throws IOException 
    {
	System.err.println("in SH.writeTransform3DObject: t3d = " + t3d);

	Matrix4f mat = new Matrix4f();
	t3d.get(mat);

	out.writeFloat(mat.m00);
	out.writeFloat(mat.m01);
	out.writeFloat(mat.m02);
	out.writeFloat(mat.m03);
	out.writeFloat(mat.m10);
	out.writeFloat(mat.m11);
	out.writeFloat(mat.m12);
	out.writeFloat(mat.m13);
	out.writeFloat(mat.m20);
	out.writeFloat(mat.m21);
	out.writeFloat(mat.m22);
	out.writeFloat(mat.m23);
	out.writeFloat(mat.m31);
	out.writeFloat(mat.m31);
	out.writeFloat(mat.m32);
	out.writeFloat(mat.m33);
    }

    /**
     * Handle de-serialization of Transform3D
     */
    public static Transform3D readTransform3DObject(ObjectInputStream in) 
	throws IOException, ClassNotFoundException 
    {
	Matrix4f mat = new Matrix4f();

	mat.m00 = in.readFloat();
	mat.m01 = in.readFloat();
	mat.m02 = in.readFloat();
	mat.m03 = in.readFloat();
	mat.m10 = in.readFloat();
	mat.m11 = in.readFloat();
	mat.m12 = in.readFloat();
	mat.m13 = in.readFloat();
	mat.m20 = in.readFloat();
	mat.m21 = in.readFloat();
	mat.m22 = in.readFloat();
	mat.m23 = in.readFloat();
	mat.m31 = in.readFloat();
	mat.m31 = in.readFloat();
	mat.m32 = in.readFloat();
	mat.m33 = in.readFloat();

	Transform3D t3d = new Transform3D(mat);
	return t3d;
    }
}
