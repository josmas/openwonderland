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
package org.jdesktop.wonderland.modules.phone.server.cell;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.phone.common.PhoneConnectionType;

import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.view.AvatarCellMO;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

import java.util.logging.Logger;

import java.util.Properties;

import java.io.Serializable;

/**
 * Phone
 * 
 * @author jprovino
 */
public class PhoneConnectionHandler implements ClientConnectionHandler,
	Serializable {

    private static final Logger logger =
            Logger.getLogger(PhoneConnectionHandler.class.getName());
    
    private ManagedReference<ManagedPhoneMessageHandler> phoneMessageHandlerRef;

    public PhoneConnectionHandler() {
        super();

	phoneMessageHandlerRef = 
	    AppContext.getDataManager().createReference(new ManagedPhoneMessageHandler());
    }

    public ConnectionType getConnectionType() {
        return PhoneConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
	logger.info("Phone connection registered");
    }

    public void clientConnected(WonderlandClientSender sender, 
	    ClientSession session, Properties properties) {

        //throw new UnsupportedOperationException("Not supported yet.");
	logger.warning("client connected...");
    }

    public void messageReceived(WonderlandClientSender sender, 
	    ClientSession session, Message message) {

	phoneMessageHandlerRef.get().processMessage(sender, session, message);
    }

    public void clientDisconnected(WonderlandClientSender sender, ClientSession session) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    static class ManagedPhoneMessageHandler extends PhoneMessageHandler implements ManagedObject {
    }

}
