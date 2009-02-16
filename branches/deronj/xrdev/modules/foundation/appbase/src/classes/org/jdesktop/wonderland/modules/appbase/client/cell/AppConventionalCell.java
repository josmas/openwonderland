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

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.OKMessage;
import org.jdesktop.wonderland.modules.appbase.common.cell.AppConventionalCellClientState;
import org.jdesktop.wonderland.modules.appbase.common.cell.AppConventionalCellSetConnectionInfoMessage;

/**
 * The client-side cell for an 2D conventional application.
 *
 * @author deronj
 */
@ExperimentalAPI
public abstract class AppConventionalCell extends App2DCell {

    /** The session used by the cell cache of this cell to connect to the server */
    private WonderlandSession session;
    /** The user-visible app name */
    protected String appName;
    /** The connection info. */
    protected Serializable connectionInfo;
    /** The App Conventional connection to the server. */
    private AppConventionalConnection connection;

    // TODO: eventually: do we need to save client state in the cell?
    /** 
     * Creates a new instance of AppConventionalCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public AppConventionalCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        session = cellCache.getSession();

        /* TODO: notyet
        // The first cell of this type for this session creates the connection
        connection = (AppConventionalConnection) session.getConnection(AppConventionalConnection.getConnectionTypeStatic());
        if (connection == null) {
            connection = new AppConventionalConnection(session);
            try {
                connection.connect(session);
            } catch (ConnectionFailureException ex) {
                throw new RuntimeException("Cannot create App Conventional connection, exception = " + ex);
            }
        }
        */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);

        AppConventionalCellClientState state = (AppConventionalCellClientState) clientState;
        appName = state.getAppName();

        if (state.getLaunchLocation().equalsIgnoreCase("user") &&
            state.getLaunchUser().equals(session.getUserID().getUsername())) {

            // Master case

            // TODO: later?: boolean bestView = state.isBestView();

            connectionInfo = startMaster(appName, state.getCommand(), false);
            if (connectionInfo == null) {
                logger.warning("Cannot launch app " + appName);
                // TODO: what else to do? Delete the cell? If so, how?
                return;
            }

            /* TODO: notyet
            // Notify server and clients of the new connection info.
            AppConventionalCellSetConnectionInfoMessage msg =
                new AppConventionalCellSetConnectionInfoMessage(getCellID(), connectionInfo);
            Message response = connection.sendAndWait(msg);
            if (!(response instanceof OKMessage)) {
                logger.warning("Cannot notify others of connection info for app + " + appName);
                // TODO: what else to do? Delete the cell? If so, how?
                return;
            }
            */

        } else {

            // Slave case
            //
            // Slaves must wait to connect until valid connection info is known. This can happen in one
            // of two ways. If the slave cell was loaded into this client AFTER the master app started 
            // the connection info will already be known (i.e. non-null). Otherwise, if the slave cell
            // was loaded into this client BEFORE the master app started this client will eventually
            // receive a SetConnectionInfo message whic contains the connection info.

            connectionInfo = state.getConnectionInfo();
            synchronized (this) {
                while (connectionInfo == null) {
                    logger.fine("Slave is waiting for connection info.");
                    try { wait(); } catch (InterruptedException ex) {}
                }
                logger.fine("Slave received connection info. Proceeding to connect client.");
            }

            // App User or World Launch: Slave case
            startSlave(connectionInfo);
        }
    }

    /**
     * This is called when the server sends the connection info.
     */
    synchronized void setConnectionInfo (Serializable connInfo) {
        
        // If we already know the connection info then we can skip this.
        // Note: this will happen if we are the master, or if this cell was created after
        // the server learned of the connection info.

        if (connectionInfo != null) {
            return;
        }

        if (connInfo != null) {
            connectionInfo = connInfo;
            notifyAll();
        }
    }

    /** 
     * Launch a master client.
     * @param appName The name of the app.
     * @param command The command string which launches the master app program (used only by master).
     * @param initInBestView Force this cell to be initialized in approximately the best view
     * based on the viewer position at the time of client cell creation.
     * @return Subclass-specific data for making a peer-to-peer connection between master and slave.
     */
    protected abstract Serializable startMaster(String appName, String command, boolean initInBestView);

    /** 
     * Launch a slave client.
     * @parem connectionInfo Subclass-specific data for making a peer-to-peer connection between 
     * master and slave.
     */
    protected abstract void startSlave(Serializable connectionInfo);
}
