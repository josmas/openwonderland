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
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.audiomanager.common.ConeOfSilenceComponentSetup;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.ConeOfSilenceEnterCellMessage;
import com.sun.mpk20.voicelib.app.VoiceManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author jprovino
 */
public class ConeOfSilenceComponentMO extends CellComponentMO {

    private static final Logger logger =
            Logger.getLogger(ConeOfSilenceComponentMO.class.getName());

    private static final String ASSET_PREFIX = "wonderland-web-asset/asset/";

    private ManagedReference<ChannelComponentMO> channelComponentRef = null;
    
    private static String serverURL;

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
    public void setServerState(CellComponentServerState setup) {
	ConeOfSilenceComponentSetup cs = (ConeOfSilenceComponentSetup) setup;
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState setup) {
        if (setup == null) {
            setup = new ConeOfSilenceComponentSetup();
        }

        return setup;
    }

    @Override
    public CellComponentClientState getClientState(
            CellComponentClientState state,
            WonderlandClientID clientID,
            ClientCapabilities capabilities) {
        
        return super.getClientState(state, clientID, capabilities);
    }


    @Override
    public void setLive(boolean live) {
	VoiceManager vm = AppContext.getManager(VoiceManager.class);

        ChannelComponentMO channelComponent = (ChannelComponentMO) 
	    cellRef.get().getComponent(ChannelComponentMO.class);

        if (channelComponent == null) {
            logger.warning("Cell does not have a ChannelComponent");
	    return;
	}

        channelComponentRef = AppContext.getDataManager().createReference(channelComponent); 
                
        channelComponent.addMessageReceiver(ConeOfSilenceEnterCellMessage.class, 
	    new ComponentMessageReceiverImpl(this));
    }
    
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.audiomanager.client.ConeOfSilenceComponent";
    }

    private static class ComponentMessageReceiverImpl implements ComponentMessageReceiver {

        private ManagedReference<ConeOfSilenceComponentMO> compRef;
        
        public ComponentMessageReceiverImpl(ConeOfSilenceComponentMO comp) {
            compRef = AppContext.getDataManager().createReference(comp);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID,
		CellMessage message) {

            ConeOfSilenceEnterCellMessage msg = (ConeOfSilenceEnterCellMessage) message;

	    System.out.println("Got ConeOfSilenceMessage");
        }
    }

}
