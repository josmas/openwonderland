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

package org.jdesktop.wonderland.modules.service.resources;

import java.io.StringWriter;
import java.util.Collection;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.modules.util.ModuleList;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.modules.service.ModuleManager.State;

/**
 * The ModuleListResource class is a Jersey RESTful service that returns the
 * names of all of the modules in a given state. The state is defined by
 * the state query argument which can be either "added", "pending", "installed",
 * or "removed". The getModuleInfo() method handles the HTTP GET request.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path("/list/{state}")
public class ModuleListResource {
    
    /**
     * Returns a list of modules in a given state.
     * <p>
     * /module/list/{state}
     * <p>
     * where {state} is the state of the module, either added, pending,
     * installed, or removed.
     * <p>
     * All spaces in the module name must be encoded to %20. Returns BAD_REQUEST
     * to the HTTP connection if the module name is invalid or if there was an
     * error encoding the module's information.
     * 
     * @param moduleName The unique name of the module
     * @return An XML encoding of the module's basic information
     */
    @GET
    @ProduceMime("text/plain")
    public Response getModuleInfo(@PathParam("state") String state) {
        /* Fetch thhe error logger for use in this method */
        Logger logger = ModuleManager.getLogger();
        
        /* Ignore the state for now XXX */
        /*
         * Fetch the module from the module manager, convert into a ModuleList
         * object, encoding and return
         */
        try {
            Collection<String> modules = ModuleManager.getModuleManager().getModules(State.INSTALLED);
            ModuleList moduleList = new ModuleList(modules.toArray(new String[] {}));
            StringWriter sw = new StringWriter();
            moduleList.encode(sw);
            ResponseBuilder rb = Response.ok(sw.toString());
            return rb.build();
        } catch (javax.xml.bind.JAXBException excp) {
            /* Log an error and return an error response */
            logger.warning("ModuleManager: unable to encode module list");
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
    }
}
