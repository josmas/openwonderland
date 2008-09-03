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
package org.jdesktop.wonderland.server.setup;

import org.jdesktop.wonderland.common.cell.setup.CellSetup;

/**
 *
 * @author jkaplan
 */
public class BasicCellMOSetup<T extends CellSetup>
    implements CellMOSetup, CellLocation 
{    
    /** the setup object to send to the cell */
    private T cellSetup;

    /** the class of cell MO */
    private String cellMOClassName;
    
   /* the location of the cell */
    private double[] origin   = new double[] { 0.0, 0.0, 0.0 };
    private double[] rotation = new double[] { 0.0, 1.0, 0.0, 0.0 };
    private double   scale    = 1.0;

    /* the bounds of the cell */
    private BoundsType boundsType   = BoundsType.SPHERE;
    private double     boundsRadius = 4.0f;
    
    /* The cell priority, this should have a default value XXX */
    private short priority;
    
    /** Default constructor */
    public BasicCellMOSetup() {
    }
    
    public T getCellSetup() {
        return cellSetup;
    }

    public void setCellSetup(T cellSetup) {
        this.cellSetup = cellSetup;
    }

    public String getCellMOClassName() {
        return cellMOClassName;
    }

    public void setCellMOClassName(String cellMOClassName) {
        this.cellMOClassName = cellMOClassName;
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
    
    public BoundsType getBoundsType() {
        return boundsType;
    }

    public void setBoundsType(BoundsType boundsType) {
        this.boundsType = boundsType;
    }
    
    public void setBoundsType(String boundsType) {
        if (boundsType.equals("SPHERE"))
            this.boundsType = BoundsType.SPHERE;
        else if (boundsType.equals("BOX"))
            this.boundsType = BoundsType.BOX;
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
    
    public short getPriority() {
        return priority;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }
    
}
