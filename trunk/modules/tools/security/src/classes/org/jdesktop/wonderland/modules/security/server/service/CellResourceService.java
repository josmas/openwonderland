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
package org.jdesktop.wonderland.modules.security.server.service;

import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.sharedutil.PropertiesWrapper;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.impl.util.TransactionContext;
import com.sun.sgs.impl.util.TransactionContextFactory;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.security.Action;
import org.jdesktop.wonderland.modules.security.common.ActionDTO;
import org.jdesktop.wonderland.modules.security.common.Permission;
import org.jdesktop.wonderland.modules.security.common.Permission.Access;
import org.jdesktop.wonderland.modules.security.common.Principal;
import org.jdesktop.wonderland.modules.security.server.SecurityComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.security.Resource;

/**
 *
 * @author jkaplan
 */
public class CellResourceService extends AbstractService {
    /** The name of this class. */
    private static final String NAME = CellResourceService.class.getName();

    /** The package name. */
    private static final String PKG_NAME = "org.jdesktop.wonderland.modules.security.server.service";

    /** The logger for this class. */
	private static final LoggerWrapper logger =
        new LoggerWrapper(Logger.getLogger(PKG_NAME));

    /** The name of the version key. */
    private static final String VERSION_KEY = PKG_NAME + ".service.version";

    /** The major version. */
    private static final int MAJOR_VERSION = 1;

    /** The minor version. */
    private static final int MINOR_VERSION = 0;

    /** Placeholder for a null resource */
    private static final CellResourceImpl NULL_RESOURCE = new CellResourceImpl("null");

    /** the component registry */
    private ComponentRegistry registry;

    /** manages the context of the current transaction */
    private TransactionContextFactory<CellResourceContext> ctxFactory;

    /** the set of resources, mapped by cell ID */
    private Map<CellID, CellResourceImpl> resourceCache =
            new ConcurrentHashMap<CellID, CellResourceImpl>();

    public CellResourceService(Properties props,
                               ComponentRegistry registry,
                               TransactionProxy proxy)
    {
        super(props, registry, proxy, logger);

        this.registry = registry;

        logger.log(Level.CONFIG, "Creating SecurityService properties: {0}",
                   props);
        PropertiesWrapper wrappedProps = new PropertiesWrapper(props);

        // create the transaction context factory
        ctxFactory = new TransactionContextFactory<CellResourceContext>(proxy, NAME) {
            @Override
            protected CellResourceContext createContext(Transaction txn) {
                return new CellResourceContext(txn);
            }
        };

        try {
            /*
	         * Check service version.
 	         */
            transactionScheduler.runTask(new KernelRunnable() {
                public String getBaseTaskType() {
                    return NAME + ".VersionCheckRunner";
                }

                public void run() {
                    checkServiceVersion(
                            VERSION_KEY, MAJOR_VERSION, MINOR_VERSION);
                }
            }, taskOwner);
        } catch (Exception ex) {
            logger.logThrow(Level.SEVERE, ex, "Error reloading cells");
        }
    }

    public Resource getCellResource(CellID cellID) {
        // check the existing context object for this transaction.  This will
        // first check any local changes we have made, and return either the
        // locally modified version or the cached version.
        CellResourceContext ctx = ctxFactory.joinTransaction();
        CellResourceImpl rsrc = ctx.getResource(cellID);
        if (rsrc != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Found resource " + rsrc + " for " +
                           cellID);
            }

            // we found a resource.  Before we return it, make sure it's not
            // the placeholder for a null
            if (rsrc == NULL_RESOURCE) {
                return null;
            } else {
                return rsrc;
            }
        }

        // if we didn't find the resource in the cache anywhere, recreate
        // it from the cell.
        CellMO cell = CellManagerMO.getCell(cellID);
        SecurityComponentMO sc = cell.getComponent(SecurityComponentMO.class);
        if (sc == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "No security component for cell " +
                           cellID);
            }

            // there is no security component for this cell.  Add a null
            // entry to the cache, so we won't try to look it up every
            // time
            ctx.addResource(cellID, NULL_RESOURCE);
            return null;
        }

        // create a new resource for this cell and add it to the cache
        rsrc = new CellResourceImpl(cellID.toString());
        rsrc.setOwners(sc.getOwners());
        rsrc.setPermissions(sc.getPermissions());
        ctx.addResource(cellID, rsrc);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Created resource for cell " + cellID);
        }

        // return the newly created resource
        return rsrc;
    }

    /**
     * Update a particular resource in the cache.  If there is no entry
     * for this cell in the cache, it will be added.
     * @param cellID the id of the cell to update
     * @param owners the updated owner set
     * @param permissions the update permission set
     */
    public void updateCellResource(CellID cellID, Set<Principal> owners,
                                   SortedSet<Permission> permissions)
    {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Update resource for cell " + cellID);
        }

        CellResourceContext ctx = ctxFactory.joinTransaction();
        CellResourceImpl rsrc = ctx.getResource(cellID);
        if (rsrc == null || rsrc == NULL_RESOURCE) {
            rsrc = new CellResourceImpl(cellID.toString());
            ctx.addResource(cellID, rsrc);

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Update created resource for cell " +
                           cellID);
            }
        }

        rsrc.setOwners(owners);
        rsrc.setPermissions(permissions);
    }

    /**
     * Remove a particular cell from the cache.  It will be reloaded next
     * time a security check is requested.
     * @param cellID the cell id to update
     */
    public void invalidateCellResource(CellID cellID) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Invalidate resource for cell " + cellID);
        }
            
        CellResourceContext ctx = ctxFactory.joinTransaction();
        ctx.removeResource(cellID);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void doReady() {
        logger.log(Level.CONFIG, "Security service ready");
    }

    @Override
    protected void doShutdown() {
        // nothing to do
    }

    @Override
    protected void handleServiceVersionMismatch(Version oldVersion,
                                                Version currentVersion)
    {
        throw new IllegalStateException(
 	            "unable to convert version:" + oldVersion +
	            " to current version:" + currentVersion);
    }

    /**
     * Transaction state
     */
    private class CellResourceContext extends TransactionContext {
        private final Map<CellID, CellResourceRecord> resources =
                new HashMap<CellID, CellResourceRecord>();
        
        public CellResourceContext(Transaction txn) {
            super (txn);
        }

        public void addResource(CellID cellID, CellResourceImpl resource) {
            resources.put(cellID, CellResourceRecord.add(resource));
        }

        public boolean containsResource(CellID cellID) {
            CellResourceRecord rec = resources.get(cellID);
            if (rec == null) {
                return resourceCache.containsKey(cellID);
            }
            
            return (rec.getAction() == CellResourceRecord.Action.ADD);
        }

        public CellResourceImpl getResource(CellID cellID) {
            CellResourceRecord rec = resources.get(cellID);
            if (rec == null) {
                return resourceCache.get(cellID);
            }

            return rec.getResource();
        }

        public void removeResource(CellID cellID) {
            resources.put(cellID, CellResourceRecord.remove());
        }

        @Override
        public void abort(boolean retryable) {
            resources.clear();
        }

        @Override
        public void commit() {
            isCommitted = true;

            for (Entry<CellID, CellResourceRecord> e : resources.entrySet()) {
                switch (e.getValue().getAction()) {
                    case ADD:
                        resourceCache.put(e.getKey(), e.getValue().getResource());
                        break;
                    case REMOVE:
                        resourceCache.remove(e.getKey());
                        break;
                }
            }
        }
    }

    private static class CellResourceRecord {
        private static final CellResourceRecord REMOVE_RECORD =
                new CellResourceRecord(Action.REMOVE, null);

        private enum Action { ADD, REMOVE };

        private Action action;
        private CellResourceImpl rsrc;

        protected CellResourceRecord(Action action, CellResourceImpl rsrc) {
            this.action = action;
            this.rsrc = rsrc;
        }

        public Action getAction() {
            return action;
        }

        public CellResourceImpl getResource() {
            return rsrc;
        }

        public static CellResourceRecord add(CellResourceImpl rsrc) {
            return new CellResourceRecord(Action.ADD, rsrc);
        }

        public static CellResourceRecord remove() {
            return REMOVE_RECORD;
        }
    }

    /**
     * A resource for a particular cell
     */
    private static class CellResourceImpl implements Resource, Serializable {
        private String cellID;
        private SortedSet<Permission> permissions;
        private Set<Principal> owners;

        public CellResourceImpl(String cellID) {
            this.cellID = cellID;
        }

        public void setPermissions(SortedSet<Permission> permissions) {
            this.permissions = permissions;
        }

        public void setOwners(Set<Principal> owners) {
            this.owners = owners;
        }

        public String getId() {
            return CellResourceImpl.class.getName() + "-" + cellID;
        }

        public Result request(WonderlandIdentity identity, Action action) {
            Set<Principal> userPrincipals =
                    UserPrincipals.getUserPrincipals(identity.getUsername(),
                                                     false);
            if (userPrincipals == null) {
                return Result.SCHEDULE;
            } else if (getPermission(userPrincipals, action)) {
                return Result.GRANT;
            } else {
                return Result.DENY;
            }
        }

        public boolean request(WonderlandIdentity identity, Action action,
                               ComponentRegistry registry)
        {
            Set<Principal> userPrincipals =
                    UserPrincipals.getUserPrincipals(identity.getUsername(),
                                                     true);
            return getPermission(userPrincipals, action);
        }

        /**
         * Return true if any of the given principals have the requested
         * permission.
         * @param userPrincipals a set of principals to check.
         * @param action the action to check for.
         * @return true if any of the specified principals have the given
         * permission, or false if the result is denied or undefined.
         */
        protected boolean getPermission(Set<Principal> userPrincipals,
                                        Action action)
        {
            for (Principal p : userPrincipals) {
                // first, check if this principal is an owner
                if (owners.contains(p)) {
                    return true;
                }

                // now see if this particular principal has this permission
                if (getPermission(p, action)) {
                    return true;
                }
            }

            // the permission wasn't found or was denied to all principals
            // for this cell
            return false;
        }

        /**
         * Return whether a principal has the permission for the given action.
         * This will iterate up the action if the given action is not
         * specified but has a parent.
         * @param p the principal to search
         * @param action the action to search for
         * @return true if the given principal has permission for the given
         * action, or false if it is denied or undefined.
         */
        protected boolean getPermission(Principal p, Action action) {
            // construct a prototype permission to search for
            Permission search = new Permission(p, new ActionDTO(action), null);

            // use the sorted set to find the first matching permission.
            // This will correspond to the permission for this user
            // if it is defined.
            Permission perm = null;
            SortedSet<Permission> perms = permissions.tailSet(search);
            if (!perms.isEmpty() && perms.first().equals(search)) {
                perm = perms.first();
            }
            
            // if the permission exists, return its value
            if (perm != null) {
                return (perm.getAccess() == Access.GRANT);
            }

            // If we get here, it means the permission was not specified.
            // If this is a sub-permission, iterate up the tree testing for
            // any parent permissions
            if (action.getParent() != null) {
                return getPermission(p, action.getParent());
            }

            // if we get here, the permission was a top-level permission that
            // was not specified.  Default to deny.
            return false;
        }
    }
}
