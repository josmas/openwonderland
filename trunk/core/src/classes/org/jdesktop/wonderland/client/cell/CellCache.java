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
package org.jdesktop.wonderland.client.cell;

import org.jdesktop.wonderland.client.avatar.ViewCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public interface CellCache {

    /**
     * Return the cell with the specified id
     * @param cellId
     * @return
     */
    public Cell getCell(CellID cellId);
    
    /**
     * Return the session with which this cell cache is associated. 
     * 
     * @return
     */
    public WonderlandSession getSession();
    
    /**
     * Set the view cell for this cache
     * 
     * @param viewCell the view cell
     */
    public void viewSetup(ViewCell viewCell);
}
