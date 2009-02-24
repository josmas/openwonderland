/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitysession.noauth.web.identity;

import com.sun.jersey.api.Responses;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.modules.securitysession.noauth.weblib.SessionManager;
import org.jdesktop.wonderland.modules.securitysession.noauth.weblib.SessionManagerFactory;

/**
 *
 * @author jkaplan
 */
@Path("isTokenValid")
public class ValidateResource {
    private SessionManager sm = SessionManagerFactory.getSessionManager();

    @GET
    public Response get(@QueryParam("tokenid") String tokenId) {
        return post(tokenId);
    }

    @POST
    public Response post(@QueryParam("tokenid") String tokenId) {
        if (tokenId == null) {
            return Responses.notAcceptable().build();
        }

        boolean res = (sm.getByToken(tokenId) != null);
        String out = "boolean=" + String.valueOf(res);
        return Response.ok(out).build();
    }
}
