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
package org.jdesktop.wonderland.server.cell.bounds.darkstar;

import org.jdesktop.wonderland.server.cell.bounds.CellDescriptionImpl;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.service.DataService;
import com.sun.sgs.service.TransactionProxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.Math3DUtils;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.RevalidatePerformanceMonitor;
import org.jdesktop.wonderland.server.cell.CellDescription;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.bounds.CellDescriptionService;

/**
 * Stores the cached nodes in Darkstar datastore
 * @author jkaplan
 */
public class CellDescriptionServiceImpl implements CellDescriptionService {
    // logger
    private static final Logger logger =
            Logger.getLogger(CellDescriptionServiceImpl.class.getName());
    
    // our name
    private static final String NAME = CellDescriptionServiceImpl.class.getName();
    
    // Darkstar structures
    private Properties properties;
    private ComponentRegistry systemRegistry;
    private TransactionProxy proxy;
   
    /**
     * SGS 0.9.5 constructor
     * @param properties
     * @param systemRegistry
     * @param proxy
     */
    public CellDescriptionServiceImpl(Properties properties, 
                                      ComponentRegistry systemRegistry,
                                      TransactionProxy proxy)
    {
        this.properties = properties;
        this.systemRegistry = systemRegistry;
        this.proxy = proxy;
    }
    
    public String getName() {
        return CellDescriptionServiceImpl.class.getName();
    }
    
    public void ready() throws Exception {
        // ignore
    }

    public boolean shutdown() {
        return true;
    }
    
    public CellDescriptionImpl getCellDescription(CellID cellID) {
        DataService ds = proxy.getService(DataService.class);
        return (CellDescriptionImpl) ds.getServiceBinding(cellID.toString());
    }
   
    /**
     * Return a collection of child cells whose bounds intersect
     * with the supplied bounds
     * 
     * @param rootCell from which to start search
     * @param bounds bounds within which visible cells are contained
     * @param perfMonitor performance measurement service
     * @return
     */
    public Collection<CellDescription> getVisibleCells(CellID rootCell, 
                                                  BoundingVolume bounds, 
                                                  RevalidatePerformanceMonitor perfMonitor) 
    {
       ArrayList<CellDescription> result = new ArrayList();
       getCellDescription(rootCell).getVisibleCells(result, bounds, perfMonitor);
       return result;
    }
    
    public CellDescriptionImpl addCellDescription(final CellMO cell) {
        final CellDescriptionImpl desc = new ManagedCellDescriptionImpl(cell);
             
        DataService ds = proxy.getService(DataService.class);
        ds.setServiceBinding(cell.getCellID().toString(), desc);
        
        return desc;
    }
    
    public CellDescriptionImpl removeCellDescription(CellID cellID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void cellTransformChanged(CellID cellID, 
                                     final CellTransform transform) 
    {
        CellDescriptionImpl cellBounds = getCellDescription(cellID);
        if (cellBounds != null) {
            logger.finest("Transform changed " + cellID);
                
            cellBounds.setTransform(transform);
            cellTransformChanged(cellBounds);
        } else {
            logger.warning("Unknown cell " + cellID);
        }
    }

    public void cellBoundsChanged(CellID cellID,
                                  final BoundingVolume localBounds) 
    {
        CellDescriptionImpl cellBounds = getCellDescription(cellID);
        if (cellBounds != null) {
            logger.finest("Bounds changed " + cellID);
                                        
            cellBounds.setLocalBounds(localBounds);
            cellLocalBoundsChanged(cellBounds);
        } else {
            logger.warning("Unknown cell " + cellID);
        }
    }
  
    /**
     * Notify the system that a child graph has changed, either because a child
     * graph has been added or removed
     * 
     * @param parentID
     * @param childID
     * @param childAdded
     */
    public void cellChildrenChanged(final CellID parentID, 
                                    final CellID childID, 
                                    final boolean childAdded)
    {
        logger.finest("Schedule add " + childID + " to " + parentID);
        
        CellDescriptionImpl parent = getCellDescription(parentID);
        CellDescriptionImpl child  = getCellDescription(childID);
        
        if (parent == null) {
            logger.warning("Unknown parent " + parentID);
            return;
        } else if (child == null) {
            logger.warning("Unknown child " + childID);
            return;
        }
                
        if (childAdded) {
            logger.finest("Child " + childID + " added to " + parentID);
            parent.addChild(child);
            transformTreeUpdate(parent, child);
        } else {
            logger.finest("Child " + childID + " removed from " + parentID);
            parent.removeChild(child);
            cellLocalBoundsChanged(parent);
        }
    }

    public void cellContentsChanged(CellID cellID) {
        CellDescriptionImpl cellBounds = getCellDescription(cellID);
        if (cellBounds != null) {
            logger.finest("Contents changed " + cellID);
            cellBounds.contentsChanged();
        } else {
            logger.warning("Unknown cell " + cellID);
        }
    }
    
    private void cellLocalBoundsChanged(CellDescriptionImpl cell) {
        // Compute and setTranslation computedWorldBounds
        BoundingVolume b = cell.getCachedVWBounds();
        Iterator<CellDescriptionImpl> it = cell.getAllChildren();
        while (it.hasNext()) {
            b.mergeLocal(it.next().getComputedWorldBounds());
        }
        cell.setComputedWorldBounds(b);

        // Ensure this cells bounds are fully enclosed by parents
        checkParentBounds(cell.getParent(), cell);    
    }

    private void cellTransformChanged(CellDescriptionImpl cell) {
        if (cell.getParent() == null) {
            // Special case for root cell
            BoundingVolume b = cell.getLocalBounds();
            CellTransform t = cell.getTransform();
            t.transform(b);
            cell.setComputedWorldBounds(b);
            cell.setLocalToVWorld(t);
        } else {
            // Change the bounds and localToVWorld on all children
            transformTreeUpdate(cell.getParent(), cell);

            //checkForReparent(cell.getParent(), cell);

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
    private BoundingVolume transformTreeUpdate(CellDescriptionImpl parent, CellDescriptionImpl child) {
        CellTransform parentL2VW = parent.getLocalToVWorld();
        
        CellTransform childTransform = child.getTransform();
        
        if (childTransform!=null) {
            childTransform.mul(parentL2VW);
            child.setLocalToVWorld(childTransform);
        } else {
            child.setLocalToVWorld(parentL2VW);
        }
        
        BoundingVolume ret = child.getCachedVWBounds();
        
        Iterator<CellDescriptionImpl> it = child.getAllChildren();
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
    private void checkForReparent(CellDescriptionImpl parent, CellDescriptionImpl child) {
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
    private void checkParentBounds(CellDescriptionImpl parent, CellDescriptionImpl child) {
        BoundingVolume childComputedWorldBounds = child.getComputedWorldBounds();
        BoundingVolume parentBounds = parent.getComputedWorldBounds();

//        System.out.println("Parent "+parent.getCellID()+" b "+parentBounds);
//        System.out.println("Child cwb "+childComputedWorldBounds);
        
        if (Math3DUtils.encloses(parentBounds, childComputedWorldBounds))
            return;
        
        parentBounds.mergeLocal(childComputedWorldBounds);
        parent.setComputedWorldBounds(parentBounds);
        checkParentBounds(parent.getParent(), parent);
    }
}
