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

import org.jdesktop.wonderland.common.cell.CellID;

/**
 * A mirror of a cell from the Bounds service
 * 
 * @author paulby
 */
public interface CellMirror {
    
    /**
     * Returns the cell ID
     * @return
     */
    public CellID getCellID();
    
    /**
     * Return the version number of the cells transform
     * @return
     */
    public long getTransformVersion();
}
