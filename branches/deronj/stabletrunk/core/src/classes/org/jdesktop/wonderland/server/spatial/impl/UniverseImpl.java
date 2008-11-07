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
package org.jdesktop.wonderland.server.spatial.impl;

import com.sun.sgs.auth.Identity;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.kernel.TaskScheduler;
import com.sun.sgs.kernel.TransactionScheduler;
import com.sun.sgs.service.DataService;
import com.sun.sgs.service.TransactionProxy;
import java.math.BigInteger;
import java.util.HashMap;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 *
 * @author paulby
 */
public class UniverseImpl implements Universe {

    private SpaceManager spaceManager = new SpaceManagerGridImpl();
    private HashMap<CellID, SpatialCell> cells = new HashMap();
    private TaskScheduler taskScheduler;
    private static UniverseImpl universe;
    private TransactionProxy transactionProxy;
    private DataService dataService;
    private TransactionScheduler transactionScheduler;

    public UniverseImpl(ComponentRegistry componentRegistry, TransactionProxy transactionProxy) {
//        this.taskScheduler = taskScheduler;
        this.transactionProxy = transactionProxy;
        this.dataService = transactionProxy.getService(DataService.class);
        this.transactionScheduler = componentRegistry.getComponent(TransactionScheduler.class);
        universe = this;
    }

    public static UniverseImpl getUniverse() {
        return universe;
    }

    public TransactionProxy getTransactionProxy() {
        return transactionProxy;
    }

    public DataService getDataService() {
        return dataService;
    }

//    public void scheduleTask(KernelRunnable task, Identity identity) {
//        taskScheduler.scheduleTask(task, identity);
//    }

    public void scheduleTransaction(KernelRunnable transaction, Identity identity) {
        transactionScheduler.scheduleTask(transaction, identity);
    }

    public void addRootSpatialCell(SpatialCell cell) {
        SpatialCellImpl cellImpl = (SpatialCellImpl) cell;
        
        cellImpl.setRoot(cell);
        
        cellImpl.acquireRootReadLock();
//        System.out.println("Getting spaces for "+cell.getWorldBounds());
        Iterable<Space> it = spaceManager.getEnclosingSpace(cell.getWorldBounds());
        for(Space s : it) {
            s.addRootSpatialCell(cellImpl);
        }
        cellImpl.releaseRootReadLock();
    }

    public void removeRootSpatialCell(SpatialCell cell) {
        SpatialCellImpl cellImpl = (SpatialCellImpl) cell;

        cellImpl.setRoot(null);

        cellImpl.acquireRootReadLock();
//        System.out.println("Getting spaces for "+cell.getWorldBounds());
        Iterable<Space> it = spaceManager.getEnclosingSpace(cell.getWorldBounds());
        for(Space s : it) {
            s.removeRootSpatialCell(cellImpl);
        }
        cellImpl.releaseRootReadLock();

    }

    public SpatialCell createSpatialCell(CellID id, BigInteger cellCacheId, Identity identity) {
        SpatialCell ret;
        if (cellCacheId!=null) {
            ret = new ViewCellImpl(id, spaceManager, identity, cellCacheId);
        } else
            ret = new SpatialCellImpl(id);
        synchronized(cells) {
            cells.put(id, ret);
        }

        return ret;
    }

    public SpatialCell getSpatialCell(CellID cellID) {
        synchronized(cells) {
            return cells.get(cellID);
        }
    }

    public void viewLogin(CellID viewCellId, BigInteger cellCacheId, Identity identity) {
        ViewCellImpl viewCell;
        synchronized(cells) {
            viewCell = (ViewCellImpl) cells.get(viewCellId);
        }
        
        ViewCache viewCache = new ViewCache(viewCell, spaceManager, identity, cellCacheId);
        viewCell.setViewCache(viewCache);
        viewCache.login();
    }

    public void viewLogout(CellID viewCellId) {
        System.err.println("UniverseImpl.viewLogout <-----------------");
        ViewCellImpl viewCell;
        synchronized(cells) {
            viewCell = (ViewCellImpl) cells.get(viewCellId);
        }
        viewCell.getViewCache().logout();
    }

}
