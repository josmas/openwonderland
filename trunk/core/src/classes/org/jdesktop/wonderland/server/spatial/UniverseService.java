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
package org.jdesktop.wonderland.server.spatial;

import java.util.logging.Level;
import org.jdesktop.wonderland.server.spatial.impl.SpatialCell;
import org.jdesktop.wonderland.server.spatial.impl.Universe;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Matrix4f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.impl.service.data.DataServiceImpl;
import com.sun.sgs.impl.util.TransactionContext;
import com.sun.sgs.impl.util.TransactionContextFactory;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.TaskScheduler;
import com.sun.sgs.service.Service;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ViewCellCacheMO;
import org.jdesktop.wonderland.server.cell.view.ViewCellMO;
import org.jdesktop.wonderland.server.spatial.impl.SpatialCellImpl;
import org.jdesktop.wonderland.server.spatial.impl.UniverseImpl;

/**
 *
 * @author paulby
 */
public class UniverseService implements UniverseServiceManager, Service {

        // logger
    private static final Logger logger =
            Logger.getLogger(UniverseService.class.getName());

    // our name
    private static final String NAME = UniverseService.class.getName();

    // Darkstar structures
    private Properties properties;
    private ComponentRegistry systemRegistry;
    private TransactionProxy proxy;

    private Universe universe;
    private ChangeApplication changeApplication;

    // manages the context of the current transaction
    private TransactionContextFactory<BoundsTransactionContext> ctxFactory;

    public UniverseService(Properties prop,
                           ComponentRegistry registry,
                           TransactionProxy transactionProxy) {

        this.properties = prop;
        this.systemRegistry = registry;
        this.proxy = transactionProxy;

        ctxFactory = new TransactionContextFactory<BoundsTransactionContext>(proxy, NAME) {
            @Override
            protected BoundsTransactionContext createContext(Transaction txn) {
                return new BoundsTransactionContext(txn);
            }
        };

        changeApplication = new ChangeApplication();
        universe = new UniverseImpl(registry, transactionProxy);

    }

    public String getName() {
        return NAME;
    }

    public void ready() throws Exception {
        // nothing to do
    }

    public boolean shutdown() {
        return true; // Success
    }


//    private SpatialCell cloneGraph(CellMO cellMO) {
//        SpatialCell ret = universe.createSpatialCell(cellMO.getCellID(), false);
//
//        for(ManagedReference<CellMO> childRef : cellMO.getAllChildrenRefs()) {
//            ret.addChild(cloneGraph(childRef.get()));
//        }
//
//        return ret;
//    }

    private void scheduleChange(Change change) {
        // get the current transaction state
        ctxFactory.joinTransaction().addChange(change);
    }

    public void addRootToUniverse(CellMO rootCellMO) {
        scheduleChange(new Change(rootCellMO.getCellID(), null, null) {
            public void run() {
                universe.addRootSpatialCell(cellID);
            }
        });
    }

    public void removeRootFromUniverse(CellMO rootCellMO) {
        scheduleChange(new Change(rootCellMO.getCellID(), null, null) {
            public void run() {
                universe.removeRootSpatialCell(cellID);
            }
        });
    }

    public void createCell(CellMO cellMO) {
        final BigInteger cellCacheId = (cellMO instanceof ViewCellMO) ? AppContext.getDataManager().createReference( ((ViewCellMO)cellMO).getCellCache()).getId() : null;
        final Identity identity = proxy.getCurrentOwner();

        scheduleChange(new Change(cellMO.getCellID(), cellMO.getLocalBounds(), cellMO.getLocalTransform(null)) {
            public void run() {
                SpatialCell sc = universe.createSpatialCell(cellID, cellCacheId, identity);
                sc.setLocalBounds(localBounds);
                sc.setLocalTransform(localTransform);
            }
        });

    }

    public void removeCell(CellMO cell) {
        scheduleChange(new Change(cell.getCellID(), null, null) {

            public void run() {
                universe.removeCell(cellID);
            }
        });
    }

    public void addChild(CellMO parent, CellMO child) {
        scheduleChange(new Change(parent.getCellID(), child.getCellID()) {
            public void run() {
                SpatialCell parent = universe.getSpatialCell(cellID);
                parent.addChild(universe.getSpatialCell(childCellID));
            }
        });

    }

    public void removeChild(CellMO parent, CellMO child) {
        scheduleChange(new Change(parent.getCellID(), child.getCellID()) {
            public void run() {
                SpatialCell parent = universe.getSpatialCell(cellID);
                parent.removeChild(universe.getSpatialCell(childCellID));
            }
        });
    }

    public void setLocalTransform(CellMO cellMO, CellTransform localTransform) {
        scheduleChange(new Change(cellMO.getCellID(), null, localTransform) {
            public void run() {
                SpatialCell sc = universe.getSpatialCell(cellID);
                sc.setLocalTransform(localTransform);
            }
        });
    }

    public void viewLogin(ViewCellMO viewCell) {
        final BigInteger cellCacheId = AppContext.getDataManager().createReference( viewCell.getCellCache()).getId();
        final Identity identity = proxy.getCurrentOwner();

        scheduleChange(new Change(viewCell.getCellID(), null, null) {
            public void run() {
                universe.viewLogin(cellID, cellCacheId, identity);
            }
        });

    }

    public void viewLogout(ViewCellMO viewCell) {
        scheduleChange(new Change(viewCell.getCellID(), null, null) {
            public void run() {
                System.err.println("Calling universe.logout");
                universe.viewLogout(cellID);
            }
        });

    }

    public CellTransform getWorldTransform(CellMO cell, CellTransform result) {
        SpatialCellImpl spatial = (SpatialCellImpl) universe.getSpatialCell(cell.getCellID());

        CellTransform ret;
        spatial.acquireRootReadLock();
        ret = spatial.getWorldTransform().clone(result);
        spatial.releaseRootReadLock();

        return ret;
    }

    public BoundingVolume getWorldBounds(CellMO cell, BoundingVolume result) {
        SpatialCellImpl spatial = (SpatialCellImpl) universe.getSpatialCell(cell.getCellID());
        BoundingVolume ret;

        spatial.acquireRootReadLock();
        ret = spatial.getWorldBounds().clone(result);
        spatial.releaseRootReadLock();

        return ret;
    }

    /**
     * A change to apply to the cell.  This change will be applied when
     * the current transaction commits.  The run() method of subclasses
     * should perform any actual changes.
     */
    private static abstract class Change implements Runnable {
        protected CellID cellID;
        protected BoundingVolume localBounds;
        protected CellTransform localTransform;
        protected CellID childCellID;

        public Change(CellID cellID, BoundingVolume localBounds, CellTransform localTransform) {
            this.cellID = cellID;
            this.localBounds = localBounds;
            this.localTransform = localTransform;
        }

        public Change(CellID cellID, CellID childCellID) {
            this.cellID = cellID;
            this.childCellID = childCellID;
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
            try {
                changeApplication.addChanges(changes);
                // done with all changes
                changes.clear();
            } finally {
//                boundsLock.writeLock().unlock();
            }

            isCommitted = true;
        }
    }

    private class ChangeApplication extends Thread {

        private LinkedBlockingQueue<Change> changeList = new LinkedBlockingQueue();

        public ChangeApplication() {
            super("ChangeApplication");
            start();
        }

        public void addChanges(Collection<Change> changes) {
            changeList.addAll(changes);
        }

        @Override
        public void run() {
            Change change;

            while(true) {
                try {
                    change = changeList.take();
                    change.run();
                } catch (InterruptedException ex) {

                }
            }
        }
    }

}
