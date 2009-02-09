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
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.appbase.server.cell.AppConventionalCellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.AppContext;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Provides the main server-side logic for SAS. This singleton contains the Registry,
 * the Distributor and all of the server-side communications components for SAS.
 *
 * @author deronj
 */

@ExperimentalAPI
public class SasServer implements ManagedObject, Serializable, AppConventionalCellMO.AppServerLauncher {

    private static final Logger logger = Logger.getLogger(SasServer.class.getName());

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
        logger.severe("**** Sas provider connected, clientID = " + clientID);
        
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

        logger.severe("**** provider added to xremwin list, clientID = " + clientID);

        // See if there are any pending launches
        tryPendingLaunches(execCap);

        // Mark server modified
        AppContext.getDataManager().markForUpdate(this);
    }

    /**
     * Called when provider client disconnects from the SAS server.
     */
    public void providerDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
        logger.severe("**** Sas provider disconnnected, clientID = " + clientID);
        //providers.remove(clientID);
    }

    /**
     * {@inheritDoc}
     */
    public Serializable appLaunch (CellID cellID, String executionCapability, String appName, 
                                   String command) {
        logger.severe("***** appLaunch, command = " + command);

        // TODO: For now eventually make sure that only one app can be launched per cell.
        // Later: allow multiple apps per cell.

        LinkedList<ProviderProxy> providers = execCapToProviderList.get(executionCapability);
        if (providers == null || providers.size() <= 0) {
            // No provider. Launch must pend
            logger.warning("No SAS provider for " + executionCapability + " is available.");
            logger.warning("Launch attempt will pend.");
            pendingLaunches.add(new PendingLaunches.LaunchRequest(cellID, executionCapability, appName,
                                                                  command));
            AppContext.getDataManager().markForUpdate(this);
            return null;
        }

        // TODO: for now, just try only the first provider
        ProviderProxy provider = providers.getFirst();
        Serializable connectionInfo = provider.tryLaunch(cellID, executionCapability, appName, command);
        if (connectionInfo == null) {
            // Provider cannot launch. Launch must pend.
            logger.warning("SAS provider launch failed for command = " + command);
            logger.warning("Launch attempt will pend.");
            pendingLaunches.add(new PendingLaunches.LaunchRequest(cellID, executionCapability, appName,
                                                                  command));
            AppContext.getDataManager().markForUpdate(this);
            return null;
        }

        return connectionInfo;
    }
        
    /**
     * {@inheritDoc}
     */
    public void appStop (CellID cellID) {
        logger.severe("***** appLaunch, cellID = " + cellID);
    }

    private void tryPendingLaunches (String executionCapability) {
        LinkedList<PendingLaunches.LaunchRequest> reqs = pendingLaunches.getPendingLaunches(executionCapability);
        if (reqs == null) {
            return;
        }

        for (PendingLaunches.LaunchRequest req : reqs) {

            // TODO: Some of this code is dup from above in tryLaunch; share it

            // See if there are any more providers to try
            LinkedList<ProviderProxy> providers = execCapToProviderList.get(executionCapability);
            if (providers == null || providers.size() <= 0) {
                continue;
            }
            // TODO: weed out providers already tried

            // TODO: for now, just try only the first provider
            ProviderProxy provider = providers.getFirst();
            Serializable connectionInfo = provider.tryLaunch(req.cellID, req.executionCapability, 
                                                             req.appName, req.command);
            if (connectionInfo != null) {
                // TODO: add to noLongerPendingList
                // TODO: need to set connection info in cell
            }
        }

        // TODO: process noLongerPendingList. Remove these from pendingLaunches.
    }
}

