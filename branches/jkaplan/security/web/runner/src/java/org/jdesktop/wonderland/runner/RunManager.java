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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;

/**
 * Singleton manager for starting and stopping services.
 * <p>
 * When a new runner is added to the manager, the manager will attempt to
 * deploy the correct environment to the runner.  This consists of calling
 * the runner's <code>deploy()</code> method with a set of libraries.
 * The libraries are determined by reading a configuration file, in this
 * case located in the <code>RunManager</code>'s jar file, in the
 * <code>META-INF/deploy</code> directory.  The manager will attempt
 * to deploy all files listed in a file with the fully-qualified classname
 * of the runner.  So if the Runner is of class 
 * <code>org.jdesktop.wonderland.sample.SampleRunner</code>, the file
 * <code>META-INF/deploy/org.jdesktop.wonderland.sample.SampleRunner</code>
 * will be expected to contain the list of zip files to deploy.  The 
 * list of files is formatted one per line, with each line containing the
 * path to a zip file in the manager's jar file.
 * 
 * @author jkaplan
 */
public class RunManager {
    /** a logger */
    private static final Logger logger =
            Logger.getLogger(RunManager.class.getName());

    /** the properties for starting and stopping */
    private static final String START_PROP = "wonderland.runner.autostart";
    private static final String STOP_PROP  = "wonderland.runner.autostop";

    /** the singleton instance */
    private static RunManager runManager;
    
    /** the path of the deploy information in this archive */
    private static final String DEPLOY_DIR = "/META-INF/deploy";
    
    /** the set of runners we manage, index by name */
    private Map<String, Runner> runners = new LinkedHashMap<String, Runner>();
    
    /**
     * Get the singleton instance
     */
    public synchronized static RunManager getInstance() {
        if (runManager == null) {
            runManager = new RunManager();
        }
        
        return runManager;
    }
    
    /**
     * Constructor is private.  Use getInstance() to get the singleton
     * instance.
     */
    private RunManager() {
    }
    
    /**
     * Initialize this manager by loading all runners from the
     * default deployment plan, and starting them if they are set
     * to autostart.
     * @throws RunnerException if there is a problem starting one of the
     * runners.
     */
    public void initialize() throws RunnerException {
        logger.info("[RunManager] Starting all apps");

        if (Boolean.parseBoolean(SystemPropertyUtil.getProperty(STOP_PROP))) {
            // Add a listener that will stop all active processes when the
            // container shuts down
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    shutdown();
                }
            });
        }

        boolean start =
            Boolean.parseBoolean(SystemPropertyUtil.getProperty(START_PROP));

        DeploymentPlan dp = DeploymentManager.getInstance().getPlan();
        for (DeploymentEntry de : dp.getEntries()) {
            // copy System properties to pass into this runner
            Properties props = new Properties(System.getProperties());
            props.setProperty("runner.name", de.getRunnerName());

            try {
                Runner r = RunnerFactory.create(de.getRunnerClass(), props);
                r = add(r);
                if (start) {
                    r.start(getStartProperties(r));
                }
            } catch (IOException ioe) {
                throw new RunnerException("Error adding runner " +
                                          de.getRunnerName(), ioe);
            }
        }
    }

    /**
     * Stop all existing applications.
     */
    public void shutdown() {
        logger.info("[RunManager] Stopping all apps");

        // stop all active applications
        for (Runner runner : getAll()) {
            if (runner.getStatus() == Runner.Status.RUNNING ||
                    runner.getStatus() == Runner.Status.STARTING_UP)
            {
                runner.stop();
            }
        }
    }

    /**
     * Add a new <code>Runner</code> managed by this manager.  Note that
     * the returned runner may be different than the original one you
     * passed in, for example because it is decorated. After calling this
     * method, use the returned runner and not the one you passed in.
     * <p>
     * This step additionally deploys all zip files associated with the
     * runner.  After <code>add()</code> is called, the runner is ready
     * to be started with the <code>start()</code> method.
     * 
     * @param runner the runner to manage
     * @return a decorated version of the managed runner
     * @throws IOException if there is an error adding the runner or
     * deploying zips to it.
     */
    public synchronized Runner add(Runner runner) throws IOException {
        // deploy to this runner
        deployTo(runner);
        
        runners.put(runner.getName(), runner);
        return runner;
    }
    
    /**
     * Get a runner by name
     * @param name the name of the runner to get
     * @return a runner with the given name, or null if no runner exists
     * with that name
     */
    public synchronized Runner get(String name) {
        return runners.get(name);
    }
    
    /**
     * Get all runners.  The order that runners is returned should be 
     * consistent based on the order they were added.
     * @return the runners
     */
    public synchronized Collection<Runner> getAll() {
        return runners.values();
    }
    
    /**
     * Get all runners of the given type. Each runner will be tested
     * using "instanceof", and this method will return the list of
     * runners that implement the given type.
     * @param clazz the class of runner to get
     * @return the list of runners matching the given class, or an empty
     * list if no runners match
     */
    public synchronized <T extends Runner> Collection<T> getAll(Class<T> clazz) {
        Collection<T> out = new ArrayList<T>();
        for (Runner r : getAll()) {
            if (clazz.isAssignableFrom(r.getClass())) {
                out.add(clazz.cast(r));
            }
        }
        return out;
    }
        
    
    /**
     * Remove a runner by name
     * @param name the name of the runner to remove
     * @return the removed runner, or null if the runner isn't found
     */
    public synchronized Runner remove(String name) {
        return runners.remove(name);
    } 

    /**
     * Start the given runner.
     * @param runner the runner to start
     * @param wait whether or not to wait for the runner to start
     * @return the StatusWaiter that waits for this runner to start, or
     * null if wait is false
     * @throws RunnerException if there is a problem starting the runner
     */
    public StatusWaiter start(Runner runner, boolean wait)
        throws RunnerException
    {
        StatusWaiter out = null;

        if (runner.getStatus() == Runner.Status.NOT_RUNNING) {
            runner.start(getStartProperties(runner));

            if (wait) {
                out = new StatusWaiter(runner, Runner.Status.RUNNING);
            }
        }

        return out;
    }

    /**
     * Stop the given runner.
     * @param runner the runner to stop
     * @param wait whether or not to wait for the runner to stop
     * @return the StatusWaiter that waits for this runner to stop, or
     * null if wait is false
     * @throws RunnerException if there is a problem stopping the runner
     */
    public StatusWaiter stop(Runner runner, boolean wait)
        throws RunnerException
    {
        StatusWaiter out = null;

        if (runner.getStatus() == Runner.Status.RUNNING || 
                runner.getStatus() == Runner.Status.STARTING_UP)
        {
            runner.stop();

            if (wait) {
                out = new StatusWaiter(runner, Runner.Status.NOT_RUNNING);
            }
        }

        return out;
    }

    /**
     * Get the run properties for starting the given runner.  Properties are
     * determined by getting the deployment entry for the given runner.
     * If the deployment entry doesn't have any properties, the defaults
     * as specified by the runner are used.
     *
     * @param runner the runner to get properties for
     * @return the properties to use when starting that runner
     */
    public Properties getStartProperties(Runner runner) {
        // find a properties file from the current deployment plan
        DeploymentPlan dp = DeploymentManager.getInstance().getPlan();
        DeploymentEntry de = dp.getEntry(runner.getName());
        if (de != null && !de.getRunProps().isEmpty()) {
            return de.getRunProps();
        } else {
            return runner.getDefaultProperties();
        }
    }

    /**
     * Deploy zips to a runner.  This will find the appropriate zips
     * based on the classname of the runner, and deploy each of them
     * in turn.
     * @param runner the runner to deploy to
     * @throws IOException if there is an error reading the zip files
     * from the archive or an error writing them to the runner.
     */
    private void deployTo(Runner runner) throws IOException {
        // get the list of deployment files
        Collection<String> files = runner.getDeployFiles();
        for (String file : files) {
            String fullPath = DEPLOY_DIR + "/" + file;
            InputStream deploy = getClass().getResourceAsStream(fullPath);
            if (deploy == null) {
                throw new IOException("Unable to find deploy file " + fullPath);
            }
            
            // deploy the file
            runner.deploy(file, deploy);
        }
    }
}
