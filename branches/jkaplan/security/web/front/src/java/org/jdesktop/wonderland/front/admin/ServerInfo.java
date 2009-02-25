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
package org.jdesktop.wonderland.front.admin;

import org.jdesktop.wonderland.client.login.DarkstarServer;
import org.jdesktop.wonderland.client.login.ServerDetails;
import org.jdesktop.wonderland.common.login.AuthenticationInfo;
import org.jdesktop.wonderland.utils.Constants;

/**
 * Some basic information about the server
 * @author jkaplan
 */
public class ServerInfo {
    private static ServerDetails serverDetails;

    static {
       serverDetails = new ServerDetails();
       serverDetails.setServerURL(getServerURL());

       // default to no authentication
       AuthenticationInfo authInfo =
               new AuthenticationInfo(AuthenticationInfo.Type.NONE, null);
       serverDetails.setAuthInfo(authInfo);

       // Darkstar servers will be filled in later as they are
       // created
       serverDetails.setDarkstarServers(new DarkstarServer[0]);
    }

    /**
     * Get the base server URL
     * @return the base server URL for this server
     */
    public static String getServerURL() {
        return System.getProperty(Constants.WEBSERVER_URL_PROP);
    }

    /**
     * Get the server details object that will be sent to clients.
     * Modifying this object modifies the values that are sent to all
     * clients.
     * @return the serverDetails object
     */
    public static ServerDetails getServerDetails() {
        return serverDetails;
    }
}
