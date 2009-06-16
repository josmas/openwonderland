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
package org.jdesktop.wonderland.modules.appbase.client.cell;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalConnectionType;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.appbase.common.cell.AppConventionalCellSetConnectionInfoMessage;

/**
 * Handler for app base conventional messages.
 *
 * @author deronj
 */

@InternalAPI
public class AppConventionalConnection extends BaseConnection {

    private static final Logger logger = Logger.getLogger(AppConventionalConnection.class.getName());

    /** The session to which this connection is connected. */
    private WonderlandSession session;

    /**
     * A static method which returns the type of cibbectuib type used by this type of connection.
     */
    public static ConnectionType getConnectionTypeStatic() {
        return AppConventionalConnectionType.CLIENT_TYPE;
    }

    /** 
     * Create a new instance of AppConventionalConnection.
     */
    public AppConventionalConnection (WonderlandSession session) {
        this.session = session;
    }
    
    /**
     * Returns the type of client type used by the connection.
     */
    public ConnectionType getConnectionType() {
        return AppConventionalConnectionType.CLIENT_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseMessage sendAndWait(Message message) {
	try {
	    return super.sendAndWait(message);
	} catch (InterruptedException ex) {}
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Message message) {
        super.send(message);
    }

    /**
     * {@inheritDoc}
     */
    public void handleMessage (Message message) {
        if (!(message instanceof AppConventionalCellSetConnectionInfoMessage)) {
            logger.warning("Invalid message type, message type = " + message.getClass());
            return;
        }

        AppConventionalCellSetConnectionInfoMessage msg =
            (AppConventionalCellSetConnectionInfoMessage) message;

        CellID cellID = msg.getCellID();
        CellCache cellCache = ClientContext.getCellCache(session);
        if (cellCache == null) {
            logger.warning("Cannot set cell connection info for this session. Session has no cell cache.");
            return;
        }
        Cell cell = cellCache.getCell(cellID);
        if (cell == null) {
            logger.warning("Cannot find cell to set connection info, cellID = " + cellID);
            return;
        }
        if (!(cell instanceof AppConventionalCell)) {
            logger.warning("Cell " + cellID + " is not of type AppConventionalCell.");
            return;
        }
        AppConventionalCell appConvCell = (AppConventionalCell) cell;
        appConvCell.setConnectionInfo(msg.getConnectionInfo());
    }
}
