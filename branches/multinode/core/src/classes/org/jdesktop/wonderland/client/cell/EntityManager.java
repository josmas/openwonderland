/**
 * Project Wonderland
 *
 * $Id$
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
 */
package org.jdesktop.wonderland.client.cell;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jdesktop.wonderland.ExperimentalAPI;

/**
 * Global services for client entities. The manager will only
 * report information on currently loaded EntityCells.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class EntityManager {

    private static EntityManager entityManager = null;
    
    private Set<EntityListener> entityListeners=Collections.synchronizedSet(new LinkedHashSet());            
;
    
    private EntityManager() {
    }
    
    public static EntityManager getEntityManager() {
        if (entityManager==null)
            entityManager = new EntityManager();
        return entityManager;
    } 
    
    /**
     * Called by the EntityCell to notify the system that it has moved.
     * @param cell
     * @param fromServer
     */
    void notifyEntityMoved(EntityCell cell, boolean fromServer) {
        for(EntityListener listener : entityListeners)
            listener.entityMoved(cell, fromServer);
    }
    
    /**
     * Add a listener that will be notified of entity cell movement
     * @param listener
     */
    public synchronized void addEntityListener(EntityListener listener) {
        entityListeners.add(listener);
    }
    
    /**
     * Remove the specified entity cell listener
     * @param listener
     */
    public synchronized void removeEntityListener(EntityListener listener) {
        entityListeners.remove(listener);
    }
    
    @ExperimentalAPI
    public interface EntityListener {
        /**
         * Notification that an entity has moved. 
         * @param cell the entity cell that moved
         * @param fromServer, if true then the move came from the server, otherwise
         * the move originated on this client
         */
        public void entityMoved(EntityCell cell, boolean fromServer);
    }
}
