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
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.MultipleParentException;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public abstract class CellManager implements ManagedObject, Serializable {

    // Used to generate unique cell ids
    private long cellCounter=0;
    
    private static final String BINDING_NAME=CellManager.class.getName();
    
    /**
     * Creates a new instance of CellManager
     */
    CellManager() {
        AppContext.getDataManager().setBinding(BINDING_NAME, this);
    }
    
    public static CellManager initialize() {
        return new SimpleCellManager();
    }
    
    /**
     * Return singleton cell manager
     * @return
     */
    public static CellManager getCellManager() {
        return AppContext.getDataManager().getBinding(BINDING_NAME, CellManager.class);                
    }
    
    /**
     * Add the cell to the active world, in the process making the cell active
     * @param cell
     */
    public abstract void addCell(CellMO cell) throws MultipleParentException;

    /**
     * Return the cell with the given ID, or null if the id is invalid
     * 
     * @param cellID
     * @return
     */
    public static CellMO getCell(CellID cellID) {
        return AppContext.getDataManager().getBinding("CELL_"+cellID.toString(), CellMO.class);        
    }
    
    /**
     *  Traverse all trees and return the set of cells which are within
     * the specified bounds and are of the give Class 
     * 
     * @param b
     * @param cellClasses
     * @return
     */
    public CellDescription[] getCells(Bounds b, Class[] cellClasses) {
        return new CellDescription[0];
    }

    public void loadWorld() {
        try {
            Matrix4d m4 = new Matrix4d();
            m4.setIdentity();
            BoundingBox bounds = new BoundingBox();

            CellMO c1 = new CellMO();
            m4.setTranslation(new Vector3d(1, 1, 1));
            c1.setTransform(m4);
            c1.setName("c1");
            c1.setLocalBounds(bounds);

            CellMO c2 = new CellMO();
            m4.setTranslation(new Vector3d(10, 10, 10));
            c2.setTransform(m4);
            c2.setName("c2");
            c2.setLocalBounds(bounds);

            c1.addChild(c2);
            addCell(c1);
        } catch (MultipleParentException ex) {
            Logger.getLogger(CellManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     *  Cell has moved, revalidate the user cell caches for those
     * users that are close
     * @param cell
     */
//    void revalidate(CellMO cell) {
//        throw new RuntimeException("Not Implemented");                
//    }
    
    /**
     * Returns a unique cell id and registers the cell with the system
     * @return
     */
    CellID createCellID(CellMO cell) {
        CellID cellID = new CellID(cellCounter++);
        
        AppContext.getDataManager().setBinding("CELL_"+cellID.toString(), cell);
        
        return cellID;
    }
    
    /**
     * Called by a cell when it's localBounds are changed
     * @param cell
     */
    abstract void cellLocalBoundsChanged(CellMO cell);
    
    /**
     * Called by a cell when it's transform is changed
     * @param cell
     */
    abstract void cellTransformChanged(CellMO cell);
    
    /**
     * Called when a child is added to a parent cell
     * @param parent
     * @param childAdded true when a child is added, false when child is removed
     */
    abstract void cellChildrenChanged(CellMO parent, CellMO child, boolean childAdded);

}
