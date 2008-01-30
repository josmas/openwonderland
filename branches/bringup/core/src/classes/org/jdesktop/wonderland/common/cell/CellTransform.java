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

import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.Serializable;
import org.jdesktop.wonderland.ExperimentalAPI;

/**
 * The transform for a cell.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class CellTransform implements Serializable {

    private Quaternion rotate;
    private Vector3f translation;
    private static final Vector3f scale = new Vector3f(1,1,1);
    
    /**
     * Create a cell transform. Either (or both) values may be null
     * 
     * @param quat
     * @param translation
     */
    public CellTransform(Quaternion rotate, Vector3f translation) {
        this.rotate = rotate;
        this.translation = translation;
        
        if (this.rotate==null)
            this.rotate = new Quaternion();
        if (this.translation==null)
            this.translation = new Vector3f();
    }

    private CellTransform(CellTransform orig) {
        this.rotate = orig.rotate;
        this.translation = orig.translation;
    }
    
    @Override
    public Object clone() {
        return new CellTransform(this);
    }

    /**
     * Transform the BoundingVolume 
     * @param ret
     */
    public void transform(BoundingVolume ret) {
        ret.transform(rotate, translation, scale);
    }
    
    /**
     * Transform the vector ret by this transform. ret is modified and returned.
     * @param ret
     */
    public Vector3f transform(Vector3f ret) {
        ret.multLocal(translation);
        
        return ret;
    }
    
    /**
     * Multiply this transform by t1. This transform will be update
     * and the result returned
     * 
     * @param transform
     * @return this
     */
    public CellTransform mul(CellTransform t1) {
        rotate.mult(t1.rotate);
        translation.mult(t1.translation);
        return this;
    }
    
    /**
     * Populates translation with the translation of this CellTransform, if translation
     * is null, a new Vector3f will be created and returned
     * 
     * @param translation
     * @return
     */
    public Vector3f get(Vector3f translation) {
        if (translation==null)
            return new Vector3f(translation);
        
        translation.set(this.translation);
        return translation;
    }
    
    /**
     * Set the translation.
     * @param translation
     */
    public void set(Vector3f translation) {
        this.translation = translation;
        if (this.translation==null)
            this.translation = new Vector3f();
    }
}
