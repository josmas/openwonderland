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
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentClientState;
import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState.PlayWhen;
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

    @UsesCellComponent
    private ContextMenuComponent contextMenu;

    private ContextMenuFactorySPI factory;

    private boolean menuItemAdded;

    private boolean play = true;

    private ChannelComponent.ComponentMessageReceiver msgReceiver;

    private ArrayList<AudioTreatmentDoneListener> listeners = new ArrayList();

    private PlayWhen playWhen = PlayWhen.ALWAYS;

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
            }

	    if (menuItemAdded == false) {
		menuItemAdded = true;

		if (playWhen.equals(PlayWhen.ALWAYS)) {
	            addMenuItems("Stop", "Pause");
		} else {
	            addMenuItems("Play", null);
		}
	    }
            break;
        }
    }

    private void addMenuItems(final String s1, final String s2) {
        // An event to handle the context menu item action
        final ContextMenuActionListener l = new ContextMenuActionListener() {
            public void actionPerformed(ContextMenuItemEvent event) {
                menuItemSelected(event);
            }
        };

	if (factory != null) {
	    contextMenu.removeContextMenuFactory(factory);
	}

        // Create a new ContextMenuFactory for the Volume... control
        factory = new ContextMenuFactorySPI() {
            public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
		SimpleContextMenuItem[] menuItems;

		if (s2 == null) {
		    menuItems = new SimpleContextMenuItem[1];
		} else {
		    menuItems = new SimpleContextMenuItem[2];
		    menuItems[1] =  new SimpleContextMenuItem(s2, l);
		}

	        menuItems[0] =  new SimpleContextMenuItem(s1, l);

                return menuItems;
            }
        };

        contextMenu.addContextMenuFactory(factory);
    }

    public void menuItemSelected(ContextMenuItemEvent event) {
        if (event.getContextMenuItem().getLabel().equals("Play") ||
		event.getContextMenuItem().getLabel().equals("Resume")) {

	    addMenuItems("Stop", "Pause");
	    channelComp.send(new AudioTreatmentMessage(cell.getCellID(), false, false));
	    return;
        }

        if (event.getContextMenuItem().getLabel().equals("Pause")) {
	    addMenuItems("Stop", "Resume");
	    channelComp.send(new AudioTreatmentMessage(cell.getCellID(), false, true));
	    return;
	}

        if (event.getContextMenuItem().getLabel().equals("Stop") == false) {
	    return;
	}

	addMenuItems("Play", null);
	channelComp.send(new AudioTreatmentMessage(cell.getCellID(), true, true));
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

	AudioTreatmentComponentClientState state = (AudioTreatmentComponentClientState)
            clientState;

	playWhen = state.getPlayWhen();
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
