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
package org.jdesktop.wonderland.modules.sasxremwin.weblib;

import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;
import org.jdesktop.wonderland.runner.BaseRunner;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.Runner;
import org.jdesktop.wonderland.runner.Runner.RunnerStatusListener;
import org.jdesktop.wonderland.runner.Runner.Status;
import org.jdesktop.wonderland.runner.RunnerConfigurationException;
import org.jdesktop.wonderland.runner.RunnerException;
import org.jdesktop.wonderland.utils.Constants;

/**
 * An extension of <code>BaseRunner</code> to launch the Darkstar server.
 * @author jkaplan
 */
public class SasProviderRunner extends BaseRunner 
        implements RunnerStatusListener
{
    /** the default name if none is specified */
    private static final String DEFAULT_NAME = "Shared App Server";
    
    /** the logger */
    private static final Logger logger =
            Logger.getLogger(SasProviderRunner.class.getName());
    
    /** the URL of the base web server */
    private String webserverURL;

    /**
     * the Darkstar runner
     */
    private Runner darkstar;

    /** 
     * the properties we were started with -- only valid after start was
     * called, for delayed startup
     */
    private Properties startupProps;

    /**
     * Configure this runner. 
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
        webserverURL = props.getProperty(Constants.WEBSERVER_URL_PROP);
    }
    
    
    /**
     * Get the set of files to deploy.
     * @return the files to deploy
     */
    @Override
    public Collection<String> getDeployFiles() {
        Collection<String> out = super.getDeployFiles();
        out.add("wonderland-client-dist.zip");
        out.add("sasxremwin-dist.zip");
        return out;
    }
    
    /**
     * Get the default properties for the sas provider.
     * @return the default properties
     */
    @Override
    public Properties getDefaultProperties() {
        Properties props = new Properties();
        props.setProperty(Constants.WEBSERVER_URL_PROP, webserverURL);
        return props;
    }

    @Override
    public synchronized void start(Properties props) throws RunnerException {
        startupProps = props;

        // XXX there are better ways to do this XXX
        darkstar = RunManager.getInstance().get("Darkstar Server");

        // add a listener that will be notified when the Darkstar server
        // starts or stops
        darkstar.addStatusListener(this);

        // if the Darkstar server is already started, just start immediately
        if (darkstar.getStatus() == Runner.Status.RUNNING) {
            super.start(props);
        } else {
            setStatus(Runner.Status.STARTING_UP);
        }
    }

    @Override
    public synchronized void stop() {
        startupProps = null;
        darkstar.removeStatusListener(this);

        if (getStatus() == Status.RUNNING ||
            getStatus() == Status.STARTING_UP)
        {
            super.stop();
        }
    }
    
    /**
     * Notification that the status of the Darkstar server has changed.
     * This will only happen if this runner has been started and not
     * stopped.
     * 
     * @param runner the Darkstar runner
     * @param status the status of the Darkstar runner
     */
    public void statusChanged(Runner runner, Status status) {
        switch (status) {
            case RUNNING:
                // hack -- set our status to NOT_RUNNING so we can start
                setStatus(Status.NOT_RUNNING);

                // the Darkstar server has started up, start the SAS provider
                try {
                    super.start(startupProps);
                } catch (RunnerException re) {
                    throw new IllegalStateException(re);
                }
                break;
            case NOT_RUNNING:
                // if the Darkstar server shuts down, stop the SAS provider
                if (getStatus() == Status.RUNNING ||
                    getStatus() == Status.STARTING_UP)
                {
                    super.stop();
                }
                break;
        }
    }
}
