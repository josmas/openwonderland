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
package org.jdesktop.wonderland.modules.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.spi.ModuleDeployerSPI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModulePart;
import sun.misc.Service;

/**
 * The DeployManager is responsible for deploying modules during installation.
 * It loads the a list of deployers (each implement ModuleDeployerSPI) and
 * deploys and undeploys modules to/from them.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class DeployManager {
    /* A set of deployer objects */
    private HashSet<ModuleDeployerSPI> deployers = new HashSet();
    
    /* The error logger */
    private Logger logger = Logger.getLogger(DeployManager.class.getName());
    
    /**
     * Constructor
     */
    public DeployManager() {
        /*
         * Initialize the list of deployers. For each found, create a deployer
         * object and put into the set of deployer objects
         */
        Class[] clazzes = this.getClasses();
        for (Class clazz : clazzes) {
            try {
                ModuleDeployerSPI deployer = (ModuleDeployerSPI) clazz.newInstance();
                this.deployers.add(deployer);
            } catch (InstantiationException ex) {
                logger.log(Level.WARNING, "[DEPLOY] INSTANTIATE", ex);
            } catch (IllegalAccessException ex) {
                logger.log(Level.WARNING, "[DEPLOY] INSTANTIATE", ex);
            }
        }
    }
    
    /**
     * Deploys a given module to the proper deployers. The contract between the
     * module manager and the deployers is that once the deployers say that a
     * module may be deployed to it, the deployment should not fail. If it does
     * then DeployerException is thrown.
     * 
     * @param module The module to deploy
     * @throw DeployerException If the module cannot be deployed
     */
    public void deploy(Module module) throws DeployerException {
        logger.info("[DEPLOY] Deploying module " + module.getName());
        
        /*
         * If the deployment fails for one deployer, then keep an exception
         * around for it.
         */
        DeployerException deployException = null;
        
        /*
         * Fetch all of the deployers and iterate through them. For each of
         * the parts they support, find whether the module contains such a
         * part. If so, deploy the module.
         */
        Map<String, ModulePart> parts = module.getParts();
        Iterator<ModuleDeployerSPI> it = this.deployers.iterator();
        logger.info("[DEPLOY] Module has parts " + parts.keySet().toString());
        logger.info("[DEPLOY] Number of Deployers " + this.deployers.size());
        while (it.hasNext() == true) {
            /*
             * Fetch the module part types that the deployer supports. If none,
             * just continue to the next module.
             */
            ModuleDeployerSPI deployer = it.next();
            String[] partTypes = deployer.getTypes();
            if (partTypes == null) {
                continue;
            }
            
            /*
             * Loop through each part type and see if there is a module part
             */
            for (String partType : partTypes) {
                if (parts.containsKey(partType) == true) {
                    try {
                        logger.info("[DEPOY] Deploying " + module.getName() + " to " + deployer.getName());
                        deployer.deploy(partType, module, parts.get(partType));
                    } catch (java.lang.Exception excp) {
                        /*
                         * Catch all exceptions here. Report them and pass them
                         * up as DeployerException, but continue to
                         * the remainder of the deployers.
                         */
                        logger.log(Level.WARNING, "[DEPLOY] Failed", excp);
                        deployException = new DeployerException(deployer.getName(), module);
                    }
                }
            }
        }
        
        /* If there is an exception then throw it, otherwise just return */
        if (deployException != null) {
            throw deployException;
        }
    }
    
    /**
     * Undeploys a given module to the proper deployers. The contract between the
     * module manager and the deployers is that once the deployers say that a
     * module may be undeployed to it, the undeployment should not fail. If it does
     * then DeployerException is thrown.
     * 
     * @param module The module to undeploy
     * @throw DeployerException If the module cannot be deployed
     */
    public void undeploy(Module module) throws DeployerException {
        /*
         * If the deployment fails for one deployer, then keep an exception
         * around for it.
         */
        DeployerException undeployException = null;
        
        /*
         * Fetch all of the deployers and iterate through them. For each of
         * the parts they support, find whether the module contains such a
         * part. If so, deploy the module.
         */
        Map<String, ModulePart> parts = module.getParts();
        Iterator<ModuleDeployerSPI> it = this.deployers.iterator();
        while (it.hasNext() == true) {
            /*
             * Fetch the module part types that the deployer supports. If none,
             * just continue to the next module.
             */
            ModuleDeployerSPI deployer = it.next();
            String[] partTypes = deployer.getTypes();
            if (partTypes == null) {
                continue;
            }
            
            /*
             * Loop through each part type and see if there is a module part
             */
            for (String partType : partTypes) {
                if (parts.containsKey(partType) == true) {
                    try {
                        deployer.undeploy(partType, module, parts.get(partType));
                    } catch (java.lang.Exception excp) {
                        /*
                         * Catch all exceptions here. Report them and pass them
                         * up as DeployerException, but continue to
                         * the remainder of the deployers.
                         */
                        logger.log(Level.WARNING, "[UNDEPLOY] Failed", excp);
                        undeployException = new DeployerException(deployer.getName(), module);
                    }
                }
            }
        }
        
        /* If there is an exception then throw it, otherwise just return */
        if (undeployException != null) {
            throw undeployException;
        }
    }
    
    /**
     * Returns true if all of the deployers can deploy the parts of the module,
     * false if not
     */
    public boolean canDeploy(Module module) {
        /*
         * Fetch all of the deployers and iterate through them. For each of
         * the parts they support, find whether the module contains such a
         * part. If so, ask the deployer whether it is ready to deploy
         */
        Map<String, ModulePart> parts = module.getParts();
        Iterator<ModuleDeployerSPI> it = this.deployers.iterator();
        while (it.hasNext() == true) {
            ModuleDeployerSPI deployer = it.next();
            String[] partTypes = deployer.getTypes();
            if (partTypes == null) {
                continue;
            }
            
            /* Loop through each part type and see if there is a module part */
            for (String partType : partTypes) {
                if (parts.containsKey(partType) == true) {
                    if (deployer.isDeployable(partType, module, parts.get(partType)) == false) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Returns true if all of the deployers can undeploy the parts of the module,
     * false if not
     */
    public boolean canUndeploy(Module module) {
        /*
         * Fetch all of the deployers and iterate through them. For each of
         * the parts they support, find whether the module contains such a
         * part. If so, ask the deployer whether it is ready to deploy
         */
        Map<String, ModulePart> parts = module.getParts();
        Iterator<ModuleDeployerSPI> it = this.deployers.iterator();
        while (it.hasNext() == true) {
            ModuleDeployerSPI deployer = it.next();
            String[] partTypes = deployer.getTypes();
            if (partTypes == null) {
                continue;
            }
            
            /* Loop through each part type and see if there is a module part */
            for (String partType : partTypes) {
                if (parts.containsKey(partType) == true) {
                    if (deployer.isUndeployable(partType, module, parts.get(partType)) == false) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Find and return all the classes that implement the ModuleDeployerSPI
     * inteface
     * 
     * @return
     */
    private Class[] getClasses() {
        Iterator<ModuleDeployerSPI> it = Service.providers(ModuleDeployerSPI.class);
        Collection<Class> names = new HashSet<Class>();
        while (it.hasNext() == true) {
            names.add(it.next().getClass());
        }

        return names.toArray(new Class[]{} );
    }
}
