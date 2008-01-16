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

import javax.media.j3d.Bounds;
import org.jdesktop.wonderland.common.cell.MultipleParentException;

/**
 * Cells that implement this interface support automatic insertion of content
 * in the cell hierarchy.
 * 
 * TODO Better name required
 *
 * @author paulby
 */
public interface CellContainerInterface {

    /**
     * Traverse the cell hierarchy and insert the child cell in the most
     * appropriate place. If the cell is inserted successfully its new parent is
     * returned, otherwise null is returned.
     * 
     * The CellMO passed to this method must not have an existing parent
     *
     * @returns  the parent
     */
    public CellMO insertCellInHierarchy(CellMO insertChild, Bounds cellVWBounds) throws MultipleParentException;
}
