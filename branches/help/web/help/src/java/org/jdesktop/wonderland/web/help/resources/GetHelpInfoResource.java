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

package org.jdesktop.wonderland.web.help.resources;

import java.io.StringWriter;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.common.help.HelpInfo;
import org.jdesktop.wonderland.web.help.deployer.HelpDeployer;

/**
 * The GetHelpInfoResource class is a Jersey RESTful resource that allows clients
 * to query for the XXX.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path(value="/info/get")
public class GetHelpInfoResource {
    
    /**
     * Returns the JAXB XML serialization of the cell setup class given the
     * name of the root WFS (without the -wfs extension) and the path of the
     * cell within the WFS (without any -wld or -wlc.xml extensions). Returns
     * the XML via an HTTP GET request.
     * 
     * @param wfsName The name of the WFS root (no -wfs extension)
     * @param path The relative path of the file (no -wld, -wlc.xml extensions)
     * @return The XML serialization of the cell setup information via HTTP GET.
     */
    @GET
    @ProduceMime("text/plain")
    public Response getHelpInfo() {
        /* Formulate the HTTP response and send the string */
        HelpInfo info = HelpDeployer.getHelpInfo();
        StringWriter sw = new StringWriter();
        try {
            info.encode(sw);
            ResponseBuilder rb = Response.ok(sw.toString());
            return rb.build();
        } catch (Exception excp) {
        }
        return null;
    }
}
