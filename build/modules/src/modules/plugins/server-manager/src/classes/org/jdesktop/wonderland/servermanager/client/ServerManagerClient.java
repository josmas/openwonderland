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
package org.jdesktop.wonderland.servermanager.client;

import org.jdesktop.wonderland.client.comms.BaseClient;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.servermanager.common.ServerManagerClientType;


/**
 *
 * @author paulby
 */
public class ServerManagerClient extends BaseClient {

    public ServerManagerClient() {
    }
    
    public ClientType getClientType() {
        return ServerManagerClientType.CLIENT_TYPE;
    }

    public void messageReceived(Message message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}