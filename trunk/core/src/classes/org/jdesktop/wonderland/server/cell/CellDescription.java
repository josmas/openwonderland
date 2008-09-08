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
import com.sun.sgs.app.ManagedObject;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * A light weight container for perfomance sensiteive cell data. Used to provide in memory access to
 * some portion of cell data without requiring a Darkstar database access.
 * 
 * @author paulby
 */
@ExperimentalAPI
public interface CellDescription {
    
    /**
     * Returns the cell ID
     * @return
     */
    public CellID getCellID();

    /**
     * Returns the time stamp for when the contents was last modified
     * @return
     */
    public long getContentsTimestamp();
    
    /**
     * Returns the time stamp for when the transform was last modified
     * @return
     */
    public long getTransformTimestamp();
    
     /**
     * Returns a copy of the cell's local bounds
     * @return the cells bounds.
     */
    public BoundingVolume getLocalBounds();
    
    /**
     * Returns a copy of the cell's world bounds
     * @return
     */
    public BoundingVolume getWorldBounds();
    
    /**
     * Set the world bounds
     * @param worldBounds
     */
    public void setWorldBounds(BoundingVolume worldBounds);
    
    /**
     * Returns a copy of the cell's current transform
     * @return the cells transform.
     */
    public CellTransform getLocalTransform();
    
    void setLocalTransform(CellTransform localTransform, long timestamp);
    
    /**
     * Return the cells priority
     * @return
     */
    public short getPriority();
    
    /**
     * Return the class of the cell represented by this mirror
     * @return
     */
    public Class getCellClass();
    
    /**
     * Return true if this is a movable cell, false if its static
     * @return
     */
    public boolean isMovable();
}
