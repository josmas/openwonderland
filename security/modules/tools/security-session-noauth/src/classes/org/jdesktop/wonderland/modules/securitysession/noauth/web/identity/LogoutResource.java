/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitysession.noauth.web.identity;

import com.sun.jersey.api.Responses;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
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
@Path("logout")
public class LogoutResource {
    private SessionManager sm = SessionManagerFactory.getSessionManager();

    @GET
    public Response get(@QueryParam("subjectid") String subjectId) {
        return post(subjectId);
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response post(@FormParam("subjectid") String subjectId) {
        if (subjectId == null) {
            return Responses.notAcceptable().build();
        }

        sm.remove(subjectId);
        return Response.ok().build();
    }
}
