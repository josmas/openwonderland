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
package org.jdesktop.wonderland.modules.microphone.client.cell;

//import org.jdesktop.wonderland.avatarorb.client.cell.AvatarOrbCell;

import com.sun.sgs.client.ClientChannel;

import java.util.logging.Logger;

import org.jdesktop.wonderland.client.cell.ChannelComponent;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.microphone.common.messages.MicrophoneVolumeMessage;

/**
 *
 * @author jprovino
 */
public class MicrophoneMessageHandler {

    private static final Logger logger =
            Logger.getLogger(MicrophoneMessageHandler.class.getName());

    private MicrophoneCell microphoneCell;

    private ChannelComponent channelComp;

    public MicrophoneMessageHandler(MicrophoneCell microphoneCell) {
	this.microphoneCell = microphoneCell;

	channelComp = microphoneCell.getComponent(ChannelComponent.class);

        ChannelComponent.ComponentMessageReceiver msgReceiver =
	    new ChannelComponent.ComponentMessageReceiver() {
                public void messageReceived(CellMessage message) {
		    processMessage(message);
                }
            };

	channelComp.addMessageReceiver(MicrophoneVolumeMessage.class, msgReceiver);

    }

    public void done() {
	channelComp.removeMessageReceiver(MicrophoneVolumeMessage.class);
    }

    public void processMessage(final Message message) {
    }
    
}
