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
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.modules.ModuleChecksums;
import org.jdesktop.wonderland.common.modules.ModuleRepository;

/**
 * The ModuleCache class represents the cache associated with a single server,
 * each identified by a unique name. This class stores the identity of each
 * module installed on the server, the repository information, and the checksum
 * information for each module.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleCache {

    /* The base URL of the web server */
    private String serverURL = null;
    
    /* Maps of the unique module names and info (identity, checksum, etc). */
    private HashMap<String, ModuleRepository> repositories = new HashMap();
    private HashMap<String, ModuleChecksums> checksums = new HashMap();
    
    /* The error logger */
    private static Logger logger = Logger.getLogger(ModuleCache.class.getName());
    
    /** Constructor, takes the unique URL of the web server */
    public ModuleCache(String serverURL) {
        this.serverURL = serverURL;
    }
    
    /**
     * Given the unique name of the module, returns the object representing the
     * the module checksum information, or null if the module does not exist or
     * upon some general I/O error.
     * <p>
     * @param uniqueName The unique name of the module
     * @return The module checksum information
     */
    public ModuleChecksums getModuleChecksums(String uniqueName) {
        /*
         * First check to see whether the information already exists, and if
         * so, return it.
         */
        synchronized (this.checksums) {
            ModuleChecksums checksums = this.checksums.get(uniqueName);
            if (checksums == null) {
                logger.info("[MODULE] Fetching checksums for " + uniqueName);
            
                /*
                 * Otherwise, load in the module checksums from the server. If
                 * we cannot, then add the module name to the list of names
                 * not found and return null.
                 */
                checksums = ModuleUtils.fetchModuleChecksums(serverURL, uniqueName);
                if (checksums == null) {
                    return null;
                }
                
                /* If the module checksums does exist, add it and return */
                this.checksums.put(uniqueName, checksums);
            }
            return checksums;
        }
    }
    
    /**
     * Given the unique name of the module, returns the object representing the
     * the module repository information, or null if the module does not exist
     * or upon some general I/O error.
     * <p>
     * @param uniqueName The unique name of the module
     * @return The module repository information
     */
    public ModuleRepository getModuleRepository(String uniqueName) {
        /*
         * First check to see whether the information already exists, and if
         * so, return it.
         */
        synchronized (this.repositories) {
            ModuleRepository ModuleRepository = this.repositories.get(uniqueName);
            if (ModuleRepository == null) {
                logger.info("[MODULE] Loading Repository list for module " + uniqueName);
            
                /*
                 * Otherwise, load in the module repositories from the server. If
                 * we cannot, then add the module name to the list of names
                 * not found and return null.
                 */
                ModuleRepository = ModuleUtils.fetchModuleModuleRepository(serverURL, uniqueName);
                if (ModuleRepository == null) {
                    return null;
                }
                
                /* If the module checksums does exist, add it and return */
                this.repositories.put(uniqueName, ModuleRepository);
            }
            return ModuleRepository;
        }
    }
}
