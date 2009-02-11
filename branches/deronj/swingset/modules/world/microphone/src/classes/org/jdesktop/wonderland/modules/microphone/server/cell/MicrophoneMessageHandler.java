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

import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellClientState;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState;

import org.jdesktop.wonderland.modules.microphone.common.messages.MicrophoneEnterCellMessage;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.sgs.app.AppContext;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.server.WonderlandContext;

import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import java.io.IOException;
import java.io.Serializable;

import java.util.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.common.cell.MultipleParentException;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.server.UserManager;

import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellMOFactory;

import org.jdesktop.wonderland.modules.microphone.server.cell.MicrophoneCellMO;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.server.cell.ChannelComponentImplMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell that provides conference microphone functionality
 * @author jprovino
 */
public class MicrophoneMessageHandler extends AbstractComponentMessageReceiver
	implements Serializable {

    private static final Logger logger =
        Logger.getLogger(MicrophoneMessageHandler.class.getName());
     
    private String name;

    public MicrophoneMessageHandler(MicrophoneCellMO microphoneCellMO, String name) {
	super(microphoneCellMO);

	this.name = name;

        getChannelComponent().addMessageReceiver(MicrophoneEnterCellMessage.class, this);
    }

    public void done() {
	getChannelComponent().removeMessageReceiver(MicrophoneEnterCellMessage.class);
    }

    public void messageReceived(final WonderlandClientSender sender, 
	    final WonderlandClientID clientID, final CellMessage message) {

    }

}
