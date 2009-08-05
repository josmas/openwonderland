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
package org.jdesktop.wonderland.modules.sas.server;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.sas.common.SasProviderLaunchMessage;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.appbase.server.cell.AppConventionalCellMO;
import org.jdesktop.wonderland.modules.appbase.server.cell.AppConventionalCellMO.AppServerLauncher;
import org.jdesktop.wonderland.modules.sas.common.SasProviderLaunchStatusMessage;
import org.jdesktop.wonderland.modules.sas.common.SasProviderAppStopMessage;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import org.jdesktop.wonderland.common.messages.MessageID;

/**
 * This represents a provider on the server side.
 *
 * @author deronj
 */

class ProviderProxy implements Serializable {

    private static final Logger logger = Logger.getLogger(ProviderProxy.class.getName());

    /** The client ID of the provider. */
    private WonderlandClientID clientID;

    /** The provider's sender. */
    private WonderlandClientSender sender;

    /** The set of execution capabilities provided by this provider. */
    private HashSet<String> executionCapabilities = new HashSet<String>();

    /** 
     * The name of the Darkstar binding we use to store the reference to the cells launched by 
     * this provider. 
     */
    // TODO: AppConventionalCellMO should notify SAS of deleted cells so that we can better 
    // clean out this list
    private static String PROVIDER_CELLS_LAUNCHED_BINDING_NAME = 
        "org.jdesktop.wonderland.modules.sas.server.ProviderCellsLaunched";

    ProviderProxy (WonderlandClientID clientID, WonderlandClientSender sender) {
        this.clientID = clientID;
        this.sender = sender;

        ProviderCellsLaunched cellsLaunched = new ProviderCellsLaunched();
        AppContext.getDataManager().setBinding(PROVIDER_CELLS_LAUNCHED_BINDING_NAME, cellsLaunched);
    }

    public static ProviderCellsLaunched getProviderCellsLaunched () {
        return (ProviderCellsLaunched)
            AppContext.getDataManager().getBinding(PROVIDER_CELLS_LAUNCHED_BINDING_NAME);
    }

    WonderlandClientID getClientID () {
        return clientID;
    }

    synchronized void addExecutionCapability (String executionCapability) {
        executionCapabilities.add(executionCapability);
    }

    synchronized void removeExecutionCapability (String executionCapability) {
        executionCapabilities.remove(executionCapability);
    }

    /**
     * Does this provider provide the given execution capability?
     * @param executionCapability The execution capability to check for.
     */
    synchronized boolean provides (String executionCapability) {
        return executionCapabilities.contains(executionCapability);
    }

    /** 
     * See if this provider will launch the app. The result is reported by calling
     * server.appLaunchResult.
     */
    void tryLaunch (CellID cellID, String executionCapability, String appName, String command) 
        throws InstantiationException
    {
        logger.severe("**** Provider tryLaunch, clientID = " + clientID);
        logger.severe("command = " + command);

        SasProviderLaunchMessage msg = new SasProviderLaunchMessage(executionCapability, appName, command, cellID);

        // Record this message so we can match it up with its corresponding status message
        logger.info("message ID = " + msg.getMessageID());
        SasProviderConnectionHandler.addProviderMessageInFlight(msg.getMessageID(), this, cellID);

        // Now send the message. The response will come back asynchronously via the 
        // SasProviderConnectionHandler and it will report the launch status to the 
        // SasServer.appLaunchResult method
        try {
            sender.send(clientID, msg);
        } catch (Exception ex) {
            InstantiationException ie = new InstantiationException();
            ie.initCause(ex);
            throw ie;
        }
    }


    /**
     * Called by the provider connection handler to report the result of a launch
     */
    public void appLaunchResult (SasProviderLaunchStatusMessage.LaunchStatus status, CellID cellID,
                                 String connInfo) {

        SasServer sasServer = (SasServer) AppConventionalCellMO.getAppServerLauncher();

        AppConventionalCellMO.AppServerLauncher.LaunchStatus aslStatus = AppServerLauncher.LaunchStatus.FAIL;
        if (status == SasProviderLaunchStatusMessage.LaunchStatus.SUCCESS) {
            aslStatus = AppServerLauncher.LaunchStatus.SUCCESS;
            getProviderCellsLaunched().add(cellID);
        }

        logger.info("############### ProviderProxy: Launch result received");
        logger.info("aslStatus = " + aslStatus);
        logger.info("cellID = " + cellID);
        logger.info("connInfo = " + connInfo);

        sasServer.appLaunchResult(aslStatus, cellID, connInfo);
    }

    /**
     * Called when an app exits on this provider.
     * @param cellID The cell which launched the app.
     * @param exitValue The exit value of the app.
     */
    public void appExitted (CellID cellID, int exitValue) {
        getProviderCellsLaunched().remove(cellID);
        CellMO cell = CellManagerMO.getCell(cellID);
        if (cell != null) {
            if (!(cell instanceof AppConventionalCellMO)) {
                logger.warning("Cell to whom we are reporting app exit is not an AppConventionalMO");
            }
            ((AppConventionalCellMO)cell).appExitted(exitValue);
        }
    }
    
    /**
     * Called by the cell to stop the app. Usually called when the cell is deleted.
     * TODO: someday: currently assumes that there is only one app running per cell.
     */
    public void appStop (CellMO cell) {

        // Get the launch message ID which will identify the app to the provider
        MessageID launchMessageID = SasProviderConnectionHandler.getLaunchMessageIDForCellAndProvider(
                                                                      this, cell.getCellID());

        // Send a stop message to the provider
        if (launchMessageID != null) {
            SasProviderAppStopMessage msg = new SasProviderAppStopMessage(launchMessageID);
            try {
                sender.send(clientID, msg);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        // Remove information about the cell and app from the cells launched list
        getProviderCellsLaunched().remove(cell.getCellID());

        // Remove information about the cell and app from the connection handler
        SasProviderConnectionHandler.removeCell(cell.getCellID(), this);
    }

    /**
     * Clean up resources.
     */
    public void cleanup () {
        // Traverse through cells which have launched an app on this provider and ensure
        // that their connection infos are cleaned up.
        Iterator<CellID> it = getProviderCellsLaunched().getIterator();
        while (it.hasNext()) {
            CellID cellID = it.next();
            CellMO cell = CellManagerMO.getCell(cellID);
            if (cell != null) {
                if (!(cell instanceof AppConventionalCellMO)) {
                    logger.warning("Cell being cleaned up is not an AppConventionalMO");
                }
                ((AppConventionalCellMO)cell).appExitted(-1);
            }
        }
        getProviderCellsLaunched().clear();
    }
}
