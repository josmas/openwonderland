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
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.NameNotBoundException;
import java.util.ArrayList;
import org.jdesktop.wonderland.server.WonderlandContext;

/**
 *
 * @author paulby
 */
class SpaceManagerGridImpl implements SpaceManager {

    private static final int SPACE_SIZE = 25; // Radius
 
    // The spaces must overlap slightly so that the view does not land between 2 spaces
    private static final float fudge = 1.0001f;
    private long nextID = 0L;
     
    public void initialize() {
    }
    
    /**
     * Return the space that encloses this point, if the space does not exist, create it
     * @param position
     * @return
     */
    public SpaceMO[] getEnclosingSpace(Vector3f point) {
        int x = (int) (point.x / (SPACE_SIZE*2));
        int y = (int) (point.y / (SPACE_SIZE*2));
        int z = (int) (point.z / (SPACE_SIZE*2));
        
        if (point.x<0) x-=1;
        if (point.y<0) y-=1;
        if (point.z<0) z-=1;
        
        SpaceMO ret = getEnclosingSpaceImpl(x,y,z);
        
        if (ret==null)
            ret = createSpace(point, x, y, z);
        
        if (!ret.worldBounds.contains(point))
            throw new RuntimeException("BAD ENCLOSING SPACE "+ret.getWorldBounds(null)+"  does not contain "+point+"   name "+getSpaceBindingName(x, y, z));
        
        return new SpaceMO[] {ret};
    }
    
    private SpaceID nextSpaceID() {
        return new SpaceID(nextID++);
    }
    
    private SpaceMO createSpace(Vector3f point, int x, int y, int z) {
        
        Vector3f center = new Vector3f((x * SPACE_SIZE*2)+SPACE_SIZE, 
                                       (y * SPACE_SIZE*2)+SPACE_SIZE, 
                                       (z * SPACE_SIZE*2)+SPACE_SIZE);
        BoundingBox gridBounds = new BoundingBox(center, 
                                                 SPACE_SIZE*fudge, 
                                                 SPACE_SIZE*fudge, 
                                                 SPACE_SIZE*fudge);
        
//        System.out.println("CREATING SPACE "+x+" "+y+" "+z+"    bounds "+gridBounds+"  for point "+point);
        SimpleSpaceMO space = new SimpleSpaceMO(gridBounds, 
                                                center,
                                                nextSpaceID());
        
        ArrayList<SimpleSpaceMO> list = new ArrayList();
        list.add((SimpleSpaceMO)getEnclosingSpaceImpl(x,y,z+1));    // North
        list.add((SimpleSpaceMO)getEnclosingSpaceImpl(x+1,y,z));    // East
        list.add((SimpleSpaceMO)getEnclosingSpaceImpl(x,y,z-1));    // South
        list.add((SimpleSpaceMO)getEnclosingSpaceImpl(x-1,y,z));    // West
        list.add((SimpleSpaceMO)getEnclosingSpaceImpl(x+1,y,z+1));  // NE
        list.add((SimpleSpaceMO)getEnclosingSpaceImpl(x+1,y,z-1));  // SE
        list.add((SimpleSpaceMO)getEnclosingSpaceImpl(x-1,y,z-1));  // SW
        list.add((SimpleSpaceMO)getEnclosingSpaceImpl(x-1,y,z+1));  // NW
        space.setAdjacentSpaces(list.toArray(new SimpleSpaceMO[list.size()]));
        
        // connect existing neighbour spaces to the new space
        for(SimpleSpaceMO n : list) {
            if (n!=null)
                n.addAdjacentSpace(space);
        }
        
        AppContext.getDataManager().setBinding(getSpaceBindingName(x,y,z), space);
        
        // Add root cell, TODO remove once we hardcode root cells on clients
        CellManagerMO cellManager = WonderlandContext.getCellManager();
        CellMO rootCell = CellManagerMO.getCell(cellManager.getRootCellID());
        rootCell.addToSpace(space);
        
        return space;
    }
    
    /**
     * Return the space that encloses this point, or null if the space does not exist.
     * @param position
     * @return
     */
    private SpaceMO getEnclosingSpaceImpl(int x, int y, int z) {
        
        try {
            return (SpaceMO) AppContext.getDataManager().getBinding(getSpaceBindingName(x,y,z));
        } catch(NameNotBoundException e) {
            return null;
        }
    }
    
    private String getSpaceBindingName(int x, int y, int z) {
        return "SPACE_"+x+"_"+y+"_"+z;
    }
 
}
