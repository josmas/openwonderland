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
package org.jdesktop.wonderland.modules.affordances.server.cell;

import org.jdesktop.wonderland.server.cell.*;
import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.affordances.common.cell.state.AffordanceTestCellClientState;
import org.jdesktop.wonderland.modules.affordances.common.cell.state.AffordanceTestCellServerState;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 * Cell that renders a basic shape
 * 
 * @author paulby
 */
@ExperimentalAPI
public class AffordanceTestCellMO extends CellMO {
    
    private String shape;

    
    /** Default constructor, used when cell is created via WFS */
    public AffordanceTestCellMO() {
        this(new Vector3f(), 1);
    }

    public AffordanceTestCellMO(Vector3f center, float size) {
        this(center, size, "BOX");
    }

    public AffordanceTestCellMO(Vector3f center, float size, String shape) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
        this.shape = shape;
//        addComponent(new MovableComponentMO(this));
    }
    
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.affordances.client.cell.AffordanceTestCell";
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new AffordanceTestCellClientState();
        }
        ((AffordanceTestCellClientState)cellClientState).setShape(shape);
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setServerState(CellServerState setup) {
        if (setup == null) {
            setup = new AffordanceTestCellServerState();
        }
        shape = ((AffordanceTestCellServerState)setup).getShapeType();
        super.setServerState(setup);
    }

    @Override
    public CellServerState getServerState(CellServerState setup) {
        if (setup == null) {
            setup = new AffordanceTestCellServerState();
        }
        ((AffordanceTestCellServerState)setup).setShapeType(shape);
        return super.getServerState(setup);
    }
}
