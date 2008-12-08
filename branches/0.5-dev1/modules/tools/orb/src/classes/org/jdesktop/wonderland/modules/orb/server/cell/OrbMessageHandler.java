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

package org.jdesktop.wonderland.modules.orb.server.cell;

import org.jdesktop.wonderland.modules.orb.common.OrbCellSetup;

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
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

import com.sun.voip.CallParticipant;
import com.sun.voip.client.connector.CallStatus;


import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import java.io.IOException;
import java.io.Serializable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;

import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;

import org.jdesktop.wonderland.server.UserManager;

import com.jme.math.Vector3f;

import org.jdesktop.wonderland.modules.orb.common.messages.OrbEndCallMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbMuteCallMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbSetVolumeMessage;

/**
 * A server cell that provides Orb functionality
 * @author jprovino
 */
public class OrbMessageHandler implements Serializable, ComponentMessageReceiver {

    private static final Logger logger =
        Logger.getLogger(OrbCellMO.class.getName());
     
    private String callID;

    private ManagedReference<OrbCellMO> orbCellMORef;

    private ManagedReference<ChannelComponentMO> channelComponentRef = null;

    private ManagedReference<OrbStatusListener> orbStatusListenerRef;

    public OrbMessageHandler(OrbCellMO orbCellMO) {
        orbCellMORef = AppContext.getDataManager().createReference(
                (OrbCellMO) CellManagerMO.getCell(orbCellMO.getCellID()));

        OrbStatusListener orbStatusListener = new OrbStatusListener(orbCellMORef);

        orbStatusListenerRef =  AppContext.getDataManager().createReference(orbStatusListener);

        ChannelComponentMO channelComponent = (ChannelComponentMO)
            orbCellMO.getComponent(ChannelComponentMO.class);

        if (channelComponent == null) {
            throw new IllegalStateException("Cell does not have a ChannelComponent");
        }

        channelComponentRef = AppContext.getDataManager().createReference(channelComponent);

        channelComponent.addMessageReceiver(OrbEndCallMessage.class, this);
        channelComponent.addMessageReceiver(OrbMuteCallMessage.class, this);
        channelComponent.addMessageReceiver(OrbSetVolumeMessage.class, this);

    }

    public void messageReceived(WonderlandClientSender sender, 
	    ClientSession session, CellMessage message) {

	logger.fine("got message " + message);
    }
   
    public void setCallID(String callID) {
	orbStatusListenerRef.get().addCallStatusListener(callID);
    }

}
