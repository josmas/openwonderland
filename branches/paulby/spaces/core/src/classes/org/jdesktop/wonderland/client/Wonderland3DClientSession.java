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
package org.jdesktop.wonderland.client;

import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.client.cell.CellChannelConnection;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSessionImpl;

/**
 * An extension of WonderlandSession that attaches all the relevant
 * handlers needed for the standard 3D client.
 * @author jkaplan
 */
@InternalAPI
public class Wonderland3DClientSession extends WonderlandSessionImpl {
    
    /** the cell channel handler */
    private CellChannelConnection cellClient;
    
    public Wonderland3DClientSession(WonderlandServerInfo serverInfo) {
        super (serverInfo);
        
        cellClient = new CellChannelConnection();
    }
    
    /** 
     * Get the CellChannelConnection for sending cell messages 
     * @return the cell client
     */
    public CellChannelConnection getCellChannelHandler() {
        return cellClient;
    }

    /**
     * Override the login message to connect clients after the login
     * succeeds.  If a client fails to connect, the login will be aborted and
     * a LoginFailureException will be thrown
     * @param loginParams the parameters to login with
     * @throws LoginFailureException if the login fails or any of the clients
     * fail to connect
     */
    @Override
    public void login(LoginParameters loginParams) 
            throws LoginFailureException 
    {
        // this will wait for login to succeed
        super.login(loginParams);
        
        // if login succeeds, connect the various clients
        try {
            cellClient.connect(this);
        } catch (ConnectionFailureException afe) {
            // a client failed to connect -- logout
            logout();
            
            // throw a login exception
            throw new LoginFailureException("Failed to attach client" , afe);
        }
    }
}
