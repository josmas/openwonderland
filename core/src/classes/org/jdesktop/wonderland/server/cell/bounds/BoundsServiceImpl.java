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
import com.sun.sgs.impl.util.TransactionContext;
import com.sun.sgs.impl.util.TransactionContextFactory;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.Math3DUtils;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.RevalidatePerformanceMonitor;
import org.jdesktop.wonderland.server.cell.CellMirror;

/**
 * Current implementation synchronizes updates to the CellDescriptionImpl graph, in the
 * future this will be updated so only updates to a common subgraph are synchronized.
 * Plan is to introduce high level graph nodes which are the sychronization points.
 * @author paulby
 */
public class BoundsServiceImpl implements BoundsService {
    // logger
    private static final Logger logger =
            Logger.getLogger(BoundsServiceImpl.class.getName());
    
    // our name
    private static final String NAME = BoundsServiceImpl.class.getName();
    
    // Darkstar structures
    private Properties properties;
    private ComponentRegistry systemRegistry;
    private TransactionProxy proxy;
    
    // lock for reading and writing the bounds data
    private ReadWriteLock boundsLock;
    
    // the actual bounds data
    private Map<CellID, CellDescriptionImpl> bounds;
    
    // manages the context of the current transaction
    private TransactionContextFactory<BoundsTransactionContext> ctxFactory;
   
    /**
     * SGS 0.9.5 constructor
     * @param properties
     * @param systemRegistry
     * @param proxy
     */
    public BoundsServiceImpl(Properties properties, 
                             ComponentRegistry systemRegistry,
                             TransactionProxy proxy)
    {
        this.properties = properties;
        this.systemRegistry = systemRegistry;
        this.proxy = proxy;
        
        bounds = new HashMap<CellID, CellDescriptionImpl>();
        boundsLock = new ReentrantReadWriteLock();
        
        ctxFactory = new TransactionContextFactory<BoundsTransactionContext>(proxy, NAME) {
            @Override
            protected BoundsTransactionContext createContext(Transaction txn) {
                return new BoundsTransactionContext(txn);
            }
        };
    }
    
    /**
     * SGS 0.9.4 constructor 
     * @param properties
     * @param systemRegistry
     */
    public BoundsServiceImpl(Properties properties, 
                             ComponentRegistry systemRegistry)
    {
        this.properties = properties;
        this.systemRegistry = systemRegistry;
        
        bounds = new HashMap<CellID, CellDescriptionImpl>();
        boundsLock = new ReentrantReadWriteLock();
    }
    
    /**
     * SGS 0.9.4 configure method
     * @param systemRegistry
     * @param proxy
     */
    public void configure(ComponentRegistry systemRegistry, 
                          TransactionProxy proxy) 
    {
        this.systemRegistry = systemRegistry;
        this.proxy = proxy;
        
        ctxFactory = new TransactionContextFactory<BoundsTransactionContext>(proxy, NAME) {
            @Override
            protected BoundsTransactionContext createContext(Transaction txn) {
                return new BoundsTransactionContext(txn);
            }
        };
    }
    
    public String getName() {
        return BoundsServiceImpl.class.getName();
    }
    
    public void ready() throws Exception {
        // ignore
    }

    public boolean shutdown() {
        bounds.clear();
        return true;
    }
    
    public CellDescriptionImpl getCellMirrorImpl(CellID cellID) {
        boundsLock.readLock().lock();
        
        try {
            return bounds.get(cellID);
        } finally {
            boundsLock.readLock().unlock();
        }
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
    public Collection<CellMirror> getVisibleCells(CellID rootCell, 
                                                  BoundingVolume bounds, 
                                                  RevalidatePerformanceMonitor perfMonitor) 
    {
        boundsLock.readLock().lock();
        try {
            ArrayList<CellMirror> result = new ArrayList();
            getCellMirrorImpl(rootCell).getVisibleCells(result, bounds, perfMonitor);
            return result;
        } finally {
            boundsLock.readLock().unlock();
        }
    }
    
    public void putCellMirrorImpl(final CellDescriptionImpl cellBounds) {
        scheduleChange(new Change(cellBounds.getCellID()) {
            public void run() {
                logger.finest("Add cell " + getCellID());
                bounds.put(getCellID(), cellBounds);
            }
        });
    }
    
    public void removeCellMirrorImpl(CellID cellID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void cellTransformChanged(CellID cellID, 
                                     final CellTransform transform) 
    {
        scheduleChange(new Change(cellID) {
            public void run() {                
                CellDescriptionImpl cellBounds = bounds.get(getCellID());
                if (cellBounds != null) {
                    logger.finest("Transform changed " + getCellID());
                
                    cellBounds.setTransform(transform);
                    cellTransformChanged(cellBounds);
                } else {
                    logger.warning("Unknown cell " + getCellID());
                }
            }
        });
    }

    public void cellBoundsChanged(CellID cellID,
                                  final BoundingVolume localBounds) 
    {
        scheduleChange(new Change(cellID) {
            public void run() {
                CellDescriptionImpl cellBounds = bounds.get(getCellID());
                if (cellBounds != null) {
                    logger.finest("Bounds changed " + getCellID());
                                        
                    cellBounds.setLocalBounds(localBounds);
                    cellLocalBoundsChanged(cellBounds);
                } else {
                    logger.warning("Unknown cell " + getCellID());
                }
            }
        });
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
        
        scheduleChange(new Change(parentID) {
            public void run() {
                CellDescriptionImpl parent = bounds.get(getCellID());
                CellDescriptionImpl child = bounds.get(childID);
                if (parent == null) {
                    logger.warning("Unknown parent " + getCellID());
                    return;
                } else if (child == null) {
                    logger.warning("Unknown child " + childID);
                    return;
                }
                
                if (childAdded) {
                    logger.finest("Child " + childID + " added to " + getCellID());
                    parent.addChild(child);
                    transformTreeUpdate(parent, child);
                } else {
                    logger.finest("Child " + childID + " removed from " + getCellID());
                    parent.removeChild(child);
                    cellLocalBoundsChanged(parent);
                }
            }
        });
    }

    public void cellContentsChanged(CellID cellID) {
        scheduleChange(new Change(cellID) {
            public void run() {
                CellDescriptionImpl cellBounds = bounds.get(getCellID());
                if (cellBounds != null) {
                    logger.finest("Contents changed " + getCellID());
                    cellBounds.contentsChanged();
                } else {
                    logger.warning("Unknown cell " + getCellID());
                }
            }
        });
    }
    
    private void scheduleChange(Change change) {
        // get the current transaction state
        ctxFactory.joinTransaction().addChange(change);
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
    
    /**
     * A change to apply to the bounds.  This change will be applied when
     * the current transaction commits.  The run() method of subclasses
     * should perform any actual changes.  When run() is called, the write 
     * lock is guaranteed to already be held for the bounds map.
     */
    private static abstract class Change implements Runnable {
        private CellID cellID;
    
        public Change(CellID cellID) {
            this.cellID = cellID;
        }
        
        protected CellID getCellID() {
            return cellID;
        }
    }
    
    /**
     * Transaction state
     */
    private class BoundsTransactionContext extends TransactionContext {
        private List<Change> changes;
        
        public BoundsTransactionContext(Transaction txn) {
            super (txn);
            
            changes = new ArrayList();
        }
        
        public void addChange(Change change) {
            changes.add(change);
        }
        
        @Override
        public void abort(boolean retryable) {
            changes.clear();
        }

        @Override
        public void commit() {
            // acquire the write lock to the bounds
            boundsLock.writeLock().lock();
            try {
                // process each change
                for (Change change : changes) {
                    change.run();
                }
                
                // done with all changes
                changes.clear();
            } finally {
                boundsLock.writeLock().unlock();
            }
            
            isCommitted = true;
        }
    }
}
