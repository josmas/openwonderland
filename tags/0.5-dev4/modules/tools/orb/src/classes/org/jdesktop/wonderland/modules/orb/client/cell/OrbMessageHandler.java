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
package org.jdesktop.wonderland.modules.orb.client.cell;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import java.util.logging.Logger;

import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.Cell;

import org.jdesktop.wonderland.client.cell.view.LocalAvatar;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.orb.common.messages.OrbAttachMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbEndCallMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbMuteCallMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbSetVolumeMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbSpeakingMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbStartCallMessage;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTag;

import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.client.jme.JmeClientMain;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingUtilities;

/**
 *
 * @author jprovino
 */
public class OrbMessageHandler {

    private static final Logger logger =
            Logger.getLogger(OrbMessageHandler.class.getName());

    private ChannelComponent channelComp;
        
    private OrbCell orbCell;

    private WonderlandSession session;

    private OrbDialog orbDialog;

    private String username;

    private String callID;

    private NameTag nameTag;

    public OrbMessageHandler(OrbCell orbCell, WonderlandSession session) {
	this.orbCell = orbCell;
	this.session = session;

        channelComp = orbCell.getComponent(ChannelComponent.class);

        logger.finer("OrbCellID " + orbCell.getCellID() + ", Channel comp is " + channelComp);

        ChannelComponent.ComponentMessageReceiver msgReceiver =
            new ChannelComponent.ComponentMessageReceiver() {
                public void messageReceived(CellMessage message) {
                    processMessage(message);
                }
            };

        channelComp.addMessageReceiver(OrbStartCallMessage.class, msgReceiver);
        channelComp.addMessageReceiver(OrbEndCallMessage.class, msgReceiver);
        channelComp.addMessageReceiver(OrbMuteCallMessage.class, msgReceiver);
        channelComp.addMessageReceiver(OrbSpeakingMessage.class, msgReceiver);
        channelComp.addMessageReceiver(OrbSetVolumeMessage.class, msgReceiver);

	nameTag = new NameTag(orbCell, orbCell.getUsername(), (float) .17);
    }

    public void done() {
	channelComp.removeMessageReceiver(OrbStartCallMessage.class);
	channelComp.removeMessageReceiver(OrbEndCallMessage.class);
	channelComp.removeMessageReceiver(OrbMuteCallMessage.class);
        channelComp.removeMessageReceiver(OrbSpeakingMessage.class);
	channelComp.removeMessageReceiver(OrbSetVolumeMessage.class);

	nameTag.done();
    }

    public void processMessage(final Message message) {
	logger.finest("process message " + message);

	if (message instanceof OrbStartCallMessage) {
	    username = ((OrbStartCallMessage) message).getUsername();
	    callID = ((OrbStartCallMessage) message).getCallID();
	    return;
	}

	if (message instanceof OrbSpeakingMessage) {
	    OrbSpeakingMessage msg = (OrbSpeakingMessage) message;

	    logger.info("Orb speaking " + msg.isSpeaking());

	    nameTag.setSpeaking(msg.isSpeaking());
	    return;
	}

	if (message instanceof OrbMuteCallMessage) {
	    OrbMuteCallMessage msg = (OrbMuteCallMessage) message;

            nameTag.setMute(msg.isMuted());
	    return;
	}
    }
    
    public void orbSelected() {

	if (orbDialog == null) {
	    LocalAvatar avatar = ((CellClientSession)session).getLocalAvatar();
	    
	    orbDialog = new OrbDialog(orbCell, channelComp, avatar.getViewCell().getCellID());
	} 

	orbDialog.setVisible(true);
    }

}
