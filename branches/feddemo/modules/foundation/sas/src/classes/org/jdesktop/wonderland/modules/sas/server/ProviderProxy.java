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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.sas.server;

import java.io.Serializable;
import java.util.HashSet;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.sas.common.SasProviderLaunchMessage;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.appbase.server.cell.AppConventionalCellMO;
import org.jdesktop.wonderland.modules.appbase.server.cell.AppConventionalCellMO.AppServerLauncher;
import org.jdesktop.wonderland.modules.sas.common.SasProviderLaunchStatusMessage;

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

    /** Whether the last launch response has arrived. */
    private boolean lastLaunchResponded;

    /** Whether the last launch succeeded. */
    private boolean lastLaunchSuccess;

    /** The connection info of the last successful launch. */
    private String lastLaunchConnInfo;

    ProviderProxy (WonderlandClientID clientID, WonderlandClientSender sender) {
        this.clientID = clientID;
        this.sender = sender;
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
    void tryLaunch (CellID cellID, String executionCapability, String appName, String command) {
        logger.severe("**** Provider tryLaunch, clientID = " + clientID);
        logger.severe("command = " + command);

        SasProviderLaunchMessage msg = new SasProviderLaunchMessage(executionCapability, appName, command);

        // Record this message so we can match it up with its corresponding status message
        logger.severe("message ID = " + msg.getMessageID());
        SasProviderConnectionHandler.addProviderMessageInFlight(msg.getMessageID(), this, cellID);

        // Now send the message. The response will come back asynchronously via the 
        // SasProviderConnectionHandler and it will report the launch status to the 
        // SasServer.appLaunchResult method
        sender.send(clientID, msg);
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
        }

        logger.severe("############### ProviderProxy: Launch result received");
        logger.severe("aslStatus = " + aslStatus);
        logger.severe("cellID = " + cellID);
        logger.severe("connInfo = " + connInfo);

        sasServer.appLaunchResult(aslStatus, cellID, connInfo);
    }
}
