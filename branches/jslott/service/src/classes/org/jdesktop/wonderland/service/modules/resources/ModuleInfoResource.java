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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import org.jdesktop.wonderland.service.modules.InstalledModule;
import org.jdesktop.wonderland.service.modules.ModuleManager;

/**
 * The ModuleInfoResource class is a Jersey RESTful service that returns the
 * basic information about a module given its name encoding into a request
 * URI. The getModuleInfo() method handles the HTTP GET request.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path("/module/{modulename}/info")
public class ModuleInfoResource {
    
    /**
     * Returns the basic information about a module, given its module name
     * encoded into the URI. The format of the URI is:
     * <p>
     * /module/{modulename}/info
     * <p>
     * where {modulename} is the name of the module. All spaces in the module
     * name must be encoded to %20. Returns INVALID to the HTTP connection if
     * the module name is invalid or if there was an error encoding the module's
     * information.
     * 
     * @param moduleName The unique name of the module
     * @return An XML encoding of the module's basic information
     */
    @GET
    @ProduceMime("text/plain")
    public String getModuleInfo(@PathParam("modulename") String moduleName) {
        /* Fetch the module from the module manager */
        ModuleManager mm = ModuleManager.getModuleManager();
        InstalledModule im = mm.getInstalledModule(moduleName);
        if (im == null) {
            return "INVALID";
        }
        
        /* Write the XML encoding to a writer and return it */
        StringWriter sw = new StringWriter();
        try {
            im.getModuleInfo().encode(sw);
        } catch (javax.xml.bind.JAXBException excp) {
            return "INVALID";
        }
        return sw.toString();
    }
}
