/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.server.cell.view;

import org.jdesktop.wonderland.server.cell.*;
import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

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

    public ViewCellMO() {
        super();
        addComponent(new ChannelComponentMO(this), ChannelComponentMO.class);
        //addComponent(new MovableAvatarComponentMO(this), MovableComponentMO.class);
    }
    
    public ViewCellMO(BoundingVolume localBounds, CellTransform transform) {
        super(localBounds, transform);
        addComponent(new ChannelComponentMO(this), ChannelComponentMO.class);
        addComponent(new MovableAvatarComponentMO(this), MovableComponentMO.class);
    }
    
    /**
     * Get the user who owns this view
     * @return
     */
    public abstract UserMO getUser();

    /**
     * Get the WonderlandClientID of the user session that owns this view.
     * May return null if the view is not associated with a client
     * session.
     * @return the client ID for the session associated with this view
     */
    public abstract WonderlandClientID getClientID();

    /**
     * Return the cell cache managed object for this view, or null if there
     * is no associated cache.
     * 
     * @return the cell cache for this view, or null
     */
    public abstract ViewCellCacheMO getCellCache();
    
}
