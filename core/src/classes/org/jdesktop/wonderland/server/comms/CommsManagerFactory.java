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
package org.jdesktop.wonderland.server.comms;

import com.sun.sgs.app.AppContext;

/**
 * Create the comms manager.
 * @author jkaplan
 */
public class CommsManagerFactory {
    static final String BINDING_NAME = CommsManagerImpl.class.getName();
    
    /**
     * Setup the comms manager
     */
    public static void initialize() {
        // initialize the comms manager
        CommsManagerImpl.initialize();
        
        // instantiate the manager
        CommsManagerImpl cm = new CommsManagerImpl();
        AppContext.getDataManager().setBinding(BINDING_NAME, cm);
    }

    /**
     * Get the comms manager
     * @return the comms manager
     */
    public static CommsManager getCommsManager() {
        return (CommsManager) AppContext.getDataManager().getBinding(BINDING_NAME);
    }
}
