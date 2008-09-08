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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * The ModuleCache class represents the cache associated with a single server,
 * each identified by a unique name. This class stores the identity of each
 * module installed on the server, the repository information, and the checksum
 * information for each module.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleCache {

    /* The unique name of the server associated with this cache */
    private String serverName = null;
    
    /* Maps of the unique module names and info (identity, checksum, etc). */
    private HashMap<String, ModuleIdentity> identities = new HashMap();
    private HashMap<String, RepositoryList> repositories = new HashMap();
    private HashMap<String, ModuleChecksums> checksums = new HashMap();

    /* A hashmap of module names already searched and not found */
    private Set<String> modulesNotFound = null;
    
    /** Constructor, takes the unique name of the server */
    public ModuleCache(String serverName) {
        this.serverName = serverName;
        
        /*
         * This needs to be synchronized since we do not protected it below,
         * that is, each of the methods may access this Set at the same time
         * in any one of the methods below
         */
        this.modulesNotFound = Collections.synchronizedSet(new HashSet<String>());
    }
    
    /**
     * Given the unique name of the module, returns the object representing the
     * the module identity, or null if the module does not exist or upon some
     * general I/O error.
     * <p>
     * @param uniqueName The unique name of the module
     * @return The module identity
     */
    public ModuleIdentity getModuleIdentity(String uniqueName) {
        /*
         * First check to see whether the information already exists, and if
         * so, return it.
         */
        synchronized (this.identities) {
            ModuleIdentity identity = this.identities.get(uniqueName);
            if (identity == null) {
                /*
                 * If the module does not exist, see if we have already checked
                 * (so that we don't repeatedly ping the server), and only try
                 * to load the module's information if we haven't checked 
                 * previously.
                 */
                if (this.modulesNotFound.contains(uniqueName) == true) {
                    return null;
                }
            
                /*
                 * Otherwise, load in the module identity from the server. If
                 * we cannot, then add the module name to the list of names
                 * not found and return null.
                 */
                identity = ModuleUtils.fetchModuleIdentity(uniqueName);
                if (identity == null) {
                    this.modulesNotFound.add(uniqueName);
                    return null;
                }
                
                /* If the module identity does exist, add it and return */
                this.identities.put(uniqueName, identity);
            }
            return identity;
        }
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
                /*
                 * If the module does not exist, see if we have already checked
                 * (so that we don't repeatedly ping the server), and only try
                 * to load the module's checksums if we haven't checked 
                 * previously.
                 */
                if (this.modulesNotFound.contains(uniqueName) == true) {
                    return null;
                }
            
                /*
                 * Otherwise, load in the module checksums from the server. If
                 * we cannot, then add the module name to the list of names
                 * not found and return null.
                 */
                checksums = ModuleUtils.fetchModuleChecksums(uniqueName);
                if (checksums == null) {
                    this.modulesNotFound.add(uniqueName);
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
    public RepositoryList getModuleRepositoryList(String uniqueName) {
        /*
         * First check to see whether the information already exists, and if
         * so, return it.
         */
        synchronized (this.repositories) {
            RepositoryList repositoryList = this.repositories.get(uniqueName);
            if (repositoryList == null) {
                /*
                 * If the module does not exist, see if we have already checked
                 * (so that we don't repeatedly ping the server), and only try
                 * to load the module's repository list if we haven't checked 
                 * previously.
                 */
                if (this.modulesNotFound.contains(uniqueName) == true) {
                    return null;
                }
            
                /*
                 * Otherwise, load in the module repositories from the server. If
                 * we cannot, then add the module name to the list of names
                 * not found and return null.
                 */
                repositoryList = ModuleUtils.fetchModuleRepositoryList(uniqueName);
                if (repositoryList == null) {
                    this.modulesNotFound.add(uniqueName);
                    return null;
                }
                
                /* If the module checksums does exist, add it and return */
                this.repositories.put(uniqueName, repositoryList);
            }
            return repositoryList;
        }
    }
}
