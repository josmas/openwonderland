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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import org.jdesktop.wonderland.checksum.RepositoryChecksums;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleInfo;
import org.jdesktop.wonderland.modules.ModuleRepository;
import org.jdesktop.wonderland.modules.memory.MemoryModule;

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
    
    /* A map of unique module names and the module */
    private HashMap<String, Module> modules = null;

    /* A hashmap of module names already searched and not found */
    private HashSet<String> modulesNotFound = null;
    
    /** Constructor, takes the unique name of the server */
    public ModuleCache(String serverName) {
        this.serverName = serverName;
        this.modules = new HashMap<String, Module>();
        this.modulesNotFound = new HashSet<String>();
    }
    
    /**
     * Given the unique name of the module, returns the object representing the
     * the module, or null if it does not exist.
     * <p>
     * @param uniqueName The unique name of the module
     * @return The module, null if it does not exist
     */
    public Module getModule(String uniqueName) {
        Module module = this.modules.get(uniqueName);
        if (module == null) {
            /*
             * If the module does not exist, see if we have already checked
             * (so that we don't repeatedly ping the server), and only try
             * to load the module's information if we haven't checked 
             * previously.
             */
            if (this.modulesNotFound.contains(uniqueName) == true) {
                // log an error xxx
                return null;
            }
            
            /* Otherwise, load in the module from the server */
            this.modulesNotFound.add(uniqueName);
            module = new MemoryModule();
            
            /* Fetch its basic information, return null upon error */
            ModuleInfo info = this.fetchModuleInfo(uniqueName);
            if (info == null) {
                // Log an error xxx
                return null;
            }
            System.out.println("INFO: " + info.toString());
            
            /* Fetch the repository information, return null upon error */
            ModuleRepository repository = this.fetchModuleRepository(uniqueName);
            if (repository == null) {
                // Log an error XXX
                return null;
            }
            System.out.println("REPOSITORY: " + repository.toString());
            
            /* Fetch the checksum information for the module */
            RepositoryChecksums checksums = this.fetchModuleChecksums(uniqueName);
            
            module.setModuleInfo(info);
            module.setModuleRepository(repository);
            module.setModuleChecksums(checksums);
            
            this.modules.put(uniqueName, module);
        }           
        return module;
    }
    
    /**
     * Asks the server for the module's basic information given its name, returns
     * null upon error
     */
    private ModuleInfo fetchModuleInfo(String uniqueName) {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            String baseURL = "http://localhost:9998/module/";
            URL url = new URL(baseURL + uniqueName + "/info");
            InputStream is = url.openStream();
            
            /* Try to parse the xml stream and return a ModuleInfo class */
            System.out.println("FETCH: " + url.toString());
            ModuleInfo mi = ModuleInfo.decode(new InputStreamReader(is));
            return mi;
        } catch (java.net.MalformedURLException excp) {
            System.out.println(excp.toString());
            return null;
        } catch (java.io.IOException excp) {
            // log an error
            System.out.println(excp.toString());
            return null;
        } catch (javax.xml.bind.JAXBException excp) {
            // log an error
            System.out.println(excp.toString());
            return null;
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
            return null;
        }
    }
    
    /**
     * Asks the server for the module's repository information given its name,
     * returns null upon error
     */
    private ModuleRepository fetchModuleRepository(String uniqueName) {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            String baseURL = "http://localhost:9998/module/";
            URL url = new URL(baseURL + uniqueName + "/repository");
            InputStream is = url.openStream();
            
            /* Try to parse the xml stream and return a ModuleInfo class */
            ModuleRepository mr = ModuleRepository.decode(new InputStreamReader(is));
            return mr;
        } catch (java.net.MalformedURLException excp) {
            // log an error
            return null;
        } catch (java.io.IOException excp) {
            // log an error
            return null;
        } catch (javax.xml.bind.JAXBException excp) {
            // log an error
            return null;
        }
    }
    
    /**
     * Asks the server for the module's checksum information given its name,
     * returns null upon error
     */
    private RepositoryChecksums fetchModuleChecksums(String uniqueName) {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            String baseURL = "http://localhost:9998/module/";
            URL url = new URL(baseURL + uniqueName + "/checksums");
            InputStream is = url.openStream();
            
            /* Try to parse the xml stream and return a RepositoryChecksums class */
            RepositoryChecksums rc = RepositoryChecksums.decode(new InputStreamReader(is));
            return rc;
        } catch (java.net.MalformedURLException excp) {
            // log an error
            return null;
        } catch (java.io.IOException excp) {
            // log an error
            return null;
        } catch (javax.xml.bind.JAXBException excp) {
            // log an error
            return null;
        }
    }
}
