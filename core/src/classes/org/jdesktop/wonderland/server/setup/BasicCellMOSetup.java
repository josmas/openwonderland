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

import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import org.jdesktop.wonderland.common.cell.CellSetup;

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
    private double[] origin = new double[] { 0.0, 0.0, 0.0 };
    private double[] rotation = new double[] { 0.0, 1.0, 0.0, 0.0 };
    private double scale = 1.0;

    /* the bounds of the cell */
    private String boundsType = "SPHERE";
    private double boundsRadius = 4.0;
    
    public BasicCellMOSetup() {
        this (null, null, null, null);
    }
    
    public BasicCellMOSetup(Bounds bounds, Matrix4d origin, 
                             String cellGLOClassName, T cellSetup)
    {
        setCellGLOClassName(cellGLOClassName);
        setCellSetup(cellSetup);
        
        if (bounds != null) {
            setBoundsType(BasicCellMOHelper.getBoundsType(bounds));
            setBoundsRadius(BasicCellMOHelper.getBoundsRadius(bounds));
        }
        if (origin != null) {
            setOrigin(BasicCellMOHelper.getTranslation(origin));
            setRotation(BasicCellMOHelper.getRotation(origin));
            setScale(origin.getScale());
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

    public double[] getOrigin() {
        return origin;
    }
    
    public void setOrigin(double[] origin) {
        this.origin = origin;
    }
    public double[] getRotation() {
        return rotation;
    }

    public void setRotation(double[] rotation) {
        this.rotation = rotation;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public String getBoundsType() {
        return boundsType;
    }

    public void setBoundsType(String boundsType) {
        this.boundsType = boundsType;
    }

    public double getBoundsRadius() {
        return boundsRadius;
    }

    public void setBoundsRadius(double boundsRadius) {
        this.boundsRadius = boundsRadius;
    }

    public void validate() throws InvalidCellMOSetupException {
        // do nothing
    }
}
