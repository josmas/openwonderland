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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

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
     * Remove a runner by name
     * @param name the name of the runner to remove
     * @return the removed runner, or null if the runner isn't found
     */
    public synchronized Runner remove(String name) {
        return runners.remove(name);
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
        // first, find the deploment descriptor file
        String deployFile = DEPLOY_DIR + "/" + runner.getClass().getName();
        InputStream is = getClass().getResourceAsStream(deployFile);
        if (is == null) {
            throw new IOException("Unable to find deployment information at " +
                                  deployFile);
        }
        
        // now read the file
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) {
            InputStream deploy = getClass().getResourceAsStream(line.trim());
            if (deploy == null) {
                throw new IOException("Unable to find deploy file " + line);
            }
            
            // deploy the file
            runner.deploy(deploy);
        }
    }
}
