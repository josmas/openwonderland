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
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.AppContext;

/**
 *
 * @author paulby
 */
public class CellComponentMO implements ManagedObject, Serializable {
    protected ManagedReference<CellMO> cellRef;
    
    public CellComponentMO(CellMO cell) {
        this.cellRef = AppContext.getDataManager().createReference(cell);
    }
    
    protected void setLive(boolean live) {
        // Do nothing by default 
    }
}
