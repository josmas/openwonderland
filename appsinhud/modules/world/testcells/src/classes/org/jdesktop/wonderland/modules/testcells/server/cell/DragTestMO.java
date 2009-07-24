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
package org.jdesktop.wonderland.modules.testcells.server.cell;

import org.jdesktop.wonderland.server.cell.*;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.server.cell.annotation.DependsOnCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 * Simple test for cell dragging.
 *
 * @deprecated
 *
 * @author paulby
 */
@ExperimentalAPI
@DependsOnCellComponentMO(MovableComponentMO.class)
public class DragTestMO extends SimpleShapeCellMO {
    
    /** Default constructor, used when cell is created via WFS */
    public DragTestMO () {
        this(new Vector3f(), 50);
    }

    public DragTestMO (Vector3f center, float size) {
        super(center, size);
    }
    
    @Override 
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.testcells.client.cell.DragTest";
    }

    @Override
    public void setServerState(CellServerState serverState) {
        super.setServerState(serverState);
    }
}
