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
package org.jdesktop.wonderland.runner;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;

/**
 * Managed deployment properties.
 * @author jkaplan
 */
public class DeploymentManager {
    // singleton instance
    private static DeploymentManager inst;
    
    // default place to read the plan from
    private static final String DEFAULT_PLAN = "/META-INF/deploy/DeploymentPlan.xml";
    
    // the deployment plan
    private DeploymentPlan plan;
    
    /**
     * Constructor is private.  Use getInstance() instead.
     */
    private DeploymentManager() {
        try {
            plan = loadPlan();
        } catch (IOException ioe) {
            throw new IllegalStateException("Unable to load plan", ioe);
        }
    }
    
    /**
     * Get the default DeploymentManager instance
     * @return the deployment manager
     */
    public synchronized static DeploymentManager getInstance() {
        if (inst == null) {
            inst = new DeploymentManager();
        }
        
        return inst;
    }
    
    /**
     * Get the current deployment plan
     * @return the current deployment plan
     */
    public DeploymentPlan getPlan() {
        return plan;
    }
    
    
    /**
     * Get a deployment entry by name
     * @param name the name of the deployment entry to get
     * @return the entry with the given name, or null if no entry
     * exists with the given name
     */
    public DeploymentEntry getEntry(String name) {
        return getPlan().getEntry(name);
    }
 
    /**
     * Load a plan from disk
     * @return the loaded plan
     */
    protected DeploymentPlan loadPlan() throws IOException {
        Reader planReader;
        
        File planFile = getPlanFile();
        if (planFile.exists()) {
            planReader = new FileReader(planFile);
        } else {
            planReader = new InputStreamReader(
                    DeploymentManager.class.getResourceAsStream(DEFAULT_PLAN));
        }
        
        try {
            return DeploymentPlan.decode(planReader);
        } catch (JAXBException je) {
            IOException ioe = new IOException("Error decoding plan file " + 
                                              planFile);
            ioe.initCause(je);
            throw ioe;
        }   
    }
    
    /**
     * Save the current deployment plan to disk.
     */
    public void savePlan() throws IOException {
        try {
            FileWriter writer = new FileWriter(getPlanFile());
            getPlan().encode(writer);
        } catch (JAXBException je) {
            IOException ioe = new IOException("Error encoding plan file " + 
                                              getPlanFile());
            ioe.initCause(je);
            throw ioe;
        }
    }
    
    /**
     * Get the directory to load or save deployment plans from
     * @return the deployment plan directory
     */
    protected File getPlanFile() {
        String deployDirName = SystemPropertyUtil.getProperty("wonderland.config.dir");
        File deployDir = new File(deployDirName);
        return new File(deployDir, "DeploymentPlan.xml");
    }   
}
