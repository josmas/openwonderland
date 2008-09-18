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
package org.jdesktop.wonderland.server.app.base;

import com.jme.bounding.BoundingVolume;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.app.base.AppCellSetup;
import org.jdesktop.wonderland.common.cell.setup.CellSetup;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.wonderland.darkstar.server.setup.CellGLOSetup;

/**
 * A server-side <code>app.base</code> app cell.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppCellMO extends CellMO { 

    /** Contains all of the properties defined in the cell's WFS wlc file */
    private BasicCellMOSetup<AppCellSetup> beanSetup;
    
    /** Default constructor, used when the cell is created via WFS */
    public AppCellMO() {
	super();
    }
    
    /**
     * Creates a new instance of <code>AppCellMO</code> with the specified localBounds and transform.
     * If either parameter is null an IllegalArgumentException will be thrown.
     *
     * @param localBounds the bounds of the new cell, must not be null.
     * @param transform the transform for this cell, must not be null.
     */
    public AppCellMO (BoundingVolume localBounds, CellTransform transform) {
        super(localBounds, CellTransform transform);
    }
    
    /** 
     * Return the app type of this cell.
     */
    public abstract AppTypeMO getAppType ();

    /**
     * Get the server-to-client setup data for this cell.
     *
     * @return The cell server-to-client setup data
     * TODO: 0.5: the term setup is used for multiple purposes here
     * and it is confusing! There is both JavaBean setup data and 
     * server-to-client setup data!
     */
    @Override
    // TODO: whither
    public CellSetup getSetupData() {
        return beanSetup.getCellSetup();
    }
    
    /**
     * Set up the properties of this cell MO from a JavaBean.  After calling
     * this method, the state of the cell MO should contain all the information
     * represented in the given cell properties file.
     *
     * @param setupData The Java bean to read setup information from
     */
    // TODO: whither
    public void setupCell(CellMOSetup setupData) {
        beanSetup = (BasicCellMOSetup<AppCellSetup>) setupData;
        
	System.err.println("beanSetup = " + beanSetup);
        AxisAngle4d aa = new AxisAngle4d(beanSetup.getRotation());
        Matrix3d rot = new Matrix3d();
        rot.set(aa);
        Vector3f origin = new Vector3f(beanSetup.getOrigin());
        
        Matrix4d o = new Matrix4d(rot, origin, beanSetup.getScale() );
        setOrigin(o);
        
        if (beanSetup.getBoundsType().equals("SPHERE")) {
            setBounds(createBoundingSphere(origin, (float)beanSetup.getBoundsRadius()));
        } else {
            throw new RuntimeException("Unimplemented bounds type");
        }
    }
    
    /**
     * Called when the properties of a cell have changed.
     *
     * @param setupData A Java bean with updated properties
     */
    // TODO: whither
    public void reconfigureCell(CellMOSetup setupData) {
        setupCell(setupData);
    }
    
    /**
     * Write the cell's current state to a JavaBean.
     *
     * @return a JavaBean representing the current state
     */
    // TODO: whither
    public CellMOSetup getCellMOSetup() {
        return new BasicCellMOSetup<AppCellSetup>(getBounds(),
                getOrigin(), getClass().getName(),
                (AppCellSetup)getSetupData());
    }
}
