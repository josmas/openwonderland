/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.login;

import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.common.login.AuthenticationInfo;

/**
 *
 * @author Ryan
 */
public class NoAuthLoginControl extends WebServiceLoginControl {

    public NoAuthLoginControl(AuthenticationInfo info, ServerSessionManager sessionManager) {
        super(info, sessionManager);
    }

    @Override
    public void requestLogin(LoginUI ui) {
        super.requestLogin(ui);
        // only request credentials from the user if we don't have them
        // from an existing AuthenticationService
        if (needsLogin()) {
            ui.requestLogin(this);
        }
    }

    public void authenticate(String username, String fullname) throws LoginFailureException {
        super.authenticate(username, fullname);
    }
    
}
