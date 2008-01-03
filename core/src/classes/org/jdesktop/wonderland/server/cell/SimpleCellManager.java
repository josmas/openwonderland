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
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.jdesktop.wonderland.common.cell.MultipleParentException;

/**
 * A simple implementation of the CellManager
 * 
 * @author paulby
 */
public class SimpleCellManager extends CellManager {

    private ManagedReference worldRootRef;
    
    SimpleCellManager() {
        super();
                
        CellMO worldRoot = new CellMO();
        worldRoot.setLocalBounds(new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY));
        worldRoot.setLive(true);
        worldRoot.setLocalToVWorld(worldRoot.getTransform());
        worldRoot.setComputedWorldBounds(worldRoot.getCachedVWBounds());
        worldRootRef = AppContext.getDataManager().createReference(worldRoot);
    }
    
    @Override
    public void addCell(CellMO cell) throws MultipleParentException {
        worldRootRef.get(CellMO.class).addChild(cell);
    }

    @Override
    void cellLocalBoundsChanged(CellMO cell) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    void cellTransformChanged(CellMO cell) {
        throw new UnsupportedOperationException("Not supported yet.");
        
        // Change the bounds and localToVWorld on all children
        // If the bounds of this cell got larger traverse up parent
        // hierarchy ensuring parent bounds enclose their children
    }

    @Override
    void cellChildrenChanged(CellMO parent, CellMO child, boolean childAdded) {
        if (childAdded) {
            Bounds childCombinedBounds = setLive(parent, child);
            Bounds parentBounds = parent.getComputedWorldBounds();
            parentBounds.combine(childCombinedBounds);
            parent.setComputedWorldBounds(parentBounds);
        } else {
            clearLive(parent, child);
            
            // Recompute the cached bounds for the parent
            Bounds parentBounds = parent.getCachedVWBounds();
            Iterator<ManagedReference> it = parent.getAllChildrenRefs();
            while(it.hasNext()) {
                parentBounds.combine(setLive(child, it.next().getForUpdate(CellMO.class)));
            }
            parent.setComputedWorldBounds(parentBounds);
            
        }
    }

    /**
     * Make the child and it's subgraph live.
     * Traverse the child tree and compute the localToVworld for each cell and
     * set the computedWorldBounds
     * 
     * @param parent
     * @param child
     * @return the combined bounds of the child and all it's children
     */
    private Bounds setLive(CellMO parent, CellMO child) {
        Matrix4d parentL2VW = parent.getLocalToVWorld();
        
        Matrix4d childTransform = child.getTransform();
        
        childTransform.mul(parentL2VW);
        
        child.setLocalToVWorld(childTransform);
        child.setLive(true);
        Bounds ret = child.getCachedVWBounds();
        System.out.println("Cell "+child.getName()+"  l2vw "+childTransform+"\nbounds"+ret);
        
        Iterator<ManagedReference> it = child.getAllChildrenRefs();
        while(it.hasNext()) {
            ret.combine(setLive(child, it.next().getForUpdate(CellMO.class)));
        }
        
        child.setComputedWorldBounds(ret);
        System.out.println("Cell "+child.getName()+" ComputedBounds "+ret);
        
        return ret;
    }
    
    /**
     * Clear the live flag on the child and it's subgraph
     * @param parent
     * @param child
     */
    private void clearLive(CellMO parent, CellMO child) {
        Iterator<ManagedReference> it = child.getAllChildrenRefs();
        while(it.hasNext()) {
            clearLive(child, it.next().getForUpdate(CellMO.class));
        }
        child.setLive(false);
        child.setComputedWorldBounds(null);
        child.setLocalToVWorld(null);
    }
}
