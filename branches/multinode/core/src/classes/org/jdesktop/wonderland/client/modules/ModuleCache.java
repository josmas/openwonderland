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
import org.jdesktop.wonderland.modules.ModuleInfo;
import org.jdesktop.wonderland.modules.ModuleRepository;

/**
 * The ModuleCache class represents the cache associated with a single server,
 * each identified by a unique name. This class stores the identity of each
 * module installed on the server, the resourc repository and plugin info
 * assocaiated with each.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleCache {

    /* The unique name of the server associated with this cache */
    private String serverName = null;
    
    /* A map of unique module names and their identity information */
    private HashMap<String, ModuleInfo> moduleInfos = null;
    
    /* A map of unique module names and their repository information */
    private HashMap<String, ModuleRepository> moduleRepositories = null;
    
    /** Constructor, takes the unique name of the server */
    public ModuleCache(String serverName) {
        this.serverName = serverName;
        this.moduleInfos = new HashMap<String, ModuleInfo>();
        this.moduleRepositories = new HashMap<String, ModuleRepository>();
    }
    
    /**
     * Adds a new module info to the map, given the unique module name and
     * its basic information. If the information is already contained in the
     * map, then this method replaces the existing information.
     * <p>
     * @param uniqueName The unique name of the module
     * @param moduleInfo The module's basic information
     */
    // XXX synchronization?
    public void addModuleInfo(String uniqueName, ModuleInfo moduleInfo) {
        this.moduleInfos.put(uniqueName, moduleInfo);
    }
    
    /**
     * Given the unique name of the module, returns the basic information
     * about the module, or null if it does not exist.
     * <p>
     * @param uniqueName The unique name of the module
     * @return The module's basic information, null if it does not exist
     */
    public ModuleInfo getModuleInfo(String uniqueName) {
        return this.moduleInfos.get(uniqueName);
    }
    
    /**
     * Adds a new module repository to the map, given the unique module name and
     * its repository information. If the information is already contained in the
     * map, then this method replaces the existing information.
     * <p>
     * @param uniqueName The unique name of the module
     * @param moduleRepository The module's repository information
     */
    // XXX synchronization?
    public void addModuleRepository(String uniqueName, ModuleRepository moduleRepository) {
        this.moduleRepositories.put(uniqueName,  moduleRepository);
    }
    
    /**
     * Given the unique name of the module, returns the repository information
     * about the module, or null if it does not exist.
     * <p>
     * @param uniqueName The unique name of the module
     * @return The module's repository information, null if it does not exist
     */
    public ModuleRepository getModuleRepository(String uniqueName) {
        return this.moduleRepositories.get(uniqueName);
    }   
}
