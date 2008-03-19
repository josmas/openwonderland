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
package org.jdesktop.wonderland.common.messages;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * Utility class for packing objects into an ObjectStream
 * 
 * @author paulby
 */
public class PackHelper {
    
    private static final byte BOUNDS_NULL = 0;
    private static final byte BOUNDS_BOX = 1;
    private static final byte BOUNDS_SPHERE = 2;

    public static void writeBoundingVolume(ObjectOutputStream out, BoundingVolume v) throws IOException {
        if (v==null) {
            out.writeByte(BOUNDS_NULL);
        } else if (v instanceof BoundingBox) {
            BoundingBox bbox = ((BoundingBox)v);
            Vector3f center = bbox.getCenter();
            Vector3f extent = bbox.getExtent(null);
            out.writeFloat(center.x);
            out.writeFloat(center.y);
            out.writeFloat(center.z);
            out.writeFloat(extent.x);
            out.writeFloat(extent.y);
            out.writeFloat(extent.z);
        } else if (v instanceof BoundingSphere) {
            BoundingSphere bsphere = (BoundingSphere)v;
            Vector3f center = bsphere.getCenter();
            out.writeFloat(center.x);
            out.writeFloat(center.y);
            out.writeFloat(center.z);
            out.writeFloat(bsphere.getRadius());
        } else
            throw new RuntimeException("Unsupported BoundingVolume type "+v.getClass().getName());
            
    }
    
    public static BoundingVolume readBoundingVolume(ObjectInputStream in) throws IOException {
        BoundingVolume ret;
        
        byte type = in.readByte();
 
        if (type==BOUNDS_NULL)
            return null;
        
        Vector3f center = new Vector3f();
        center.x = in.readFloat();
        center.y = in.readFloat();
        center.z = in.readFloat();
        
        
        switch(type) {
            case BOUNDS_BOX :
                Vector3f extent = new Vector3f();
                extent.x = in.readFloat();
                extent.y = in.readFloat();
                extent.z = in.readFloat();
                ret = new BoundingBox(center, extent.x, extent.y, extent.z);
                break;
            case BOUNDS_SPHERE :
                float radius = in.readFloat();
                ret = new BoundingSphere(radius, center);
                break;
            default :
                throw new RuntimeException("Unknown BoundingVolume type "+type);
        }
        
        return ret;
    }
    
    public static void writeCellTransform(ObjectOutputStream out, CellTransform transform) throws IOException {
        if (transform==null) {
            writeVector3f(out, null);
            writeQuaternion(out, null);
        } else {
            writeVector3f(out, transform.getTranslation(null));
            writeQuaternion(out, transform.getRotation(null));
        }
    }
    
    public static CellTransform readCellTransform(ObjectInputStream in) throws IOException {
        CellTransform ret;
        
        Vector3f translation = readVector3f(in);
        Quaternion rotation = readQuaternion(in);
        
        if (translation==null && rotation==null)
            ret = null;
        else {
            ret = new CellTransform(rotation, translation);
        }
        
        return ret;
    }
    
    public static void writeVector3f(ObjectOutputStream out, Vector3f v) throws IOException {
        if (v==null) {
            out.writeFloat(Float.MAX_VALUE);
            return;
        }
        
        out.writeFloat(v.x);
        out.writeFloat(v.y);
        out.writeFloat(v.z);
    }
    
    public static Vector3f readVector3f(ObjectInputStream in) throws IOException {
        float x = in.readFloat();
        if (x==Float.MAX_VALUE)
            return null;
        
        return new Vector3f(x, in.readFloat(), in.readFloat());
    }
    
    public static void writeQuaternion(ObjectOutputStream out, Quaternion q) throws IOException {
        if (q==null) {
            out.writeFloat(Float.MAX_VALUE);
            return;
        }
        
        out.writeFloat(q.x);
        out.writeFloat(q.y);
        out.writeFloat(q.z);
        out.writeFloat(q.w);
    }
    
    public static Quaternion readQuaternion(ObjectInputStream in) throws IOException {
        float x = in.readFloat();
        if (x==Float.MAX_VALUE)
            return null;
        
        return new Quaternion(x, in.readFloat(), in.readFloat(), in.readFloat());
    }
}
