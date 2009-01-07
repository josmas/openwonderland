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
package org.jdesktop.wonderland.modules.affordances.server.cell;

import com.jme.math.Vector3f;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.affordances.common.cell.config.AffordanceTestCellConfig;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.ServerPlugin;

/**
 *
 * @author paulby
 */
public class TestWorld implements ServerPlugin {

    public TestWorld() {
    }

    public void initialize() {
        try {
            WonderlandContext.getCellManager().insertCellInWorld(new AffordanceTestCellMO(new Vector3f(0, 2, -12), 1, AffordanceTestCellConfig.Shape.SPHERE));
            WonderlandContext.getCellManager().insertCellInWorld(new AffordanceTestCellMO(new Vector3f(0, 2, -10), 2, AffordanceTestCellConfig.Shape.CONE));
            WonderlandContext.getCellManager().insertCellInWorld(new AffordanceTestCellMO(new Vector3f(5, 2, -5), 3, AffordanceTestCellConfig.Shape.CYLINDER));
        } catch (Exception ex) {
            Logger.getLogger(CellManagerMO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
