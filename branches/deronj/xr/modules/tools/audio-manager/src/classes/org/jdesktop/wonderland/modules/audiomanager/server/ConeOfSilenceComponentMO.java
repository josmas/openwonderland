/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.audiomanager.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
import org.jdesktop.wonderland.modules.audiomanager.common.ConeOfSilenceComponentServerState;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;

/**
 *
 * @author jprovino
 */
public class ConeOfSilenceComponentMO extends CellComponentMO {

    private static final Logger logger =
            Logger.getLogger(ConeOfSilenceComponentMO.class.getName());

    private float fullVolumeRadius = 0;

    /**
     * Create a ConeOfSilenceComponent for the given cell. The cell must already
     * have a ChannelComponent otherwise this method will throw an IllegalStateException
     * @param cell
     */
    public ConeOfSilenceComponentMO(CellMO cellMO) {
        super(cellMO);

        // The Cone of Silence Components depends upon the Proximity Component.
        // We add this component as a dependency if it does not yet exist
        if (cellMO.getComponent(ProximityComponentMO.class) == null) {
            cellMO.addComponent(new ProximityComponentMO(cellMO));
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void setServerState(CellComponentServerState serverState) {
        super.setServerState(serverState);

        // Fetch the component-specific state and set member variables
        ConeOfSilenceComponentServerState cs = (ConeOfSilenceComponentServerState) serverState;

        fullVolumeRadius = cs.getFullVolumeRadius();
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public CellComponentServerState getServerState(CellComponentServerState serverState) {
        // Create the proper server state object if it does not yet exist
        if (serverState == null) {
            serverState = new ConeOfSilenceComponentServerState();
        }
        ((ConeOfSilenceComponentServerState)serverState).setFullVolumeRadius(fullVolumeRadius);

        return super.getServerState(serverState);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public CellComponentClientState getClientState(CellComponentClientState state,
            WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        // TODO: Create own client state object?
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    protected String getClientClass() {
        // There is no client-side component class, so return null.
        return null;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void setLive(boolean live) {
        super.setLive(live);

        // Fetch the proximity component, we will need this below. If it does
        // not exist (it should), then log an error
        ProximityComponentMO component = cellRef.get().getComponent(ProximityComponentMO.class);
        if (component == null) {
            logger.warning("The Cone of Silence Component does not have a " +
                    "Proximity Component for Cell ID " + cellID);
            return;
        }

        // If we are making this component live, then add a listener to the
        // proximity component.
        if (live == true) {
            BoundingVolume[] bounds = new BoundingVolume[1];
            bounds[0] = new BoundingSphere(fullVolumeRadius, new Vector3f());
            ConeOfSilenceProximityListener proximityListener = 
		new ConeOfSilenceProximityListener(cellRef.get().getName());
            component.addProximityListener(proximityListener, bounds);
        }
        else {
            // Really should remove the proximity listener here! XXX
        }
    }
}
