/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.server.state;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState.Bounds.BoundsType;
import org.jdesktop.wonderland.common.cell.state.CellServerState.Origin;
import org.jdesktop.wonderland.common.cell.state.CellServerState.Rotation;
import org.jdesktop.wonderland.common.cell.state.CellServerState.Scaling;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * The BasicCellServerStateHelper class implements a collection of utility routines
 * that help convert between JMonkeyEngine (JME) types for the cell bounds and
 * tranform and the representations of these quantities using basic Java types
 * in the CellServerState class.
 * 
 * @author jkaplan
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class BasicCellServerStateHelper {
   
    /**
     * Returns the bounds of a cell as a BoundingVolume object, given the cell's
     * setup information.
     * 
     * @param setup The cell's setup information
     * @return The bounds as a JME BoundingVolume object
     */
    public static BoundingVolume getCellBounds(CellServerState setup) {
        BoundsType type = setup.getBounds().type;
        float radius = (float)setup.getBounds().radius;
        
        if (type.equals(BoundsType.SPHERE) == true) {
            return new BoundingSphere(radius, new Vector3f());
        }
        else if (type.equals(BoundsType.BOX) == true) {
            return new BoundingBox(new Vector3f(), radius, radius, radius);
        }
        
        /* This should never happen, but in case it does... */
        throw new RuntimeException("Unsupported bounds type " + type);
    }

    /**
     * Creates and returns a new CellTransform object that representing the
     * translation (to origin), rotation, and scaling of a cell.
     * 
     * @param setup The cell's setup parameters
     * @return A CellTranform class representing the origin, rotation, scaling
     */
    public static CellTransform getCellTransform(CellServerState setup) {
        /* Fetch the raw values from the setup class */
        Origin origin = setup.getOrigin();
        Rotation rotation = setup.getRotation();
        Scaling scaling = setup.getScaling();
        
        /* Create a Quaternion object for the rotation */
        Vector3f axis  = new Vector3f((float)rotation.x, (float)rotation.y, (float)rotation.z);
        Quaternion quat = new Quaternion().fromAngleAxis((float)rotation.angle, axis);
        
        /* Create a Vector3f object for the origin and scale */
        Vector3f origin3f = new Vector3f((float)origin.x, (float)origin.y, (float)origin.z);
        Vector3f scale3f = new Vector3f((float)scaling.x, (float)scaling.y, (float)scaling.z);
        
        /* Create an return a new CellTransform class */
        return new CellTransform(quat, origin3f, scale3f);
    }
    
    /**
     * Given the JME BoundingVolume object, returns the bounds used in the cell
     * setup information.
     * 
     * @param bounds The JME bounds object
     * @return The CellServerState.Bounds object
     */
    public static CellServerState.Bounds getSetupBounds(BoundingVolume bounds) {
        CellServerState.Bounds cellBounds = new CellServerState.Bounds();
        if (bounds instanceof BoundingSphere) {
            cellBounds.type = BoundsType.SPHERE;
            cellBounds.radius = ((BoundingSphere)bounds).getRadius();
            return cellBounds;
        }
        else if (bounds instanceof BoundingBox) {
            cellBounds.type = BoundsType.BOX;
            cellBounds.radius = ((BoundingBox)bounds).getExtent(null).x;
            return cellBounds;
        }
        
        /* This should never happen, but in case it does... */
        throw new RuntimeException("Unsupported bounds type " + bounds.getClass().getName());
    }

    /**
     * Given a (non-null) CellTranform class, returns the Origin class that
     * is used in the cell setup information.
     * 
     * @param transform The cell's transform
     * @return The origin used in the cell setup information
     */
    public static CellServerState.Origin getSetupOrigin(CellTransform transform) {
        Vector3f trans = transform.getTranslation(null);
        CellServerState.Origin origin = new CellServerState.Origin();
        origin.x = (double)trans.getX();
        origin.y = (double)trans.getY();
        origin.z = (double)trans.getZ();
        return origin;
    }
    
    /**
     * Given a (non-null) CellTranform class, returns the Rotation class that
     * is used in the cell setup information.
     * 
     * @param transform The cell's transform
     * @return The rotation used in the cell setup information
     */
    public static CellServerState.Rotation getSetupRotation(CellTransform transform) {
        Quaternion quat = transform.getRotation(null);
        Vector3f axis  = new Vector3f();
        double angle = (double)quat.toAngleAxis(axis);
        
        CellServerState.Rotation rotation = new CellServerState.Rotation();
        rotation.x = (double)axis.getX();
        rotation.y = (double)axis.getY();
        rotation.z = (double)axis.getZ();
        rotation.angle = angle;
        return rotation;
    }
    
    /**
     * Given a (non-null) CellTranform class, returns the Scaling class that
     * is used in the cell setup information.
     * 
     * @param transform The cell's transform
     * @return The scaling used in the cell setup information
     */
    public static CellServerState.Scaling getSetupScaling(CellTransform transform) {
        Vector3f scale = transform.getScaling(null);
        
        CellServerState.Scaling scaling = new CellServerState.Scaling();
        scaling.x = (double)scale.getX();
        scaling.y = (double)scale.getY();
        scaling.z = (double)scale.getZ();
        return scaling;
    }
} 
