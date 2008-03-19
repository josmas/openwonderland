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
import org.jdesktop.wonderland.server.cell.RevalidatePerformanceMonitor;
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
     
    /**
     * List of currently visible cells (ManagedReference of CellGLO)
     */
    private Map<CellID, CellRef> currentCells = new HashMap<CellID, CellRef>();
    
    private static final boolean USE_CACHE_MANAGER = false;
    
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
        if (userID == null || userID.getClientSession() == null) {
            return;
        }
        
        // create a performance monitor
        RevalidatePerformanceMonitor monitor = new RevalidatePerformanceMonitor();
        long startTime = System.nanoTime();
        
        try {
            // getTranslation the current user's bounds
            AvatarMO user = avatarRef.get(AvatarMO.class);
            BoundingSphere proximityBounds = AvatarBoundsHelper.getProximityBounds(user.getTransform().getTranslation(null));
            
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Revalidating CellCache for   " + 
                              userID.getClientSession().getName() + "  " + 
                              proximityBounds);
            }

            // find the visible cells
            BoundsManager boundsMgr = AppContext.getManager(BoundsManager.class);
            Collection<CellMirror> visCells = 
                    boundsMgr.getVisibleCells(rootCellID, proximityBounds, monitor);
            monitor.setVisibleCellCount(visCells.size());
            
            // copy the existing cells into the list of known cells 
            Collection<CellRef> oldCells = new ArrayList(currentCells.values());
        
            CellHierarchyMessage msg;
            /*
             * Loop through all of the visible cells and:
             * 1. If not already present, create it
             * 2. If already present, check to see if has been modified
             * These two steps only happen if we are given access to view the cell
             */
            for(CellMirror cellMirror : visCells) {
                long cellStartTime = System.nanoTime();
                CellID cellID = cellMirror.getCellID();
            
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
                    CellMO cell = CellManager.getCell(cellID);
                    cellRef = new CellRef(cell, cellMirror);
                    currentCells.put(cellID, cellRef);
                    markForUpdate();
                    
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Entering cell " + cell +
                                     "   cellcache for user " + username);
                    }
                    
                    msg = CellManager.newCreateCellMessage(cell);
                    monitor.incMessageBytes(msg.getBytes().length);
                    channel.send(userID, msg);
                    //System.out.println("SENDING "+msg.getActionType()+" "+msg.getBytes().length);
                    cell.addUserToCellChannel(userID);
                    
                    // update performance monitoring
                    monitor.incNewCellTime(System.nanoTime() - cellStartTime);
                } else if (cellRef.hasUpdates(cellMirror)) {
                    for (CellRef.UpdateType update : cellRef.getUpdateTypes()) {
                        switch (update) {
                            case TRANSFORM:
                                msg = CellManager.newCellMoveMessage(cellMirror);
                                monitor.incMessageBytes(msg.getBytes().length);
                                channel.send(userID, msg);
                                //System.out.println("SENDING "+msg.getActionType()+" "+msg.getBytes().length);
                                break;
                            case CONTENT:
                                msg = CellManager.newContentUpdateCellMessage(cellRef.get());
                                monitor.incMessageBytes(msg.getBytes().length);
                                channel.send(userID, msg);
                                break;
                        }
                    }
                    cellRef.clearUpdateTypes(cellMirror);
                
                    // it is still visible, so remove it from the oldCells set
                    oldCells.remove(cellRef);
                    
                    // update the monitor
                    monitor.incUpdateCellTime(System.nanoTime() - cellStartTime);
                } else {
                    // t is still visible, so remove it from the oldCells set
                    oldCells.remove(cellRef);
                }
            }
               
            // oldCells contains the set of cells to be removed from client memory
            for(CellRef ref : oldCells) {
                long cellStartTime = System.nanoTime();
                
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Leaving cell " + ref.getCellID() +
                                 " cellcache for user "+username);
                }
                
                // the cell may be inactive or removed.  Try to get the cell,
                // and catch the exception if it no longer exists.
                try {
                    CellMO cell = ref.get();

                    // get suceeded, so cell is just inactive
                    msg = CellManager.newUnloadCellMessage(cell);
                    cell.removeUserFromCellChannel(userID);
                } catch (ObjectNotFoundException onfe) {
                    // get failed, cell is deleted
                    msg = CellManager.newDeleteCellMessage(ref.getCellID());
                }
                
                monitor.incMessageBytes(msg.getBytes().length);
                channel.send(userID, msg);
                //System.out.println("SENDING "+msg.getClass().getName()+" "+msg.getBytes().length);

                // the cell is no longer visible on this client, so remove
                // our current reference to it.  This client will no longer
                // receive any updates about the given cell, including future
                // deletes.  This implies that the client must clear out
                // its cache of inactive cells periodically, as some of them
                // may have been deleted.
                // TODO periodically clean out client cell cache
                currentCells.remove(ref.getCellID());
                markForUpdate();
                
                // update the monitor
                monitor.incOldCellTime(System.nanoTime() - cellStartTime);
            }
        } catch(RuntimeException e) {
            monitor.setException(true);
            
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Rethrowing exception", e);
            }
            
            throw e;
        } finally {
            monitor.incTotalTime(System.nanoTime() - startTime);
            monitor.updateTotals();
            
            // logger.info(monitor.getMessageStats());
            
            // print stats
            if (RevalidatePerformanceMonitor.printTotals()) {
                logger.info(RevalidatePerformanceMonitor.getTotals());
                RevalidatePerformanceMonitor.resetTotals();
            }
        }
    }
 
    /**
     * The Users avatar has either entered or exited the cell
     */
    void avatarEnteredExitedCell(boolean enter, CellID cellID) {
        
    }
    
    /**
     * Utility method to mark ourself for update
     */
    protected void markForUpdate() {
        DataManager dm = AppContext.getDataManager();
        dm.markForUpdate(this);
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
        
        private int transformVersion=Integer.MIN_VALUE;
        private int contentsVersion=Integer.MIN_VALUE;
        
        public CellRef(CellMO cell) {
            this (cell, BoundsHandler.get().getCellMirror(cell.getCellID()));
        }
        
        public CellRef(CellMO cell, CellMirror cellMirror) {
            this (cell.getCellID(), cell, cellMirror);
        }
        
        private CellRef(CellID id, CellMO cell, CellMirror cellMirror) {
            this.id = id;
            
            // create a reference to the given CellMO
            if (cell != null) {
                DataManager dm = AppContext.getDataManager();
                cellRef = dm.createReference(cell);
            }
            
            // initialize versions
            if (cellMirror != null) {
                transformVersion = cellMirror.getTransformVersion();
                contentsVersion = cellMirror.getContentsVersion();
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
            contentsVersion = cellMirror.getContentsVersion();
      
            updateTypes.clear();
        }
        
        public int getTransformVersion() {
            return transformVersion;
        }

        public void setTransformVersion(int transformVersion) {
            this.transformVersion = transformVersion;
        }

        public int getContentsVersion() {
            return contentsVersion;
        }

        public void setContentsVersion(int contentsVersion) {
            this.contentsVersion = contentsVersion;
        }

        public boolean hasUpdates(CellMirror cellMirror) {
            boolean ret = false;
            if (cellMirror.getTransformVersion()!=transformVersion) {
                addUpdateType(UpdateType.TRANSFORM);
                ret = true;
            }
            if (cellMirror.getContentsVersion()!=contentsVersion) {
                addUpdateType(UpdateType.CONTENT);
                ret = true;
            }
            
            return ret;
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

}

