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
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.Runner;
import org.jdesktop.wonderland.runner.RunnerException;

/**
 * The RunnerActionResource class is a Jersey RESTful service that allows
 * services to be started and stopped using a REST API. The get() 
 * method handles the HTTP GET request.
 * 
 * @author jkaplan
 */
@Path(value="/{runner}/{action}")
public class RunnerActionResource {
    private static final Logger logger =
            Logger.getLogger(RunnerActionResource.class.getName());
    
    /**
     * Return a list of all runners currently running.
     * @return An XML encoding of the module's basic information
     */
    @GET
    @ProduceMime("text/plain")
    public Response get(@PathParam(value="runner") String runner,
                        @PathParam(value="action") String action,
                        @QueryParam(value="wait")  String waitParam) 
    {
        try {
            Runner r = RunManager.getInstance().get(runner);
            if (r == null) {
                throw new RunnerException("Request for unknown runner: " + 
                                          runner);
            }
        
            boolean wait = false;
            if (waitParam != null) {
                wait = Boolean.parseBoolean(waitParam);
            }
            StatusWaiter waiter = null;
            
            if (action.equalsIgnoreCase("start")) {
                waiter = startRunner(r, wait);
            } else if (action.equalsIgnoreCase("stop")) {
                waiter = stopRunner(r, wait);
            } else if (action.equalsIgnoreCase("restart")) {
                // stop the runner and wait for it to stop
                waiter = stopRunner(r, true);
                if (waiter != null) {
                    waiter.waitFor();
                }
                
                // restart the runner
                waiter = startRunner(r, wait);
            } else {
                throw new RunnerException("Unkown action " + action);      
            } 
            
            // if necessary, wait for the runner
            if (waiter != null) {
                waiter.waitFor();
            }
            
            ResponseBuilder rb = Response.ok();
            return rb.build();
        } catch (RunnerException re) {
            logger.log(Level.WARNING, re.getMessage(), re);
            ResponseBuilder rb = Response.status(Status.BAD_REQUEST);
            return rb.build();
        } catch (InterruptedException ie) {
            logger.log(Level.WARNING, ie.getMessage(), ie);
            ResponseBuilder rb = Response.status(Status.INTERNAL_SERVER_ERROR);
            return rb.build();
        }
    }
    
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
            runner.start(new Properties());
        
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
