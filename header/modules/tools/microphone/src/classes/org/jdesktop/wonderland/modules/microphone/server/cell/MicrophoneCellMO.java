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
package org.jdesktop.wonderland.modules.microphone.server.cell;

import java.util.logging.Logger;

import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;

import org.jdesktop.wonderland.server.cell.CellMO;


import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellClientState;

import org.jdesktop.wonderland.modules.audiomanager.server.MicrophoneComponentMO;

import com.jme.bounding.BoundingBox;

import com.jme.math.Vector3f;

import org.jdesktop.wonderland.server.comms.WonderlandClientID;

import org.jdesktop.wonderland.modules.audiomanager.server.MicrophoneComponentMO;

import com.sun.sgs.app.ManagedReference;

import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;

/**
 * A server cell that provides conference microphone functionality
 * @author jprovino
 */
public class MicrophoneCellMO extends CellMO {

    private static final Logger logger =
            Logger.getLogger(MicrophoneCellMO.class.getName());

    private String modelFileName;
    
    @UsesCellComponentMO(MicrophoneComponentMO.class)
    private ManagedReference<MicrophoneComponentMO> compRef;

    public MicrophoneCellMO() {
    }

    public MicrophoneCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size),
                new CellTransform(null, center));
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        return "org.jdesktop.wonderland.modules.microphone.client.cell.MicrophoneCell";
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        if (cellClientState == null) {
            cellClientState = new MicrophoneCellClientState();
        }

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setServerState(CellServerState cellServerState) {
        super.setServerState(cellServerState);

        MicrophoneCellServerState microphoneCellServerState = (MicrophoneCellServerState) cellServerState;
    }

    /**
     * Return a new CellServerState Java bean class that represents the current
     * state of the cell.
     *
     * @return a CellServerState representing the current state
     */
    @Override
    public CellServerState getServerState(CellServerState cellServerState) {
        /* Create a new BasicCellState and populate its members */
        if (cellServerState == null) {
            cellServerState = new MicrophoneCellServerState();
        }

        return super.getServerState(cellServerState);
    }

}
