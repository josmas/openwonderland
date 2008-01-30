/**
 * Project Looking Glass
 *
 * $RCSfile: BasicCellMOSetup.java,v $
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

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author jkaplan
 */
public class BasicCellMOSetup<T extends CellSetup>
    implements CellMOSetup, CellLocation 
{
    /** the setup object to send to the cell */
    private T cellSetup;

    /** the class of cell GLO */
    private String cellGLOClassName;

    /* the location of the cell */
    private CellTransform cellTransform;

    /* the bounds of the cell */
    private String boundsType = "SPHERE";
    private float boundsRadius = 4.0f;
    
    public BasicCellMOSetup() {
        this (null, null, null, null);
    }
    
    public BasicCellMOSetup(BoundingVolume bounds, CellTransform transform, 
                             String cellGLOClassName, T cellSetup)
    {
        setCellGLOClassName(cellGLOClassName);
        setCellSetup(cellSetup);
        this.cellTransform = transform;
        
        if (bounds != null) {
            setBoundsType(BasicCellMOHelper.getBoundsType(bounds));
            setBoundsRadius(BasicCellMOHelper.getBoundsRadius(bounds));
        }
    }
    
    public T getCellSetup() {
        return cellSetup;
    }

    public void setCellSetup(T cellSetup) {
        this.cellSetup = cellSetup;
    }

    public String getCellGLOClassName() {
        return cellGLOClassName;
    }

    public void setCellGLOClassName(String cellGLOClassName) {
        this.cellGLOClassName = cellGLOClassName;
    }

    public CellTransform getCellTransform() {
        return cellTransform;
    }
    
    public void setCellTransform(CellTransform cellTransform) {
        this.cellTransform = cellTransform;
    }
    
    public String getBoundsType() {
        return boundsType;
    }

    public void setBoundsType(String boundsType) {
        this.boundsType = boundsType;
    }

    public float getBoundsRadius() {
        return boundsRadius;
    }

    public void setBoundsRadius(float boundsRadius) {
        this.boundsRadius = boundsRadius;
    }

    public void validate() throws InvalidCellMOSetupException {
        // do nothing
    }
    
    public double[] getOrigin() {
        Vector3f v3f = cellTransform.get(null);
        return new double[] { v3f.x, v3f.y, v3f.z };
    }
    
}
