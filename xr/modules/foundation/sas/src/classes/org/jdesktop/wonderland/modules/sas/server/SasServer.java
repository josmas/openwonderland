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

/**
 * Provides the main server-side logic for SAS. This singleton contains the Registry,
 * the Distributor and all of the server-side communications components for SAS.
 *
 * @author deronj
 */

@ExperimentalAPI
public class SasServer implements ManagedObject, Serializable, AppConventionalCellMO.AppServerLauncher {

    private static final Logger logger = Logger.getLogger(SasServer.class.getName());

    /** A collection of the SAS providers which have connected. */
    /*
   private HashMap<WonderlandClientID,WonderlandClientSender> providers = 
        new HashMap<WonderlandClientID,WonderlandClientSender>();
    */

    /**
     * Called when a new provider client connects to the SAS server.
     */
    public void providerConnected(WonderlandClientSender sender, WonderlandClientID clientID) {
        logger.severe("**** Sas provider connected, clientID = " + clientID);
        //providers.put(clientID, sender);
        //AppContext.getDataManager.markForUpdate(this);
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
    public Serializable appLaunch (CellID cellID, String executionCapability, String appname, 
                                   String command) {
        logger.severe("***** appLaunch, command = " + command);


        /*
          Map execution cap to a provider.
              if can't, pend the launch
          Map
         */
        return null;
    }
        
    /**
     * {@inheritDoc}
     */
    public void appStop (CellID cellID) {
        logger.severe("***** appLaunch, cellID = " + cellID);
    }
}

