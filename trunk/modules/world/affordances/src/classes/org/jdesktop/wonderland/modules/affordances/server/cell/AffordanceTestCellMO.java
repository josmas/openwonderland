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
import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;
import org.jdesktop.wonderland.modules.affordances.common.cell.config.AffordanceTestCellConfig;
import org.jdesktop.wonderland.modules.affordances.common.cell.state.AffordanceTestCellServerState;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 * Cell that renders a basic shape
 * 
 * @author paulby
 */
@ExperimentalAPI
public class AffordanceTestCellMO extends CellMO {
    
    private AffordanceTestCellConfig.Shape shape;
    private MaterialJME materialJME = null;
    
    /** Default constructor, used when cell is created via WFS */
    public AffordanceTestCellMO() {
        this(new Vector3f(), 1);
    }

    public AffordanceTestCellMO(Vector3f center, float size) {
        this(center, size, AffordanceTestCellConfig.Shape.BOX);
    }

    public AffordanceTestCellMO(Vector3f center, float size, AffordanceTestCellConfig.Shape shape) {
        this(center, size, shape, null);
    }

    public AffordanceTestCellMO(Vector3f center, float size, AffordanceTestCellConfig.Shape shape, MaterialJME materialJME) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
        this.shape = shape;
        this.materialJME = materialJME;

        addComponent(new MovableComponentMO(this));
    }
    
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.affordances.client.cell.AffordanceTestCell";
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new AffordanceTestCellConfig(shape, materialJME);
        }
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setServerState(CellServerState setup) {
        super.setServerState(setup);
    }

    @Override
    public CellServerState getServerState(CellServerState setup) {
        if (setup == null) {
            setup = new AffordanceTestCellServerState();
        }
        return super.getServerState(setup);
    }


}
