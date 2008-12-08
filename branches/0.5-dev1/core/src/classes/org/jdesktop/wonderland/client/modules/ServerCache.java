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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.modules.ModuleInfo;
import org.jdesktop.wonderland.common.modules.ModuleList;

/**
 * A cache of all modules on a particular server
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ServerCache {

    /* The base url of the server: http://<server name>:<port> */
    private String serverURL = null;
    
    /* A hashmap of module names and caches of their information */
    private Map<String, CachedModule> cachedModules = new HashMap();
    
    /* The error logger */
    private Logger logger = Logger.getLogger(ServerCache.class.getName());
    
    /* A hashmap of base server urls and their ServerCache objects */
    private static Map<String, ServerCache> serverCaches = new HashMap();
    
    /** Constructor, takes base URL  of the server */
    public ServerCache(String serverURL) {
        this.serverURL = serverURL;
        this.reload();
    }
    
    /**
     * Returns an instance of the ServerCache object for the given server URL
     * (http://<server name>:<port>). If the cache for the server does not
     * exist yet, creates it.
     *
     * @param serverURL The base URL of the server
     * @return The cache of information pertaining to the server
     */
    public static ServerCache getServerCache(String serverURL) {
        synchronized (serverCaches) {
            ServerCache cache = serverCaches.get(serverURL);
            if (cache == null) {
                cache = new ServerCache(serverURL);
                serverCaches.put(serverURL, cache);
            }
            return cache;
        }
    }
    
    /**
     * Returns a collection of cached module names. If no module names exist,
     * returns an empty collection.
     * 
     * @return A collection of cached module names
     */
//    public Collection<String> getModuleNames() {
//        return this.cachedModules.keySet();
//    }
    
    /**
     * Returns a module given its unique name, or null if none exists.
     * 
     * @param moduleName The unique name of the module
     * @return The Module or null if none with the name exists
     */
    public CachedModule getModule(String moduleName) {
        // Look for the cached module. If we can't find it, see if it exists
        // on the server nevertheless.
        CachedModule cm = this.cachedModules.get(moduleName);
        if (cm == null) {
            ModuleInfo info = ModuleUtils.fetchModuleInfo(this.serverURL, moduleName);
            if (info == null) {
                logger.info("[MODULES] No module information found for " + moduleName);
                return null;
            }
            cm = new CachedModule(serverURL, info);
            this.cachedModules.put(moduleName, cm);
        }
        return cm;
    }
    
    /**
     * Reloads the list of modules from the server from scratch
     */
    private synchronized void reload() {
        /* Clear out the existing cache, load from the server */
        this.cachedModules.clear();
        ModuleList list = ModuleUtils.fetchModuleList(this.serverURL);
        if (list == null) {
            logger.info("[MODULES] No module information found");
            return;
        }
        
        /* Loop through each and create the module object, insert into map */
        for (ModuleInfo moduleInfo : list.getModuleInfos()) {
            CachedModule cachedModule = new CachedModule(serverURL, moduleInfo);
            this.cachedModules.put(moduleInfo.getName(), cachedModule);
        }
    }
}
