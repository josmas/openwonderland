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
package org.jdesktop.wonderland.modules.audiomanager.client;


import java.util.logging.Logger;

import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;

import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;

import org.jdesktop.wonderland.client.softphone.AudioQuality;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.client.softphone.SoftphoneListener;

import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.common.cell.CallID;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioParticipantSpeakingMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioParticipantMuteCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioVolumeMessage;

import org.jdesktop.wonderland.modules.orb.client.cell.OrbCell;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarNameEvent;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode.EventType;

import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;

/**
 * A component that provides audio participant control
 * 
 * @author jprovino
 */
@ExperimentalAPI
public class AudioParticipantComponent extends CellComponent implements 
	ComponentMessageReceiver, VolumeChangeListener {
    
    private static Logger logger = Logger.getLogger(AudioParticipantComponent.class.getName());

    private ChannelComponent channelComp;

    @UsesCellComponent
    private ContextMenuComponent contextMenu;

    private static String[] menuItem;

    PresenceManager pm;

    public AudioParticipantComponent(Cell cell) {
        super(cell);

	pm = PresenceManagerFactory.getPresenceManager(cell.getCellCache().getSession());
    }
    
    @Override
    public void setStatus(CellStatus status) {
	switch(status) {
        case DISK:
	    channelComp.removeMessageReceiver(AudioParticipantSpeakingMessage.class);
	    channelComp.removeMessageReceiver(AudioParticipantMuteCallMessage.class);
            break;

	case BOUNDS:
            channelComp = cell.getComponent(ChannelComponent.class);
            channelComp.addMessageReceiver(AudioParticipantSpeakingMessage.class, this);
            channelComp.addMessageReceiver(AudioParticipantMuteCallMessage.class, this);

	    if (cell instanceof OrbCell == false && menuItem == null) {
		menuItem = new String[] {"Volume"};

                contextMenu.addMenuItem(new SimpleContextMenuItem("Volume",
                    new ContextMenuActionListener() {
                        public void actionPerformed(ContextMenuItemEvent event) {
                            adjustVolume(event);
                        }
                    })
                );
	    }

	    break;
        }
    }
    
    public void messageReceived(CellMessage message) {
        if (message instanceof AudioParticipantSpeakingMessage) {
            AudioParticipantSpeakingMessage msg = (AudioParticipantSpeakingMessage) message;

            PresenceInfo info = pm.getPresenceInfo(msg.getCellID());

            if (info == null) {
                logger.warning("No presence info for " + msg.getCellID());
                return;
            }

            pm.setSpeaking(info, msg.isSpeaking());

            AvatarNameEvent avatarNameEvent;

            if (msg.isSpeaking()) {
                avatarNameEvent = new AvatarNameEvent(EventType.STARTED_SPEAKING,
                        info.userID.getUsername(), info.usernameAlias);
            } else {
                avatarNameEvent = new AvatarNameEvent(EventType.STOPPED_SPEAKING,
                        info.userID.getUsername(), info.usernameAlias);
            }

            InputManager.inputManager().postEvent(avatarNameEvent);
        } else if (message instanceof AudioParticipantMuteCallMessage) {
            AudioParticipantMuteCallMessage msg = (AudioParticipantMuteCallMessage) message;

            PresenceInfo info = pm.getPresenceInfo(msg.getCellID());

            if (info == null) {
                logger.warning("No presence info for " + msg.getCellID());
                return;
            }

            pm.setMute(info, msg.isMuted());

            AvatarNameEvent avatarNameEvent;

            if (msg.isMuted()) {
                avatarNameEvent = new AvatarNameEvent(EventType.MUTE,
                        info.userID.getUsername(), info.usernameAlias);
            } else {
                avatarNameEvent = new AvatarNameEvent(EventType.UNMUTE,
                        info.userID.getUsername(), info.usernameAlias);
            }

            InputManager.inputManager().postEvent(avatarNameEvent);
	}
    }

    private VolumeControlJFrame volumeControlJFrame;

    private void adjustVolume(ContextMenuItemEvent event) {
	if (event.getContextMenuItem().getLabel().equals(menuItem[0]) == false) {
	    return;
	}

	if (cell instanceof OrbCell) {
	    return;  // Orb's have their own volume dialog
	}

	String callID = CallID.getCallID(cell.getCellID());
	String username = cell.getName();

	if (volumeControlJFrame == null) {
	    volumeControlJFrame = new VolumeControlJFrame(this, username);
	} 

	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

	if (callID.equals(sc.getCallID())) {
	    volumeControlJFrame.setTitle("Master Volume for " + username);
	} else {
	    volumeControlJFrame.setTitle("Volume Control for " + username);
	}

	volumeControlJFrame.setVisible(true);
    }

    public void volumeChanged(String userName, double volume) {
	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

	channelComp.send(new AudioVolumeMessage(cell.getCellID(), sc.getCallID(), volume));
    }

}
