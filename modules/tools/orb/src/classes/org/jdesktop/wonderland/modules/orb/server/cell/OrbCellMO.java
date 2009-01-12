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
package org.jdesktop.wonderland.modules.orb.server.cell;


import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.AudioGroupSetup;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.mpk20.voicelib.app.ZeroVolumeSpatializer;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;

import com.sun.voip.CallParticipant;
import com.sun.voip.client.connector.CallStatus;

import java.lang.String;
import java.util.logging.Logger;



import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.modules.orb.common.OrbCellSetup;
import org.jdesktop.wonderland.modules.orb.common.OrbCellConfig;

import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;

import org.jdesktop.wonderland.server.setup.BasicCellSetupHelper;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;


import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.server.cell.ChannelComponentImplMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell that provides Orb functionality
 * @author jprovino
 */
public class OrbCellMO extends CellMO implements BeanSetupMO {

    private static final Logger logger =
        Logger.getLogger(OrbCellMO.class.getName());
     
    private ManagedReference<OrbMessageHandler> orbMessageHandlerRef;

    public OrbCellMO() {
	if (orbMessageHandlerRef == null) {
	    addComponent(new ChannelComponentImplMO(this), ChannelComponentMO.class);
	    //addComponent(new MovableComponentMO(this));

            orbMessageHandlerRef = AppContext.getDataManager().createReference(
		new OrbMessageHandler(this, null, false));
	}
    }
    
    public OrbCellMO(Vector3f center, float size, String callID, boolean simulateCalls) {
        super(new BoundingBox(new Vector3f(), size, size, size), 
	    new CellTransform(null, center));

	logger.fine("Orb center " + center + " size " + size);

	if (orbMessageHandlerRef == null) {
	    addComponent(new ChannelComponentImplMO(this), ChannelComponentMO.class);

            orbMessageHandlerRef = AppContext.getDataManager().createReference(
		new OrbMessageHandler(this, callID, simulateCalls));
	}
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
	    ClientCapabilities capabilities) {

        return "org.jdesktop.wonderland.modules.orb.client.cell.OrbCell";
    }

    @Override
    public CellClientState getCellClientState(CellClientState cellClientState, WonderlandClientID clientID,
	    ClientCapabilities capabilities) {

        if (cellClientState == null) {
            cellClientState = new OrbCellConfig();
        }

        cellClientState.addClientComponentClasses(new String[] {
              "org.jdesktop.wonderland.client.cell.ChannelComponent"
        });

	return super.getCellClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setServerState(CellServerState setup) {
        super.setServerState(setup);

	OrbCellSetup pcs = (OrbCellSetup) setup;
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
            cellServerState = new OrbCellSetup();
        }
	return super.getCellServerState(cellServerState);
    }
}
