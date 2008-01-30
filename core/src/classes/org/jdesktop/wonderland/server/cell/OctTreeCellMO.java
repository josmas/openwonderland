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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.PrivateAPI;
import org.jdesktop.wonderland.common.Math3DUtils;
import org.jdesktop.wonderland.common.cell.CellTransform;
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
@PrivateAPI
public class OctTreeCellMO extends GroupCellMO implements CellContainerInterface {

    public enum Octant { 
        UPPER_NE, UPPER_NW ,UPPER_SE, UPPER_SW, 
        LOWER_NE, LOWER_NW, LOWER_SE, LOWER_SW };

    public OctTreeCellMO(BoundingBox bounds, CellTransform cellOrigin) {
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
        Vector3f octantCenter=null;
        
        Vector3f extents = ((BoundingBox)getLocalBounds()).getExtent(null);
        
        float xDim = extents.x;
        float yDim = extents.y;
        float zDim = extents.z;

        Vector3f center = getLocalBounds().getCenter();
        
        float xDim4 = xDim/4;
        float yDim4 = yDim/4;
        float zDim4 = zDim/4;
        
        switch(octant) {
            case UPPER_NE :
                octantCenter = new Vector3f(center.x+xDim4, center.y+yDim4, center.z-zDim4);
            break;
            case UPPER_NW :
                octantCenter = new Vector3f(center.x-xDim4, center.y+yDim4, center.z-zDim4);
            break;
            case UPPER_SE :
                octantCenter = new Vector3f(center.x+xDim4, center.y+yDim4, center.z+zDim4);
            break;
            case UPPER_SW :
                octantCenter = new Vector3f(center.x-xDim4, center.y+yDim4, center.z+zDim4);
            break;
            case LOWER_NE :
                octantCenter = new Vector3f(center.x+xDim4, center.y-yDim4, center.z-zDim4);
            break;
            case LOWER_NW :
                octantCenter = new Vector3f(center.x-xDim4, center.y-yDim4, center.z-zDim4);
            break;
            case LOWER_SE :
                octantCenter = new Vector3f(center.x+xDim4, center.y-yDim4, center.z+zDim4);
            break;
            case LOWER_SW :
                octantCenter = new Vector3f(center.x-xDim4, center.y-yDim4, center.z+zDim4);
            break;
        }
        
        BoundingBox octantBounds = new BoundingBox(new Vector3f(), xDim4, yDim4, zDim4);
        
        GroupCellMO ret;
        
        if (xDim4<1000)
            ret = new GroupCellMO(octantBounds, new CellTransform(null, octantCenter));
        else
            ret = new OctTreeCellMO(octantBounds, new CellTransform(null, octantCenter));
        
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
    public CellMO insertCellInHierarchy(CellMO insertChild, BoundingVolume childVWBounds)  throws MultipleParentException {
        CellMO parent = null;

        System.out.println("OctTree bounds "+getCachedVWBounds());
        System.out.println("Child bounds "+childVWBounds);
        
        if (!Math3DUtils.encloses(getCachedVWBounds(), childVWBounds)) {
            System.out.println("child not enclosed by our bounds");
            return null;
        }   

        ManagedReference ref;
        
        Iterator<ManagedReference> it = getAllChildrenRefs().iterator();
        
        while(it.hasNext() && parent==null) {
            ref = it.next();
            if (ref!=null) {
                CellMO tmp = ref.get(CellMO.class);
                if (Math3DUtils.encloses(tmp.getCachedVWBounds(), childVWBounds)) {
                    if (tmp instanceof CellContainerInterface) {
                        parent = ((CellContainerInterface)tmp).insertCellInHierarchy(insertChild, childVWBounds);
                    }

                    if (parent==null) {
                        try {
                            AppContext.getDataManager().markForUpdate(tmp);
                            Vector3f parentLoc = new Vector3f();
                            tmp.getTransform().get(parentLoc);
                            
                            // Adjust childs transform
                            Vector3f childLoc = new Vector3f();
                            CellTransform childTransform = insertChild.getTransform();
                            childTransform.get(childLoc);
                            
                            childLoc.subtract(parentLoc,childLoc);                            
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
