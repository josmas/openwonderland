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

import java.util.ArrayList;
import java.util.Collection;
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
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.Runner;
import org.jdesktop.wonderland.runner.RunnerException;

/**
 * The ActionResource class is a Jersey RESTful service that allows
 * all services to be started and stopped using a REST API. The get() 
 * method handles the HTTP GET request.
 * 
 * @author jkaplan
 */
@Path(value="/all/{action}")
public class ActionResource extends RunnerActionResource {
    private static final Logger logger =
            Logger.getLogger(ActionResource.class.getName());
    
    /**
     * Return a list of all runners currently running.
     * @return An XML encoding of the module's basic information
     */
    @GET
    @Produces({"text/plain", "application/xml", "application/json"})
    public Response get(@PathParam(value="action") String action,
                        @QueryParam(value="wait") String waitParam) 
    {
        boolean wait = false;
        if (waitParam != null) {
            wait = Boolean.parseBoolean(waitParam);
        }
        
        Collection<Runner> runners = RunManager.getInstance().getAll();
        Collection<StatusWaiter> waiters = new ArrayList<StatusWaiter>();
        
        try {
            if (action.equalsIgnoreCase("start")) {
                for (Runner r : runners) {
                    StatusWaiter w = startRunner(r, wait);
                    if (w != null) {
                        waiters.add(w);
                    }
                }
            } else if (action.equalsIgnoreCase("stop")) {
                for (Runner r : runners) {
                    StatusWaiter w = stopRunner(r, wait);
                    if (w != null) {
                        waiters.add(w);
                    }
                }
            } else if (action.equalsIgnoreCase("restart")) {
                // first stop everyone
                for (Runner r : runners) {
                    StatusWaiter w = stopRunner(r, true);
                    if (w != null) {
                        waiters.add(w);
                    }
                }
                
                // now wait for all runners to stop
                for (StatusWaiter sw : waiters) {
                    try {
                        sw.waitFor();
                    } catch (InterruptedException ie) {
                        // ignore
                    }
                }
                waiters.clear();
                
                // now start everyone back up
                for (Runner r : runners) {
                    StatusWaiter w = startRunner(r, wait);
                    if (w != null) {
                        waiters.add(w);
                    }
                }
            } else {
                throw new RunnerException("Unkown action " + action);      
            } 
            
            // wait for start or stop
            // now wait for all runners to stop
            for (StatusWaiter sw : waiters) {
                try {
                    sw.waitFor();
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
            
            ResponseBuilder rb = Response.ok();
            return rb.build();
        } catch (RunnerException re) {
            logger.log(Level.WARNING, re.getMessage(), re);
            ResponseBuilder rb = Response.status(Status.BAD_REQUEST);
            return rb.build();
        }
    }
}
