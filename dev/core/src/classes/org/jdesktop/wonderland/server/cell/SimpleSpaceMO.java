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

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * A simple 2D grid space implementation
 * 
 * @author paulby
 */
public class SimpleSpaceMO extends SpaceMO {

    // Unordered list of neighbours
    private ArrayList<ManagedReference<SimpleSpaceMO>> adjacentSpaces;
    private ArrayList<BoundingVolume> adjacentBounds;
    
    SimpleSpaceMO(BoundingVolume bounds, Vector3f position, SpaceID spaceID) {
        super(bounds, position, spaceID);
    }
    
    void setAdjacentSpaces(SimpleSpaceMO... neighbours) {
        adjacentSpaces = new ArrayList();
        adjacentBounds = new ArrayList();
        
        for(SimpleSpaceMO n : neighbours) {
            if (n!=null) {
                adjacentSpaces.add(AppContext.getDataManager().createReference(n));
                adjacentBounds.add(n.getWorldBounds(null));     
                n.addAdjacentSpace(this);
            }
        }        
    }
    
    void addAdjacentSpace(SimpleSpaceMO neighbour) {
        ManagedReference<SimpleSpaceMO> neighbourRef = AppContext.getDataManager().createReference(neighbour);
        if (!adjacentSpaces.contains(neighbourRef)) {
            adjacentSpaces.add(neighbourRef);
            adjacentBounds.add(neighbour.getWorldBounds(null));     
        }
    }
    
    @Override
    Collection<ManagedReference<SpaceMO>> getSpaces(BoundingVolume v) {
        HashSet<ManagedReference<SpaceMO>> ret = new HashSet();
        if (v.intersects(worldBounds)) {
            getSpacesImpl(v, ret);
        }
        return ret;
    }
    
    protected void getSpacesImpl(BoundingVolume v, HashSet set) {
        if (set.add(AppContext.getDataManager().createReference(this))==false) {
            // This space has already been added, so return
            return;
        }
        
        // Add our adjacent spaces, recursively.
        for(int i=0; i<adjacentBounds.size(); i++) {
            if (adjacentBounds.get(i).intersects(v)) {
                adjacentSpaces.get(i).get().getSpacesImpl(v, set);                    
            }
        }
    }

    @Override
    Collection<ManagedReference<SpaceMO>> getAdjacentSpaces() {
        if (adjacentSpaces==null)
            System.out.println("SimpleSpaceMO Null neighbour in "+position);
        return (Collection<ManagedReference<SpaceMO>>) adjacentSpaces.clone();
    }

}
