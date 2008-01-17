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
import com.sun.sgs.app.ManagedReference;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.jdesktop.j3d.utils.math.Math3D;
import org.jdesktop.wonderland.common.cell.MultipleParentException;

/**
 * A cell which provides Oct tree structure to help provide good
 * localisation of cells in tree. The cell type is not expected to have
 * any geometry associated with it.
 * 
 * The transform for this cell must contain translation only, no scale or rotation.
 *
 * @author paulby
 */
public class OctTreeCellMO extends GroupCellMO implements CellContainerInterface {

    public enum Octant { 
        UPPER_NE, UPPER_NW ,UPPER_SE, UPPER_SW, 
        LOWER_NE, LOWER_NW, LOWER_SE, LOWER_SW };

    public OctTreeCellMO(BoundingBox bounds, Matrix4d cellOrigin) {
        super(bounds, cellOrigin);

        for(Octant oct : Octant.values()) {
            try {
                addChild(createOctant(oct));
            } catch (MultipleParentException ex) {
                Logger.getLogger(OctTreeCellMO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private GroupCellMO createOctant( Octant octant ) {
        Vector3d octantCenter=null;
        
        Point3d upper = new Point3d();
        Point3d lower = new Point3d();
        ((BoundingBox)getLocalBounds()).getUpper(upper);
        ((BoundingBox)getLocalBounds()).getLower(lower);

        double xDim = upper.x - lower.x;
        double yDim = upper.y - lower.y;
        double zDim = upper.z - lower.z;

        Point3d center = new Point3d(lower.x+xDim/2,
                                        lower.y+yDim/2,
                                        lower.z+zDim/2);
        
        double xDim4 = xDim/4;
        double yDim4 = yDim/4;
        double zDim4 = zDim/4;
        
        switch(octant) {
            case UPPER_NE :
                octantCenter = new Vector3d(center.x+xDim4, center.y+yDim4, center.z-zDim4);
            break;
            case UPPER_NW :
                octantCenter = new Vector3d(center.x-xDim4, center.y+yDim4, center.z-zDim4);
            break;
            case UPPER_SE :
                octantCenter = new Vector3d(center.x+xDim4, center.y+yDim4, center.z+zDim4);
            break;
            case UPPER_SW :
                octantCenter = new Vector3d(center.x-xDim4, center.y+yDim4, center.z+zDim4);
            break;
            case LOWER_NE :
                octantCenter = new Vector3d(center.x+xDim4, center.y-yDim4, center.z-zDim4);
            break;
            case LOWER_NW :
                octantCenter = new Vector3d(center.x-xDim4, center.y-yDim4, center.z-zDim4);
            break;
            case LOWER_SE :
                octantCenter = new Vector3d(center.x+xDim4, center.y-yDim4, center.z+zDim4);
            break;
            case LOWER_SW :
                octantCenter = new Vector3d(center.x-xDim4, center.y-yDim4, center.z+zDim4);
            break;
        }
        
        BoundingBox octantBounds = new BoundingBox(new Point3d(-xDim4, -yDim4, -zDim4), 
                                                   new Point3d(xDim4, yDim4, zDim4));
        Matrix4d m4d = new Matrix4d();
        m4d.setIdentity();
        m4d.setTranslation(octantCenter);
        
        GroupCellMO ret;
        
        if (xDim4<1000)
            ret = new GroupCellMO(octantBounds, m4d);
        else
            ret = new OctTreeCellMO(octantBounds, m4d);
        
        ret.setName(octant.toString()+":"+xDim4);
        
        return ret;
    }


//    @Override
//    public void addChildCell(ManagedReference childRef) {
//        super.addChildCell(childRef);
//        System.out.println("Child count "+childCells.size());
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellMO insertCellInHierarchy(CellMO insertChild, Bounds childVWBounds)  throws MultipleParentException {
        CellMO parent = null;

        System.out.println("OctTree bounds "+getCachedVWBounds());
        System.out.println("Child bounds "+childVWBounds);
        
        if (!Math3D.encloses(getCachedVWBounds(), childVWBounds)) {
            System.out.println("child not enclosed by our bounds");
            return null;
        }   

        ManagedReference ref;
        
        Iterator<ManagedReference> it = getAllChildrenRefs().iterator();
        
        while(it.hasNext() && parent==null) {
            ref = it.next();
            if (ref!=null) {
                CellMO tmp = ref.get(CellMO.class);
                if (Math3D.encloses(tmp.getCachedVWBounds(), childVWBounds)) {
                    if (tmp instanceof CellContainerInterface) {
                        parent = ((CellContainerInterface)tmp).insertCellInHierarchy(insertChild, childVWBounds);
                    }

                    if (parent==null) {
                        try {
                            AppContext.getDataManager().markForUpdate(tmp);
                            Vector3d parentLoc = new Vector3d();
                            tmp.getTransform().get(parentLoc);
                            
                            // Adjust childs transform
                            Vector3d childLoc = new Vector3d();
                            Matrix4d childTransform = insertChild.getTransform();
                            childTransform.get(childLoc);
                            
                            childLoc.sub(parentLoc,childLoc);                            
                            childTransform.set(childLoc);
                            insertChild.setTransform(childTransform);
                            
                            tmp.addChild(insertChild);
                            parent = tmp;
                            System.out.println(tmp.getName()+" "+tmp.getCachedVWBounds() + " fully encloses " + childVWBounds);
                            //System.out.println("Addchild child to octant "+Octant.values()[i] +"  "+((SimpleTerrainCellGLO)insertChild).getCellName());
                        } catch (MultipleParentException ex) {
                            Logger.getLogger(OctTreeCellMO.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        //System.out.println("Addchild child to octant "+Octant.values()[i] +"  "+((SimpleTerrainCellGLO)insertChild).getCellName());
                    }
                }
            }
        }
        
        return parent;
    }
}
