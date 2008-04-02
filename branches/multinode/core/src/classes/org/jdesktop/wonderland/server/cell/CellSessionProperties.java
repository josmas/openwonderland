/*
 *  Project Wonderland
 * 
 *  $Id$
 * 
 *  Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 * 
 *  Redistributions in source code form must reproduce the above
 *  copyright and this condition.
 * 
 *  The contents of this file are subject to the GNU General Public
 *  License, Version 2 (the "License"); you may not use this file
 *  except in compliance with the License. A copy of the License is
 *  available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 *  $Revision$
 *  $Date$
 */

package org.jdesktop.wonderland.server.cell;

import java.io.Serializable;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The properties of the cell for a particular combination of session
 * and client capabilities
 * 
 * @author paulby
 */
@ExperimentalAPI
public class CellSessionProperties implements Serializable {

    private ViewCellCacheRevalidationListener viewCacheOperation=null;
    
    public CellSessionProperties() {
    }

    /**
     * Returns the ViewCacheOperation, or null
     * @return
     */
    public ViewCellCacheRevalidationListener getViewCacheOperation() {
        return viewCacheOperation;
    }

    /**
     * Set the ViewCacheOperation. This object can be session and capability
     * specific.
     * 
     * @param viewCacheOperation
     */
    public void setViewCacheOperation(ViewCellCacheRevalidationListener viewCacheOperation) {
        this.viewCacheOperation = viewCacheOperation;
    }
    
}
