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

import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;

import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;

import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioParticipantComponentClientState;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioParticipantSpeakingMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioParticipantMuteCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioVolumeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.ChangeUsernameAliasMessage;

import org.jdesktop.wonderland.modules.orb.client.cell.OrbCell;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarNameEvent;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode.EventType;

import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;

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

    PresenceManager pm;

    private boolean menuItemAdded;

    public AudioParticipantComponent(Cell cell) {
        super(cell);

	pm = PresenceManagerFactory.getPresenceManager(cell.getCellCache().getSession());
    }
    
   @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);

	AudioParticipantComponentClientState state = (AudioParticipantComponentClientState) 
	    clientState;

	logger.fine("setClientState for " + cell.getCellID() 
	    + " " + state.isSpeaking() + " " + state.isMuted());

	setSpeakingIndicator(cell.getCellID(), state.isSpeaking());
	setMuteIndicator(cell.getCellID(), state.isMuted());
    }

    @Override
    public void setStatus(CellStatus status) {
	switch(status) {
        case DISK:
	    channelComp.removeMessageReceiver(AudioParticipantSpeakingMessage.class);
	    channelComp.removeMessageReceiver(AudioParticipantMuteCallMessage.class);
	    channelComp.removeMessageReceiver(ChangeUsernameAliasMessage.class);
            break;

	case BOUNDS:
            channelComp = cell.getComponent(ChannelComponent.class);
            channelComp.addMessageReceiver(AudioParticipantSpeakingMessage.class, this);
            channelComp.addMessageReceiver(AudioParticipantMuteCallMessage.class, this);
            channelComp.addMessageReceiver(ChangeUsernameAliasMessage.class, this);

	    if (cell instanceof OrbCell == false && menuItemAdded == false) {
                // An event to handle the context menu item action
                final ContextMenuActionListener l = new ContextMenuActionListener() {
                    public void actionPerformed(ContextMenuItemEvent event) {
                        adjustVolume(event);
                    }
                };

                // Create a new ContextMenuFactory for the Volume... control
                ContextMenuFactorySPI factory = new ContextMenuFactorySPI() {
                    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                        return new ContextMenuItem[] {
                            new SimpleContextMenuItem("Volume", l)
                        };
                    }
                };

                contextMenu.addContextMenuFactory(factory);
		menuItemAdded = true;
	    }

	    break;
        }
    }
    
    public void messageReceived(CellMessage message) {
        if (message instanceof AudioParticipantSpeakingMessage) {
            AudioParticipantSpeakingMessage msg = (AudioParticipantSpeakingMessage) message;
	    setSpeakingIndicator(msg.getCellID(), msg.isSpeaking());
        } else if (message instanceof AudioParticipantMuteCallMessage) {
            AudioParticipantMuteCallMessage msg = (AudioParticipantMuteCallMessage) message;
	    setMuteIndicator(msg.getCellID(), msg.isMuted());
        } else if (message instanceof ChangeUsernameAliasMessage) {
            ChangeUsernameAliasMessage msg = (ChangeUsernameAliasMessage) message;
	    changeUsernameAlias(msg.getCellID(), msg.getPresenceInfo());
	}
    }

    private void setSpeakingIndicator(CellID cellID, boolean isSpeaking) {
	PresenceInfo info = pm.getPresenceInfo(cellID);

        if (info == null) {
            logger.fine("No presence info for " + cellID);
            return;
        }

        pm.setSpeaking(info, isSpeaking);

        AvatarNameEvent avatarNameEvent;

        if (isSpeaking) {
            avatarNameEvent = new AvatarNameEvent(EventType.STARTED_SPEAKING,
                info.userID.getUsername(), info.usernameAlias);
        } else {
            avatarNameEvent = new AvatarNameEvent(EventType.STOPPED_SPEAKING,
                info.userID.getUsername(), info.usernameAlias);
        }

        InputManager.inputManager().postEvent(avatarNameEvent);
    }

    private void setMuteIndicator(CellID cellID, boolean isMuted) {
	PresenceInfo info = pm.getPresenceInfo(cellID);

        if (info == null) {
            logger.fine("No presence info for " + cellID);
            return;
        }

        pm.setMute(info, isMuted);

        AvatarNameEvent avatarNameEvent;

        if (isMuted) {
            avatarNameEvent = new AvatarNameEvent(EventType.MUTE,
                info.userID.getUsername(), info.usernameAlias);
        } else {
            avatarNameEvent = new AvatarNameEvent(EventType.UNMUTE,
                info.userID.getUsername(), info.usernameAlias);
        }

        InputManager.inputManager().postEvent(avatarNameEvent);
    }

    private void changeUsernameAlias(CellID cellID, PresenceInfo info) {
        pm.changeUsernameAlias(info);

	AvatarNameEvent avatarNameEvent = new AvatarNameEvent(EventType.CHANGE_NAME,
	    info.userID.getUsername(), info.usernameAlias);

        InputManager.inputManager().postEvent(avatarNameEvent);
    }

    private VolumeControlJFrame volumeControlJFrame;

    private void adjustVolume(ContextMenuItemEvent event) {
	if (event.getContextMenuItem().getLabel().equals("Volume") == false) {
	    return;
	}

	if (cell instanceof OrbCell) {
	    return;  // Orb's have their own volume dialog
	}

	String callID = CallID.getCallID(cell.getCellID());
	String name = cell.getName();

	if (volumeControlJFrame == null) {
	    volumeControlJFrame = new VolumeControlJFrame(cell.getCellID(), this, name, callID);
	} 

	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

	if (callID.equals(sc.getCallID())) {
	    volumeControlJFrame.setTitle("Master Volume for " + name);
	} else {
	    volumeControlJFrame.setTitle("Volume Control for " + name);
	}

	volumeControlJFrame.setVisible(true);
    }

    public void volumeChanged(CellID cellID, String otherCallID, double volume) {
	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

	channelComp.send(new AudioVolumeMessage(cellID, sc.getCallID(), otherCallID, volume));
    }

    public void usernameChanged(String username) {
    }

}
