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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.kernel.ComponentRegistry;
import java.util.Properties;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.security.Action;
import org.jdesktop.wonderland.server.cell.view.ViewCellMO;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyUnloadMessage;
import org.jdesktop.wonderland.common.cell.security.ViewAction;
import org.jdesktop.wonderland.common.messages.MessageList;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.security.ActionMap;
import org.jdesktop.wonderland.server.security.Resource;
import org.jdesktop.wonderland.server.security.ResourceMap;
import org.jdesktop.wonderland.server.security.SecurityManager;
import org.jdesktop.wonderland.server.security.SecureTask;
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
    
    protected ManagedReference<ViewCellMO> viewRef;

    // If this Set becomes large we may want to move it into it's own managed object
    // so we don't pay the penalty of serialization when processing avatar moves
    protected Set<CellID> loaded = new HashSet<CellID>();
    
    protected WonderlandClientSender sender;
    protected WonderlandClientID clientID;
    
    protected WonderlandIdentity identity;

    protected ClientCapabilities capabilities = null;
         
    private PeriodicTaskHandle task = null;
    
    // handle revalidates
    protected RevalidateScheduler scheduler;
    
    // whether or not to aggregate messages
    private static final boolean AGGREGATE_MESSAGES = true;
            
    private HashSet<ViewCellCacheRevalidationListener> revalidationsListeners = new HashSet();

    private Properties connectionProperties = new Properties();
    private static final String INITIAL_POSITION_PROP_PREFIX = "view.initial.";

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
    public void login(WonderlandClientSender sender, WonderlandClientID clientID) {
        this.sender = sender;
        this.clientID = clientID;

        ViewCellMO view = viewRef.get();

        // see if there is an initial position specified
        CellTransform xform = getInitialPosition();
        if (xform != null) {
            view.setLocalTransform(xform);
        }

        if (!view.isLive()) {
            try {
                WonderlandContext.getCellManager().insertCellInWorld(view);
            } catch (MultipleParentException ex) {
                Logger.getLogger(ViewCellCacheMO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        UniverseManagerFactory.getUniverseManager().viewLogin(view);
        
        logger.info("AvatarCellCacheMO.login() CELL CACHE LOGIN FOR USER "
                    + clientID.getSession().getName() + " AS " + identity.getUsername());
                        
        // set up the revalidate scheduler
        scheduler = new ImmediateRevalidateScheduler(sender, clientID);
    }
    
    /**
     * Notify CellCache that user has logged out
     */
    protected void logout(WonderlandClientID clientID) {
        logger.warning("DEBUG - logout");
        ViewCellMO view = viewRef.get();
        UniverseManagerFactory.getUniverseManager().viewLogout(view);
        WonderlandContext.getCellManager().removeCellFromWorld(view);
    }
     

    public void generateLoadMessagesService(Collection<CellDescription> cells) {
        // check if this user has permission to view the cells in this
        // collection, and then generate load messages for any that we
        // do have permission for
        CellResourceManager crm = AppContext.getManager(CellResourceManager.class);
        SecurityManager security = AppContext.getManager(SecurityManager.class);
        ResourceMap rm = new ResourceMap();

        // cells we have permission for (since they don't have a resource)
        Map<CellID, CellDescription> granted = new HashMap<CellID, CellDescription>();

        // cells we need to check for permission
        Map<CellID, CellDescription> check = new HashMap<CellID, CellDescription>();

        // get the resource for each cell and add it to the appropriate map
        for (CellDescription cell : cells) {
            Resource resource = crm.getCellResource(cell.getCellID());
            if (resource == null) {
                // don't need to check this cell
                granted.put(cell.getCellID(), cell);
            } else {
                Resource r = new CellIDResource(cell.getCellID(), resource);
                rm.put(r.getId(), new ActionMap(r, new ViewAction()));

                // do check this cell
                check.put(cell.getCellID(), cell);
            }
        }

        // see if we need to check any of the cells
        if (check.size() > 0) {
            // we do need to do this securely -- start a task
            SecureTask checkLoad = new LoadCellsTask(check, granted, this);
            security.doSecure(rm, checkLoad);
        } else {
            // just send the messages directly
            sendLoadMessages(cells);
        }
    }

    /**
     * Get the initial position for this view based on the connection
     * properties
     * @return the initial transform, or null if there is no initial 
     * transform
     */
    protected CellTransform getInitialPosition() {
        float transX = 0f;
        float transY = 0f;
        float transZ = 0f;

        String transXStr = connectionProperties.getProperty(INITIAL_POSITION_PROP_PREFIX + "x");
        if (transXStr != null) {
            transX = Float.parseFloat(transXStr);
        }

        String transYStr = connectionProperties.getProperty(INITIAL_POSITION_PROP_PREFIX + "y");
        if (transYStr != null) {
            transY = Float.parseFloat(transYStr);
        }

        String transZStr = connectionProperties.getProperty(INITIAL_POSITION_PROP_PREFIX + "z");
        if (transZStr != null) {
            transZ = Float.parseFloat(transZStr);
        }

        Vector3f translation = new Vector3f(transX, transY, transZ);

        float rotX = 0f;
        float rotY = 0f;
        float rotZ = 0f;

        String rotXStr = connectionProperties.getProperty(INITIAL_POSITION_PROP_PREFIX + "rotx");
        if (rotXStr != null) {
            rotX = Float.parseFloat(rotXStr);
        }

        String rotYStr = connectionProperties.getProperty(INITIAL_POSITION_PROP_PREFIX + "roty");
        if (rotYStr != null) {
            rotY = Float.parseFloat(rotYStr);
        }

        String rotZStr = connectionProperties.getProperty(INITIAL_POSITION_PROP_PREFIX + "rotz");
        if (rotZStr != null) {
            rotZ = Float.parseFloat(rotZStr);
        }

        Quaternion rotate = new Quaternion();
        rotate.fromAngles(rotX, rotY, rotZ);

        return new CellTransform(rotate, translation);
    }

    private static final class LoadCellsTask implements SecureTask, Serializable {
        private Map<CellID, CellDescription> check;
        private Map<CellID, CellDescription> granted;
        private ManagedReference<ViewCellCacheMO> viewCellCacheRef;

        public LoadCellsTask(Map<CellID, CellDescription> check,
                             Map<CellID, CellDescription> granted,
                             ViewCellCacheMO viewCellCache)
        {
            this.check = check;
            this.granted = granted;

            viewCellCacheRef = AppContext.getDataManager().createReference(viewCellCache);
        }

        public void run(ResourceMap grants) {
            // go through and move any cells that have been ok'd into the
            // granted list
            for (ActionMap am : grants.values()) {
                // the resource is OK'dif the view action is granted
                if (am.size() == 1) {
                    CellID id = ((CellIDResource) am.getResource()).getCellID();
                    CellDescription desc = check.get(id);
                    granted.put(id, desc);
                }
            }

            // now send a load message with all the granted cells
            ViewCellCacheMO cache = viewCellCacheRef.getForUpdate();
            cache.sendLoadMessages(granted.values());
        }
    }

    /**
     * Update our cache because the given cells may have changed
     * @param cells the cells to revalidate
     */
    public void revalidateCellsService(Collection<CellDescription> cells) {
        // check if this user has permission to view the cells in this
        // collection, and then generate load messages for any that we
        // do have permission for
        CellResourceManager crm = AppContext.getManager(CellResourceManager.class);
        SecurityManager security = AppContext.getManager(SecurityManager.class);
        ResourceMap rm = new ResourceMap();

        // cells we need to check for permission
        Map<CellID, CellDescription> check = new HashMap<CellID, CellDescription>();

        // get the resource for each cell and add it to the appropriate map
        for (CellDescription cell : cells) {
            Resource resource = crm.getCellResource(cell.getCellID());
            if (resource != null) {
                Resource r = new CellIDResource(cell.getCellID(), resource);
                rm.put(r.getId(), new ActionMap(r, new ViewAction()));

                // do check this cell
                check.put(cell.getCellID(), cell);
            } 
        }

        // see if we need to check any of the cells
        if (check.size() > 0) {
            // we do need to do this securely -- start a task
            SecureTask checkCells = new RevalidateCellsTask(check, this);
            security.doSecure(rm, checkCells);
        } else {
            // Nothing to do, no security changes for these cells.
        }
    }

    private static final class RevalidateCellsTask implements SecureTask, Serializable {
        private Map<CellID, CellDescription> check;
        private ManagedReference<ViewCellCacheMO> viewCellCacheRef;

        public RevalidateCellsTask(Map<CellID, CellDescription> check,
                                  ViewCellCacheMO viewCellCache)
        {
            this.check = check;
            viewCellCacheRef = AppContext.getDataManager().createReference(viewCellCache);
        }

        public void run(ResourceMap grants) {
            List<CellDescription> load = new LinkedList<CellDescription>();
            List<CellDescription> unload = new LinkedList<CellDescription>();
            ViewCellCacheMO cache = viewCellCacheRef.get();

            // go through and look at each cell to see if its granted or denied
            for (ActionMap am : grants.values()) {
                CellID id = ((CellIDResource) am.getResource()).getCellID();
                CellDescription desc = check.get(id);

                // the resource is OK'd if the view action is granted
                if (am.size() == 1 && !cache.isLoaded(id)) {
                    load.add(desc);
                } else if (am.size() == 0 && cache.isLoaded(id)) {
                    unload.add(desc);
                }
            }

            // now send any messages
            cache.sendLoadMessages(load);
            cache.sendUnloadMessages(unload);
        }
    }

    protected void sendLoadMessages(Collection<CellDescription> cells) {


        ManagedReference<ViewCellCacheMO> viewCellCacheRef =
                AppContext.getDataManager().createReference(this);

        scheduler.startRevalidate();
        for(CellDescription cellDescription : cells) {
            // if we haven't already loaded the cell, send a message
            if (loaded.add(cellDescription.getCellID())) {
               
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Entering cell " + cellDescription.getCellID() +
                                 " cellcache for user " + identity.getUsername());
                }

                CellLoadOp op = new CellLoadOp(cellDescription, clientID,
                                               viewCellCacheRef, capabilities);
                scheduler.schedule(op);
            }
        }
        scheduler.endRevalidate();
    }

    private boolean isLoaded(CellID cellID) {
        return loaded.contains(cellID);
    }

    public void sendUnloadMessages(Collection<CellDescription> removeCells) {
        ManagedReference<ViewCellCacheMO> viewCellCacheRef =
                AppContext.getDataManager().createReference(this);


        scheduler.startRevalidate();
        // oldCells contains the set of cells to be removed from client memory
//        System.err.println("-- Loaded size "+loaded.size());
        for(CellDescription ref : removeCells) {
//            System.err.println("UNLOADING "+ref.getCellID());
            if (loaded.remove(ref.getCellID())) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.fine("Leaving cell " + ref.getCellID() +
                                " cellcache for user "+identity.getUsername());
                }

                // schedule the remove operation
                CellUnloadOp op = new CellUnloadOp(ref, clientID,
                                                   viewCellCacheRef,
                                                   capabilities);
                scheduler.schedule(op);
            }
        }
        scheduler.endRevalidate();
    }

    void setConnectionProperties(Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    /**
     * Return the property from the connection handler for this cache.
     * The properties are set when the client creates the connection
     * 
     * @param key
     * @return
     */
    public String getConnectionProperty(String key) {
        if (connectionProperties==null)
            return null;

        return connectionProperties.getProperty(key);
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
    protected static abstract class CellOp
            implements Serializable, Runnable 
    {
        protected CellDescription desc;
        protected WonderlandClientID clientID;
        protected ManagedReference<? extends ViewCellCacheMO> viewCellCacheRef;
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
                      ManagedReference<? extends ViewCellCacheMO> viewCellCacheRef,
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
    public static class CellLoadOp extends CellOp {
        public CellLoadOp(CellDescription desc,
                         WonderlandClientID clientID,
                         ManagedReference<? extends ViewCellCacheMO> viewCellCacheRef,
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

            if (logger.isLoggable(Level.FINER))
                logger.finer("Sending NEW CELL to Client: " + cell.getCellID().toString()+"  "+cell.getClass().getName());
            sendMessage(newCreateCellMessage(cell, prop));
        }
    }
    
    /**
     * Operation to remove a cell from the list of cached cells
     */
    protected static class CellUnloadOp extends CellOp {
        public CellUnloadOp(CellDescription desc,
                         WonderlandClientID clientID,
                         ManagedReference<? extends ViewCellCacheMO> viewCellCacheRef,
                         ClientCapabilities capabilities) {
            super (desc, clientID, viewCellCacheRef, capabilities);
        }
        
        public void run() {
            CellHierarchyMessage msg;
            CellMO cell=null;
                    
            // the cell may be inactive or removed.  Try to get the cell,
            // and catch the exception if it no longer exists.
            try {
                cell = CellManagerMO.getCellManager().getCell(desc.getCellID());

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

            if (logger.isLoggable(Level.FINER)) {
                logger.finer("SENDING Unload to Client "+msg.getCellID().toString()+"  "+(cell!=null ? cell.getClass().getName() : "null"));
            }
            sendMessage(msg);

        }
    }
    
    /**
     * A revalidate scheduler defines how the various revalidate operations
     * are managed.  Some schedulers will perform the operations immediately,
     * while others will try to batch them up in a single task.
     */
    public interface RevalidateScheduler {
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
    public class ImmediateRevalidateScheduler
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

    private static class CellIDResource implements Resource, Serializable {
        private CellID cellID;
        private Resource wrapped;

        public CellIDResource(CellID cellID, Resource wrapped) {
            this.cellID = cellID;
            this.wrapped = wrapped;
        }

        public CellID getCellID() {
            return cellID;
        }

        public String getId() {
            return wrapped.getId();
        }

        public Result request(WonderlandIdentity identity, Action action) {
            return wrapped.request(identity, action);
        }

        public boolean request(WonderlandIdentity identity, Action action,
                               ComponentRegistry registry)
        {
            return wrapped.request(identity, action, registry);
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

