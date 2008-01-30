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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.Math3DUtils;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author jkaplan
 */
public class BasicCellMOHelper {
   
    public static BoundingVolume getCellBounds(BasicCellMOSetup setup) {
        if (setup.getBoundsType() == null) {
            return null;
        } else if (setup.getBoundsType().equals("SPHERE")) {
            return new BoundingSphere((float)setup.getBoundsRadius(), new Vector3f() );
        } else if (setup.getBoundsType().equals("BOX")) {
//            return Math3DUtils.createBoundingBox(new Vector3d(setup.getOrigin()),
//                                             (float) setup.getBoundsRadius());
            return new BoundingBox(new Vector3f(), setup.getBoundsRadius(), setup.getBoundsRadius(), setup.getBoundsRadius());
        }
        
        return null;
    }
    
    public static CellTransform getCellTransform(BasicCellMOSetup setup) {
        return setup.getCellTransform();
    }

    public static String getBoundsType(BoundingVolume bounds) {
        if (bounds instanceof BoundingSphere) {
            return "SPHERE";
        } else if (bounds instanceof BoundingBox) {
            return "BOX";
        } else {
            throw new RuntimeException("Unsupported bounds type "+bounds.getClass().getName());
        }
    }

    public static float getBoundsRadius(BoundingVolume bounds) {
        if (bounds instanceof BoundingSphere) {
            return ((BoundingSphere) bounds).getRadius();
        } else if  (bounds instanceof BoundingBox) {
            BoundingBox bb = (BoundingBox) bounds;

            return bb.getExtent(null).x;
        }

        return 0;
    }

//    public static double[] getTranslation(Matrix4d origin) {
//        Vector3d trans = new Vector3d();
//        origin.get(trans);
//
//        return new double[] { trans.x, trans.y, trans.z };
//    }
//
//    public static float[] getRotation(Matrix4d origin) {
//        // create an angle from the origin
//        AxisAngle4f rot = new AxisAngle4f();
//        rot.set(origin);
//
//        return new float[] { rot.x, rot.y, rot.z, rot.angle };
//    }
} 
