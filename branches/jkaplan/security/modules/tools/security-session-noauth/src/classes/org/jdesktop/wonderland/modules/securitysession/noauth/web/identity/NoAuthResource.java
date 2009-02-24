/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitysession.noauth.web.identity;

import com.sun.jersey.api.Responses;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.directory.BasicAttribute;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.modules.securitysession.noauth.weblib.SessionManager;
import org.jdesktop.wonderland.modules.securitysession.noauth.weblib.SessionManagerFactory;
import org.jdesktop.wonderland.modules.securitysession.noauth.weblib.UserRecord;

/**
 *
 * @author jkaplan
 */
@Path("noauth")
public class NoAuthResource {
    private static final Logger logger =
            Logger.getLogger(NoAuthResource.class.getName());

    private final SessionManager sm = SessionManagerFactory.getSessionManager();

    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response post(@FormParam("username") String username,
                         @FormParam("fullname") String fullname,
                         @FormParam("email") String email)
    {
        if (username == null) {
            return Responses.notAcceptable().build();
        }

        // decode arguments
        try {
            username = URLDecoder.decode(username, "UTF-8");
            fullname = URLDecoder.decode(fullname, "UTF-8");
            email = URLDecoder.decode(email, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            logger.log(Level.WARNING, "Decoding error", uee);
            throw new WebApplicationException(uee,
                                        Response.Status.INTERNAL_SERVER_ERROR);
        }


        // get or create the record
        UserRecord rec = sm.get(username, true);

        // update attributes
        rec.getAttributes().put(new BasicAttribute("uid", username));
        rec.getAttributes().put(new BasicAttribute("cn", fullname));
        rec.getAttributes().put(new BasicAttribute("mail", email));

        String res = "string=" + rec.getToken();
        return Response.ok(res).build();
    }
}
