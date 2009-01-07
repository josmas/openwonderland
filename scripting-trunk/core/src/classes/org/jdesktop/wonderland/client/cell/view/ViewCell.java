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
package org.jdesktop.wonderland.client.cell.view;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.MovableAvatarComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * ViewCell defines the view into the virtual world for a specific window
 * on a client. A client may have many ViewCells instanstantiated, however
 * there is a 1-1 correlation between the ViewCell and a rendering of the
 * virtual world.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class ViewCell extends Cell {
    
    private MovableAvatarComponent movableComp;

    public ViewCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        addComponent(new ChannelComponent(this));
        addComponent(new MovableAvatarComponent(this), MovableComponent.class);
        movableComp = (MovableAvatarComponent) getComponent(MovableComponent.class);
    }
    
    /**
     * Convenience method, simply calls moveableComponent.localMoveRequest
     * @param transform
     */
    public void localMoveRequest(CellTransform transform) {
        movableComp.localMoveRequest(transform, -1, false, null);
    }
}
