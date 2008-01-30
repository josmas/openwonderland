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
package org.jdesktop.wonderland.server.cell.bounds;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.service.TransactionProxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.Math3DUtils;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.UserPerformanceMonitor;
import org.jdesktop.wonderland.server.cell.GroupCellMO;

/**
 * Current implementation synchronizes updates to the CellMirror graph, in the
 * future this will be updated so only updates to a common subgraph are synchronized.
 * Plan is to introduce high level graph nodes which are the sychronization points.
 * @author paulby
 */
public class BoundsServiceImpl implements BoundsService {
    private Map<CellID, CellMirror> bounds;
    
    public BoundsServiceImpl(Properties properties,
                             ComponentRegistry systemRegistry) {
        bounds = new HashMap<CellID, CellMirror>();
    }
    
    public String getName() {
        return BoundsServiceImpl.class.getName();
    }

    public void configure(ComponentRegistry arg0, TransactionProxy arg1) {
        // nothing to do
        System.out.println("configure");
    }

    public boolean shutdown() {
        bounds.clear();
        return true;
    }

    public void ready() throws Exception {
        // ignore
    }

    public CellMirror getCellBounds(CellID cellID) {
        return bounds.get(cellID);
    }
    
    public void putCellBounds(CellMirror cellBounds) {
        bounds.put(cellBounds.getCellID(), cellBounds);
    }

    public void cellTransformChanged(CellID cellID, CellTransform transform) {
        CellMirror cellBounds = getCellBounds(cellID);
        cellBounds.setTransform(transform);
        cellTransformChanged(cellBounds);
    }

    public void cellBoundsChanged(CellID cellID, BoundingVolume bounds) {
        CellMirror cellBounds = getCellBounds(cellID);
        cellBounds.setLocalBounds(bounds);
        cellLocalBoundsChanged(cellBounds);
    }

    public void removeCellBounds(CellID cellID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    void cellLocalBoundsChanged(CellMirror cell) {
        // Synchronize to make changes atomic
        synchronized(bounds) {        
            // Compute and set computedWorldBounds
            BoundingVolume b = cell.getCachedVWBounds();
            Iterator<CellMirror> it = cell.getAllChildren();
            while(it.hasNext()) {
                b.mergeLocal(it.next().getComputedWorldBounds());
            }
            cell.setComputedWorldBounds(b);

            // Ensure this cells bounds are fully enclosed by parents
            checkParentBounds(cell.getParent(), cell);    
        }
    }

    void cellTransformChanged(CellMirror cell) {
        synchronized(bounds) {
            // Change the bounds and localToVWorld on all children
            transformTreeUpdate(cell.getParent(), cell);

            checkForReparent(cell.getParent(), cell);

            //Ensure this cells bounds are fully enclosed by parents
            checkParentBounds(cell.getParent(), cell);    
        }
    }

    /**
     * Update local2VWorld and bounds of child and all its children to
     * reflect changes in a parent
     * 
     * @param parent
     * @param child
     * @return the combined bounds of the child and all it's children
     */
    private BoundingVolume transformTreeUpdate(CellMirror parent, CellMirror child) {
        CellTransform parentL2VW = parent.getLocalToVWorld();
        
        CellTransform childTransform = child.getTransform();
        
        if (childTransform!=null) {
            childTransform.mul(parentL2VW);
            child.setLocalToVWorld(childTransform);
        } else {
            child.setLocalToVWorld(parentL2VW);
        }
        
        BoundingVolume ret = child.getCachedVWBounds();
        
        Iterator<CellMirror> it = child.getAllChildren();
        while(it.hasNext()) {
            ret.mergeLocal(transformTreeUpdate(child, it.next()));
        }
        
        child.setComputedWorldBounds(ret);
        
        return ret;
    }
    
    
    /**
     * Check if the child should be reparented and reparent if necessary. 
     * 
     * The parent is choosen by finding a cell whos defined (as opposed to computed)
     * bounds contain the center point of the child bounds.
     * 
     * When a cell is automatically reparented the system will choose a prent
     * that implements CellContainerInterface
     * 
     * @param parent
     * @param child
     */
    private void checkForReparent(CellMirror parent, CellMirror child) {
        Vector3f center = child.getCachedVWBounds().getCenter();
        if (!parent.getCachedVWBounds().contains(center)) {
            System.out.println("WARNING child outside parents preferred bounds");
        }        
    }
    
    /**
     * Check child bounds are fully enclosed in parent bounds, if not update
     * parent bounds and traverse up tree
     * 
     * @param parent
     * @param childComputedWorldBounds
     */
    private void checkParentBounds(CellMirror parent, CellMirror child) {
        BoundingVolume childComputedWorldBounds = child.getComputedWorldBounds();
        BoundingVolume parentBounds = parent.getComputedWorldBounds();

        if (Math3DUtils.encloses(parentBounds, childComputedWorldBounds))
            return;
        
        parentBounds.mergeLocal(childComputedWorldBounds);
        parent.setComputedWorldBounds(parentBounds);
        checkParentBounds(parent.getParent(), parent);
    }

    /**
     * Return an iterator over the set of visible cells, specifically child cells whose bounds interesect
     * with the supplied bounds
     * 
     * @param rootCell from which to start search
     * @param bounds bounds within which visible cells are contained
     * @param perfMonitor performance measurement service
     * @return
     */
    public Collection<CellID> getVisibleCells(CellID rootCell, BoundingVolume bounds, UserPerformanceMonitor perfMonitor) {
        ArrayList<CellID> result = new ArrayList();
        
        getCellBounds(rootCell).getVisibleCells(result, bounds, perfMonitor);
        
        return result;
    }

    /**
     * Notify the system that a child graph has changed, either because a child
     * graph has been added or removed
     * 
     * @param parentID
     * @param childID
     * @param childAdded
     */
    public void childrenChanged(CellID parentID, CellID childID, boolean childAdded) {
        System.out.println("childrenChanged "+parentID);
        synchronized(bounds) {
            if (childAdded) {
                transformTreeUpdate(getCellBounds(parentID), getCellBounds(childID));
            } else {
                cellLocalBoundsChanged(getCellBounds(parentID));
            }
        }
    }

}
