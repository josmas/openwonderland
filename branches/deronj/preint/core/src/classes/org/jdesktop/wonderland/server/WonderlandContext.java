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
package org.jdesktop.wonderland.server;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.CommsManagerFactory;

/**
 * Provides a single point of access to all the major Wonderland subsystems on
 * a server.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class WonderlandContext {

    /**
     * Initialize the WonderlandContext, create all singletons
     */
    static void intialize() {
        // initialize the comms manager
        CommsManagerFactory.initialize();
        CellManagerMO.initialize();
        UserManager.initialize();
    }
    
    /**
     * Return the cell manager singleton.
     * @return  the master cell cache
     */
    public static CellManagerMO getCellManager() {
        return CellManagerMO.getCellManager();
    }
    
    /**
     * Return the user manager singleton.
     * @return the user manager
     */
    public static UserManager getUserManager() {
        return UserManager.getUserManager();      
    }
    
    /**
     * Return the communications manager singleton
     * @return the communications manager
     */
    public static CommsManager getCommsManager() {
        return CommsManagerFactory.getCommsManager();
    }
    
}
