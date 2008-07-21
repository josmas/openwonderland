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

package org.jdesktop.wonderland.service.wfs.resources;

import java.io.StringWriter;
import java.util.logging.Level;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.service.wfs.WFSManager;
import org.jdesktop.wonderland.wfs.WFSRoots;

/**
 * The WFSRootsResource class is a Jersey RESTful resource that allows clients
 * to query for the WFS root names by using a URI. 
 * <p>
 * The format of the URI is: /wfs/roots/directory.
 * <p>
 * The root information returned is the JAXB serialization of the root name
 * information (the WFSRoots class).
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path(value="/wfs/roots")
public class WFSRootsResource {
    
    /**
     * Returns the JAXB XML serialization of the WFS root names. Returns
     * the XML via an HTTP GET request.
     * 
     * @return The XML serialization of the cell setup information via HTTP GET
     */
    @GET
    public Response getCellResource() {
        /* Fetch the wfs manager and the WFS roots. */
        WFSManager wfsm = WFSManager.getWFSManager();
        String roots[] = wfsm.getWFSRoots();
        WFSRoots wfsRoots = new WFSRoots(roots);
        
        /* Send the serialized cell names to the client */
        try {
            StringWriter sw = new StringWriter();
            wfsRoots.encode(sw);
            ResponseBuilder rb = Response.ok(sw.toString());
            return rb.build();
        } catch (javax.xml.bind.JAXBException excp) {
            WFSManager.getLogger().log(Level.SEVERE, "Unable to write roots: " + excp.toString());
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
    }
}
