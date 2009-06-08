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
package org.jdesktop.wonderland.modules.microphone.server.cell;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import java.io.Serializable;

import java.util.logging.Logger;

import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;

import org.jdesktop.wonderland.modules.microphone.server.cell.MicrophoneCellMO;

import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell that provides conference microphone functionality
 * @author jprovino
 */
public class MicrophoneMessageHandler extends AbstractComponentMessageReceiver
	implements Serializable {

    private static final Logger logger =
        Logger.getLogger(MicrophoneMessageHandler.class.getName());
     
    public MicrophoneMessageHandler(MicrophoneCellMO microphoneCellMO) {
        super(microphoneCellMO);
    }

    public void messageReceived(final WonderlandClientSender sender, 
	    final WonderlandClientID clientID, final CellMessage message) {

    }
}
