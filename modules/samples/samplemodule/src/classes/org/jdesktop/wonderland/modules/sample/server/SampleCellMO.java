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
import org.jdesktop.wonderland.modules.sample.common.SampleCellConfig;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;


/**
 * A sample cell
 * @author jkaplan
 */
@ExperimentalAPI
public class SampleCellMO extends CellMO implements BeanSetupMO { 
    	
    /** Default constructor, used when cell is created via WFS */
    public SampleCellMO() {
    }

    public SampleCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
    }
    
    @Override 
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.sample.client.SampleCell";
    }

    @Override
    public CellClientState getCellClientState(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return new SampleCellConfig();
    }

    @Override
    public void setServerState(CellServerState serverState) {
        super.setServerState(serverState);
    }

    @Override
    public void reconfigureCell(CellServerState setup) {
        super.reconfigureCell(setup);
        setServerState(setup);
    }
}
