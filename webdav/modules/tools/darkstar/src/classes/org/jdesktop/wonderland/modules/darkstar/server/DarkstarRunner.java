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

package org.jdesktop.wonderland.modules.darkstar.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.runner.BaseRunner;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.RunnerChecksum;
import org.jdesktop.wonderland.runner.RunnerChecksums;
import org.jdesktop.wonderland.runner.RunnerConfigurationException;
import org.jdesktop.wonderland.runner.RunnerException;
import org.jdesktop.wonderland.utils.Constants;
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

    /** the URL for listing available files */
    private static final String CHECKSUM_URL =
                                  "darkstar/darkstarserver/services/checksums";

    /** the logger */
    private static final Logger logger =
            Logger.getLogger(DarkstarRunner.class.getName());

    /** the webserver URL to link back to */
    private String webserverURL;

    /** the sgs port.  Only valid when starting up or running */
    private int currentPort;

    /** managers and services found as we deployed files */
    private List<String> managers;
    private List<String> services;

    /**
     * The current list of modules, implemented as a thread-local variable
     * that is only valid during a single call to start()
     */
    private static ThreadLocal<RunnerChecksums> moduleChecksums =
        new ThreadLocal<RunnerChecksums>();

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
        // add all the files from the superclass
        Collection<String> files = super.getDeployFiles();

        // and the Darkstar server jar
        files.add("wonderland-server-dist.zip");

        // now add each module
        try {
            for (String module : getModuleChecksums().getChecksums().keySet()) {
                files.add(module);
            }
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error reading module checksums", ioe);
        }

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

    @Override
    public synchronized void start(Properties props) throws RunnerException {
        try {
            super.start(props);
        } finally {
            // reset the module checksums
            moduleChecksums.remove();
        }
    }
   
    /**
     * Deploy files to the Darkstar server with the given properties.
     * This method first manages deploying any modules specified by the module
     * manager, and also detects any Darkstar runners or services we need
     * to install
     * 
     * @param props the properties to run with
     * @throws IOException if there is an error deploying files
     */
    @Override
    protected void deployFiles(Properties props) throws IOException {
        ModuleManager mm = ModuleManager.getModuleManager();
        
        // first tell the module manager to remove any modules scheduled for
        // removal
        mm.uninstallAll();
        
        // next tell the module manager to install any pending modules
        mm.installAll();

        // then call the super class's deployFiles() method, which will
        // call the other methods in this class
        super.deployFiles(props);

        // go through al the module jars looking for any Darkstar managers and
        // services
        managers = new ArrayList<String>();
        services = new ArrayList<String>();

        File[] moduleFiles = getModuleDir().listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (moduleFiles != null) {
            for (File moduleFile : moduleFiles) {
                checkForServices(moduleFile, managers, services);
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
    }

    /**
     * Deploy zip files as normal using the superclass.  Copy any
     * .jar files (which are assumed to be modules) into the modules
     * directory
     * @param deploy the file to deploy
     * @throws IOException
     */
    @Override
    protected void deployFile(File deploy) throws IOException {
        if (deploy.getName().endsWith(".jar")) {
            File out = new File(getModuleDir(), deploy.getName());
            RunUtil.writeToFile(new FileInputStream(deploy), out);
        } else {
            super.deployFile(deploy);
        }
    }

    @Override
    protected RunnerChecksums getServerChecksums() throws IOException {
        // get the server checksums
        RunnerChecksums serverChecksums = super.getServerChecksums();

        // now add in the checksums for the modules
        Map<String, RunnerChecksum> checksums = serverChecksums.getChecksums();
        checksums.putAll(getModuleChecksums().getChecksums());
        serverChecksums.setChecksums(checksums);
        return serverChecksums;
    }

    /**
     * Get the module checksums from the thread-local variable.  This is only
     * valid during the method calls within a single invocation of start()
     */
    protected synchronized RunnerChecksums getModuleChecksums()
        throws IOException
    {
        RunnerChecksums out = moduleChecksums.get();
        if (out == null) {
            // read in the new checksums from the server
            URL checksumURL = new URL(webserverURL + CHECKSUM_URL);
            try {
                Reader in = new InputStreamReader(checksumURL.openStream());
                out = RunnerChecksums.decode(in);

                moduleChecksums.set(out);
            } catch (JAXBException je) {
                IOException ioe = new IOException("Error reading checksums " +
                                                  "from " + checksumURL);
                ioe.initCause(je);
                throw ioe;
            }
        }

        return out;
    }

    /**
     * Get the Darkstar server name for clients to connect to.
     * @return the external hostname of the Darkstar server
     */
    public String getHostname() {
        return System.getProperty(Constants.WEBSERVER_HOST_PROP);
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
        File moduleDir = new File(getRunDir(), "modules");
        moduleDir.mkdirs();
        return moduleDir;
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
    protected void setStatus(Status status) {
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
