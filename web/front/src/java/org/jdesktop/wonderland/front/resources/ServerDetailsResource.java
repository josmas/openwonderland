/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.front.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.jdesktop.wonderland.client.login.AuthenticationInfo;
import org.jdesktop.wonderland.client.login.DarkstarServer;
import org.jdesktop.wonderland.client.login.ServerDetails;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.darkstar.DarkstarRunner;

/**
 * The ServletDetailsResource class is a Jersey RESTful service that returns
 * login details for this server.
 *
 * @author kaplanj
 */
@Path(value="/ServerDetails")
public class ServerDetailsResource {
    private static final Logger logger =
            Logger.getLogger(ServerDetailsResource.class.getName());

    @Context
    private UriInfo uriInfo;

    /**
     * Returns the details for this server
     */
    @GET
    public Response getServerDetails() {
        ServerDetails out = new ServerDetails();
        out.setServerURL(getServerURL());
        out.setAuthInfo(getAuthInfo());
        out.setDarkstarServers(getDarkstarServers());

        try {
            ResponseBuilder rb = Response.ok(out);
            return rb.build();
        } catch (Exception excp) {
            logger.log(Level.WARNING, "Error writing response", excp);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();            
        }
    }

    /**
     * Get the URL for this server
     * @return the server URL
     */
    protected String getServerURL() {
        UriBuilder b = uriInfo.getBaseUriBuilder();
        b.replacePath("");

        return b.build().toString();
    }

    /**
     * Get the authentication info for this server
     * @return the authentication info
     */
    protected AuthenticationInfo getAuthInfo() {
        return new AuthenticationInfo(AuthenticationInfo.Type.NONE, null);
    }

    /**
     * Get the list of Darkstar server to connect to
     * @return the ordered list of Darkstar servers
     */
    protected DarkstarServer[] getDarkstarServers() {
        Collection<DarkstarRunner> runners =
                RunManager.getInstance().getAll(DarkstarRunner.class);

        Collection<DarkstarServer> out =
                new ArrayList<DarkstarServer>(runners.size());

        for (DarkstarRunner runner : runners) {
            DarkstarServer ds = new DarkstarServer(runner.getHostname(),
                                                   runner.getPort());
            out.add(ds);
        }
                
        return out.toArray(new DarkstarServer[out.size()]);
    }
}
