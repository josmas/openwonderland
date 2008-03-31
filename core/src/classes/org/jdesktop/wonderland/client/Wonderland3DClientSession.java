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
package org.jdesktop.wonderland.client;

import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.client.cell.CellHandler;
import org.jdesktop.wonderland.client.comms.AttachFailureException;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSessionImpl;

/**
 * An extension of WonderlandSession that attaches all the relevant
 * clients needed for the standard 3D client.
 * @author jkaplan
 */
@ExperimentalAPI
public class Wonderland3DClientSession extends WonderlandSessionImpl {
    
    /** the cell client */
    private CellHandler cellClient;
    
    public Wonderland3DClientSession(WonderlandServerInfo serverInfo) {
        super (serverInfo);
        
        cellClient = new CellHandler();
    }
    
    /** 
     * Get the CellHandler for sending cell messages 
     * @return the cell client
     */
    public CellHandler getCellClient() {
        return cellClient;
    }

    /**
     * Override the login message to attach clients after the login
     * succeeds.  If a client fails to attach, the login will be aborted and
     * a LoginFailureException will be thrown
     * @param loginParams the parameters to login with
     * @throws LoginFailureException if the login fails or any of the clients
     * fail to attach
     */
    @Override
    public void login(LoginParameters loginParams) 
            throws LoginFailureException 
    {
        // this will wait for login to succeed
        super.login(loginParams);
        
        // if login succeeds, attach the various clients
        try {
            cellClient.attach(this);
        } catch (AttachFailureException afe) {
            // a client failed to attach -- logout
            disconnect();
            
            // throw a login exception
            throw new LoginFailureException("Failed to attach client" , afe);
        }
    }
}
