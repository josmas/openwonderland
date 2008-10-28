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
package org.jdesktop.wonderland.runner.darkstar;

import org.jdesktop.wonderland.runner.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModulePart;
import org.jdesktop.wonderland.modules.spi.ModuleDeployerSPI;

/**
 * Deploy server and common jars to the Darkstar server
 * @author jkaplan
 */
public class DarkstarModuleDeployer implements ModuleDeployerSPI {
    /** the types of modules we deploy */
    private static final String[] TYPES = { "server", "common" };
   
    /** the files to deploy */
    private static Map<Module, List<ModulePart>> modules = 
            new LinkedHashMap<Module, List<ModulePart>>();

    public String getName() {
        return "Darkstar Server";
    }
    
    /**
     * The types of module we are interested in
     * @return common and server modules
     */
    public String[] getTypes() {
        return TYPES; 
    }
    
     /**
     * Modules can only be deployed when the server is not running.
     * @param type The module part type
     * @param module The module associated with the module part
     * @param part The part of the module to be deployed
     * @return true if the module part can be deployed by the deployer
     */
    public boolean isDeployable(String type, Module module, ModulePart part) {
        // get all darkstar servers, and make sure they are in the 
        // NOT_RUNNING state
        boolean running = false;
        
        for (Runner r : RunManager.getInstance().getAll(DarkstarRunner.class)) {
            if (r.getStatus() != Runner.Status.NOT_RUNNING) {
                running = true;
                break;
            }
        }
        
        return !running;
    }

    /**
     * Modules can only be undeployed when the server is not running
     * @param type The module part type
     * @param module The module associated with the module part
     * @param part The part of the module to be deployed
     * @return true if the module part can be undeployed by the deployer
     */
    public boolean isUndeployable(String type, Module module, ModulePart part) {
         // get all darkstar servers, and make sure they are in the 
        // NOT_RUNNING state
        boolean running = false;
        
        for (Runner r : RunManager.getInstance().getAll(DarkstarRunner.class)) {
            if (r.getStatus() != Runner.Status.NOT_RUNNING) {
                running = true;
                break;
            }
        }
        
        return !running;
    }
     
    /**
     * Deploy the given module part.  This puts the module part on a list
     * to deploy the next time the server is actually started.
     * @param type the type of module
     * @param module the module
     * @param part the part of the module
     */
    public void deploy(String type, Module module, ModulePart part) 
    {
        synchronized (modules) {
            List<ModulePart> parts = modules.get(module);
            if (parts == null) {
                parts = new ArrayList<ModulePart>();
                modules.put(module, parts);
            }
            
            parts.add(part);
        }
        
    }

    /**
     * Undeploy the given module part.  This removes the module part from
     * the list of modules to deploy.
     * @param type the type of module
     * @param module the module
     * @param part the part of the module
     */
    public void undeploy(String type, Module module, ModulePart part) 
    {
        synchronized (modules) {
            List<ModulePart> parts = modules.get(module);
            if (parts != null) {
                parts.remove(part);
                
                if (parts.isEmpty()) {
                    modules.remove(module);
                }
            }   
        }
    } 
    
    /**
     * Return the modules and parts that are currently installed.
     * @return the set of modules that are available
     */
    public static Map<Module, List<ModulePart>> getModules() {
        synchronized (modules) {
            return Collections.unmodifiableMap(modules);
        }
    }   
}
