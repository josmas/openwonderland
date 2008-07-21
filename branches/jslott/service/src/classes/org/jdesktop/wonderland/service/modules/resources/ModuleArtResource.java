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

import java.io.InputStream;
import java.io.StringWriter;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.modules.ModuleResource;
import org.jdesktop.wonderland.service.modules.InstalledModule;
import org.jdesktop.wonderland.service.modules.ModuleManager;

/**
 * The ModuleArtResource class is a Jersey RESTful service that returns some
 * art that is contained within the module system. The getModuleArt() method
 * handles the HTTP GET request.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path(value="/module/{modulename}/art/{path}", limited=false)
public class ModuleArtResource {
    
    /**
     * Returns a piece of artwork that is stored within the module system,
     * given the name of the module and the relative path of the art resource
     * encoded into the URI. The format of the URI is:
     * <p>
     * /module/{modulename}/art/{path}
     * <p>
     * where {modulename} is the name of the module and {path} is the relative
     * path of the art resource. All spaces in the module name must be encoded
     * to %20. Returns INVALID to the HTTP connection if
     * the module name is invalid or if there was an error encoding the module's
     * information.
     * 
     * @param moduleName The unique name of the module
     * @return An XML encoding of the module's basic information
     */
    @GET
    public Response getModuleArt(@PathParam("modulename") String moduleName, @PathParam("path") String path) {
        System.out.println(moduleName);
        System.out.println(path);
        /* Fetch the module from the module manager */
        ModuleManager mm = ModuleManager.getModuleManager();
        InstalledModule im = mm.getInstalledModule(moduleName);
        if (im == null) {
            // XXX write out to log
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        /*
         * If the path has a leading slash, then remove it (this is typically
         * the case with @PathParam
         */
        if (path.startsWith("/") == true) {
            path = path.substring(1);
        }
        
        /* Fetch the input stream for the art resource */
        ModuleResource mr = im.getModuleArtResource(path);
        if (mr == null) {
            // XXX write out to log
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        InputStream is = im.getInputStream(mr);
        ResponseBuilder rb = Response.ok(is);
        return rb.build();
    }
}
