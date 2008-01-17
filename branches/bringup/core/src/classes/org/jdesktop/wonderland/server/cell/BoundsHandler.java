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

import java.util.Collection;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.UserPerformanceMonitor;
import org.jdesktop.wonderland.server.cell.bounds.ServiceBoundsHandler;

/**
 * Provides an abstraction for the cell bounds computation engine allowing
 * various implementation options.
 * 
 * @author paulby
 */
public abstract class BoundsHandler {

    private static final BoundsHandler handler = new ServiceBoundsHandler();
    
    public static BoundsHandler get() {
        return handler;
    }
    
    public abstract Bounds getCachedVWBounds(CellID cellID);

    public abstract Bounds getComputedWorldBounds(CellID cellID);

    public abstract Bounds getLocalBounds(CellID cellID);

    public abstract void setLocalBounds(CellID cellID, Bounds bounds);

    public abstract void setComputedWorldBounds(CellID cellID, Bounds bounds);

    public abstract void setLocalToVworld(CellID cellID, Matrix4d transform);

    public abstract void addChild(CellMO parent, CellMO child) throws MultipleParentException ;
    
    public abstract void removeChild(CellMO parent, CellMO child);
    
    public abstract void createBounds(CellMO cell);
    
    public abstract void removeBounds(CellMO cell);
    
    public abstract void cellTransformChanged(CellID cellID, Matrix4d transform);
    
    public abstract void cellChildrenChanged(CellID parent, CellID child, boolean childAdded);
    
    public abstract void cellBoundsChanged(CellID cellID, Bounds bounds);
    
    public abstract Collection<CellID> getVisibleCells(CellID rootCell, Bounds bounds, UserPerformanceMonitor perfMonitor);
}
