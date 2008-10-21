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
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * Provides global static access to the various client subsystems.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class ClientContext {

    private static HashMap<WonderlandSession, CellCache> cellCaches=null;
    private static InputManager inputManager=null;
    private static WonderlandSessionManager sessionManager = new WonderlandSessionManager();
    
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
     * Register the implementation of CellCache for the session. This
     * call can only be made once. If you attempt to call this method more
     * than once a RuntimeException will be thrown;
     * @param clientCellCache
     */
    @InternalAPI
    public static void registerCellCache(CellCache clientCellCache, WonderlandSession session) {
        if (cellCaches==null) {
            cellCaches = new HashMap();
        }
        
        CellCache previous = cellCaches.put(session, clientCellCache);
        
        if (previous!=null)
            throw new RuntimeException("registerCellCache can only be called once");
    }
    
    /**
     * Return the CellManager for this client
     * @return
     */
    public static CellManager getCellManager() {
        return CellManager.getCellManager();
    }
    
    /**
     * 
     * @param regInputManager
     */
    @InternalAPI
    public static void registerInputManager(InputManager regInputManager) {
        if (inputManager!=null)
            throw new RuntimeException("registerInputManager can only be called once");
        
        inputManager = regInputManager;
    }

    public static InputManager getInputManager() {
        return inputManager;
    }

    public static WonderlandSessionManager getWonderlandSessionManager() {
        return sessionManager;
    }
}
