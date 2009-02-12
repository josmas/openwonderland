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

import java.util.ArrayList;
import java.util.logging.Logger;
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

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioTreatmentMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioVolumeMessage;

import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

/**
 * A component that provides audio audio treatments
 * 
 * @author jprovino
 */
@ExperimentalAPI
public class AudioTreatmentComponent extends CellComponent implements VolumeChangeListener {

    private static Logger logger = Logger.getLogger(AudioTreatmentComponent.class.getName());
    private ChannelComponent channelComp;
    private ChannelComponent.ComponentMessageReceiver msgReceiver;
    private ArrayList<AudioTreatmentDoneListener> listeners = new ArrayList();

    @UsesCellComponent
    private ContextMenuComponent contextMenu;

    private String[] menuItem = new String[] {"Volume"};  // TODO I18N

    public AudioTreatmentComponent(Cell cell) {
        super(cell);
    }

    @Override
    public void setStatus(CellStatus status) {
        super.setStatus(status);
        switch (status) {
            case DISK:
                if (msgReceiver != null) {
                    channelComp.removeMessageReceiver(AudioTreatmentMessage.class);
                    msgReceiver = null;
                    contextMenu.removeMenuItem(menuItem);
                }
                break;

            case BOUNDS:
                if (msgReceiver == null) {
                    msgReceiver = new ChannelComponent.ComponentMessageReceiver() {

                        public void messageReceived(CellMessage message) {
                            AudioTreatmentMessage msg = (AudioTreatmentMessage) message;
                        }
                    };

                    channelComp = cell.getComponent(ChannelComponent.class);
                    channelComp.addMessageReceiver(AudioTreatmentMessage.class, msgReceiver);
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

    /**
     * Listen for audio treatment done
     * @param listener
     */
    public void addTreatmentDoneListener(AudioTreatmentDoneListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove the audio treatment done listener.
     * @param listener
     */
    public void removeAudioTreatmentDoneListener(AudioTreatmentDoneListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        // TODO: set own client state?
        super.setClientState(clientState);
    }

    /**
     * Notify any audio treatment done listeners
     * 
     * @param transform
     */
    private void notifyAudioTreatmentDoneListeners() {
        for (AudioTreatmentDoneListener listener : listeners) {
            listener.audioTreatmentDone();
        }
    }

    @ExperimentalAPI
    public interface AudioTreatmentDoneListener {

        /**
         * Notification that the cell has moved. Source indicates the source of 
         * the move, local is from this client, remote is from the server.
         * XXX arguments?
         */
        public void audioTreatmentDone();
    }
}
