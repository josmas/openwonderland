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

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import java.util.logging.Level;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.affordances.common.cell.config.AffordanceTestCellConfig;
import org.jdesktop.wonderland.modules.affordances.common.cell.state.TestWorldCellServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 * Test Cell for use until WFS is integrated, this will be removed.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class TestWorldCellMO extends CellMO {
    
    /** Default constructor, used when cell is created via WFS */
    public TestWorldCellMO() {
        this(new Vector3f(), 50);
    }

    public TestWorldCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));

        try {
            addChild(new AffordanceTestCellMO(new Vector3f(0, 2, -12), 1,
                                              AffordanceTestCellConfig.Shape.SPHERE));
            addChild(new AffordanceTestCellMO(new Vector3f(0, 2, -10), 2,
                                              AffordanceTestCellConfig.Shape.CONE));
            addChild(new AffordanceTestCellMO(new Vector3f(5, 2, -5), 3,
                                              AffordanceTestCellConfig.Shape.CYLINDER));
        } catch (Exception ex) {
            // do nothing
            logger.log(Level.WARNING, "Error creating cell", ex);
        }
    }
    
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
                                            ClientCapabilities capabilities)
    {
        return "org.jdesktop.wonderland.modules.affordances.client.cell.TestWorldCell";
    }

    @Override
    public CellServerState getServerState(CellServerState setup) {
        if (setup == null) {
            setup = new TestWorldCellServerState();
        }

        return super.getServerState(setup);
    }
}
