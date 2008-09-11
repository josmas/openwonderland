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

package org.jdesktop.wonderland.client.modules;

import java.util.HashMap;

/**
 * The ModuleCacheList class stores a map of Wonderland server names and the
 * collection of modules that are installed on the system. For each server name,
 * the module name must be unique. For each module, such information as the
 * basic module info (name, version), the repository information and the
 * collection of assets within the module, and any plugins.
 * <p>
 * Upon connecting (or reconnecting) to a Wonderland server, messages are sent
 * from the server to client upon login to refresh the state of this cache.
 * Since maintaining connections to multiple servers is likely possible in the
 * near future, the cache is not flushed until a client disconnects from the
 * server entirely.
 * <p>
 * This class is multi-thread safe -- multiple threads may attempt to read the
 * module information at the same time.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleCacheList {

    /* A hashtable of server names to their entries */
    private HashMap<String, ModuleCache> moduleCaches = null;

    
    /** Default constructor */
    protected ModuleCacheList() {
        this.moduleCaches = new HashMap<String, ModuleCache>();
    }
   
    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
     * or the first access to SingletonHolder.instance , not before.
     */
    private static class ModuleCacheListHolder {
        private final static ModuleCacheList moduleCache = new ModuleCacheList();
    }
    
    /**
     * Create an instance of this class if one does not exist
     * <p>
     * @return An instance of the ModuleCache class.
     */
    public static final ModuleCacheList getModuleCacheList() {
        return ModuleCacheListHolder.moduleCache;
    }
    
    /**
     * Returns a ModuleCacheServer object for the given server name, creating
     * it if it does not yet exist.
     * <p>
     * @param serverName The unique name of the server
     * @return A ModuleCacheServer object
     */
    public ModuleCache getModuleCache(String serverName) {
        /*
         * Check to see if the server exists within the list, and if not, create
         * a new entry. Make this happens atomically just in case two threads
         * call this method for the same server name at the same time.
         */
        synchronized(this.moduleCaches) {
            ModuleCache mcs = this.moduleCaches.get(serverName);
            if (mcs == null) {
                mcs = new ModuleCache(serverName);
                this.moduleCaches.put(serverName, mcs);
            }
            return mcs;
        }
    }
}
