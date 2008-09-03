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
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.setup.CellLocation.BoundsType;

/**
 *
 * @author jkaplan
 */
public class BasicCellMOHelper {
   
    public static BoundingVolume getCellBounds(BasicCellMOSetup setup) {
        if (setup.getBoundsType() == null) {
            return null;
        } else if (setup.getBoundsType().equals(BoundsType.SPHERE)) {
            return new BoundingSphere((float)setup.getBoundsRadius(), new Vector3f() );
        } else if (setup.getBoundsType().equals(BoundsType.BOX)) {
//            return Math3DUtils.createBoundingBox(new Vector3d(setup.getOrigin()),
//                                             (float) setup.getBoundsRadius());
            return new BoundingBox(new Vector3f(),
                    (float)setup.getBoundsRadius(),
                    (float)setup.getBoundsRadius(),
                    (float)setup.getBoundsRadius());
        }
        
        return null;
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

       /**
     * Creates and returns a new CellTransform object that representing the
     * translation (to origin), rotation, and scaling of a cell.
     * 
     * @param setup The cell's setup parameters
     * @return A CellTranform class representing the origin, rotation, scaling
     */
    public static CellTransform getCellTransform(BasicCellMOSetup setup) {
        /* Fetch the raw values from the setup class */
        double[] origin   = setup.getOrigin();
        double[] rotation = setup.getRotation();
        double   scale    = setup.getScale();
        
        /* Create a Quaternion object for the rotation */
        float      angle = (float)rotation[3];
        Vector3f   axis  = new Vector3f((float)rotation[0], (float)rotation[1], (float)rotation[2]);
        Quaternion quat  = new Quaternion().fromAngleAxis(angle, axis);
        
        /* Create a Vector3f object for the origin and scale */
        Vector3f origin3f = new Vector3f((float)origin[0], (float)origin[1], (float)origin[2]);
        Vector3f scale3f  = new Vector3f((float)scale, (float)scale, (float)scale);
        
        /* Create an return a new CellTransform class */
        return new CellTransform(quat, origin3f, scale3f);
    }

    /**
     * Returns an array of doubles that represent the translation to the origin,
     * given a (non-null) CellTransform class.
     * 
     * @param transform The cell's transform
     * @return An array of doubles representing the x,y,z of the origin
     */
    public static double[] getTranslation(CellTransform transform) {
        Vector3f trans = transform.getTranslation(null);
        return new double[] {
                    (double) trans.getX(),
                    (double) trans.getY(),
                    (double) trans.getZ()
                };
    }
    
    /**
     * Returns an array of doubles that represents the rotation of the cell
     * about an axis of rotation (x, y, z) and an angle, given a (non-null)
     * CellTransform class.
     * 
     * @param transform The cell's transform
     * @return An array of doubles representing the <x,y,z,angle> of rotation.
     */
    public static double[] getRotation(CellTransform transform) {
        Quaternion quat  = transform.getRotation(null);
        Vector3f   axis  = new Vector3f();
        double     angle = (double)quat.toAngleAxis(axis);
        
        return new double[] {
            (double)axis.getX(),
            (double)axis.getY(),
            (double)axis.getZ(),
            angle
        };
    }

    /**
     * Returns a double that represents the scaling of the cell for
     * each cartesian (x, y, z) axis, given a (non-null) CellTransform class.
     * 
     * @param transform The cell's transform
     * @return A double representing the <x, y, z> scaling.
     */
    public static double getScaling(CellTransform transform) {
        Vector3f scale = transform.getScaling(null);
        
        /*
         * Currently, WFS assumes there is only one scaling factor that applies
         * to each of the axes. Simple take the scaling factor for the x-axis
         * and assume it is the scaling factor for all axes.
         */
        return (double)scale.getX();
    }
} 
