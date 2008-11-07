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

package org.jdesktop.wonderland.runner.resources;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.jdesktop.wonderland.runner.DeploymentEntry;
import org.jdesktop.wonderland.runner.DeploymentManager;
import org.jdesktop.wonderland.runner.DeploymentPlan;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.Runner;
import org.jdesktop.wonderland.runner.RunnerException;

/**
 * Utility methods used by ActionResource and RunnerActionResource
 * 
 * @author jkaplan
 */
public abstract class BaseActionResource {

    /**
     * Start the given runner.
     * @param runner the runner to start
     * @param wait whether or not to wait for the runner to start
     * @return the StatusWaiter that waits for this runner to start, or
     * null if wait is false
     * @throws RunnerException if there is a problem starting the runner
     */
    protected StatusWaiter startRunner(Runner runner, boolean wait)
        throws RunnerException
    {
        StatusWaiter out = null;
    
        if (runner.getStatus() == Runner.Status.NOT_RUNNING) {
            // find a properties file from the current deployment plan
            Properties props;
            DeploymentPlan dp = DeploymentManager.getInstance().getPlan();
            DeploymentEntry de = dp.getEntry(runner.getName());
            if (de != null && !de.getRunProps().isEmpty()) {
                props = de.getRunProps();
            } else {
                props = runner.getDefaultProperties();
            }
            
            runner.start(props);
        
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
    protected StatusWaiter stopRunner(Runner runner, boolean wait)
        throws RunnerException
    {
        StatusWaiter out = null;
    
        if (runner.getStatus() == Runner.Status.RUNNING) {
            runner.stop();
        
            if (wait) {
                out = new StatusWaiter(runner, Runner.Status.NOT_RUNNING);
            }
        } 
        
        return out;
    }
}
