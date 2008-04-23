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
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.app.PeriodicTaskHandle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.AvatarBoundsHelper;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMoveMessage;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyUnloadMessage;
import org.jdesktop.wonderland.common.messages.MessageList;
import org.jdesktop.wonderland.server.CellAccessControl;
import org.jdesktop.wonderland.server.UserSecurityContextMO;
import org.jdesktop.wonderland.server.cell.RevalidatePerformanceMonitor;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.bounds.CellDescriptionManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

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
@InternalAPI
public class ViewCellCacheMO implements ManagedObject, Serializable {
    
    private final static Logger logger = Logger.getLogger(ViewCellCacheMO.class.getName());
    
    private ManagedReference<View> viewRef;
    private ManagedReference<UserSecurityContextMO> securityContextRef;
    
    private String username;
   
    private WonderlandClientSender sender;
    private ManagedReference<ClientSession> sessionRef;
    
    private CellID rootCellID;
    
    private ClientCapabilities capabilities = null;
     
    /**
     * List of currently active cells
     */
    private Map<CellID, CellRef> currentCells = new HashMap<CellID, CellRef>();
    
    private PeriodicTaskHandle task = null;
    
    // Combine all client messages for a single revalidation into a single message
    private static final boolean AGGREGATE_MESSAGES = true;
    
    /**
     * Creates a new instance of ViewCellCacheMO
     */
    public ViewCellCacheMO(View view) {
        logger.config("Creating ViewCellCache");
        
        username = view.getUser().getUsername();
        rootCellID = WonderlandContext.getCellManager().getRootCellID();
        
        DataManager dm = AppContext.getDataManager();
        viewRef = dm.createReference(view);
        dm.setBinding(username + "_CELL_CACHE", this);
        
        UserSecurityContextMO securityContextMO = view.getUser().getUserSecurityContext();
        if (securityContextMO!=null)
            securityContextRef = dm.createReference(securityContextMO);
        else
            securityContextRef = null;
    }
    
    /**
     * Notify CellCache that user has logged in
     */
    void login(WonderlandClientSender sender, ClientSession session) {
        this.sender = sender;
        
        DataManager dm = AppContext.getDataManager();
        sessionRef = dm.createReference(session);
        
        logger.info("AvatarCellCacheMO.login() CELL CACHE LOGIN FOR USER "
                    + session.getName() + " AS " + username);
                
        // Setup the Root Cell on the client
        CellHierarchyMessage msg;
        CellMO rootCell = CellManagerMO.getCell(rootCellID);
        
        msg = newCreateCellMessage(rootCell, capabilities);
        sender.send(session, msg);
        
        currentCells.put(rootCellID, new CellRef(rootCell));
        
        if (CacheManager.USE_CACHE_MANAGER) {
            CacheManager.addCache(this);            
        } else {
            // Periodically revalidate the cache
             task = AppContext.getTaskManager().schedulePeriodicTask(
                    new ViewCellCacheRevalidateTask(this), 100, 500); 
        }
    }
    
    /**
     * Notify CellCache that user has logged out
     */
    void logout(ClientSession session) {
        logger.warning("DEBUG - logout");
        currentCells.clear();
        if (CacheManager.USE_CACHE_MANAGER) {
            CacheManager.removeCache(this);
        } else {
            task.cancel();
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
        ClientSession session = getSession();
        MessageList messageList = null;
        
        if (session == null) {
            logger.warning("Null session, have not seen a logout");
            return;
        }
        
        if (AGGREGATE_MESSAGES)
            messageList = new MessageList();
        
        // create a performance monitor
        RevalidatePerformanceMonitor monitor = new RevalidatePerformanceMonitor();
        long startTime = System.nanoTime();
        
        try {
            // getTranslation the current user's bounds
            View view = viewRef.get();
            BoundingSphere proximityBounds = AvatarBoundsHelper.getProximityBounds(view.getTransform().getTranslation(null));
            
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Revalidating CellCache for   " + 
                              session.getName() + "  " + proximityBounds);
            }

            // find the visible cells
            CellDescriptionManager boundsMgr = AppContext.getManager(CellDescriptionManager.class);
            Collection<CellDescription> visCells = 
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
            for(CellDescription cellMirror : visCells) {
                long cellStartTime = System.nanoTime();
                CellID cellID = cellMirror.getCellID();
            
                // check this client's access to the cell
                if (securityContextRef!=null && !CellAccessControl.canView(securityContextRef.get(), cellMirror)) {
                    // the user doesn't have access to this cell -- just skip
                    // it and go on                
                    continue;
                }

                // find the cell in our current list of cells
                CellRef cellRef = currentCells.get(cellID);           
                if (cellRef == null) {
                    // the cell is new -- add it and send a message
                    CellMO cell = CellManagerMO.getCell(cellID);
                    cellRef = new CellRef(cell, cellMirror);
                    currentCells.put(cellID, cellRef);
                    markForUpdate();
                    
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Entering cell " + cell +
                                     "   cellcache for user " + username);
                    }
                    
                    //System.out.println("SENDING "+msg.getActionType()+" "+msg.getBytes().length);
                    CellSessionProperties prop = cell.addSession(session, capabilities);
                    cellRef.setCellSessionProperties(prop);
                    
                    msg = newCreateCellMessage(cell, capabilities);
                    if (AGGREGATE_MESSAGES)
                        messageList.addMessage(msg);
                    else
                       sender.send(session, msg);
                    monitor.incMessageCount();
                                        
                    // update performance monitoring
                    monitor.incNewCellTime(System.nanoTime() - cellStartTime);
                } else if (cellRef.hasUpdates(cellMirror)) {
                    for (CellRef.UpdateType update : cellRef.getUpdateTypes()) {
                        switch (update) {
                            case TRANSFORM:
                                // MovableCells manage their own movement over
                                // their cell channel, so this only deals with
                                // non movable cells.
                                if (!cellMirror.isMovableCell()) {
                                    msg = newCellMoveMessage(cellMirror);
                                    sender.send(session, msg);
                                }
                                break;
                            case CONTENT:
                                msg = newContentUpdateCellMessage(cellRef.get(), capabilities);
                                sender.send(session, msg);
                                monitor.incMessageCount();
                                break;
                            case VIEW_CACHE_OPERATION :
                                cellRef.getCellSessionProperties().getViewCellCacheRevalidationListener().cacheRevalidate(view.getTransform());
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
                    msg = newUnloadCellMessage(cell);
                    cell.removeSession(session);
                } catch (ObjectNotFoundException onfe) {
                    // get failed, cell is deleted
                    msg = newDeleteCellMessage(ref.getCellID());
                }
                
                if (AGGREGATE_MESSAGES)
                    messageList.addMessage(msg);
                else
                    sender.send(session, msg);
                monitor.incMessageCount();
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
            
            if (AGGREGATE_MESSAGES && messageList.size()>0) {
                sender.send(session, messageList);
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
            if (RevalidatePerformanceMonitor.printSingle()) {
                logger.info(monitor.getRevalidateStats());
            }
            if (RevalidatePerformanceMonitor.printTotals()) {
                logger.info(RevalidatePerformanceMonitor.getTotals());
                RevalidatePerformanceMonitor.resetTotals();
            }
        }
    }
     
    /**
     * Utility method to mark ourself for update
     */
    private void markForUpdate() {
        AppContext.getDataManager().markForUpdate(this);
    }
    
    /**
     * Utility to get the session
     */
    protected ClientSession getSession() {
        try {
            return sessionRef.get();
        } catch(ObjectNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Information about a cell in our cache.  This object contains our record
     * of the given cell, including a reference to the cell and the cell's
     * id.  CellRef object are compared by cellID, so two CellRefs with the
     * same id are considered equal, regardless of other information.
     */
    private static class CellRef implements Serializable {
        private static final long serialVersionUID = 1L;

        enum UpdateType { TRANSFORM, CONTENT, VIEW_CACHE_OPERATION };
        
        private Set<UpdateType> updateTypes=null;
        
        // the cell id of the cell we reference
        private CellID id;
        
        // a reference to the cell itself
        private ManagedReference<CellMO> cellRef;
        
        private int transformVersion=Integer.MIN_VALUE;
        private int contentsVersion=Integer.MIN_VALUE;
        
        private CellSessionProperties cellSessionProperties;
        
        public CellRef(CellMO cell) {
            this (cell, BoundsManager.get().getCellMirror(cell.getCellID()));
        }
        
        public CellRef(CellMO cell, CellDescription cellMirror) {
            this (cell.getCellID(), cell, cellMirror);
        }
        
        private CellRef(CellID id, CellMO cell, CellDescription cellMirror) {
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
            return cellRef.get();
        }
        
        public CellMO getForUpdate() {
            return cellRef.getForUpdate();
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
        
        public void clearUpdateTypes(CellDescription cellMirror) {
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

        public boolean hasUpdates(CellDescription cellDescription) {
            boolean ret = false;
            if (cellDescription.getTransformVersion()!=transformVersion) {
                addUpdateType(UpdateType.TRANSFORM);
                ret = true;
            }
            if (cellDescription.getContentsVersion()!=contentsVersion) {
                addUpdateType(UpdateType.CONTENT);
                ret = true;
            }
            if (cellSessionProperties!=null && cellSessionProperties.getViewCellCacheRevalidationListener()!=null) {
                addUpdateType(UpdateType.VIEW_CACHE_OPERATION);
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
        
        public CellSessionProperties getCellSessionProperties() {
            return cellSessionProperties;
        }

        public void setCellSessionProperties(CellSessionProperties cellSessionProperties) {
            this.cellSessionProperties = cellSessionProperties;
        }

    }

    /**
     * Return a new Create cell message
     */
    public static CellHierarchyMessage newCreateCellMessage(CellMO cell, ClientCapabilities capabilities) {
        CellID parent=null;
        
        CellMO p = cell.getParent();
        if (p!=null) {
            parent = p.getCellID();
        }
        
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.LOAD_CELL,
            cell.getClientCellClassName(null,capabilities),
            cell.getLocalBounds(),
            cell.getCellID(),
            parent,
            cell.getTransform(),
            cell.getClientSetupData(null, capabilities)
            
            
            
            );
    }
    
    /**
     * Return a new LoadLocalAvatar cell message
     */
    public static CellHierarchyMessage newLoadLocalAvatarMessage(AvatarMO cell, ClientCapabilities capabilities) {
        CellID parent=null;
        
        CellMO p = cell.getParent();
        if (p!=null) {
            parent = p.getCellID();
        }
        
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.LOAD_CLIENT_AVATAR,
            cell.getClientCellClassName(null,capabilities),
            cell.getLocalBounds(),
            cell.getCellID(),
            parent,
            cell.getTransform(),
            cell.getClientSetupData(null, capabilities)
            
            
            
            );
    }
    
    /**
     * Return a new Cell inactive message
     */
    public static CellHierarchyUnloadMessage newUnloadCellMessage(CellMO cell) {
        return new CellHierarchyUnloadMessage(cell.getCellID());
    }
    
    /**
     * Return a new Delete cell message
     */
    public static CellHierarchyMessage newDeleteCellMessage(CellID cellID) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.DELETE_CELL,
            null,
            null,
            cellID,
            null,
            null,
            null
            );
    }
    
    /**
     * Return a new Delete cell message
     */
    public static CellHierarchyMessage newChangeParentCellMessage(CellMO childCell, CellMO parentCell) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.CHANGE_PARENT,
            null,
            null,
            childCell.getCellID(),
            parentCell.getCellID(),
            null,
            null
            
            );
    }
    
    /**
     * Return a new cell move message
     */
    public static CellHierarchyMessage newCellMoveMessage(CellDescription cell) {
        return new CellHierarchyMoveMessage(cell.getLocalBounds(),
            cell.getCellID(),
            cell.getTransform()
            );
    }
    
    /**
     * Return a new cell update message. Indicates that the content of the cell
     * has changed.
     */
    public static CellHierarchyMessage newContentUpdateCellMessage(CellMO cellGLO, ClientCapabilities capabilities) {
        CellID parentID = null;
        if (cellGLO.getParent() != null) {
            parentID = cellGLO.getParent().getCellID();
        }
        
        /* Return a new CellHiearchyMessage class, with populated data fields */
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.UPDATE_CELL_CONTENT,
            cellGLO.getClientCellClassName(null,capabilities),
            cellGLO.getLocalBounds(),
            cellGLO.getCellID(),
            parentID,
            cellGLO.getTransform(),
            cellGLO.getClientSetupData(null, capabilities)    
            );
    }
}

