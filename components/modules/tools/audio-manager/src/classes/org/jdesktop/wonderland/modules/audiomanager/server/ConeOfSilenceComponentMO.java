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
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;

/**
 *
 * @author jprovino
 */
public class ConeOfSilenceComponentMO extends CellComponentMO {

    private static final Logger logger =
            Logger.getLogger(ConeOfSilenceComponentMO.class.getName());

    private float fullVolumeRadius = 0;

    /* Depends upon the proximity component, inject a reference to it */
    @UsesCellComponentMO(ProximityComponentMO.class)
    private ManagedReference<ProximityComponentMO> proximityRef = null;

    /* The proximity listener, initially null, created when necessary */
    private ManagedReference<ConeOfSilenceProximityListener> listenerRef = null;

    /**
     * Create a ConeOfSilenceComponent for the given cell. The cell must already
     * have a ChannelComponent otherwise this method will throw an IllegalStateException
     * @param cell
     */
    public ConeOfSilenceComponentMO(CellMO cellMO) {
        super(cellMO);

//        ProximityComponentMO prox = new ProximityComponentMO(cellMO);
//        BoundingVolume[] bounds = new BoundingVolume[] { new BoundingSphere(2f, new Vector3f()) };
//        prox.addProximityListener(new ConeOfSilenceProximityListener("fubar"), bounds );
//        cellMO.addComponent(prox);
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

        // Update the listener with the new radius, only can call this when
        // the component is live.
        if (isLive() == true) {
            addProximityListener();
        }
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

        // If we are making this component live, then add a listener to the
        // proximity component. Otherwise, remove the existing proximity listener
        // if the component is becoming un-live.
        if (live == true) {
            addProximityListener();
        }
        else {
           removeProximityListener();
        }
    }

    /**
     * Adds the proximity listener based upon the origin of the cell and radius
     * of the cone of silence component. Removes an existing listener if
     * present. This can only be called once setLive is called.
     */
    private void addProximityListener() {
        // Check to see if there is an existing proximity listener and remove
        // it if necessary.
        removeProximityListener();

        // Fetch the world bounds of the cell and create a new bounding sphere
        // based upon the full volume radius of the cone of silence
        BoundingVolume[] bounds = new BoundingVolume[] {
            new BoundingSphere(fullVolumeRadius, new Vector3f())};

        // Create a new listener with the sphere bounds and register.
        String name = cellRef.get().getName();
        ProximityComponentMO component = proximityRef.get();
        ConeOfSilenceProximityListener l = new ConeOfSilenceProximityListener(name);
        component.addProximityListener(l, bounds);
        listenerRef = AppContext.getDataManager().createReference(l);
        logger.warning("ADDED PROXIMITY COMPONENT WITH RADIUS " + fullVolumeRadius);
    }

    /**
     * Removes the proximity listener, if present. This can only be called
     * once setLive is called.
     */
    private void removeProximityListener() {
        ProximityComponentMO component = proximityRef.get();
        if (listenerRef != null) {
            component.removeProximityListener(listenerRef.get());
        }
    }
}
