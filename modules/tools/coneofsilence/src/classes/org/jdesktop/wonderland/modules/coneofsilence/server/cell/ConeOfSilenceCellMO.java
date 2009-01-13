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
package org.jdesktop.wonderland.modules.coneofsilence.server.cell;

import java.util.logging.Logger;

import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState.Rotation;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;

import org.jdesktop.wonderland.modules.coneofsilence.common.ConeOfSilenceCellServerState;
import org.jdesktop.wonderland.modules.coneofsilence.common.ConeOfSilenceCellClientState;

import org.jdesktop.wonderland.server.cell.CellMO;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;

import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell that provides conference coneofsilence functionality
 * @author jprovino
 */
public class ConeOfSilenceCellMO extends CellMO {

    private static final Logger logger =
        Logger.getLogger(ConeOfSilenceCellMO.class.getName());
     
    private String modelFileName;    
    
    private String name;

    public ConeOfSilenceCellMO() {
    }
    
    public ConeOfSilenceCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size), 
	    new CellTransform(null, center));
    }

    public void setLive(boolean live) {
	if (live == false) {
	    return;
	}

	new ConeOfSilenceMessageHandler(this, name);
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
	    ClientCapabilities capabilities) {

        return "org.jdesktop.wonderland.modules.coneofsilence.client.cell.ConeOfSilenceCell";
    }

    @Override
    public CellClientState getCellClientState(CellClientState cellClientState, WonderlandClientID clientID,
	    ClientCapabilities capabilities) {

        if (cellClientState == null) {
            cellClientState = new ConeOfSilenceCellClientState();
        }
        return super.getCellClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setCellServerState(CellServerState cellServerState) {
        super.setCellServerState(cellServerState);

	ConeOfSilenceCellServerState coneOfSilenceCellServerState = 
	    (ConeOfSilenceCellServerState) cellServerState;

	name = coneOfSilenceCellServerState.getName();
    }

    /**
     * Return a new CellServerState Java bean class that represents the current
     * state of the cell.
     *
     * @return a JavaBean representing the current state
     */
    @Override
    public CellServerState getCellServerState(CellServerState cellServerState) {
        /* Create a new BasicCellState and populate its members */
        if (cellServerState == null) {
            cellServerState = new ConeOfSilenceCellServerState();
        }
        return super.getCellServerState(cellServerState);
    }

}
