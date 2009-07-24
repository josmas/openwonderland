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
package org.jdesktop.wonderland.common.cell;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The transform for a cell.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class CellTransform implements Serializable {

    private Quaternion rotation;
    private Vector3f translation;
    private float scale = 1f;
    private transient Matrix4f matrix = null;
    
    /**
     * Create an identity transform
     */
    public CellTransform() {
        this(null, null, 1f);
    }

     /**
     * Constructor that takes translation, rotation, and scaling. Any/all of
     * the three arguments may be null.
     */
    public CellTransform(Quaternion rotate, Vector3f translate, float scale) {
        if (rotate==null)
            this.rotation = new Quaternion();
        else
            this.rotation = rotate.clone();

        if (translate==null)
            this.translation = new Vector3f();
        else
            this.translation = translate.clone();

        this.scale = scale;
    }

    /**
     * @deprecated Non uniform scale are not supported
     * @param rotate
     * @param translate
     * @param scale
     */
    public CellTransform(Quaternion rotate, Vector3f translate, Vector3f scale) {
        if (rotate==null)
            this.rotation = new Quaternion();
        else
            this.rotation = rotate.clone();

        if (translate==null)
            this.translation = new Vector3f();
        else
            this.translation = translate.clone();

        if (scale == null) {
            this.scale = 0.0f;
        }
        else {
            this.scale = scale.x;
            Logger.getLogger(CellTransform.class.getName()).warning("Non uniform scale is not supported, please use another CellTransform constructor");
            Thread.dumpStack();
        }
    }
    
    /**
     * Create a cell transform. Either (or both) values may be null
     * 
     * @param quat
     * @param translation
     */
    public CellTransform(Quaternion rotate, Vector3f translation) {
        this(rotate, translation, 1f);

    }

    private CellTransform(CellTransform orig) {
        this.rotation = new Quaternion(orig.rotation);
        this.translation = new Vector3f(orig.translation);
        this.scale = orig.scale;
    }
    
    public CellTransform clone(CellTransform result) {
        if (result==null)
            return new CellTransform(this);
        
        result.set(this);
        return result;
    }
    
    /**
     * set this object to have the same state as source
     * @param source
     */
    private void set(CellTransform source) {
        this.rotation.set(source.rotation);       
        this.scale = source.scale;
        this.translation.set(source.translation);
    }

    /**
     * Transform the BoundingVolume 
     * @param ret
     */
    public void transform(BoundingVolume ret) {
        assert(ret!=null);
        ret.transform(rotation,translation, new Vector3f(scale, scale, scale), ret);
    }
    
    /**
     * Transform the vector ret by this transform. ret is modified and returned.
     * @param ret
     */
    public Vector3f transform(Vector3f ret) {
        ret.multLocal(scale);
        rotation.multLocal(ret);
        ret.addLocal(translation);

        return ret;
    }

    /**
     * Return the position and current look direction
     * @param position
     * @param look
     */
    public void getLookAt(Vector3f position, Vector3f look) {
        position.set(0,0,0);
        position.addLocal(translation);

        look.set(0,0,1);
        rotation.multLocal(look);
        look.normalizeLocal();
    }

    /**
     * Multiply this transform by t1. This transform will be update
     * and the result returned
     * 
     * @param transform
     * @return this
     */
    public CellTransform mul(CellTransform t1) {
        updateMatrix();
        t1.updateMatrix();

        matrix.multLocal(t1.matrix);

        updateFromMatrix();

        // As we force uniform scales we can avoid the SVD and track scale ourselves
        scale = scale*t1.scale;
        return this;
    }

    private void updateMatrix() {
        if (matrix==null) {
            matrix = new Matrix4f();
        } else {
            matrix.loadIdentity();
        }
        matrix.multLocal(scale);
        matrix.multLocal(rotation);
        matrix.setTranslation(translation);
    }

    private void updateFromMatrix() {
        matrix.toRotationQuat(rotation);
        matrix.toTranslationVector(translation);
    }
    
    /**
     * Subtract t1 from this transform, modifying this transform and
     * returning this transform.
     * 
     * @param t1
     * @return
     */
    public CellTransform sub(CellTransform t1) {
        rotation.subtract(t1.rotation);
        scale -= t1.scale;
        translation.subtract(t1.translation);
        return this;
    }
    
    /**
     * Populates translation with the translation of this CellTransform, if translation
     * is null, a new Vector3f will be created and returned
     * 
     * @param translation object to return (to avoid gc)
     * @return the translation for this transform
     */
    public Vector3f getTranslation(Vector3f translation) {
        if (translation==null)
            return new Vector3f(this.translation);
        
        translation.set(this.translation);
        return translation;
    }
    
    /**
     * Set the translation.
     * @param translation set the translation for this transform
     */
    public void setTranslation(Vector3f translation) {
        this.translation = translation;
        if (this.translation==null)
            this.translation = new Vector3f();
    }

    /**
     * Get the rotation portion of this transform. Populates the rotation 
     * paramter with the current rotation and returns it, if rotation is null
     * a new Quaternion is returned.
     * 
     * @param rotation object to return (to avoid gc)
     * @return the rotation quaternion for this transform
     */
    public Quaternion getRotation(Quaternion rotation) {
        if (rotation==null)
            rotation = new Quaternion(this.rotation);
        else
            rotation.set(this.rotation);
        
        return rotation;
    }

    /**
     * Set the rotation portion of this transform
     * @param rotation set the rotation for this transform
     */
    public void setRotation(Quaternion rotation) {
        this.rotation = new Quaternion(rotation);
    }
    
    /**
     * Returns the scaling vector as an array of doubles to scale each axis.
     * Sets the value of the scale into the argument (if given). If a null
     * argument is passed, then this method creates and returns a new Vector3f
     * object. The scale is uniform, this is a convenience function as JME
     * expects a vector
     * 
     * @param scale Populate this object with the scale if non-null
     * @return The scaling factors
     */
    public Vector3f getScaling(Vector3f scale) {
        if (scale == null) {
            scale = new Vector3f(this.scale, this.scale, this.scale);
        }
        else {
            scale.set(this.scale, this.scale, this.scale);
        }
        return scale;
    }

    /**
     * Return the uniform scale of this transform.
     * @return
     */
    public float getScaling() {
        return scale;
    }
    
    /**
     * Sets the scaling factor for this cell transform
     * 
     * @param scale The new scaling factor
     */
    public void setScaling(float scale) {
        this.scale = scale;
    }

    /**
     * @deprecated Non uniform scale is not supported
     * @param scale
     */
    public void setScaling(Vector3f scale) {
        Logger.getLogger(CellTransform.class.getName()).warning("Non uniform scale is not supported, please use the other setScaling method");
        Thread.dumpStack();
        setScaling(scale.x);
    }

    /**
     * Invert the transform
     */
    public void invert() {
        updateMatrix();
        matrix.invertLocal();
        updateFromMatrix();
        scale = 1/scale;
    }
    
    @Override
    public boolean equals(Object o) {
        boolean ret = true;
        
        if (o instanceof CellTransform) {
            CellTransform e = (CellTransform)o;
            
            if (e.rotation!=null && !e.rotation.equals(rotation))
                ret = false;
            else if (e.rotation==null && rotation!=null)
                ret = false;
            
            if (e.translation!=null && !e.translation.equals(translation))
                ret = false;
            else if (e.translation==null && translation!=null)
                ret = false;
            
            if (e.scale!=scale)         // TODO should use EpsilonEquals
                ret = false;
        } else {
            ret = false;
        }
        
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (this.rotation != null ? this.rotation.hashCode() : 0);
        hash = 13 * hash + (this.translation != null ? this.translation.hashCode() : 0);
        hash = 13 * hash + (int)(scale*1000);
        return hash;
    }

    @Override
    public String toString() {
        return "[tx="+translation.x+" ty="+translation.y+" tz="+translation.z +
                "] [rx="+rotation.x+" ry="+rotation.y+" rz="+rotation.z+" rw="+rotation.w +
                "] [s=" + scale+"]";

    }

//    public static void main(String[] args) {
//        ArrayList<CellTransform> graphLocal = new ArrayList();
//
//        graphLocal.add(create(new Vector3f(0,1,0), 180, new Vector3f(2,0,0)));
//        graphLocal.add(create(new Vector3f(0,1,0), 0, new Vector3f(0,0,3)));
//
//        computeGraph(graphLocal);
//    }
//
//    public static CellTransform computeGraph(ArrayList<CellTransform> graphLocal) {
//        CellTransform result = new CellTransform();
//        for(int i=0; i<graphLocal.size(); i++) {
//            result.mul(graphLocal.get(i));
//        }
//        return result;
//    }
//
//    private static CellTransform create(Vector3f axis, float angle, Vector3f translation) {
//        Quaternion quat = new Quaternion();
//        quat.fromAngleAxis((float) Math.toRadians(angle), axis);
//        return new CellTransform(quat, translation);
//    }
}
