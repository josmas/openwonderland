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

import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.server.cell.*;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 * Test Cell for use until WFS is integrated, this will be removed.
 * 
 * @deprecated
 * @author paulby
 */
@ExperimentalAPI
public class MouseSpinCellMO extends SimpleShapeCellMO{
    
    /** Default constructor, used when cell is created via WFS */
    public MouseSpinCellMO() {
        this(new Vector3f(), 50);
    }

    public MouseSpinCellMO(Vector3f center, float size) {
        super(center, size);

        ProximityComponentMO prox = new ProximityComponentMO(this);
        BoundingVolume[] bounds = new BoundingVolume[] { new BoundingSphere(2f, new Vector3f()) };
        prox.addProximityListener(new ProximityTest(), bounds );
        addComponent(prox);
    }
    
    @Override protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.testcells.client.cell.MouseSpinCell";
    }

    @Override
    public void setServerState(CellServerState setup) {
        super.setServerState(setup);
    }
}
