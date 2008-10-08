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

import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.Runner;
import org.jdesktop.wonderland.runner.RunnerException;
import org.jdesktop.wonderland.runner.wrapper.RunnerListWrapper;

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
                        @PathParam(value="action") String action) 
    {
        try {
            Runner r = RunManager.getInstance().get(runner);
            if (r == null) {
                throw new RunnerException("Request for unknown runner: " + 
                                          runner);
            }
        
        
            if (action.equalsIgnoreCase("start")) {
                r.start(new Properties());
            } else if (action.equalsIgnoreCase("stop")) {
                r.stop();
            } else {
                throw new RunnerException("Unkown action " + action);      
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
