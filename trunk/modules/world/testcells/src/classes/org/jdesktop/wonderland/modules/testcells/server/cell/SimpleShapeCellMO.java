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
import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;
import org.jdesktop.wonderland.modules.testcells.common.cell.config.SimpleShapeConfig;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 * Cell that renders a basic shape
 * 
 * @author paulby
 */
@ExperimentalAPI
public class SimpleShapeCellMO extends CellMO {
    
    private SimpleShapeConfig.Shape shape;
    private float mass;
    private MaterialJME materialJME = null;
    
    /** Default constructor, used when cell is created via WFS */
    public SimpleShapeCellMO() {
        this(new Vector3f(), 1);
    }

    public SimpleShapeCellMO(Vector3f center, float size) {
        this(center, size, SimpleShapeConfig.Shape.BOX);
    }

    public SimpleShapeCellMO(Vector3f center, float size, SimpleShapeConfig.Shape shape) {
        this(center, size, shape, 0f);
    }
    
    public SimpleShapeCellMO(Vector3f center, float size, SimpleShapeConfig.Shape shape, float mass) {
        this(center, size, shape, mass, null);
    }

    public SimpleShapeCellMO(Vector3f center, float size, SimpleShapeConfig.Shape shape, float mass, MaterialJME materialJME) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
        this.shape = shape;
        this.mass = mass;
        this.materialJME = materialJME;
    }
    
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.testcells.client.cell.SimpleShapeCell";
    }

    @Override
    public CellClientState getCellClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new SimpleShapeConfig(shape, mass, materialJME);
        }
        return super.getCellClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setCellServerState(CellServerState setup) {
        super.setCellServerState(setup);
    }
}
