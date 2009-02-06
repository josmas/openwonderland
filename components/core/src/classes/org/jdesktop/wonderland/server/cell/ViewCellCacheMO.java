/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.server.cell;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.cell.view.ViewCellMO;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Quaternion;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.AvatarBoundsHelper;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyUnloadMessage;
import org.jdesktop.wonderland.common.messages.MessageList;
import org.jdesktop.wonderland.server.CellAccessControl;
import org.jdesktop.wonderland.server.TimeManager;
import org.jdesktop.wonderland.server.UserSecurityContextMO;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.spatial.UniverseManagerFactory;

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
    
    private ManagedReference<ViewCellMO> viewRef;
    private ManagedReference<UserSecurityContextMO> securityContextRef;
    
    private WonderlandClientSender sender;
    private WonderlandClientID clientID;
    
    private WonderlandIdentity identity;

    private ClientCapabilities capabilities = null;
         
    private PeriodicTaskHandle task = null;
    
    // handle revalidates
    private RevalidateScheduler scheduler;
    
    // whether or not to aggregate messages
    private static final boolean AGGREGATE_MESSAGES = true;
            
    private HashSet<ViewCellCacheRevalidationListener> revalidationsListeners = new HashSet();
    
    /**
     * Creates a new instance of ViewCellCacheMO
     */
    public ViewCellCacheMO(ViewCellMO view) {
        logger.config("Creating ViewCellCache");
        
        DataManager dm = AppContext.getDataManager();
        viewRef = dm.createReference(view);

        identity = view.getUser().getIdentity();
        
//        dm.setBinding(identity.getUsername() + "_CELL_CACHE", this);
    }
    
    /**
     * Notify CellCache that user has logged in
     */
    void login(WonderlandClientSender sender, WonderlandClientID clientID) {
        this.sender = sender;
        this.clientID = clientID;

        ViewCellMO view = viewRef.get();

        if (!view.isLive()) {
            try {
                WonderlandContext.getCellManager().insertCellInWorld(view);
            } catch (MultipleParentException ex) {
                Logger.getLogger(ViewCellCacheMO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        UniverseManagerFactory.getUniverseManager().viewLogin(view);

        UserSecurityContextMO securityContextMO = view.getUser().getUserSecurityContext();
        if (securityContextMO!=null)
            securityContextRef = AppContext.getDataManager().createReference(securityContextMO);
        else
            securityContextRef = null;

        
        logger.info("AvatarCellCacheMO.login() CELL CACHE LOGIN FOR USER "
                    + clientID.getSession().getName() + " AS " + identity.getUsername());
                
        // Setup the Root Cell on the client
        CellHierarchyMessage msg;
//        CellMO rootCell = CellManagerMO.getCell(CellManagerMO.getRootCellID());
//        msg = newCreateCellMessage(rootCell, capabilities);
//        sender.send(session, msg);
        
        // set up the revalidate scheduler
        scheduler = new ImmediateRevalidateScheduler(sender, clientID);

    }
    
    /**
     * Notify CellCache that user has logged out
     */
    void logout(WonderlandClientID clientID) {
        logger.warning("DEBUG - logout");
        ViewCellMO view = viewRef.get();
        UniverseManagerFactory.getUniverseManager().viewLogout(view);
        WonderlandContext.getCellManager().removeCellFromWorld(view);
    }
     

    public void generateLoadMessagesService(Collection<CellDescription> cellInfoList) {
        ManagedReference<ViewCellCacheMO> viewCellCacheRef = AppContext.getDataManager().createReference(this);
        scheduler.startRevalidate();
        for(CellDescription cellDescription : cellInfoList) {
            // find the cell in our current list of cells
            // check this client's access to the cell
            if (securityContextRef!=null && !CellAccessControl.canView(securityContextRef.get(), cellDescription)) {
                // the user doesn't have access to this cell -- just skip
                // it and go on
                continue;
            }

                if (logger.isLoggable(Level.FINER))
                    logger.finer("Entering cell " + cellDescription.getCellID() +
                                 " cellcache for user "+identity.getUsername());

                CellLoadOp op = new CellLoadOp(cellDescription,
                                             clientID,
                                             viewCellCacheRef,
                                             capabilities);
                scheduler.schedule(op);
        }
        scheduler.endRevalidate();
    }
    
    public void generateUnloadMessagesService(Collection<CellDescription> removeCells) {
        ManagedReference<ViewCellCacheMO> viewCellCacheRef = AppContext.getDataManager().createReference(this);
        scheduler.startRevalidate();
        // oldCells contains the set of cells to be removed from client memory
        for(CellDescription ref : removeCells) {
            if (logger.isLoggable(Level.FINER))
                logger.fine("Leaving cell " + ref.getCellID() +
                             " cellcache for user "+identity.getUsername());

            // schedule the add operation
            CellUnloadOp op = new CellUnloadOp(ref,
                                               clientID,
                                               viewCellCacheRef,
                                               capabilities);
            scheduler.schedule(op);
        }
        scheduler.endRevalidate();
    }


    private void addRevalidationListener(ViewCellCacheRevalidationListener listener) {
        // Called from the scheduler via a reference so does not need synchronization
        revalidationsListeners.add(listener);
    }
    
    private void removeRevalidationListener(ViewCellCacheRevalidationListener listener) {
        // Called from the scheduler via a reference so does not need synchronization
        revalidationsListeners.remove(listener);
    }
    
    /**
     * Utility to get the session
     */
    protected ClientSession getSession() {
        try {
            return clientID.getSession();
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
        protected WonderlandClientID clientID;
        protected ManagedReference<ViewCellCacheMO> viewCellCacheRef;
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
                      WonderlandClientID clientID,
                      ManagedReference<ViewCellCacheMO> viewCellCacheRef,
                      ClientCapabilities capabilities) 
        {
            this.desc = desc;
            this.clientID = clientID;
            this.viewCellCacheRef = viewCellCacheRef;
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
                sender.send(clientID, message);
            }
        }
    }
    
    /**
     * Operation to add a cell to the set of cached cells
     */
    private static class CellLoadOp extends CellOp {
        public CellLoadOp(CellDescription desc,
                         WonderlandClientID clientID,
                         ManagedReference<ViewCellCacheMO> viewCellCacheRef,
                         ClientCapabilities capabilities) {
            super (desc, clientID, viewCellCacheRef, capabilities);
        }
        
        public void run() {
            // the cell is new -- add it and send a message
            CellMO cell = CellManagerMO.getCell(desc.getCellID());
                          
            //System.out.println("SENDING "+msg.getActionType()+" "+msg.getBytes().length);
            CellSessionProperties prop = cell.addClient(clientID, capabilities);
            
            ViewCellCacheRevalidationListener listener = prop.getViewCellCacheRevalidationListener();
            if (listener!=null) {
                viewCellCacheRef.getForUpdate().addRevalidationListener(listener);
            }
//            cellRef.setCellSessionProperties(prop);
                    
            logger.fine("Sending NEW CELL to Client: " + cell.getCellID().toString()+"  "+cell.getClass().getName());
            sendMessage(newCreateCellMessage(cell, prop));
        }
    }
    
    /**
     * Operation to remove a cell from the list of cached cells
     */
    private static class CellUnloadOp extends CellOp {
        public CellUnloadOp(CellDescription desc,
                         WonderlandClientID clientID,
                         ManagedReference<ViewCellCacheMO> viewCellCacheRef,
                         ClientCapabilities capabilities) {
            super (desc, clientID, viewCellCacheRef, capabilities);
        }
        
        public void run() {
            CellHierarchyMessage msg;
                    
            // the cell may be inactive or removed.  Try to get the cell,
            // and catch the exception if it no longer exists.
            try {
                CellMO cell = CellManagerMO.getCellManager().getCell(desc.getCellID());

                cell.removeSession(clientID);

                ViewCellCacheRevalidationListener listener = cell.getViewCellCacheRevalidationListener();
                if (listener!=null) {
                    viewCellCacheRef.getForUpdate().removeRevalidationListener(listener);
                }
            
                // get suceeded, so cell is just inactive
                msg = newUnloadCellMessage(cell);
                cell.removeSession(clientID);
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
        
        // the client to send to
        private WonderlandClientID clientID;

        // the message list
        private MessageList messageList;
        
        
        public ImmediateRevalidateScheduler(WonderlandClientSender sender,
                                            WonderlandClientID clientID)
        {
            this.sender = sender;
            this.clientID = clientID;
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
                sender.send(clientID, messageList);
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
                tm.scheduleTask(new SharedListRevalidateTask(sender, clientID,
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
        private WonderlandClientID clientID;
        private ManagedReference<List<CellOp>> opsRef;
        private int count;
        private MessageList messageList;
        
        public SharedListRevalidateTask(WonderlandClientSender sender,
                                        WonderlandClientID clientID,
                                        int count, 
                                        ManagedReference<List<CellOp>> opsRef)
        {
            this.sender = sender;
            this.clientID = clientID;
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
                sender.send(clientID, messageList);
            }
            
            // schedule a task to handle more
            if (num > 0) {
                TaskManager tm = AppContext.getTaskManager();
                tm.scheduleTask(new SharedListRevalidateTask(sender, clientID,
                                                             count, opsRef));
            }
        }
    }
    

    /**
     * Return a new Create cell message
     */
    public static CellHierarchyMessage newCreateCellMessage(CellMO cell, CellSessionProperties properties) {
        CellID parent=null;
        
        CellMO p = cell.getParent();
        if (p!=null) {
            parent = p.getCellID();
        }
        
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.LOAD_CELL,
            properties.getClientCellClassName(),
            cell.getLocalBounds(),
            cell.getCellID(),
            parent,
            cell.getLocalTransform(null),
            properties.getClientCellSetup(),
            cell.getName()
            
            
            );
    }
    
    /**
     * Return a new LoadLocalAvatar cell message
     */
    public static CellHierarchyMessage newLoadLocalAvatarMessage(CellMO cell, CellSessionProperties properties) {
        CellID parent=null;
        
        CellMO p = cell.getParent();
        if (p!=null) {
            parent = p.getCellID();
        }
        
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.LOAD_CLIENT_AVATAR,
            properties.getClientCellClassName(),
            cell.getLocalBounds(),
            cell.getCellID(),
            parent,
            cell.getLocalTransform(null),
            properties.getClientCellSetup(),
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
     * Return a new cell update message. Indicates that the content of the cell
     * has changed.
     */
    public static CellHierarchyMessage newConfigureCellMessage(CellMO cellMO, ClientCapabilities capabilities) {
        CellID parentID = null;
        if (cellMO.getParent() != null) {
            parentID = cellMO.getParent().getCellID();
        }
        
        /* Return a new CellHiearchyMessage class, with populated data fields */
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.CONFIGURE_CELL,
            cellMO.getClientCellClassName(null,capabilities),
            cellMO.getLocalBounds(),
            cellMO.getCellID(),
            parentID,
            cellMO.getLocalTransform(null),
            cellMO.getClientState(null, null, capabilities),
            cellMO.getName()
            
            );
    }
}

