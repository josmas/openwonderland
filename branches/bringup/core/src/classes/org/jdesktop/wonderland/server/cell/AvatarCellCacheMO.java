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

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ClientSessionId;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.app.PeriodicTaskHandle;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.AvatarBoundsHelper;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.server.CellAccessControl;
import org.jdesktop.wonderland.server.UserPerformanceMonitor;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.bounds.BoundsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientChannel;

/**
 * Container for the cell cache for an avatar.
 *
 * Calculates the set of cells that the client needs to load and sends the
 * information to the client.
 *
 * This is a nieve implementation that does not contain View Frustum culling,
 * culling is performed only on relationship to users position.
 *
 * @author paulby
 */
public class AvatarCellCacheMO implements ManagedObject, Serializable {
    
    private final static Logger logger = Logger.getLogger(AvatarCellCacheMO.class.getName());
    
//    private Channel cacheChannel;
//    private CellRef rootCellRef;
    
    private ManagedReference avatarRef;
    private String username;
    private ClientSessionId userID;
    private WonderlandClientChannel channel;
    private CellID rootCellID;
    private CellCacheClientHandler cacheHandler;
    
    private BoundingSphere proximityBounds = AvatarBoundsHelper.getProximityBounds(new Vector3f());
    
    /**
     * List of currently visible cells (ManagedReference of CellGLO)
     */
    private Map<CellID, CellRef> currentCells = new HashMap<CellID, CellRef>();
    
    private static final boolean USE_CACHE_MANAGER = true;
    
    private PeriodicTaskHandle task = null;
    
    /**
     * Creates a new instance of AvatarCellCacheMO
     */
    public AvatarCellCacheMO(ManagedReference avatarRef) {
        this.avatarRef = avatarRef;
        DataManager dataMgr = AppContext.getDataManager();
        logger.config("Creating AvatarCellCache");
        username = avatarRef.get(AvatarMO.class).getUser().getUsername()+"_"+avatarRef.get(AvatarMO.class).getCellID().toString();
        ChannelManager chanMgr = AppContext.getChannelManager();
        
        dataMgr.setBinding(username+"_CELL_CACHE", this);
        rootCellID = WonderlandContext.getCellManager().getRootCellID();
        
        AvatarMO avatar = avatarRef.get(AvatarMO.class);
    }
    
    /**
     * Notify CellCache that user has logged out
     */
    void logout(ClientSessionId userID) {
        logger.warning("DEBUG - logout");
        currentCells.clear();
        if (USE_CACHE_MANAGER) {
            CacheManager.removeCache(this);
        } else {
            task.cancel();
        }
    }
    
    /**
     * Notify CellCache that user has logged in
     */
    void login(WonderlandClientChannel channel, ClientSessionId userID) {
        this.userID = userID;
        this.channel = channel;
        
        logger.info("AvatarCellCacheMO.login() CELL CACHE LOGIN FOR USER "+userID);
                
        // Setup the Root Cell on the client
        CellHierarchyMessage msg;
        CellMO rootCell = CellManager.getCell(rootCellID);
        msg = CellManager.newCreateCellMessage(rootCell);
        channel.send(userID, msg);
        msg = CellManager.newRootCellMessage(rootCell);
        channel.send(userID, msg);
        currentCells.put(rootCellID, new CellRef(rootCell));
        
        if (USE_CACHE_MANAGER) {
            CacheManager.addCache(this);            
        } else {
            // Periodically revalidate the cache
             task = AppContext.getTaskManager().schedulePeriodicTask(
                    new AvatarCellCacheRevalidateTask(this), 100, 500); 
        }
    }
    
    /**
     * Revalidate cell cache. This first finds the new list of visible cells
     * and if the cell does not exist in the current list of visible cells, then
     * creates the cell on the client. If the visible cell exists, check to see
     * if it has been modified, and send the appropriate message to the client.
     * Finally, remove all of the cells which are no longer visibe.
     */
    void revalidate() {
        // make sure the user is still logged on
        long startTime = System.nanoTime();
        long serviceTime;
        long newCellsTime;
        long getCellTime=0;
        try {
        if (userID == null || userID.getClientSession() == null) {
            return;
        }
        

        AvatarMO user = avatarRef.get(AvatarMO.class);
        proximityBounds = AvatarBoundsHelper.getProximityBounds(user.getTransform().get(null));
//        logger.warning("Revalidating CellCache for   "+userID.getClientSession().isConnected()+"  "+proximityBounds);

        UserPerformanceMonitor monitor = new UserPerformanceMonitor();
        BoundsManager boundsMgr = AppContext.getManager(BoundsManager.class);
        Collection<CellMirror> visCells = boundsMgr.getVisibleCells(rootCellID, proximityBounds, monitor);
        
        serviceTime = System.nanoTime() - startTime;
        
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(monitor.getRevalidateStats());
        }
        
        // copy the existing cells into the list of known cells 
        Collection<CellRef> oldCells = new ArrayList(currentCells.values());
        
        CellHierarchyMessage msg;
         
        // TODO now visCells is a list of cellID's refactor so we don't have to
        // get the cells from sgs if they are already visible. The main issue
        // here is handling the cell version check
        
        /*
         * Loop through all of the visible cells and:
         * 1. If not already present, create it
         * 2. If already present, check to see if has been modified
         * These two steps only happen if we are given access to view the cell
         */
//        System.out.println("VisCells.size "+visCells.size());
        for(CellMirror cellMirror : visCells) {
            CellID cellID = cellMirror.getCellID();
            /* Fetech the cell GLO class associated with the visible cell */
            
            // check this client's access to the cell
            if (!CellAccessControl.canView(user, cellMirror)) {
                // the user doesn't have access to this cell -- just skip
                // it and go on                
                continue;
            }

            // find the cell in our current list of cells
            CellRef cellRef = currentCells.get(cellID);  
            
            if (cellRef == null) {
                // the cell is new -- add it and send a message
                long a = System.nanoTime();
                CellMO cell = CellManager.getCell(cellID);
                getCellTime +=(System.nanoTime() - a);
                cellRef = new CellRef(cell);
                currentCells.put(cellID, cellRef);
                    
                if (logger.isLoggable(Level.FINER))
                    logger.finer("Entering cell " + cell +
                                 "   cellcache for user " + username);
                    
                msg = CellManager.newCreateCellMessage(cell);
                channel.send(userID, msg);
                cell.addUserToCellChannel(userID);
//                if (cell instanceof CacheHelperInterface) {
//                    AppContext.getDataManager().markForUpdate(cell);
//                    ((CacheHelperInterface)cell).addCache(this);
//                }
            } else if (cellRef.hasUpdates(cellMirror)) {
                for (CellRef.UpdateType update : cellRef.getUpdateTypes()) {
                    long a = System.nanoTime();
                    CellMO cell = cellRef.get();
                    getCellTime +=(System.nanoTime() - a);
                    switch (update) {
                        case TRANSFORM:
                            msg = CellManager.newCellMoveMessage(cell);
                            channel.send(userID, msg);
                            break;
                        case CONTENT:
                            msg = CellManager.newContentUpdateCellMessage(cell);
                            channel.send(userID, msg);
                            break;
                    }
                }
                cellRef.clearUpdateTypes(cellMirror);
                
                // t is still visible, so remove it from the oldCells set
                oldCells.remove(cellRef);
            } else {
                // t is still visible, so remove it from the oldCells set
                oldCells.remove(cellRef);
            }
        }
        
        newCellsTime = System.nanoTime() - startTime;
        
            // oldCells contains the set of cells to be removed from client memory
            for(CellRef ref : oldCells) {
                if (logger.isLoggable(Level.FINER))
                    logger.finer("Leaving cell "+ref.getCellID()+"   cellcache for user "+username);

                // the cell may be inactive or removed.  Try to get the cell,
                // and catch the exception if it no longer exists.
                try {
                    CellMO cell = ref.get();

                    // get suceeded, so cell is just inactive
                    msg = CellManager.newUnloadCellMessage(cell);
                    cell.removeUserFromCellChannel(userID);
//                    if (cell instanceof CacheHelperInterface) {
//                        AppContext.getDataManager().markForUpdate(cell);
//                        ((CacheHelperInterface)cell).removeCache(this);
//                    }
                } catch (ObjectNotFoundException onfe) {
                    // get failed, cell is deleted
                    msg = CellManager.newDeleteCellMessage(ref.getCellID());
                }

                channel.send(userID, msg);

                // the cell is no longer visible on this client, so remove
                // our current reference to it.  This client will no longer
                // receive any updates about the given cell, including future
                // deletes.  This implies that the client must clear out
                // its cache of inactive cells periodically, as some of them
                // may have been deleted.
                // TODO periodically clean out client cell cache
                currentCells.remove(ref.getCellID());
            }
        } catch(RuntimeException e) {
            logger.warning("Exception "+e.getClass().getName());
            throw e;
        }
        
//        System.out.println("Revalidation took "+toMilliSecond(System.nanoTime()-startTime)+" ms.  ServiceTime "+toMilliSecond(serviceTime)+"  newCells "+toMilliSecond(newCellsTime) +"  getCell "+toMilliSecond(getCellTime));
    }
    
    private long toMilliSecond(long nanoSecond) {
        return nanoSecond/1000000;
    }
        
    /**
     * The Users avatar has either entered or exited the cell
     */
    void avatarEnteredExitedCell(boolean enter, CellID cellID) {
        
    }
    
    /**
     * Information about a cell in our cache.  This object contains our record
     * of the given cell, including a reference to the cell and the cell's
     * id.  CellRef object are compared by cellID, so two CellRefs with the
     * same id are considered equal, regardless of other information.
     */
    private static class CellRef implements Serializable {
        private static final long serialVersionUID = 1L;

        enum UpdateType { TRANSFORM, CONTENT };
        
        private Set<UpdateType> updateTypes=null;
        
        // the cell id of the cell we reference
        private CellID id;
        
        // a reference to the cell itself
        private ManagedReference cellRef;
        
        private long transformVersion=0;
        
        public CellRef(CellID id) {
            this (id, null);
        }
        
        public CellRef(CellMO cell) {
            this (cell.getCellID(), cell);
        }
        
        private CellRef(CellID id, CellMO cell) {
            this.id = id;
            
            // create a reference to the given CellGLO
            if (cell != null) {
                DataManager dm = AppContext.getDataManager();
                cellRef = dm.createReference(cell);
            }
        }
        
        public CellID getCellID() {
            return id;
        }
        
        public CellMO get() {
            return cellRef.get(CellMO.class);
        }
        
        public CellMO getForUpdate() {
            return cellRef.getForUpdate(CellMO.class);
        }
        
        public Collection<UpdateType> getUpdateTypes() {
            return updateTypes;
        }

        public void addUpdateType(UpdateType updateType) {
            if (updateTypes==null)
                updateTypes = EnumSet.of(updateType);
            else
                updateTypes.add(updateType);
        }
        
        public void clearUpdateTypes(CellMirror cellMirror) {
            transformVersion = cellMirror.getTransformVersion();
            
            updateTypes.clear();
        }
        
        public long getTransformVersion() {
            return transformVersion;
        }

        public void setTransformVersion(long transformVersion) {
            this.transformVersion = transformVersion;
        }

        public boolean hasUpdates(CellMirror cellMirror) {
//            if (updateTypes==null)
//                return false;
//            return updateTypes.size()!=0;
            if (cellMirror.getTransformVersion()!=transformVersion) {
                addUpdateType(UpdateType.TRANSFORM);
                return true;
            }
            
            return false;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CellRef)) {
                return false;
            }
            
            return id.equals(((CellRef) o).id);
        }
        
        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    /***********************************************
     * CacheHelperListener impl
     ***********************************************/
    
    public void notifyTransformUpdate(CellMO cell) {
        CellRef cr;
    
        cr = currentCells.get(cell.getCellID());
        if (cr==null)
            return;
        cr.addUpdateType(CellRef.UpdateType.TRANSFORM);
        
        throw new RuntimeException("DEPRECATED");
    }
    
    public void notifyContentUpdate(CellMO cell) {
        CellRef cr;
        cr = currentCells.get(cell.getCellID());
        if (cr==null)
            return;
        cr.addUpdateType(CellRef.UpdateType.CONTENT);  
        
        throw new RuntimeException("DEPRECATED");
        // TODO mark the cache as dirty
    }
    
    /***********************************************
     * End CacheHelperListener impl
     ***********************************************/
}

