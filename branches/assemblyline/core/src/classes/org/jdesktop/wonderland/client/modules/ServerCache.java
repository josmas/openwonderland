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

    /* The name of the server machine */
    private String serverName = null;
    
    /* A hashmap of module names and caches of their information */
    private Map<String, CachedModule> cachedModules = new HashMap();
    
    /* The error logger */
    private Logger logger = Logger.getLogger(ServerCache.class.getName());
    
    /** Constructor, takes name of the server */
    public ServerCache(String serverName) {
        this.serverName = serverName;
        this.reload();
    }
    
    /**
     * Returns a collection of cached module names. If no module names exist,
     * returns an empty collection.
     * 
     * @return A collection of cached module names
     */
    public Collection<String> getModuleNames() {
        return this.cachedModules.keySet();
    }
    
    /**
     * Returns a module given its unique name, or null if none exists.
     * 
     * @param moduleName The unique name of the module
     * @return The Module or null if none with the name exists
     */
    public CachedModule getModule(String moduleName) {
        return this.cachedModules.get(moduleName);
    }
    
    /**
     * Reloads the list of modules from the server from scratch
     */
    private synchronized void reload() {
        /* Clear out the existing cache, load from the server */
        this.cachedModules.clear();
        ModuleList list = ModuleUtils.fetchModuleList();
        if (list == null) {
            logger.info("[MODULES] No module information found");
            return;
        }
        
        /* Loop through each and create the module object, insert into map */
        for (ModuleInfo moduleInfo : list.getModuleInfos()) {
            CachedModule cachedModule = new CachedModule(moduleInfo);
            this.cachedModules.put(moduleInfo.getName(), cachedModule);
        }
    }
}
