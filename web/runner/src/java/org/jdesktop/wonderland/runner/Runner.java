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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

/**
 * Interface to a running instance of a service.  Runners are created by
 * a factory, so must have a public, no-argument constructor. 
 * <code>configure()</code> is guaranteed to be called before any other method
 * on the runner.
 * <p>
 * A runner must start in the NOT_RUNNING state. At that point, the 
 * <code>deploy()</code> method will be called once for each deployment
 * file.  These deployment files are typically in the form of zip files.  After
 * all deployment files are written, the <code>start()</code> method will be
 * called.  At this point, the runner should attempt to start up. At some point
 * after the runner is started, the <code>stop()</code> method will be called
 * to stop the given runner.
 * 
 * @author jkaplan
 */
public interface Runner {
    /** Status of a running process */
    public enum Status { NOT_RUNNING, STARTING_UP, RUNNING, SHUTTING_DOWN, ERROR };
    
    /**
     * Get the name of this runner.  Runners must have unique names
     * @return the name
     */
    public String getName();
    
    /**
     * Configure this runner.  This method is guaranteed to be called before
     * any other method (including getName()).
     * @param props the properties to configure with
     */
    public void configure(Properties props) throws RunnerConfigurationException;

    /**
     * Get the set of files needed to deploy this runner.  It is assumed
     * that files of the given name will be available to the RunManager.
     * The contents of these files will then be passed into the "deploy" 
     * method as input streams.
     * @return a collection of file names.
     */
    public Collection<String> getDeployFiles();
    
    /**
     * Deploy the given file
     * @param filename the name of the file to deploy
     * @param in an inputstream containing the data from the file to deploy
     */
    public void deploy(String filename, InputStream in) throws IOException;
    
    /**
     * Get the default runtime properties for this runner.  Return an empty
     * property list if there are no default properties.
     * @return the default properties for this runner.
     */
    public Properties getDefaultProperties();
    
    /**
     * Start the given runner.  Only valid if the runner is in the
     * NOT_RUNNING state.
     * @param props the properties to run with
     * @throws IllegalStateException if the runner is in any state other
     * than NOT_RUNNING.
     */
    public void start(Properties props) throws RunnerException;
    
    /**
     * Stop the given runner.  Only valid if the runner is in the 
     * RUNNING state.
     * @throws IllegalStateException if the runner is in any state other
     * than RUNNING.
     */
    public void stop();
    
    /**
     * Get the current status of this runner.
     * @return the status of this runner.
     */
    public Status getStatus();
    
    /**
     * Get the log file from this runner.  The log file will contain all
     * the relevant output from the runner.  It is up to each runner whether
     * to reset the log file when the run is restart or not.  In any case,
     * the log should generally be available even after the runner has shut
     * down.
     * 
     * @return a file with the log contents, or null if there is no current
     * log
     */
    public File getLogFile();
    
    /**
     * Add a listener to be notified when the status changes
     * @param listener the listener
     * @return the current status of the runner.  The runner will be notified
     * of any changes after this status
     */
    public Status addStatusListener(RunnerStatusListener listener);
    
    /**
     * Remove a listener
     * @param listener the listener
     */
    public void removeStatusListener(RunnerStatusListener listener);
    
    /**
     * Listener that is notified on runner status changes.
     */
    public static interface RunnerStatusListener {
        /**
         * Notification that the runner's status has changed.
         * @param runner the runner that had the status change
         * @param status the new status
         */
        public void statusChanged(Runner runner, Status status);
    }
}
