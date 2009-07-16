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
package org.jdesktop.wonderland.common.cell.state;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A special cell component server state object that represents the cell
 * transform (origin, rotation, scaling) and bounds. There is no corresponding
 * server or client-side component object. This state is handled as a special
 * case by the cell.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="position-component")
public class PositionComponentServerState extends CellComponentServerState {

    /* The (x, y, z) translation of the cell */
    @XmlElement(name="translation")
    public Translation translation = new Translation();

    /* The cell bounds */
    @XmlElement(name="bounds")
    public Bounds bounds = new Bounds();

    /* The (x, y, z) components of the scaling */
    @XmlElement(name="scale")
    public Scale scaling = new Scale();

    /* The rotation about an (x, y, z) axis and angle (radians) */
    @XmlElement(name="rotation")
    public Rotation rotation = new Rotation();
        /**
     * The Translation static inner class simply stores (x, y, z) cell origin.
     */
    public static class Translation implements Serializable {
        /* The (x, y, z) origin components */
        @XmlElement(name="x") public double x = 0;
        @XmlElement(name="y") public double y = 0;
        @XmlElement(name="z") public double z = 0;

        /** Default constructor */
        public Translation() {
        }

        public Translation(Vector3f origin) {
            this.x = origin.x;
            this.y = origin.y;
            this.z = origin.z;
        }

        public Vector3f asVector3f() {
            return new Vector3f((float)x,(float)y,(float)z);
        }
    }

    /**
     * The Bounds static inner class stores the bounds type and bounds radius.
     */
    public static class Bounds implements Serializable {
        public enum BoundsType { SPHERE, BOX };

        /* The bounds type, either SPHERE or BOX */
        @XmlElement(name="type") public BoundsType type = BoundsType.SPHERE;

        /* The x dimension or radius of the bounds */
        @XmlElement(name="x") public double x = 1.0;
        @XmlElement(name="y") public double y = 1.0;
        @XmlElement(name="z") public double z = 1.0;

        /** Default constructor */
        public Bounds() {
        }

        public Bounds(BoundingVolume bv) {
            if (bv instanceof BoundingBox) {
                type = BoundsType.BOX;
                x = ((BoundingBox)bv).xExtent;
                y = ((BoundingBox)bv).yExtent;
                z = ((BoundingBox)bv).zExtent;
            } else if (bv instanceof BoundingSphere) {
                type = BoundsType.SPHERE;
                x = ((BoundingSphere)bv).radius;
            }
        }
    }

    /**
     * The Scale static inner class stores the scaling for each of the
     * (x, y, z) components
     */
    public static class Scale implements Serializable {
        /* The (x, y, z) scaling components */
        @XmlElement(name="x") public double x = 1;
        @XmlElement(name="y") public double y = 1;
        @XmlElement(name="z") public double z = 1;

        /** Default constructor */
        public Scale() {
        }

        public Scale(Vector3f scaling) {
            x = scaling.x;
            y = scaling.y;
            z = scaling.z;
        }

        public Vector3f asVector3f() {
            return new Vector3f((float)x,(float)y,(float)z);
        }
    }

    /**
     * The Rotation static inner class stores a rotation about an (x, y, z)
     * axis over an angle.
     */
    public static class Rotation implements Serializable {
        /* The (x, y, z) rotation axis components */
        @XmlElement(name="x") public double x = 0;
        @XmlElement(name="y") public double y = 0;
        @XmlElement(name="z") public double z = 0;

        /* The angle (radians) about which to rotate */
        @XmlElement(name="angle") public double angle = 0;

        /** Default constructor */
        public Rotation() {
        }

        public Rotation(Vector3f axis, double angleRadians) {
            x = axis.x;
            y = axis.y;
            z = axis.z;
            angle = angleRadians;
        }

        public Rotation(Quaternion quat) {
            Vector3f axis = new Vector3f();
            float angleRadians = quat.toAngleAxis(axis);
            x = axis.x;
            y = axis.y;
            z = axis.z;
            angle = angleRadians;

        }

        public Quaternion asQuaternion() {
            Quaternion ret = new Quaternion();
            ret.fromAngleAxis((float)angle, new Vector3f((float)x,(float)y,(float)z));
            return ret;
        }
    }

    @Override
    public String getServerComponentClassName() {
        return null;
    }

    /**
     * Returns the cell translation.
     *
     * @return The cell translation
     */
    @XmlTransient public Translation getTranslation() {
        return this.translation;
    }

    /**
     * Sets the cell translation. If null, then this property will not be written
     * out to the file.
     *
     * @param translation The new cell translation
     */
    public void setTranslation(Translation translation) {
        this.translation = translation;
    }

    /**
     * Returns the cell bounds.
     *
     * @return The cell bounds
     */
    @XmlTransient public Bounds getBounds() {
        return this.bounds;
    }

    /**
     * Sets the cell bounds. If null, then this property will not be written
     * out to the file.
     *
     * @param bounds The new cell bounds
     */
    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public void setBounds(BoundingVolume boundingVolume) {
        this.bounds = new Bounds(boundingVolume);
    }

    /**
     * Returns the cell scaling.
     *
     * @return The cell scaing
     */
    @XmlTransient public Scale getScaling() {
        return this.scaling;
    }

    /**
     * Sets the cell scaling. If null, then this property will not be written
     * out to the file.
     *
     * @param scaling The new cell scaling
     */
    public void setScaling(Scale scaling) {
        this.scaling = scaling;
    }

    /**
     * Returns the cell rotation.
     *
     * @return The cell rotation
     */
    @XmlTransient public Rotation getRotation() {
        return this.rotation;
    }

    /**
     * Sets the cell rotation. If null, then this property will not be written
     * out to the file.
     *
     * @param rotation The new cell rotation
     */
    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    @Override
    public String toString() {
        return "[BasicCellSetup] origin=(" + this.translation.x + "," + this.translation.y +
                "," + this.translation.z + ") rotation=(" + this.rotation.x + "," +
                this.rotation.y + "," + this.rotation.z + ") @ " + this.rotation.angle +
                " scaling=(" + this.scaling.x + "," + this.scaling.y + "," +
                this.scaling.z + ") bounds=" + this.bounds.type + "@" +
                this.bounds.x+", "+this.bounds.y+" "+this.bounds.z;
    }
}
