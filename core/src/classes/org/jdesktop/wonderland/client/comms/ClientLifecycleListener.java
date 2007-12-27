/**
 * Project Wonderland
 *
 * $RCSfile: LogControl.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.3 $
 * $Date: 2007/10/23 18:27:41 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.comms;

/**
 * Listen for new Wonderland clients being created, and changes to the
 * connection status of Wonderland clients.
 * @author kaplanj
 */
public interface ClientLifecycleListener {
    /**
     * Called when a new WonderlandClient is created.  When this method is
     * called, the client has been initialized, but not logged in.
     * @param client the client that was created
     */
    public void clientCreated(BaseClient client);
    
    
    /**
     * Called when a client changes state, to one of the
     * CONNECTED, CONNECTING, or DISCONNECTED states.
     */
    public void clientStatusChanged(BaseClient client, 
                                    BaseClient.Status status);
}
