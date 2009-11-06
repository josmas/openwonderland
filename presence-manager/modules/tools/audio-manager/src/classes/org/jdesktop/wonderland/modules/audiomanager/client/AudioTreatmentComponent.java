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
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentClientState;
import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState.PlayWhen;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioTreatmentDoneMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioTreatmentEndedMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioTreatmentEstablishedMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioTreatmentMenuChangeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioTreatmentRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioVolumeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;

/**
 * A component that provides audio audio treatments
 * 
 * @author jprovino
 */
@ExperimentalAPI
public class AudioTreatmentComponent extends AudioParticipantComponent implements VolumeChangeListener {

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
    private boolean playOnce = false;

    public AudioTreatmentComponent(Cell cell) {
        super(cell);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {
	case DISK:
	case INACTIVE:
            if (msgReceiver == null) {
		return;
	    }
		    
            channelComp.removeMessageReceiver(AudioTreatmentDoneMessage.class);
            channelComp.removeMessageReceiver(AudioTreatmentEndedMessage.class);
            channelComp.removeMessageReceiver(AudioTreatmentEstablishedMessage.class);
            channelComp.removeMessageReceiver(AudioTreatmentMenuChangeMessage.class);
            channelComp.removeMessageReceiver(AudioTreatmentRequestMessage.class);
            channelComp.removeMessageReceiver(AudioVolumeMessage.class);
            break;

        case ACTIVE:
            if (increasing) {
                if (msgReceiver == null) {
                    msgReceiver = new ChannelComponent.ComponentMessageReceiver() {

                        public void messageReceived(CellMessage message) {
			    receive(message);
                        }
                    };

                    channelComp = cell.getComponent(ChannelComponent.class);
                    channelComp.addMessageReceiver(AudioTreatmentDoneMessage.class, msgReceiver);
                    channelComp.addMessageReceiver(AudioTreatmentEndedMessage.class, msgReceiver);
                    channelComp.addMessageReceiver(AudioTreatmentEstablishedMessage.class, msgReceiver);
                    channelComp.addMessageReceiver(AudioTreatmentMenuChangeMessage.class, msgReceiver);
                    channelComp.addMessageReceiver(AudioTreatmentRequestMessage.class, msgReceiver);
                    channelComp.addMessageReceiver(AudioVolumeMessage.class, msgReceiver);
                }

                if (menuItemAdded == false) {
                    menuItemAdded = true;

                    if (playWhen.equals(PlayWhen.ALWAYS)) {
                        addMenuItems(new String[] {"Stop", "Pause", "Volume"});;
                    } else {
                        addMenuItems(new String[] {"Play", "Volume"});
                    }
                }
            }
            break;
        }
    }

    private void addMenuItems(final String[] items) {
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
                SimpleContextMenuItem[] menuItems = new SimpleContextMenuItem[items.length];

		for (int i = 0; i < menuItems.length; i++) {
		    menuItems[i] = new SimpleContextMenuItem(items[i], l);
		}

                return menuItems;
            }
        };

        contextMenu.addContextMenuFactory(factory);
    }

    public void menuItemSelected(ContextMenuItemEvent event) {
        if (event.getContextMenuItem().getLabel().equals("Play") ||
                event.getContextMenuItem().getLabel().equals("Resume")) {

            addMenuItems(new String[] {"Stop", "Pause", "Volume"});
            channelComp.send(new AudioTreatmentRequestMessage(cell.getCellID(), false, false));
            return;
        }

        if (event.getContextMenuItem().getLabel().equals("Pause")) {
            addMenuItems(new String[] {"Stop", "Resume", "Volume"});
            channelComp.send(new AudioTreatmentRequestMessage(cell.getCellID(), false, true));
            return;
        }

        if (event.getContextMenuItem().getLabel().equals("Volume")) {
	    String softphoneCallID = SoftphoneControlImpl.getInstance().getCallID();
            channelComp.send(new AudioVolumeMessage(cell.getCellID(), softphoneCallID,
		CallID.getCallID(cell.getCellID()), 5, false));
	    return;
        }

        if (event.getContextMenuItem().getLabel().equals("Stop") == false) {
            return;
        }

        addMenuItems(new String[] {"Play", "Volume"});
        channelComp.send(new AudioTreatmentRequestMessage(cell.getCellID(), true, true));
    }

    public void volumeChanged(CellID cellID, String otherCallID, int volume) {
	logger.fine("Volume changed " + volume);

	String softphoneCallID = SoftphoneControlImpl.getInstance().getCallID();

   	channelComp.send(new AudioVolumeMessage(cellID, softphoneCallID,
	    otherCallID, VolumeUtil.getServerVolume(volume), true));
    }

    private ArrayList<AudioTreatmentStatusListener> treatmentStatusListeners = new ArrayList();

    public void addTreatmentStatusListener(AudioTreatmentStatusListener listener) {
	synchronized (treatmentStatusListeners) {
	    treatmentStatusListeners.remove(listener);
	    treatmentStatusListeners.add(listener);
	}
    }
	
    public void removeTreatmentStatusListener(AudioTreatmentStatusListener listener) {
	synchronized (treatmentStatusListeners) {
	    treatmentStatusListeners.remove(listener);
	}
    }

    private void notifyTreatmentEstablished() {
	synchronized (treatmentStatusListeners) {
	    for (AudioTreatmentStatusListener listener : treatmentStatusListeners) {
	        listener.treatmentEstablished();
	    }
	}
    }

    private void notifyTreatmentEnded(String reason) {
	synchronized (treatmentStatusListeners) {
	    for (AudioTreatmentStatusListener listener : treatmentStatusListeners) {
	        listener.treatmentEnded(reason);
	    }
	}
    }

    private void receive(CellMessage message) {
	if (message instanceof AudioTreatmentDoneMessage) {
	    addMenuItems(new String[] {"Play", "Volume"});
	    channelComp.send(new AudioTreatmentRequestMessage(cell.getCellID(), true, true));
	    return;
	}

	if (message instanceof AudioTreatmentEndedMessage) {
	    AudioTreatmentEndedMessage msg = (AudioTreatmentEndedMessage) message;
	    logger.warning("Treatment ended:  " + msg.getReason());
	    notifyTreatmentEnded(msg.getReason());
	    return;
	}

	if (message instanceof AudioTreatmentEstablishedMessage) {
	    logger.warning("Treatment established");
	    notifyTreatmentEstablished();
	    return;
	}

	if (message instanceof AudioTreatmentMenuChangeMessage) {
	    addMenuItems(((AudioTreatmentMenuChangeMessage) message).getMenuItems());
	    return;
	}
	
	if (message instanceof AudioVolumeMessage) {
	    double serverVolume = ((AudioVolumeMessage) message).getVolume();
	    
	    int clientVolume = VolumeUtil.getClientVolume(serverVolume);

	    logger.fine("Got volume message " + serverVolume);

	    new VolumeControlJFrame(cell.getCellID(), this, "", 
		CallID.getCallID(cell.getCellID()), clientVolume);
		
	    return;
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

        AudioTreatmentComponentClientState state = (AudioTreatmentComponentClientState) clientState;

        playWhen = state.getPlayWhen();

	playOnce = state.getPlayOnce();

	if (menuItemAdded == false) {
	    return;
	}

        if (playWhen.equals(PlayWhen.ALWAYS)) {
            addMenuItems(new String[] {"Stop", "Pause", "Volume"});;
        } else {
            addMenuItems(new String[] {"Play", "Volume"});
        }
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
