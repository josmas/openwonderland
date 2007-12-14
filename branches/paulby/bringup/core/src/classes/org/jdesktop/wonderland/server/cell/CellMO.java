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
package org.jdesktop.wonderland.server.cell;

import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;

/**
 * Server side representation of a cell
 * 
 * @author paulby
 */
public class CellMO {

    
    /**
     * Set the bounds of the cell in cell local coordinates
     * @param bounds
     */
    public void setBounds(Bounds bounds) {
        throw new RuntimeException("Not Implemented");
    }
    
    /**
     *  Return (a clone) of the cells bounds in cell local coordinates
     * @return
     */
    public Bounds getBounds() {
        throw new RuntimeException("Not Implemented");        
    }
    
    /**
     * Return the combined bounds of this cell and all its children in
     * virtual worlds coordinates.
     * 
     * The bounds will be access aligned, ie only the translation component
     * of the cells transform is applied.
     * 
     * It is the users responsibility to ensure that all cell contents are within
     * the access aligned bounds.
     * @return
     */
    public Bounds getComputedBounds() {
        throw new RuntimeException("Not Implemented");        
    }
    
    /**
     *  Add a child cell to list of children contained within this cell.
     *  A cell can only be attached to a single parent cell at any given time
     * @param child
     */
    public void addChild(CellMO child) {
        throw new RuntimeException("Not Implemented");                
    }
    
    /**
     *  Return the cell which is the parent of this cell, null if this not
     * attached to a parent
     */
    public CellMO getParent() {
        throw new RuntimeException("Not Implemented");                
    }
    
    /**
     * Set the transform for this cell. This will define the localOrigin of
     * the cell on the client. This transform is combined with all parent 
     * transforms to define the location of the cell in 3 space. 
     * 
     * @param transform
     */
    public void setTransform(Matrix4d transform) {
        
    }
}
