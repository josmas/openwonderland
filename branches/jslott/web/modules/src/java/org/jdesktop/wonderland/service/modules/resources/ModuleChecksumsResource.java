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

package org.jdesktop.wonderland.service.modules.resources;

import java.io.StringWriter;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.checksum.RepositoryChecksums;
import org.jdesktop.wonderland.service.modules.InstalledModule;
import org.jdesktop.wonderland.service.modules.ModuleManager;

/**
 * The ModuleChecksumsResource class is a Jersey RESTful service that returns the
 * checksum information about all resources within a module given its name
 * encoded into a request URI. The getModuleChecksums() method handles the HTTP
 * GET request.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path("/{modulename}/checksums")
public class ModuleChecksumsResource {
    
    /**
     * Returns the checksums information about a module's resources, given its
     * module name encoded into the URI. The format of the URI is:
     * <p>
     * /module/{modulename}/checksums
     * <p>
     * where {modulename} is the name of the module. All spaces in the module
     * name must be encoded to %20. Returns BAD_REQUEST to the HTTP connection if
     * the module name is invalid or if there was an error encoding the module's
     * information.
     * 
     * @param moduleName The unique name of the module
     * @return An XML encoding of the module's basic information
     */
    @GET
    @ProduceMime("text/plain")
    public Response getModuleInfo(@PathParam("modulename") String moduleName) {
        /* Fetch thhe error logger for use in this method */
        Logger logger = ModuleManager.getLogger();
        
        /* Fetch the module from the module manager */
        ModuleManager mm = ModuleManager.getModuleManager();
        InstalledModule im = mm.getInstalledModule(moduleName);
        if (im == null) {
            /* Log an error and return an error response */
            logger.warning("ModuleManager: unable to locate module " + moduleName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        /* Check to see that the module checksums exist, return error if not */
        RepositoryChecksums rc = im.getModuleChecksums();
        if (rc == null) {
            /* Log an error and return an error response */
            logger.warning("ModuleManager: unable to locate module checksums: " + moduleName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        /* Write the XML encoding to a writer and return it */
        StringWriter sw = new StringWriter();
        try {
            rc.encode(sw);
            ResponseBuilder rb = Response.ok(sw.toString());
            return rb.build();
        } catch (javax.xml.bind.JAXBException excp) {
            /* Log an error and return an error response */
            logger.warning("ModuleManager: unable to encode module info " + moduleName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
    }
}
