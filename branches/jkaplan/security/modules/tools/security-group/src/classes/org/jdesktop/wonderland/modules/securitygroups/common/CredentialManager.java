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
