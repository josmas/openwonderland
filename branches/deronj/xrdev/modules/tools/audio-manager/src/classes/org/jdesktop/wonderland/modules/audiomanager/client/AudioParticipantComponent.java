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

import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ContextMenuComponent;

import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;

import org.jdesktop.wonderland.client.contextmenu.ContextMenuEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuListener;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioParticipantSpeakingMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioVolumeMessage;

/**
 * A component that provides audio participant control
 * 
 * @author jprovino
 */
@ExperimentalAPI
public class AudioParticipantComponent extends CellComponent implements VolumeChangeListener {
    
    private static Logger logger = Logger.getLogger(AudioParticipantComponent.class.getName());

    private ChannelComponent channelComp;
    private ChannelComponent.ComponentMessageReceiver msgReceiver;

    @UsesCellComponent
    private ContextMenuComponent contextMenu;

    private String[] menuItem = new String[] {"Volume"};  // TODO I18N

    public AudioParticipantComponent(Cell cell) {
        super(cell);
    }
    
    @Override
    public void setStatus(CellStatus status) {
	switch(status) {
        case DISK:
	    if (msgReceiver != null) {
		channelComp.removeMessageReceiver(AudioParticipantSpeakingMessage.class);
		msgReceiver = null;
	    }
            break;

	case BOUNDS:
	    if (msgReceiver == null) {
                msgReceiver = new ChannelComponent.ComponentMessageReceiver() {
                    public void messageReceived(CellMessage message) {
                        AudioParticipantSpeakingMessage msg = (AudioParticipantSpeakingMessage) message;

			logger.info(msg.getCellID().toString() 
			    + (msg.isSpeaking() ? " Started speaking" : " Stopped speaking"));
                    }
                };

                channelComp = cell.getComponent(ChannelComponent.class);
                channelComp.addMessageReceiver(AudioParticipantSpeakingMessage.class, msgReceiver);

                contextMenu.addMenuItem(menuItem, new ContextMenuListener() {
                    public void entityContextPerformed(ContextMenuEvent event) {
                        adjustVolume();
                    }
                });
            }

	    break;

        }
    }
    
    VolumeControlJFrame volumeControlJFrame;

    private void adjustVolume() {
	if (volumeControlJFrame == null) {
	    volumeControlJFrame = new VolumeControlJFrame(this, cell.getName());
	} 

	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

	if (cell.getCellID().toString().equals(sc.getCallID())) {
	    volumeControlJFrame.setTitle("Master Volume for " + cell.getName());
	} else {
	    volumeControlJFrame.setTitle("Volume Control for " + cell.getName());
	}

	volumeControlJFrame.setVisible(true);
    }

    public void volumeChanged(double volume) {
	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

	channelComp.send(new AudioVolumeMessage(cell.getCellID(), sc.getCallID(), volume));
    }

}
