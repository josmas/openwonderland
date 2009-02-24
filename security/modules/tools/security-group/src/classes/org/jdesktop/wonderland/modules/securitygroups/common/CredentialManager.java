/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitygroups.common;

import java.net.HttpURLConnection;

/**
 *
 * @author jkaplan
 */
public interface CredentialManager {
    /**
     * Get the base URL of the server this credential manager supplies
     * credentials for.
     * @return the base url of the server
     */
    public String getBaseURL();

    /**
     * Get the user name this user has authenticated as
     * @return the name of the user
     */
    public String getUsername();

    /**
     * Add the necessary cookies or request headers to convey authorization
     * to the given server.  This method must be called before the connection
     * has connected.
     * @param connection the connection to secure.
     */
    public void secureConnection(HttpURLConnection connection);
}
