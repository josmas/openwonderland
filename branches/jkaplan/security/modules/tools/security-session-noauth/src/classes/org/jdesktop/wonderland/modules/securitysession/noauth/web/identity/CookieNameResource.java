/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitysession.noauth.web.identity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.modules.security.weblib.serverauthmodule.SessionResolver;

/**
 *
 * @author jkaplan
 */
@Path("getCookieNameForToken")
public class CookieNameResource {
    @GET
    public Response get() {
        String cookieName = "string=" + SessionResolver.COOKIE_NAME;
        return Response.ok(cookieName).build();
    }
}
