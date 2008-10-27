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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModulePart;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.runner.BaseRunner;
import org.jdesktop.wonderland.runner.RunnerConfigurationException;
import org.jdesktop.wonderland.runner.RunnerException;
import org.jdesktop.wonderland.utils.RunUtil;

/**
 * An extension of <code>BaseRunner</code> to launch the Darkstar server.
 * @author jkaplan
 */
public class DarkstarRunner extends BaseRunner {
    /** the default name if none is specified */
    private static final String DEFAULT_NAME = "Darkstar Server";
    
    /** the logger */
    private static final Logger logger =
            Logger.getLogger(DarkstarRunner.class.getName());
    
    /**
     * Configure this runner.  This method sets values to the default for the
     * Darkstar server.
     * 
     * @param props the properties to deploy with
     * @throws RunnerConfigurationException if there is an error configuring
     * the runner
     */
    @Override
    public void configure(Properties props) 
            throws RunnerConfigurationException 
    {
        super.configure(props);
    
        // if the name wasn't configured, do that now
        if (!props.containsKey("runner.name")) {
            setName(DEFAULT_NAME);
        }
    }
 
    /**
     * Add the Darkstar distribution file
     * @return a list containing the core distribution file as well
     * as the Darkstar distribution file
     */
    @Override
    public Collection<String> getDeployFiles() {
        Collection<String> files = super.getDeployFiles();
        files.add("wonderland-server-dist.zip");
        
        return files;
    }

    /** Default Darkstar properties */
    @Override
    public Properties getDefaultProperties() {
        Properties props = new Properties();
        props.setProperty("sgs.port", "1139");
        return props;
    }
    
    
    
    /**
     * Start the Darkstar server with the given properties.  This method first
     * manages deploying any modules specified by the module manager.
     * 
     * @param props the properties to run with
     * @throws RunnerException if there is an error starting the server
     */
    @Override
    public synchronized void start(Properties props) throws RunnerException {
        ModuleManager mm = ModuleManager.getModuleManager();
        
        // first tell the module manager to remove any modules scheduled for
        // removal
        mm.uninstallAll();
        
        // next tell the module manager to install any pending modules
        mm.installAll();
        
        // now clear any existing modules
        File moduleDir = getModuleDir();
        if (moduleDir.exists()) {
            RunUtil.deleteDir(moduleDir);
        }
        
        Map<Module, List<ModulePart>> deploy = DarkstarModuleDeployer.getModules();
        for (Entry<Module, List<ModulePart>> e : deploy.entrySet()) {
            for (ModulePart mp : e.getValue()) {
                try {
                    writeModulePart(moduleDir, e.getKey(), mp);
                } catch (IOException ioe) {
                    // skip this module and keep going
                    logger.log(Level.WARNING, "Error writing module " + 
                               e.getKey().getName() + " part " + mp.getName(),
                               ioe);
                }
            }
        }
       
        super.start(props);
    }
    
    /**
     * Get the Darkstar server module directory
     * @return the server module directory
     */
    protected File getModuleDir() {
        return new File(getRunDir(), "modules");
    }

    /**
     * Write a module part to the module directory
     * @param moduleDir the directory to write to
     * @param module the module to write a part for
     * @param modulePart the part to write
     
     */
    protected void writeModulePart(File moduleDir, Module module, 
                                   ModulePart part)
        throws IOException
    {
        File moduleSpecificDir = new File(moduleDir, module.getName());
        File partDir = new File(moduleSpecificDir, part.getName());
        partDir.mkdirs();
        
        // write each file from the part
        copyFiles(part.getFile(), partDir);
    }
    
    /**
     * Recursively copy all files from a given source directory to a target 
     * directory
     * @param src the source directory
     * @param target the target directory
     */
    private void copyFiles(File src, File dest) 
        throws IOException
    {
        File[] files = src.listFiles();
        if (files == null) {
            return;
        }
        
        for (File f : files) {
            File newDest = new File(dest, f.getName());
            
            if (f.isDirectory()) {
                // recursively copy a directory
                newDest.mkdir();
                copyFiles(f, newDest);
            } else {
                // copy a single file
                FileInputStream fis = new FileInputStream(f);
                RunUtil.writeToFile(fis, newDest);
            }
        }
    }
    
    /**
     * Override the setStatus() method to ignore the RUNNING status.  Instead,
     * we notify other processes that Darkstar is RUNNING when the output
     * reader gets the startup line successfully.
     * @param status the status to set
     */
    @Override
    protected synchronized void setStatus(Status status) {
        if (status == Status.RUNNING) {
            return;
        }
        
        super.setStatus(status);
    }
    
    /**
     * Override the createOutputReader method to return a 
     * DarkstarOutputReader that will notify us when Darkstar is really up
     */
    @Override
    protected DarkstarOutputReader createOutputReader(InputStream in,
                                                      Logger out)
    {
        return new DarkstarOutputReader(in, out);
    }
    
    /**
     * Called when Darkstar starts up
     */
    protected void darkstarStarted() {
        super.setStatus(Status.RUNNING);
    }
    
    /**
     * Wait for Darkstar to fully startup
     */
    protected class DarkstarOutputReader extends BaseRunner.ProcessOutputReader {
        private static final String DARKSTAR_STARTUP =
                "Wonderland: application is ready";
                
        public DarkstarOutputReader(InputStream in, Logger out) {
            super (in, out);
        }
        
        @Override
        protected void handleLine(String line) {
            // see if this is a Darkstar startup message
            if (line.contains(DARKSTAR_STARTUP)) {
                darkstarStarted();
            }
            
            super.handleLine(line);
        }
    }
}
