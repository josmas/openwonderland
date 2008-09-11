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
package org.jdesktop.wonderland.servermanager.common;

import org.jdesktop.wonderland.common.comms.ClientType;

/**
 * The client type for server managers
 * @author jkaplan
 */
public class ServerManagerClientType extends ClientType {
    /** The server manager client type */
    public static final ClientType CLIENT_TYPE =
            new ServerManagerClientType();
    
    /** Use the static CLIENT_TYPE, not this constructor */
    public ServerManagerClientType() {
        super ("__ServerManagerClient");
    }
}
