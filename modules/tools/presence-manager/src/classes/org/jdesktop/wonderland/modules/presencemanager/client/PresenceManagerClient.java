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
package org.jdesktop.wonderland.modules.presencemanager.client;

import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;

import org.jdesktop.wonderland.client.cell.view.ViewCell;

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.client.jme.JmeClientMain;

import org.jdesktop.wonderland.common.NetworkAddress;

import org.jdesktop.wonderland.common.comms.ConnectionType;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.input.EventListener;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceManagerConnectionType;

import org.jdesktop.wonderland.modules.presencemanager.common.messages.CellStatusChangeMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.SessionCreatedMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.SessionEndedMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientConnectedMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientDisconnectedMessage;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;

import java.io.IOException;

import java.util.ArrayList;

import java.util.logging.Logger;

/**
 *
 * @author jprovino
 */
public class PresenceManagerClient extends BaseConnection implements 
	ViewCellConfiguredListener, CellStatusChangeListener {

    private static final Logger logger =
        Logger.getLogger(PresenceManagerClient.class.getName());

    private WonderlandSession session;

    private CellID cellID;
    private boolean connected = true;

    private PresenceManager presenceManager;

    /** 
     * Create a new PresenceManagerClient
     * @param session the session to connect to, guaranteed to be in
     * the CONNECTED state
     * @throws org.jdesktop.wonderland.client.comms.ConnectionFailureException
     */
    public PresenceManagerClient(WonderlandSession session)  
	    throws ConnectionFailureException {

	this.session = session;

        LocalAvatar avatar = ((CellClientSession)session).getLocalAvatar();
        avatar.addViewCellConfiguredListener(this);
        if (avatar.getViewCell() != null) {
            // if the view is already configured, fake an event
            viewConfigured(avatar);
        }

	CellManager.getCellManager().addCellStatusChangeListener(this);

	presenceManager = PresenceManagerFactory.getPresenceManager(session);

	logger.fine("Starting PresenceManagerClient");

	session.connect(this);
    }

    public void cellStatusChanged(Cell cell, CellStatus status) {
	if (cell.getCellID().equals(cellID) == false) {
	    return;
	}
    }

    public synchronized void execute(final Runnable r) {
    }

    @Override
    public void disconnect() {
        // LocalAvatar avatar = ((CellClientSession)session).getLocalAvatar();
        // avatar.removeViewCellConfiguredListener(this);
        super.disconnect();
	session.send(this, new SessionEndedMessage(cellID, session.getID(),
		session.getUserID()));
    }

    public void viewConfigured(LocalAvatar localAvatar) {
	cellID = localAvatar.getViewCell().getCellID();

	session.send(this, new SessionCreatedMessage(cellID, session.getID(),
	    session.getUserID()));
    }

    @Override
    public void handleMessage(Message message) {
	logger.fine("got a message...");

	if (message instanceof SessionCreatedMessage) {
	    SessionCreatedMessage m = (SessionCreatedMessage) message;

	    logger.fine("GOT SessionCreatedMessage for " + m.getUserID());

	    presenceManager.addSession(m.getCellID(), m.getSessionID(), m.getUserID());
	    return;
	}

	if (message instanceof SessionEndedMessage) {
	    SessionEndedMessage m = (SessionEndedMessage) message;

	    logger.fine("GOT SessionEndedMessage for " + m.getUserID());
	    presenceManager.removeSession(m.getCellID(), m.getSessionID(), m.getUserID());
	    return;
	}

        throw new UnsupportedOperationException("Unknown message:  " + message);
    }

    public ConnectionType getConnectionType() {
        return PresenceManagerConnectionType.CONNECTION_TYPE;
    }

}
