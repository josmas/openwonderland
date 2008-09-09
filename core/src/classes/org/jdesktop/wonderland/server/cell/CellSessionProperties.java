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
package org.jdesktop.wonderland.server.cell;

import java.io.Serializable;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.config.CellConfig;

/**
 * The properties of the cell for a particular combination of session
 * and client capabilities
 * 
 * @author paulby
 */
@ExperimentalAPI
public class CellSessionProperties implements Serializable {

    private ViewCellCacheRevalidationListener viewCacheOperation;
    private String clientCellClassName;
    private CellConfig clientCellSetup;
    
    public CellSessionProperties(ViewCellCacheRevalidationListener viewCacheOperation,
            String clientCellClassName,
            CellConfig clientCellSetup) {
        this.viewCacheOperation = viewCacheOperation;
        this.clientCellClassName = clientCellClassName;
        this.clientCellSetup = clientCellSetup;
    }

    /**
     * Returns the ViewCacheOperation, or null
     * @return
     */
    public ViewCellCacheRevalidationListener getViewCellCacheRevalidationListener() {
        return viewCacheOperation;
    }

    /**
     * Return the name of the class name of this cell that will instantiated
     * on the client for this session
     * @return
     */
    public String getClientCellClassName() {
        return clientCellClassName;
    }

    /**
     * Return the setup data that will be sent to the client for this session
     * @return
     */
    public CellConfig getClientCellSetup() {
        return clientCellSetup;
    }
    
}
