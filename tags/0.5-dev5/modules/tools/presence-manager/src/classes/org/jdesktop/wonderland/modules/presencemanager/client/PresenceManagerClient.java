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
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceManagerConnectionType;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.SessionCreatedMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.SessionEndedMessage;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellManager;
import java.util.logging.Logger;

/**
 *
 * @author jprovino
 */
public class PresenceManagerClient extends BaseConnection implements
        ViewCellConfiguredListener {

    private static final Logger logger =
            Logger.getLogger(PresenceManagerClient.class.getName());
    private WonderlandSession session;
    private CellID cellID;
    private boolean connected = true;
    private PresenceManagerImpl presenceManager;
    private PresenceInfo presenceInfo;

    /** 
     * Create a new PresenceManagerClient
     * @param session the session to connect to, guaranteed to be in
     * the CONNECTED state
     * @throws org.jdesktop.wonderland.client.comms.ConnectionFailureException
     */
    public PresenceManagerClient() {
        logger.fine("Starting PresenceManagerClient");
    }

    public synchronized void execute(final Runnable r) {
    }

    @Override
    public void connect(WonderlandSession session)
            throws ConnectionFailureException
    {
        super.connect(session);
        this.session = session;
        this.presenceManager = (PresenceManagerImpl) PresenceManagerFactory.getPresenceManager(session);

        LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();
        avatar.addViewCellConfiguredListener(this);
        if (avatar.getViewCell() != null) {
            // if the view is already configured, fake an event
            viewConfigured(avatar);
        }
    }

    @Override
    public void disconnect() {
        // LocalAvatar avatar = ((CellClientSession)session).getLocalAvatar();
        // avatar.removeViewCellConfiguredListener(this);
        super.disconnect();
        session.send(this, new SessionEndedMessage(presenceInfo));
    }

    public void viewConfigured(LocalAvatar localAvatar) {
        cellID = localAvatar.getViewCell().getCellID();

        String callID = CallID.getCallID(cellID);

        SoftphoneControlImpl.getInstance().setCallID(callID);

        presenceInfo = new PresenceInfo(cellID, session.getID(),
                session.getUserID(), callID);

	presenceManager.addSession(presenceInfo);
        session.send(this, new SessionCreatedMessage(presenceInfo));

        System.out.println("[PresenceManagerClient] view configured fpr " + cellID + " in " + presenceManager);
    }

    @Override
    public void handleMessage(Message message) {
        logger.fine("got a message...");

        if (message instanceof SessionCreatedMessage) {
            SessionCreatedMessage m = (SessionCreatedMessage) message;

            logger.fine("GOT SessionCreatedMessage for " + m.getPresenceInfo());

            presenceManager.addSession(m.getPresenceInfo());
            return;
        }

        if (message instanceof SessionEndedMessage) {
            SessionEndedMessage m = (SessionEndedMessage) message;

            logger.fine("GOT SessionEndedMessage for " + m.getPresenceInfo());
            presenceManager.removeSession(m.getPresenceInfo());
            return;
        }

        throw new UnsupportedOperationException("Unknown message:  " + message);
    }

    public ConnectionType getConnectionType() {
        return PresenceManagerConnectionType.CONNECTION_TYPE;
    }

}
