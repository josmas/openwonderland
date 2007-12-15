/**
 * Project Wonderland
 *
 * $RCSfile:$
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
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.server;

import com.sun.sgs.app.AppContext;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.server.cell.CellManager;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public class WonderlandContext {

    /**
     * Return the cell manager singleton.
     *
     * @return 
     */
    public static CellManager getCellManager() {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Return the user manager singleton.
     * @return
     */
    public static UserManager getUserManager() {
        return AppContext.getDataManager().getBinding(UserManager.BINDING_NAME, UserManager.class);        
    }
    
}
