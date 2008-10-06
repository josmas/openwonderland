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
package org.jdesktop.wonderland.server.cell.view;

import org.jdesktop.wonderland.server.cell.*;
import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.server.UserMO;

/**
 * ViewCell defines the view into the virtual world for a specific window
 * on a client. A client may have many ViewCells instanstantiated, however
 * there is a 1-1 correlation between the ViewCell and a rendering of the
 * virtual world.
 * 
 * @author paulby
 */
@ExperimentalAPI
public abstract class ViewCellMO extends CellMO {
    
    public ViewCellMO(BoundingVolume localBounds, CellTransform transform) {
        super(localBounds, transform);
        addComponent(new ChannelComponentMO(this));
        addComponent(new MovableComponentMO(this));
    }
    
    /**
     * Return the transform of the camera for this view
     * @return
     */
//    public abstract CellTransform getWorldTransform();
    
    /**
     * Get the user who owns this view
     * @return
     */
    public abstract UserMO getUser();
    
    /**
     * Return the cell cache managed object for this view, or null if there
     * is no associated cache.
     * 
     * @return the cell cache for this view, or null
     */
    public abstract ViewCellCacheMO getCellCache();
    
    /**
     * Convenience method, simply calls moveableComponent.localMoveRequest
     * @param transform
     */
    public void localMoveRequest(CellTransform transform) {
//        movableComp.localMoveRequest(transform);
    }

    @Override
    protected abstract String getClientCellClassName(ClientSession clientSession, ClientCapabilities capabilities);

}
