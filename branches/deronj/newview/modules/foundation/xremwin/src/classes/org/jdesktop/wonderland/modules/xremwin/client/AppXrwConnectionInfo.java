/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.xremwin.client;

import java.io.Serializable;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The subclass-specific data that the Xremwin master communicates to 
 * Xremwin slaves to allow them to connect to the master's server socket.
 *
 * @author deronj
 */
@InternalAPI
public class AppXrwConnectionInfo implements Serializable {

    /** The name of the host to which the slave should connect. */
    private String hostName;
    /** The port number to which the slave should connect. */
    private int portNum;

    /**
     * Create a new instance of AppXrwConnectionInfo.
     *
     * @param hostName The name of the host to which the slave should connect.
     * @param portNum The port number to which the slave should connect.
     */
    AppXrwConnectionInfo(String hostName, int portNum) {
        this.hostName = hostName;
        this.portNum = portNum;
    }

    /**
     * Returns the host name.
     * @return The host name.
     */
    String getHostName() {
        return hostName;
    }

    /**
     * Returns the port number
     * @return The port number.
     */
    int getPortNum() {
        return portNum;
    }
}