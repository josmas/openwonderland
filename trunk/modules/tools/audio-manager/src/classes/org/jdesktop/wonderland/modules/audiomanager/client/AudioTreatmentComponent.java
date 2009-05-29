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
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioTreatmentMessage;


/**
 * A component that provides audio audio treatments
 * 
 * @author jprovino
 */
@ExperimentalAPI
public class AudioTreatmentComponent extends AudioParticipantComponent {

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
                //channelComp.removeMessageReceiver(CellServerComponentMessage.class);
                msgReceiver = null;
            }
            break;

        case BOUNDS:
            if (msgReceiver == null) {
                msgReceiver = new ChannelComponent.ComponentMessageReceiver() {
                    public void messageReceived(CellMessage message) {
                    }
                };

                channelComp = cell.getComponent(ChannelComponent.class);
                channelComp.addMessageReceiver(AudioTreatmentMessage.class, msgReceiver);
		//channelComp.addMessageReceiver(CellServerComponentMessage.class, msgReceiver);
            }
            break;
        }
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
