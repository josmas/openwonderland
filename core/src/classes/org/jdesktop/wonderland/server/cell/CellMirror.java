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

import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;

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
     * Returns the version number of the cells contents
     * @return
     */
    public int getContentsVersion();
    
    /**
     * Return the version number of the cells transform
     * @return
     */
    public int getTransformVersion();
    
     /**
     * Returns a copy of the cell's local bounds
     * @return
     */
    public BoundingVolume getLocalBounds();
    
    /**
     * Returns a copy of the cell's current transform
     * @return
     */
    public CellTransform getTransform();
    
    /**
     * Return the cells priority
     * @return
     */
    public short getPriority();
    
    /**
     * Returns true if this is a mirror for an Entity cell.
     * @return
     */
    public boolean isEntity();
}
