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
package org.jdesktop.wonderland.modules.presencemanager.server;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceManagerConnectionType;

import org.jdesktop.wonderland.modules.presencemanager.common.messages.CellStatusChangeMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.SessionCreatedMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.SessionEndedMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientConnectedMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientDisconnectedMessage;

import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.CommsManagerFactory;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import java.math.BigInteger;

import java.util.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;

import java.util.Iterator;
import java.util.Properties;

import java.io.IOException;
import java.io.Serializable;

import com.sun.sgs.app.ManagedObject;

/**
 * Presence Manager
 * 
 * @author jprovino
 */
public class PresenceManagerConnectionHandler 
        implements ClientConnectionHandler, Serializable, ManagedObject {

    private static final Logger logger =
            Logger.getLogger(PresenceManagerConnectionHandler.class.getName());
    
    private ConcurrentHashMap<BigInteger, PresenceInfo> sessions = 
	new ConcurrentHashMap();

    public PresenceManagerConnectionHandler() {
        super();
    }

    public ConnectionType getConnectionType() {
        return PresenceManagerConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
	logger.fine("Presence Server manager connection registered");
    }

    public void clientConnected(WonderlandClientSender sender, 
	    WonderlandClientID clientID, Properties properties) {

        //throw new UnsupportedOperationException("Not supported yet.");
	logger.fine("client connected...");
    }

    public void messageReceived(WonderlandClientSender sender, 
	    WonderlandClientID clientID, Message message) {

	if (message instanceof SessionCreatedMessage) {
	    sessionCreated(sender, clientID, (SessionCreatedMessage) message);
	    return;
	} 

	if (message instanceof SessionEndedMessage) {
	    sessionEnded(sender, ((SessionEndedMessage) message).getPresenceInfo());
	    return;
	}

        throw new UnsupportedOperationException("Unknown message: " + message);
    }

    public void clientDisconnected(WonderlandClientSender sender, 
	    WonderlandClientID clientID) {

	PresenceInfo info = sessions.get(clientID.getID());

	if (info == null) {
	    logger.warning("Can't find PresenceInfo for " + clientID.getID());
	    return;
	}

	sessionEnded(sender, info);
    }

    private void sessionCreated(WonderlandClientSender sender, WonderlandClientID clientID,
	    SessionCreatedMessage message) {

	PresenceInfo info = message.getPresenceInfo();

	logger.fine("SESSION CREATED " + info);

	sessions.put(info.clientID, info);

	sender.send(message);

	/*
	 * Send back all of the PresenceInfo data to the new client
	 */
	Iterator<BigInteger> it = sessions.keySet().iterator();

	while (it.hasNext()) {
	    BigInteger sessionID = it.next();

	    if (clientID.equals(sessionID)) {
		continue;
	    }

	    PresenceInfo sessionInfo = sessions.get(sessionID);

	    logger.fine("Sending session created message to " + sessionInfo.userID
		+ " new session:  " + sessionInfo.userID);

	    sender.send(clientID, new SessionCreatedMessage(sessionInfo));
	}
    }

    private void sessionEnded(WonderlandClientSender sender, PresenceInfo presenceInfo) {
	logger.fine("SESSION ENDED " + presenceInfo);

	if (sessions.remove(presenceInfo.clientID) == null) {
	    logger.warning("Can't find PresenceInfo for " + presenceInfo.clientID);
	}

	sender.send(new SessionEndedMessage(presenceInfo));
    }

}
