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
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.appbase.server.cell.AppConventionalCellMO;
import org.jdesktop.wonderland.modules.appbase.server.cell.AppConventionalCellMO.AppServerLauncher;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.AppContext;
import java.util.HashMap;
import java.util.LinkedList;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;

/**
 * Provides the main server-side logic for SAS. This singleton contains the Registry,
 * the Distributor and all of the server-side communications components for SAS.
 *
 * @author deronj
 */

@ExperimentalAPI
public class SasServer implements ManagedObject, Serializable, AppServerLauncher {

    private static final Logger logger = Logger.getLogger(SasServer.class.getName());

    static class LaunchRequest implements Serializable {
        CellID cellID;
        String executionCapability;
        String appName;
        String command;

        LaunchRequest (CellID cellID, String executionCapability, String appName, String command) {
            this.cellID = cellID;
            this.executionCapability = executionCapability;
            this.appName = appName;
            this.command = command;
        }

        public String toString () {
            return "cell = " + cellID + ", executionCapability = " + executionCapability +
                "appName = " + appName + ", command = " + command;
        }
    }

    /**
     * A map of the app launch requests in flight for various cells.
     * "In flight" means that the request has been sent to a provider.
     * Note: We manage things so that only one launch request can be in flight at a time 
     * for a particular app cell.
     */
    private HashMap<CellID,LaunchRequest> launchesInFlight = new HashMap<CellID,LaunchRequest>();

    /** 
     * A map of the SAS providers which have connected, indexed by their execution capabilities
     * Note: because a provider may support multiple capabilities it may appear on more than one list.
     */
    private HashMap<String,LinkedList<ProviderProxy>> execCapToProviderList = 
        new HashMap<String,LinkedList<ProviderProxy>>();

    /**
     * A list of the app launch requests which still must be honored.
     */
    private PendingLaunches pendingLaunches = new PendingLaunches();

    /**
     * Called when a new provider client connects to the SAS server.
     */
    public void providerConnected(WonderlandClientSender sender, WonderlandClientID clientID) {
        logger.info("**** Sas provider connected, clientID = " + clientID);
        
        // TODO: for now everything uses xremwin
        String execCap = "xremwin";

        ProviderProxy provider = new ProviderProxy(clientID, sender);
        provider.addExecutionCapability(execCap);

        // Add to execution capability list
        LinkedList<ProviderProxy> providers = execCapToProviderList.get(execCap);
        if (providers == null) {
            providers = new LinkedList<ProviderProxy>();
            execCapToProviderList.put(execCap, providers);
        }
        providers.add(provider);

        logger.info("**** provider added to xremwin list, clientID = " + clientID);

        // See if there are any pending launches
        try {
            tryPendingLaunches(execCap);
        } catch (InstantiationException ie) {
            logger.warning("Exception during new provider connection execution of pending launches.");
            logger.warning("Exception = " + ie);
        }

        // Mark server modified
        AppContext.getDataManager().markForUpdate(this);
    }

    /**
     * Called when provider client disconnects from the SAS server.
     */
    public void providerDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
        logger.info("**** Sas provider disconnnected, clientID = " + clientID);

        // TODO: for now everything uses xremwin
        String execCap = "xremwin";

        // Remove provider from execution capability list 
        LinkedList<ProviderProxy> providers = execCapToProviderList.get(execCap);
        if (providers != null) {
            ProviderProxy toRemove = null;
            for (ProviderProxy provider : providers) {
                if (provider.getClientID().equals(clientID)) {
                    toRemove = provider;
                    break;
                }
            }
            if (toRemove != null) {
                toRemove.cleanup();
                providers.remove(toRemove);
            }

            if (providers.size() <= 0) {
                execCapToProviderList.remove(execCap);
            }
        }

        // Mark server modified
        AppContext.getDataManager().markForUpdate(this);
    }

    /**
     * {@inheritDoc}
     */
    public void appLaunch (AppConventionalCellMO cell, String executionCapability, String appName, String command) 
        throws InstantiationException 
    {
        logger.info("***** appLaunch, command = " + command);

        CellID cellID = cell.getCellID();

        // Construct the launch request
        LaunchRequest launchReq = new LaunchRequest(cellID, executionCapability, appName, command);

        // TODO: someday: allow multiple apps to be launched per cell.

        LinkedList<ProviderProxy> providers = execCapToProviderList.get(executionCapability);
        if (providers == null || providers.size() <= 0) {
            // No provider. Launch must pend
            logger.warning("No SAS provider for " + executionCapability + " is available.");
            logger.warning("Launch attempt will pend.");
            pendingLaunches.add(launchReq);
            AppContext.getDataManager().markForUpdate(this);
            return;
        }

        // TODO: someday: Right now we just try only the first provider. Eventually try multiple providers.
        ProviderProxy provider = providers.getFirst();
        if (provider == null) {
            throw new InstantiationException("Cannot find a provider for " + executionCapability);
        }

        // Now request the provider to launch the app
        launchesInFlight.put(cellID, launchReq);
        provider.tryLaunch(cellID, executionCapability, appName, command);
    }
        
    /**
     * Called by the provider proxy to report the result of a launch
     */
    public void appLaunchResult (AppServerLauncher.LaunchStatus status, CellID cellID, String connInfo) {
        logger.info("############### SasServer: Launch result received");
        logger.info("status = " + status);
        logger.info("cellID = " + cellID);
        logger.info("connInfo = " + connInfo);

        // Get the request that we used to launch the app
        LaunchRequest launchReq = launchesInFlight.get(cellID);
        if (launchReq == null) {
            logger.warning("Cannot app request launch for cell " + cellID);
            return;
        }
        launchesInFlight.remove(cellID);
        AppContext.getDataManager().markForUpdate(this);

        CellMO cell = CellManagerMO.getCell(cellID);
        if (cell == null) {
            logger.warning("Cannot find cell to which to report app launch result, launch request = " + 
                           launchReq);
            return;
        }
        if (!(cell instanceof AppConventionalCellMO)) {
            logger.warning("Cell reported in app launch result is not an AppConventionalMO, launch request = " + 
                           launchReq);
            return;
        }

        if (status != AppServerLauncher.LaunchStatus.SUCCESS || connInfo == null) {
            // The provider we tried cannot launch. Launch must pend.
            logger.warning("SAS provider launch failed with status " + status + 
                           " and connection info " + connInfo +
                           " for launch request = " + launchReq);
            logger.warning("Launch attempt will pend until a provider is found.");
            pendingLaunches.add(launchReq);
            // Note: server has already been marked for update above

            // TODO: someday: at some point we need to give up and call cell.appLaunchResult with a failure 
            // status. Need to implement a timeout.
            return;
        }

        ((AppConventionalCellMO)cell).appLaunchResult(status, connInfo);
    }
    
    /**
     * {@inheritDoc}
     */
    public void appStop (CellID cellID) {
        logger.info("***** appLaunch, cellID = " + cellID);
        // TODO: tell the provider to stop the app, if it is still connected
        // TODO: make sure we remove all inflight requests and messages and pending messages as well
    }

    private void tryPendingLaunches (String executionCapability) throws InstantiationException {
        LinkedList<LaunchRequest> reqs = pendingLaunches.getPendingLaunches(executionCapability);
        if (reqs == null) {
            return;
        }
        LinkedList<LaunchRequest> reqsForTraversal = (LinkedList<LaunchRequest>) reqs.clone();
        
        for (LaunchRequest req : reqsForTraversal) {

            // TODO: Some of this code is dup from above in tryLaunch; share it

            // See if there are any more providers to try
            LinkedList<ProviderProxy> providers = execCapToProviderList.get(executionCapability);
            if (providers == null || providers.size() <= 0) {
                continue;
            }
            // TODO: someday: weed out providers already tried
            ProviderProxy provider = providers.getFirst();

            // Remove request from pending list while it is in flight */
            reqs.remove(req);

            // Now request the newly selected provider to launch the app
            launchesInFlight.put(req.cellID, req);
            provider.tryLaunch(req.cellID, req.executionCapability, req.appName, req.command);
        }
    }
}

