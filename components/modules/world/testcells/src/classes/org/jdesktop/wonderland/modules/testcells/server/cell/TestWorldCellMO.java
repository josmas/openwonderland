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

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import java.util.logging.Level;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.testcells.common.cell.state.TestWorldCellServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 * Test Cell for use until WFS is integrated, this will be removed.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class TestWorldCellMO extends CellMO{
    
    /** Default constructor, used when cell is created via WFS */
    public TestWorldCellMO() {

        // BIG HACK, the bounds must encompass all the children
        super(new BoundingBox(new Vector3f(), 10, 10, 10), new CellTransform(null, null));

        try {
            //addChild(new SimpleShapeCellMO(new Vector3f(7, 0, 5), 1));
            addChild(new MouseSpinCellMO(new Vector3f(-8, 0, 2), 1));
            //addChild(new SingingTeapotCellMO(new Vector3f(-8, 0, 7), 1, new MaterialJME(ColorRGBA.green, null, null, null, 0.5f)));
            addChild(new DragTestMO(new Vector3f(8, 0, 2), 1));
            //addChild(new DisappearTestMO(new Vector3f(16, 0, 2), 1));
            //addChild(new SimpleShapeCellMO(new Vector3f(0, 2, -5), 1, SimpleShapeConfig.Shape.SPHERE));
            //addChild(new SimpleShapeCellMO(new Vector3f(0, 2, 5), 1, SimpleShapeConfig.Shape.CONE));
            //addChild(new SimpleShapeCellMO(new Vector3f(5, 2, 0), 1, SimpleShapeConfig.Shape.CYLINDER));
            //addChild(new MirrorCellMO(new Vector3f(0, 0, 0), 20));
        } catch (Exception ex) {
            // do nothing
            logger.log(Level.WARNING, "Error creating cell", ex);
        }
    }
    
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
                                            ClientCapabilities capabilities)
    {
        return "org.jdesktop.wonderland.modules.testcells.client.cell.TestWorldCell";
    }

    @Override
    public CellServerState getServerState(CellServerState setup) {
        if (setup == null) {
            setup = new TestWorldCellServerState();
        }

        return super.getServerState(setup);
    }
}
