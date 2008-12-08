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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModulePart;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.runner.BaseRunner;
import org.jdesktop.wonderland.runner.RunManager;
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

    /** the default port to run on */
    private static final int DEFAULT_PORT = 1139;

    /** the logger */
    private static final Logger logger =
            Logger.getLogger(DarkstarRunner.class.getName());

    /** the webserver URL to link back to */
    private String webserverURL;

    /** the sgs port.  Only valid when starting up or running */
    private int currentPort;

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

        // record the webserver URL
        webserverURL = props.getProperty("wonderland.web.server.url");
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
        props.setProperty("sgs.port", String.valueOf(DEFAULT_PORT));
        props.setProperty("wonderland.web.server.url", webserverURL);
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

        // go through each module part in turn and deploy the files in that
        // part.  Keep track of any Darkstar managers and services which
        // are created as well
        Map<Module, List<ModulePart>> deploy = DarkstarModuleDeployer.getModules();
        List<String> managers = new ArrayList<String>();
        List<String> services = new ArrayList<String>();
        for (Entry<Module, List<ModulePart>> e : deploy.entrySet()) {
            for (ModulePart mp : e.getValue()) {
                try {
                    writeModulePart(moduleDir, e.getKey(), mp,
                                    managers, services);
                } catch (IOException ioe) {
                    // skip this module and keep going
                    logger.log(Level.WARNING, "Error writing module " + 
                               e.getKey().getName() + " part " + mp.getName(),
                               ioe);
                }
            }
        }

        // turn the captured manager and service names into properties
        if (managers.size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (String manager : managers) {
               sb.append(":" + manager);
            }
            props.put("sgs.managers", sb.toString());
        }
        if (services.size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (String service : services) {
               sb.append(":" + service);
            }
            props.put("sgs.services", sb.toString());
        }

        // set the current port to the one we are now running with.  This
        // will stay valid until the runner is stopped.
        currentPort = getPort(props);
       
        super.start(props);
    }

    /**
     * Get the Darkstar server name for clients to connect to.
     * @return the external hostname of the Darkstar server
     */
    public String getHostname() {
        try {
            // first try the web server host property.  This is a temporary
            // workaround, since it assumes the Darkstar host and the
            // web server host are the same.  We should use some shared
            // code (i.e. NetworkAddress) to do this properly in the future.
            // TODO: replace me with generic code
            String hostname = System.getProperty("wonderland.webserver.host");
            if (hostname == null) {
                // if no property is set, use Java's version of the
                // local host address.  On Linux with DHCP, this can be wrong!
                hostname = InetAddress.getLocalHost().getHostAddress();
            } else {
                hostname = hostname.trim();
            }

            return hostname;
        } catch (UnknownHostException uhe) {
            logger.log(Level.WARNING, "Unable to determine hostname", uhe);
            return "localhost";
        }
    }

    /**
     * Get the Darkstar server port for clients to connect to.  This method
     * returns the port of the currently running server.  If the server
     * is not running, it returns what the port will be the next time the
     * server starts up.
     * @return the port
     */
    public synchronized int getPort() {
        // if the server is running, us the current port variable
        if (getStatus() == Status.RUNNING ||
                getStatus() == Status.STARTING_UP)
        {
            return currentPort;
        } else {
            return getPort(RunManager.getInstance().getStartProperties(this));
        }
    }

    /**
     * Get the port to run on from a set of properties
     * @param properties the properties to look at
     * @return the port to run on
     */
    private int getPort(Properties props) {
        // determine the current port
        String portProp = props.getProperty("sgs.port");
        if (portProp != null) {
            return Integer.parseInt(portProp);
        } else {
            return DEFAULT_PORT;
        }
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
     * @param managers the list of Darkstar managers to add to
     * @param services the list of Darkstar services to add to
     */
    protected void writeModulePart(File moduleDir, Module module, 
                                   ModulePart part, List<String> managers,
                                   List<String> services)
        throws IOException
    {
        File moduleSpecificDir = new File(moduleDir, module.getName());
        File partDir = new File(moduleSpecificDir, part.getName());
        partDir.mkdirs();
        
        // write each file from the part
        copyFiles(part.getFile(), partDir, managers, services);
    }

    /**
     * Recursively copy all files from a given source directory to a target 
     * directory.  If any jar files are encountered, check them for
     * Darkstar managers and services
     * @param src the source directory
     * @param target the target directory
     * @param managers the list of Darkstar managers to add to
     * @param services the list of Darkstar services to add to
     */
    private void copyFiles(File src, File dest, List<String> managers,
                           List<String> services)
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
                copyFiles(f, newDest, managers, services);
            } else {
                // copy a single file
                FileInputStream fis = new FileInputStream(f);
                RunUtil.writeToFile(fis, newDest);

                if (f.getName().endsWith(".jar")) {
                    checkForServices(f, managers, services);
                }
            }
        }
    }

    /**
     * Check a .jar file for any Darkstar managers and services.
     * @param f the file to check
     * @param managers the list of Darkstar managers to add to
     * @param services the list of Darkstar services to add to
     */
    private void checkForServices(File f, List<String> managers,
                                  List<String> services)
        throws IOException
    {
        JarFile jf = new JarFile(f);

        // look for services
        ZipEntry ze = jf.getEntry("META-INF/services/com.sun.sgs.service.Service");
        if (ze != null) {
            addAll(jf.getInputStream(ze), services);
        }

        // loog for managers
        ze = jf.getEntry("META-INF/services/com.sun.sgs.service.Manager");
        if (ze != null) {
            addAll(jf.getInputStream(ze), managers);
        }
    }

    /**
     * Add all services in a file to a list
     * @param is the InputStream containing the list of files to add
     * @param list the list to add entries to
     */
    private void addAll(InputStream is, List<String> list) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) {
            if (line.trim().length() > 0) {
                list.add(line.trim());
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
                "Wonderland server started successfully";
                
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
