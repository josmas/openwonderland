/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitysession.noauth.weblib;

import java.util.Map;
import org.jdesktop.wonderland.modules.security.weblib.serverauthmodule.SessionResolver;

/**
 *
 * @author jkaplan
 */
public class InternalSessionResolverImpl implements SessionResolver {

    public void initialize(Map opts) {
        // nothing to do
    }

    public String getUserId(String token) {
        String out = null;
        
        SessionManager sm = SessionManagerFactory.getSessionManager();
        UserRecord record = sm.getByToken(token);
        if (record != null) {
            out = record.getUserId();
        }

        return out;
    }
}
