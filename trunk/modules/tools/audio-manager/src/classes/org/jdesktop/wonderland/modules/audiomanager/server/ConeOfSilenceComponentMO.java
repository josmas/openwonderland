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
package org.jdesktop.wonderland.modules.audiomanager.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.audiomanager.common.ConeOfSilenceComponentServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.ConeOfSilenceEnterCellMessage;
import com.sun.mpk20.voicelib.app.VoiceManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;

import com.jme.math.Vector3f;

/**
 *
 * @author jprovino
 */
public class ConeOfSilenceComponentMO extends CellComponentMO {

    private static final Logger logger =
            Logger.getLogger(ConeOfSilenceComponentMO.class.getName());

    private static final String ASSET_PREFIX = "wonderland-web-asset/asset/";

    private static String serverURL;

    private String name;
    private float fullVolumeRadius;

    static {
	serverURL = System.getProperty("wonderland.web.server.url");
    }

    /**
     * Create a ConeOfSilenceComponent for the given cell. The cell must already
     * have a ChannelComponent otherwise this method will throw an IllegalStateException
     * @param cell
     */
    public ConeOfSilenceComponentMO(CellMO cell) {
        super(cell);
    }
    
    @Override
    public void setServerState(CellComponentServerState serverState) {
	ConeOfSilenceComponentServerState cs = (ConeOfSilenceComponentServerState) serverState;

	name = cs.getName();
	fullVolumeRadius = cs.getFullVolumeRadius();
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState serverState) {
        if (serverState == null) {
            serverState = new ConeOfSilenceComponentServerState(name, fullVolumeRadius);
        }

        return serverState;
    }

    @Override
    public CellComponentClientState getClientState(
            CellComponentClientState state,
            WonderlandClientID clientID,
            ClientCapabilities capabilities) {
        
	// TODO: Create own client state object?
        return state;
    }


    @Override
    public void setLive(boolean live) {
	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	CellMO cellMO = cellRef.get();

        ChannelComponentMO channelComponent = (ChannelComponentMO) 
	    cellMO.getComponent(ChannelComponentMO.class);

	if (live) {
	    ProximityComponentMO prox = new ProximityComponentMO(cellMO);
	    BoundingVolume[] bounds = new BoundingVolume[1];

	    bounds[0] = new BoundingSphere(fullVolumeRadius, new Vector3f());

	    ConeOfSilenceProximityListener proximityListener = new ConeOfSilenceProximityListener(name);

	    prox.addProximityListener(proximityListener, bounds);
	    cellMO.addComponent(prox);
	} else {
	    // XXX Do I need to remove the component?
	}
    }
    
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.audiomanager.client.ConeOfSilenceComponent";
    }

    private static class ComponentMessageReceiverImpl extends AbstractComponentMessageReceiver {

        private ManagedReference<ConeOfSilenceComponentMO> compRef;
        
        public ComponentMessageReceiverImpl(ManagedReference<CellMO> cellMORef,
		ConeOfSilenceComponentMO comp) {

	    super(cellMORef.get());

            compRef = AppContext.getDataManager().createReference(comp);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID,
		CellMessage message) {

            ConeOfSilenceEnterCellMessage msg = (ConeOfSilenceEnterCellMessage) message;

	    System.out.println("Got ConeOfSilenceMessage");
        }

    }

}
