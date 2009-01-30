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
package org.jdesktop.wonderland.modules.sample.server;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.modules.sample.common.SampleCellClientState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.sample.common.SampleCellComponentServerState;
import org.jdesktop.wonderland.modules.sample.common.SampleCellServerState;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 * A sample cell
 * @author jkaplan
 */
@ExperimentalAPI
public class SampleCellMO extends CellMO { 

    /* The shape of the cell: BOX or SPHERE */
    private String shapeType = null;

    /** Default constructor, used when cell is created via WFS */
    public SampleCellMO() {
        addComponent(new MovableComponentMO(this));
        SampleCellComponentMO component = new SampleCellComponentMO(this);
        SampleCellComponentServerState state = new SampleCellComponentServerState();
        state.setInfo("My component info");
        component.setServerState(state);
        addComponent(component);
    }

    public SampleCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
        addComponent(new MovableComponentMO(this));
        SampleCellComponentMO component = new SampleCellComponentMO(this);
        SampleCellComponentServerState state = new SampleCellComponentServerState();
        state.setInfo("My component info");
        component.setServerState(state);
        addComponent(component);
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
    }

    @Override 
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.sample.client.SampleCell";
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
          cellClientState = new SampleCellClientState();
        }
        ((SampleCellClientState)cellClientState).setShapeType(shapeType);
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setServerState(CellServerState serverState) {
        shapeType = ((SampleCellServerState)serverState).getShapeType();
        logger.warning("Setting server shape type " + shapeType);
        super.setServerState(serverState);
    }

    @Override
    public CellServerState getServerState(CellServerState cellServerState) {
        if (cellServerState == null) {
            cellServerState = new SampleCellServerState();
        }
        ((SampleCellServerState)cellServerState).setShapeType(shapeType);
        return super.getServerState(cellServerState);
    }
}
