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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModulePlugin;
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
        
        // write all new modules to the module directory
        moduleDir.mkdirs();
        Collection<String> moduleNames = mm.getModules(ModuleManager.State.INSTALLED);
        for (String moduleName : moduleNames) {
            Module m = mm.getModule(moduleName, ModuleManager.State.INSTALLED);
            
            try {
                writePlugins(m, moduleDir);
            } catch (IOException ioe) {
                // skip this module and keep going
                logger.log(Level.WARNING, "Error writing plugins for " + m.getName(),
                           ioe);
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
     * Write a module's plugins to the module directory
     * @param module the module to write plugins for
     * @param moduleDir the directory to write to
     */
    protected void writePlugins(Module module, File moduleDir)
        throws IOException
    {
        File moduleSpecificDir = new File(moduleDir, module.getName());
        moduleSpecificDir.mkdirs();
        
        // got through each plugin
        Collection<String> pluginNames = module.getModulePlugins();
        for (String pluginName : pluginNames) {
            // create a directory to put the data int
            File pluginDir = new File(moduleSpecificDir, pluginName);
            pluginDir.mkdirs();
            
            // get the plugin
            ModulePlugin mp = module.getModulePlugin(pluginName);
            
            // extract server jars
            Collection<String> serverJars = mp.getServerJars();
            if (!serverJars.isEmpty()) {
                File serverDir = new File(pluginDir, "server");
                serverDir.mkdirs();
            
                for (String serverJar : serverJars) {
                    InputStream is = 
                        module.getInputStreamForPlugin(pluginName, serverJar,
                                                       "server/");
                    RunUtil.writeToFile(is, new File(serverDir, serverJar));
                }
            }
            
            // extract common jars
            Collection<String> commonJars = mp.getCommonsJars();
            if (!commonJars.isEmpty()) {
                File commonDir = new File(pluginDir, "common");
                commonDir.mkdirs();
            
                for (String commonJar : commonJars) {
                    InputStream is = 
                        module.getInputStreamForPlugin(pluginName, commonJar,
                                                       "common/");
                    RunUtil.writeToFile(is, new File(commonDir, commonJar));
                }
            }
        }
    }
}
