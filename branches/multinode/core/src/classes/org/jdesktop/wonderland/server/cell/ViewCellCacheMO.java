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
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.AvatarBoundsHelper;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMoveMessage;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyUnloadMessage;
import org.jdesktop.wonderland.common.messages.MessageList;
import org.jdesktop.wonderland.server.CellAccessControl;
import org.jdesktop.wonderland.server.UserSecurityContextMO;
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
    private ManagedReference<Map<CellID, CellRef>> currentCellsRef;
    
    private PeriodicTaskHandle task = null;
    
    // handle revalidates
    private RevalidateScheduler scheduler;
    
    // whether or not to aggregate messages
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
        
        Map<CellID, CellRef> currentCells = new ManagedHashMap<CellID, CellRef>();
        currentCells.put(rootCellID, new CellRef(rootCell));
        currentCellsRef = dm.createReference(currentCells);
        
        // set up the revalidate scheduler
        scheduler = new ImmediateRevalidateScheduler(sender, session);
        
        // schedule a task for revalidating
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
        
        DataManager dm = AppContext.getDataManager();
        dm.removeObject(getCurrentCells());
        
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
        if (session == null) {
            logger.warning("Null session, have not seen a logout");
            return;
        }
        
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
            Collection<CellRef> oldCells = new ArrayList(getCurrentCells().values());
        
            // notify the schduler
            scheduler.startRevalidate();
            
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
                CellRef cellRef = getCurrentCells().get(cellID);           
                if (cellRef == null) {
                    // schedule the add operation
                    CellAddOp op = new CellAddOp(cellRef, cellMirror,
                                                 currentCellsRef, sessionRef,
                                                 capabilities);
                    scheduler.schedule(op);
                    
                    // monitor.incMessageCount();
                                        
                    // update performance monitoring
                    monitor.incNewCellTime(System.nanoTime() - cellStartTime);
                } else if (cellRef.hasUpdates(cellMirror)) {
                    for (CellRef.UpdateType update : cellRef.getUpdateTypes()) {
                        
                        // schedule the update operation
                        CellUpdateOp op = new CellUpdateOp(update, view.getTransform(),
                                                           cellRef, cellMirror,
                                                           currentCellsRef, sessionRef,
                                                           capabilities);
                        scheduler.schedule(op);
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
                
                // schedule the add operation
                CellRemoveOp op = new CellRemoveOp(ref, null,
                                                   currentCellsRef, sessionRef,
                                                   capabilities);
                scheduler.schedule(op);
                //markForUpdate();
                
                // update the monitor
                monitor.incOldCellTime(System.nanoTime() - cellStartTime);
            }
            
            scheduler.endRevalidate();
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
     * Utility to get the list of managed cells
     */
    protected Map<CellID, CellRef> getCurrentCells() {
        return currentCellsRef.get();
    }
    
    private static class ManagedHashMap<K, V> extends HashMap<K, V> implements ManagedObject {}
    private static class ManagedLinkedList<E> extends LinkedList<E> implements ManagedObject {}
            
    /**
     * Superclass of operations to modify the list of cached cells.  Operations
     * include adding, removing or updating the list of cells.
     */
    private static abstract class CellOp 
            implements Serializable, Runnable 
    {
        protected CellRef cellRef;
        protected CellDescription desc;
        protected ManagedReference<Map<CellID, CellRef>> cellsRef;
        protected ManagedReference<ClientSession> sessionRef;
        protected ClientCapabilities capabilities;
        
        // optional message list.  If the list is not null, messages will
        // be added to the list instead of sent immediately.  This is
        // set by the RevalidateScheduler prior to calling run()
        private MessageList messageList;
        
        // optional sender.  If the sender is not null, messages will be
        // sent immediately.  This is set by the RevalidateScheduler
        // prior to calling run.
        private WonderlandClientSender sender;
    
        public CellOp(CellRef cellRef,
                      CellDescription desc,
                      ManagedReference<Map<CellID, CellRef>> cellsRef,
                      ManagedReference<ClientSession> sessionRef, 
                      ClientCapabilities capabilities) 
        {
            this.cellRef = cellRef;
            this.desc = desc;
            this.cellsRef = cellsRef;
            this.sessionRef = sessionRef;
            this.capabilities = capabilities;
        }
        
        public void setMessageList(MessageList messageList) {
            this.messageList = messageList;
        }
        
        public void setClientSender(WonderlandClientSender sender) {
            this.sender = sender;
        }
        
        protected void sendMessage(CellHierarchyMessage message) {
            if (messageList != null) {
                // if there is a message list, use it to aggregate messages
                messageList.addMessage(message);
            } else {
                // no list, send immediately
                sender.send(sessionRef.get(), message);
            }
        }
    }
    
    /**
     * Operation to add a cell to the set of cached cells
     */
    private static class CellAddOp extends CellOp {
        public CellAddOp(CellRef cellRef,
                         CellDescription desc,
                         ManagedReference<Map<CellID, CellRef>> cellsRef,
                         ManagedReference<ClientSession> sessionRef, 
                         ClientCapabilities capabilities)
        {
            super (cellRef, desc, cellsRef, sessionRef, capabilities);
        }
        
        public void run() {
            // the cell is new -- add it and send a message
            CellMO cell = CellManagerMO.getCell(desc.getCellID());
            cellRef = new CellRef(cell, desc);
            cellsRef.getForUpdate().put(desc.getCellID(), cellRef);
                          
            //System.out.println("SENDING "+msg.getActionType()+" "+msg.getBytes().length);
            CellSessionProperties prop = cell.addSession(sessionRef.get(), capabilities);
            cellRef.setCellSessionProperties(prop);
                    
            sendMessage(newCreateCellMessage(cell, capabilities));
        }
    }
    
    /**
     * Operation to update the list of cached cells
     */
    private static class CellUpdateOp extends CellOp {
        private CellRef.UpdateType update;
        private CellTransform transform;
        
        public CellUpdateOp(CellRef.UpdateType update,
                            CellTransform transform,
                            CellRef cellRef,
                            CellDescription desc,
                            ManagedReference<Map<CellID, CellRef>> cellsRef,
                            ManagedReference<ClientSession> sessionRef, 
                            ClientCapabilities capabilities)
        {
            super (cellRef, desc, cellsRef, sessionRef, capabilities);
        
            this.update = update;
            this.transform = transform;
        }
        
        public void run() {
            CellHierarchyMessage msg;
            
            switch (update) {
                case TRANSFORM:
                    // MovableCells manage their own movement over
                    // their cell channel, so this only deals with
                    // non movable cells.
                    if (!desc.isMovableCell()) {
                        sendMessage(newCellMoveMessage(desc));
                    }
                    break;
                case CONTENT:
                    sendMessage(newContentUpdateCellMessage(cellRef.get(), capabilities));
                    break;
                case VIEW_CACHE_OPERATION:
                    cellRef.getCellSessionProperties().getViewCellCacheRevalidationListener().cacheRevalidate(transform);
                    break;
            }
        }
    }
    
    /**
     * Operation to remove a cell from the list of cached cells
     */
    private static class CellRemoveOp extends CellOp {
        public CellRemoveOp(CellRef cellRef,
                         CellDescription desc,
                         ManagedReference<Map<CellID, CellRef>> cellsRef,
                         ManagedReference<ClientSession> sessionRef, 
                         ClientCapabilities capabilities)
        {
            super (cellRef, desc, cellsRef, sessionRef, capabilities);
        }
        
        public void run() {
            CellHierarchyMessage msg;
                    
            // the cell may be inactive or removed.  Try to get the cell,
            // and catch the exception if it no longer exists.
            try {
                CellMO cell = cellRef.get();

                // get suceeded, so cell is just inactive
                msg = newUnloadCellMessage(cell);
                cell.removeSession(sessionRef.get());
            } catch (ObjectNotFoundException onfe) {
                // get failed, cell is deleted
                msg = newDeleteCellMessage(cellRef.getCellID());
            }

            sendMessage(msg);
            //System.out.println("SENDING "+msg.getClass().getName()+" "+msg.getBytes().length);

            // the cell is no longer visible on this client, so remove
            // our current reference to it.  This client will no longer
            // receive any updates about the given cell, including future
            // deletes.  This implies that the client must clear out
            // its cache of inactive cells periodically, as some of them
            // may have been deleted.
            // TODO periodically clean out client cell cache
            cellsRef.getForUpdate().remove(cellRef.getCellID());
        }
    }
    
    /**
     * A revalidate scheduler defines how the various revalidate operations
     * are managed.  Some schedulers will perform the operations immediately,
     * while others will try to batch them up in a single task.
     */
    private interface RevalidateScheduler {
        public void startRevalidate();
        public void schedule(CellOp op);
        public void endRevalidate();
    }
    
    /**
     * Do nothing.  This will break the system, but is good for testing by
     * ignoring the updates.
     */
    private class NoopRevalidateScheduler
            implements RevalidateScheduler, Serializable
    {
        public void startRevalidate() {}
        public void schedule(CellOp op) {}
        public void endRevalidate() {}
    }
    
    /**
     * Perform all revalidate operations immediately in this task.
     */
    private class ImmediateRevalidateScheduler 
            implements RevalidateScheduler, Serializable 
    {
        // the sender to send to
        private WonderlandClientSender sender;
        
        // a reference to the client session
        private ManagedReference<ClientSession> sessionRef;
        
        // the message list
        private MessageList messageList;
        
        
        public ImmediateRevalidateScheduler(WonderlandClientSender sender,
                                            ClientSession session)
        {
            this.sender = sender;
            
            DataManager dm = AppContext.getDataManager();
            sessionRef = dm.createReference(session);
        }
        
        public void startRevalidate() {
            if (AGGREGATE_MESSAGES) {
                messageList = new MessageList();
            }
        }
        
        public void schedule(CellOp op) {
            if (AGGREGATE_MESSAGES) {
                op.setMessageList(messageList);
            } else {
                op.setClientSender(sender);
            }
            
            op.run();
        }
        
        public void endRevalidate() {
            if (AGGREGATE_MESSAGES) {                
                sender.send(sessionRef.get(), messageList);
            }
        }
    }
    
    /**
     * Write revalidate requests to a shared list of operations to run.
     * Schedule a task to read the list and perform some number of operations.
     * The count variable in the constructor controls how many operations
     * each task should consume before scheduling another task to complete
     * the remaining operations.
     */
    private class SharedListRevalidateScheduler 
            implements RevalidateScheduler, Serializable 
    {
        // the sender to send to
        private WonderlandClientSender sender;
        
        // a reference to the client session
        private ManagedReference<ClientSession> sessionRef;
        
        // the number of tasks to consume during each run
        private int count;
        
        // a reference to the shared list of operations
        private ManagedReference<List<CellOp>> opsRef;
        
        public SharedListRevalidateScheduler(WonderlandClientSender sender,
                                             ClientSession session,
                                             int count)
        {
            this.sender = sender;
            this.count = count;
            
            // create managed references
            DataManager dm = AppContext.getDataManager();
            List<CellOp> opsList = new ManagedLinkedList<CellOp>();
            opsRef = dm.createReference(opsList);
            sessionRef = dm.createReference(session);
        }
        
        public void startRevalidate() {    
        }
        
        public void schedule(CellOp op) {
            opsRef.getForUpdate().add(op);
        }

        public void endRevalidate() {            
            // schedule tasks to handle up to count operations
            if (opsRef.get().size() > 0) {
                TaskManager tm = AppContext.getTaskManager();
                tm.scheduleTask(new SharedListRevalidateTask(sender, sessionRef,
                                                             count, opsRef));
            }
        }
    }
    
    /**
     * A task to dequeue the next operations from the shared list and
     * execute them.
     */
    private static class SharedListRevalidateTask
            implements Task, Serializable
    {
        private WonderlandClientSender sender;
        private ManagedReference<ClientSession> sessionRef;
        private ManagedReference<List<CellOp>> opsRef;
        private int count;
        private MessageList messageList;
        
        public SharedListRevalidateTask(WonderlandClientSender sender,
                                        ManagedReference<ClientSession> sessionRef,
                                        int count, 
                                        ManagedReference<List<CellOp>> opsRef)
        {
            this.sender = sender;
            this.sessionRef = sessionRef;
            this.count = count;
            this.opsRef = opsRef;
        }

        public void run() throws Exception {
            List<CellOp> ops = opsRef.get();
            
            if (AGGREGATE_MESSAGES) {
                messageList = new MessageList();
            }
            
            int num = Math.min(ops.size(), count);
            for (int i = 0; i < num; i++) {
                CellOp op = ops.remove(0);
                
                if (AGGREGATE_MESSAGES) {
                    op.setMessageList(messageList);
                } else {
                    op.setClientSender(sender);
                }
                
                op.run();
            }
            
            // send all messages
            if (AGGREGATE_MESSAGES) {
                sender.send(sessionRef.get(), messageList);
            }
            
            // schedule a task to handle more
            if (num > 0) {
                TaskManager tm = AppContext.getTaskManager();
                tm.scheduleTask(new SharedListRevalidateTask(sender, sessionRef,
                                                             count, opsRef));
            }
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

