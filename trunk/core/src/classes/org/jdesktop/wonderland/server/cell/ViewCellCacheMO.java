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
import org.jdesktop.wonderland.server.TimeManager;
import org.jdesktop.wonderland.server.UserSecurityContextMO;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellListMO.ListInfo;
import org.jdesktop.wonderland.server.cell.CellMO.SpaceInfo;
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
     * List of currently active static cells
     */
    private ManagedReference<Map<CellID, CellDescription>> currentStaticCellsRef;
    /**
     * List of currently active dynamic cells
     */
    private ManagedReference<Map<CellID, CellDescription>> currentDynamicCellsRef;
    
    private PeriodicTaskHandle task = null;
    
    // handle revalidates
    private RevalidateScheduler scheduler;
    
    // whether or not to aggregate messages
    private static final boolean AGGREGATE_MESSAGES = false;
    
    private Collection<ManagedReference<SpaceMO>> proximitySpaces=null;
    private ManagedReference<SpaceMO> currentSpaceRef = null;
    private CellListMO allCells = new CellListMO();
    
    private long lastRevalidationTimestamp;     // Last time we did a validation
    private BoundingSphere proximityBounds;
    
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
        
        lastRevalidationTimestamp=0;
        if (allCells.size()!=0) {
            logger.warning("User login, allCells is not empty !");
        }
        
        DataManager dm = AppContext.getDataManager();
        sessionRef = dm.createReference(session);
        
        logger.info("AvatarCellCacheMO.login() CELL CACHE LOGIN FOR USER "
                    + session.getName() + " AS " + username);
                
        // Setup the Root Cell on the client
        CellHierarchyMessage msg;
        CellMO rootCell = CellManagerMO.getCell(rootCellID);
        msg = newCreateCellMessage(rootCell, capabilities);
        sender.send(session, msg);
        
        Map<CellID, CellDescription> currentStaticCells = new ManagedHashMap<CellID, CellDescription>();
        currentStaticCellsRef = dm.createReference(currentStaticCells);
        Map<CellID, CellDescription> currentDynamicCells = new ManagedHashMap<CellID, CellDescription>();
        currentDynamicCellsRef = dm.createReference(currentDynamicCells);
        
        // set up the revalidate scheduler
        scheduler = new ImmediateRevalidateScheduler(sender, session);
        
        // schedule a task for revalidating
        if (CacheManager.USE_CACHE_MANAGER) {
            CacheManager.addCache(this);            
        } else {
            // Periodically revalidate the cache
             task = AppContext.getTaskManager().schedulePeriodicTask(
                    new ViewCellCacheRevalidateTask(this), 1000, 2000);  // Start delay, duration
        }
        
        View view = viewRef.get();
        Vector3f translation = view.getWorldTransform().getTranslation(null);
        proximityBounds = AvatarBoundsHelper.getProximityBounds(translation);            
    }
    
    /**
     * Notify CellCache that user has logged out
     */
    void logout(ClientSession session) {
        logger.warning("DEBUG - logout");
        
        allCells.clear();
        
        if (CacheManager.USE_CACHE_MANAGER) {
            CacheManager.removeCache(this);
        } else {
            task.cancel();
            task=null;
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
            logger.warning("Null session, have not seen a logout - terminating ViewCellCache for user");
            logout(null);
            return;
        }
        
        revalidateFromSpaces(session);
    }

    /**
     * @param session
     */    
    void revalidateFromSpaces(ClientSession session) {
        // create a performance monitor
        RevalidatePerformanceMonitor monitor = new RevalidatePerformanceMonitor();
        long startTime = System.nanoTime();
        
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Revalidating CellCache for   " + 
                          session.getName());
        }
        
        try {
            // getTranslation the current user's bounds
            View view = viewRef.get();
            Vector3f translation = view.getWorldTransform().getTranslation(null);
            proximityBounds.setCenter(translation);            
            
            CellListMO dynamicCellList = new CellListMO();
            
            // notify the schduler
            scheduler.startRevalidate();
            
            // TODO - only supports a single current space at the moment, should
            // support multiple current spaces
            SpaceMO space = CellManagerMO.getCellManager().getEnclosingSpace(translation)[0];
//            System.out.println("Current SPACE "+space.getSpaceID());
            if (AppContext.getDataManager().createReference(space)!=currentSpaceRef) {
//                System.err.println("Full revalidation "+space.position);
                // copy the existing cells into the list of old cells 
                CellListMO oldCells = (CellListMO) allCells.clone();
                CellListMO currentCells = new CellListMO();  // Cells found during this revalidation
                
                proximitySpaces = space.getSpaces(proximityBounds);
                currentSpaceRef = AppContext.getDataManager().createReference(space);
                CacheStats staticStats = new CacheStats();
                CellListMO staticCellList = space.getStaticCells(proximitySpaces, proximityBounds, currentCells, staticStats);   
                System.out.println("Static ");
                staticStats.report();
               
                generateLoadMessages(staticCellList);
                                
                CacheStats dynamicStats = new CacheStats();
                // Get all dynamics cells, no matter when they were last updated
                dynamicCellList = space.getDynamicCells(proximitySpaces, proximityBounds, currentCells, dynamicStats);                 
                System.out.println("Dynamic ");
                dynamicStats.report();
                System.out.println("----------------------------");
                generateLoadMessages(dynamicCellList);
                
                Collection<CellDescription> descList = currentCells.getCells();
                for(CellDescription desc : descList)
                    oldCells.removeCell(desc);
                generateUnloadMessages(oldCells);
                oldCells.clear();
            } else {           
                // Get only the dynamic cells that have been updated since our last revalidation
                CacheStats dynamicStats = new CacheStats();
                dynamicCellList = space.getDynamicCells(proximitySpaces, proximityBounds, null, dynamicStats, lastRevalidationTimestamp);
                System.out.println("Dynamic ");
                dynamicStats.report();
                System.out.println("----------------------------");
                generateLoadMessages(dynamicCellList);
            }
                   
            lastRevalidationTimestamp = TimeManager.getWonderlandTime();
            
            scheduler.endRevalidate();
            
        } catch(RuntimeException e) {
            monitor.setException(true);
            
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Rethrowing exception", e.getMessage());
            }
            
            throw e;
        } finally {
            monitor.incTotalTime(System.nanoTime() - startTime);
            monitor.updateTotals();
        }
    }
    
    private void generateLoadMessages(CellListMO visCellList) {
        
        Collection<CellDescription> cellInfoList = visCellList.getCells();
        for(CellDescription cellDescription : cellInfoList) {
            // find the cell in our current list of cells

            // check this client's access to the cell
            if (securityContextRef!=null && !CellAccessControl.canView(securityContextRef.get(), cellDescription)) {
                // the user doesn't have access to this cell -- just skip
                // it and go on                
                continue;
            }

            if (!allCells.contains(cellDescription)) {
                // schedule the add operation

                if (logger.isLoggable(Level.FINER)) 
                    logger.finer("Entering cell " + cellDescription.getCellID() +
                                 " cellcache for user "+username);
                
                allCells.addCell(cellDescription);
                CellLoadOp op = new CellLoadOp(cellDescription,
                                             sessionRef,
                                             capabilities);
                scheduler.schedule(op);
             } else {
                if (logger.isLoggable(Level.FINER)) 
                    logger.finer("Cell already visible " + cellDescription.getCellID() +
                                 " cellcache for user "+username);
                
            }
        }
    }
    
    private void generateUnloadMessages(CellListMO removeList) {

        Collection<CellDescription> removeCells = removeList.getCells();
        // oldCells contains the set of cells to be removed from client memory
        for(CellDescription ref : removeCells) {
            if (logger.isLoggable(Level.FINER)) 
                logger.finer("Leaving cell " + ref.getCellID() +
                             " cellcache for user "+username);
            
            allCells.removeCell(ref);
            // schedule the add operation
            CellUnloadOp op = new CellUnloadOp(ref,
                                               sessionRef,
                                               capabilities);
            scheduler.schedule(op);
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
        
    private static class ManagedHashMap<K, V> extends HashMap<K, V> implements ManagedObject {}
    private static class ManagedLinkedList<E> extends LinkedList<E> implements ManagedObject {}
            
    /**
     * Superclass of operations to modify the list of cached cells.  Operations
     * include adding, removing or updating the list of cells.
     */
    private static abstract class CellOp 
            implements Serializable, Runnable 
    {
        protected CellDescription desc;
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
    
        public CellOp(CellDescription desc,
                      ManagedReference<ClientSession> sessionRef, 
                      ClientCapabilities capabilities) 
        {
            this.desc = desc;
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
    private static class CellLoadOp extends CellOp {
        public CellLoadOp(CellDescription desc,
                         ManagedReference<ClientSession> sessionRef, 
                         ClientCapabilities capabilities) {
            super (desc, sessionRef, capabilities);
        }
        
        public void run() {
            // the cell is new -- add it and send a message
            CellMO cell = CellManagerMO.getCell(desc.getCellID());
                          
            //System.out.println("SENDING "+msg.getActionType()+" "+msg.getBytes().length);
            CellSessionProperties prop = cell.addSession(sessionRef.get(), capabilities);
//            cellRef.setCellSessionProperties(prop);
                    
            sendMessage(newCreateCellMessage(cell, capabilities));
        }
    }
    
    /**
     * Operation to remove a cell from the list of cached cells
     */
    private static class CellUnloadOp extends CellOp {
        public CellUnloadOp(CellDescription desc,
                         ManagedReference<ClientSession> sessionRef, 
                         ClientCapabilities capabilities)
        {
            super (desc, sessionRef, capabilities);
        }
        
        public void run() {
            CellHierarchyMessage msg;
                    
            // the cell may be inactive or removed.  Try to get the cell,
            // and catch the exception if it no longer exists.
            try {
                CellMO cell = CellManagerMO.getCellManager().getCell(desc.getCellID());

                // get suceeded, so cell is just inactive
                msg = newUnloadCellMessage(cell);
                cell.removeSession(sessionRef.get());
            } catch (ObjectNotFoundException onfe) {
                // get failed, cell is deleted
                msg = newDeleteCellMessage(desc.getCellID());
            }

            sendMessage(msg);
            //System.out.println("SENDING "+msg.getClass().getName()+" "+msg.getBytes().length);

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
            cell.getLocalTransform(),
            cell.getClientSetupData(null, capabilities),
            cell.getName()
            
            
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
            cell.getLocalTransform(),
            cell.getClientSetupData(null, capabilities),
            cell.getName()
            
            
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
            null,
            null
            );
    }
    
    /**
     * Return a new Delete cell message
     */
//    public static CellHierarchyMessage newChangeParentCellMessage(CellMO childCell, CellMO parentCell) {
//        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.CHANGE_PARENT,
//            null,
//            null,
//            childCell.getCellID(),
//            parentCell.getCellID(),
//            null,
//            null,
//            null
//            
//            );
//    }
    
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
    public static CellHierarchyMessage newContentUpdateCellMessage(CellMO cellMO, ClientCapabilities capabilities) {
        CellID parentID = null;
        if (cellMO.getParent() != null) {
            parentID = cellMO.getParent().getCellID();
        }
        
        /* Return a new CellHiearchyMessage class, with populated data fields */
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.UPDATE_CELL_CONTENT,
            cellMO.getClientCellClassName(null,capabilities),
            cellMO.getLocalBounds(),
            cellMO.getCellID(),
            parentID,
            cellMO.getLocalTransform(),
            cellMO.getClientSetupData(null, capabilities),
            cellMO.getName()
            );
    }
}

