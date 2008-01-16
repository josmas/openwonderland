/**
 * Project Looking Glass
 *
 * $RCSfile: BasicCellMOHelper.java,v $
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
 * $Revision: 1.2 $
 * $Date: 2007/10/17 17:11:13 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.server.setup;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import org.jdesktop.wonderland.common.Math3DUtils;

/**
 *
 * @author jkaplan
 */
public class BasicCellMOHelper {
   
    public static Bounds getCellBounds(BasicCellMOSetup setup) {
        if (setup.getBoundsType() == null) {
            return null;
        } else if (setup.getBoundsType().equals("SPHERE")) {
            return new BoundingSphere(new Point3d(setup.getOrigin()), 
                                                  setup.getBoundsRadius());
        } else if (setup.getBoundsType().equals("BOX")) {
            return Math3DUtils.createBoundingBox(new Vector3d(setup.getOrigin()),
                                             (float) setup.getBoundsRadius());
        }
        
        return null;
    }
    
    public static Matrix4d getCellOrigin(BasicCellMOSetup setup) {
        Matrix3d rot = new Matrix3d();
        rot.set(new AxisAngle4d(setup.getRotation()));
        
        Vector3d trans = new Vector3d(setup.getOrigin());
        double scale = setup.getScale();
        
        Matrix4d out = new Matrix4d();
        out.set(rot, trans, scale);
        return out;
    }

    public static String getBoundsType(Bounds bounds) {
        if (bounds instanceof BoundingSphere) {
            return "SPHERE";
        } else if (bounds instanceof BoundingBox) {
            return "BOX";
        }

        return null;
    }

    public static double getBoundsRadius(Bounds bounds) {
        if (bounds instanceof BoundingSphere) {
            return ((BoundingSphere) bounds).getRadius();
        } else if  (bounds instanceof BoundingBox) {
            BoundingBox bb = (BoundingBox) bounds;

            Point3d upper = new Point3d();
            Point3d lower = new Point3d();
            bb.getUpper(upper);
            bb.getLower(lower);

            upper.sub(lower);
            return Math.abs(upper.x);
        }

        return 0;
    }

    public static double[] getTranslation(Matrix4d origin) {
        Vector3d trans = new Vector3d();
        origin.get(trans);

        return new double[] { trans.x, trans.y, trans.z };
    }

    public static double[] getRotation(Matrix4d origin) {
        // create an angle from the origin
        AxisAngle4d rot = new AxisAngle4d();
        rot.set(origin);

        return new double[] { rot.x, rot.y, rot.z, rot.angle };
    }
} 
