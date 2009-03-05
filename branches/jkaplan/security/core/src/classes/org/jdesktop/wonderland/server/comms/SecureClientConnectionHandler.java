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
package org.jdesktop.wonderland.server.comms;

import java.util.Properties;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.server.security.Resource;

/**
 * An extension of the ClientConnectionHandler that allows security checks
 * for who may or may not connect to the handler.
 * @author jkaplan
 */
@ExperimentalAPI
public interface SecureClientConnectionHandler extends ClientConnectionHandler {
    /**
     * Get the resource to use for security checks.  This resource will
     * be queried with the connecting user's ID and and instance of
     * ConnectAction.  If the resource grants access to the ConnectAction,
     * the connection will be allowed to proceed.  If access is denied, the
     * client's connection will be aborted.
     *
     * @param clientID the ID of the session that connected
     * @param properties the properties the client is connecting with
     * @return a resource that can be used for security checks, or null
     * to skip security checks.
     */
    public Resource checkConnect(WonderlandClientID clientID,
                                 Properties properties);
}
