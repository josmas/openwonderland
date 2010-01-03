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
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import com.jme.bounding.BoundingVolume;

import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;

/**
 * A component that provides a microphone
 * 
 * @author jprovino
 */
@ExperimentalAPI
public class MicrophoneComponent extends CellComponent {

    private static Logger logger = Logger.getLogger(MicrophoneComponent.class.getName());
    private ChannelComponent channelComp;
    private ChannelComponent.ComponentMessageReceiver msgReceiver;

    public MicrophoneComponent(Cell cell) {
        super(cell);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        switch (status) {
            case DISK:
                if (msgReceiver != null) {
                    channelComp.removeMessageReceiver(CellServerComponentMessage.class);
                    msgReceiver = null;
                }
                break;

            case ACTIVE:
                if (increasing && msgReceiver == null) {
                    msgReceiver = new ChannelComponent.ComponentMessageReceiver() {

                        public void messageReceived(CellMessage message) {
                        }
                    };

                    channelComp = cell.getComponent(ChannelComponent.class);
                    channelComp.addMessageReceiver(CellServerComponentMessage.class, msgReceiver);
                }
                break;
        }
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);

        logger.info("setClientState for microphone! " + clientState);
    }

}
