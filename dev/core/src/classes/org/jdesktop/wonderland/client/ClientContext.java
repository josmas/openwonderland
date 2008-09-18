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

import java.util.HashMap;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSessionManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Provides global static access to the various client subsystems.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class ClientContext {

    private static HashMap<WonderlandSession, CellCache> cellCaches=null;
    private static WonderlandSessionManager sessionManager = null;
    // TODO: private static HashMap<WonderlandSession, SasUserClient> sasUserClients = null;

    /**
     * Return the CellCache if the session has one, otherwise
     * return null
     * @return
     */
    public static CellCache getCellCache(WonderlandSession session) {
        if (cellCaches==null)
            return null;
        return cellCaches.get(session);
    }
    
    /**
     * Return the SAS user client if the session has one, otherwise return null
     */
    /* TODO
    public static UserClient getUserClient(WonderlandSession session) {
        if (sasUserClients==null)
            return null;
        return sasUserClients.get(session);
    }
    */

    /**
     * Register the implementation of CellCache for the session. This
     * call can only be made once. If you attempt to call this method more
     * than once a RuntimeException will be thrown;
     * @param clientCellCache
     */
    public static void registerCellCache(CellCache clientCellCache, WonderlandSession session) {
        if (cellCaches==null) {
            cellCaches = new HashMap();
        }
        
        CellCache previous = cellCaches.put(session, clientCellCache);
        
        if (previous!=null)
            throw new RuntimeException("registerCellCache can only be called once");
    }
    
    /**
     * Register the implementation of the SAS UserClient for the session. This
     * call can only be made once. If you attempt to call this method more
     * than once a RuntimeException will be thrown;
     * @param sasUserClient 
     */
    /* TODO
    public static void registerSasUserClient(SasUserClient sasUserClient, WonderlandSession session) {

        if (sasUserClients==null) {
	    sasUserCaches = new HashMap<WonderlandSession, UserClient>();
        }
        
        SasUserClient previous = sasUserClients.put(session, sasUserClient);
        
        if (previous!=null)
            throw new RuntimeException("registerSasUserClient can only be called once per session");
    }
    */

    /**
     * Return the WonderlandSessionManager for this client
     * 
     */
    public static WonderlandSessionManager getWonderlandSessionManager() {
        if (sessionManager==null)
            sessionManager = new WonderlandSessionManager();
        return sessionManager;
    }
    
    /**
     * Return the CellManager for this client
     * @return
     */
    public static CellManager getCellManager() {
        return CellManager.getCellManager();
    }
}
