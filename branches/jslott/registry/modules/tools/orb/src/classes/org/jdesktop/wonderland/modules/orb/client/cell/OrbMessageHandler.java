/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.orb.client.cell;

import java.util.logging.Logger;

import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.Cell;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.orb.common.messages.OrbEndCallMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbMuteCallMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbSetVolumeMessage;
import org.jdesktop.wonderland.modules.orb.common.messages.OrbStartCallMessage;

import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
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

    private WonderlandSession session;

    private String name;

    private ChannelComponent channelComp;
        
    private OrbCell orbCell;

    public OrbMessageHandler(OrbCell orbCell) {
	this.orbCell = orbCell;

        channelComp = orbCell.getComponent(ChannelComponent.class);

        logger.fine("Channel comp is " + channelComp);

        ChannelComponent.ComponentMessageReceiver msgReceiver =
            new ChannelComponent.ComponentMessageReceiver() {
                public void messageReceived(CellMessage message) {
                    processMessage(message);
                }
            };

        channelComp.addMessageReceiver(OrbStartCallMessage.class, msgReceiver);
        channelComp.addMessageReceiver(OrbEndCallMessage.class, msgReceiver);
        channelComp.addMessageReceiver(OrbMuteCallMessage.class, msgReceiver);
        channelComp.addMessageReceiver(OrbSetVolumeMessage.class, msgReceiver);
    }

    public void processMessage(final Message message) {
	logger.finest("process message " + message);
    }
    
}
