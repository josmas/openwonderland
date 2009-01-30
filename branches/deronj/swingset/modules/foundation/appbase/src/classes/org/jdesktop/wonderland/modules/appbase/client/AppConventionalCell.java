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
package org.jdesktop.wonderland.modules.appbase.client;

import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalCellClientState;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The client-side cell for an 2D conventional application.
 *
 * @author deronj
 */
@ExperimentalAPI
public abstract class AppConventionalCell extends App2DCell {

    private static final Logger logger = Logger.getLogger(AppConventionalCell.class.getName());
    /** The session used by the cell cache of this cell to connect to the server */
    private WonderlandSession session;
    /** The user-visible app name */
    protected String appName;
    /** The connection info. */
    protected Serializable connectionInfo;

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
                // TODO: what to do?
                return;
            }

        // TODO: xxxxx: set connection info

        } else {

            // Slave case

            connectionInfo = state.getConnectionInfo();
            if (connectionInfo == null) {
                // TODO: xxx
                //waitForConnectionInfo();
            }

            // App User or World Launch: Slave case
            startSlave(connectionInfo);
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
