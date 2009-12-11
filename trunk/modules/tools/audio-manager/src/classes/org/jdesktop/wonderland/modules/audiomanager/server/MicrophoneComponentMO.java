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
package org.jdesktop.wonderland.modules.audiomanager.server;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.ActiveArea;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.FullVolumeArea;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.MicrophoneBoundsType;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;

/**
 * A server component that provides microphone functionality
 * @author jprovino
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
public class MicrophoneComponentMO extends CellComponentMO {

    private static final Logger LOGGER =
            Logger.getLogger(MicrophoneComponentMO.class.getName());
    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/audiomanager/common/Bundle");
    private static final String DEFAULT_NAME = BUNDLE.getString("Microphone");
    private String name = DEFAULT_NAME;
    private double volume = 1;
    private FullVolumeArea fullVolumeArea = new FullVolumeArea();
    private boolean showBounds = false;
    private ActiveArea activeArea = new ActiveArea();
    private boolean showActiveArea = false;
    private ManagedReference<MicrophoneEnterProximityListener> enterProximityListenerRef;
    private ManagedReference<MicrophoneActiveAreaProximityListener> activeAreaProximityListenerRef;

    public MicrophoneComponentMO(CellMO cellMO) {
        super(cellMO);

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

        MicrophoneComponentServerState state =
                (MicrophoneComponentServerState) serverState;

        setMyName(state);

        volume = state.getVolume();

        fullVolumeArea = state.getFullVolumeArea();

        showBounds = state.getShowBounds();

        activeArea = state.getActiveArea();

        showActiveArea = state.getShowActiveArea();

        LOGGER.info("name " + name + " volume " + volume + " fva " +
                fullVolumeArea + " aa " + activeArea);

        addProximityListeners(isLive());
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public CellComponentServerState getServerState(
            CellComponentServerState serverState) {
        MicrophoneComponentServerState state =
                (MicrophoneComponentServerState) serverState;

        // Create the proper server state object if it does not yet exist
        if (state == null) {
            state = new MicrophoneComponentServerState();
        }

        setMyName(state);

        state.setName(name);
        state.setVolume(volume);
        state.setFullVolumeArea(fullVolumeArea);
        state.setShowBounds(showBounds);
        state.setActiveArea(activeArea);
        state.setShowActiveArea(showActiveArea);

        return super.getServerState(state);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public CellComponentClientState getClientState(
            CellComponentClientState state, WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        // TODO: Create own client state object?
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.audiomanager.client.MicrophoneComponent";
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void setLive(boolean live) {
        super.setLive(live);

        addProximityListeners(live);
    }

    private void setMyName(MicrophoneComponentServerState state) {
        if (name == null) {
            name = DEFAULT_NAME;
        } else {
            name = state.getName();
        }

        String appendName = "-" + cellRef.get().getCellID();
        if (name.indexOf(appendName) < 0) {
            name += appendName;
        }
    }

    private void addProximityListeners(boolean live) {
        // Fetch the proximity component, we will need this below. If it does
        // not exist (it should), then log an error

        ProximityComponentMO component = cellRef.get().getComponent(ProximityComponentMO.class);

        if (component == null) {
            LOGGER.warning("The Microphone Component does not have a " +
                    "Proximity Component for Cell ID " + cellID);
            return;
        }

        if (enterProximityListenerRef != null) {
            MicrophoneEnterProximityListener enterProximityListener = 
		enterProximityListenerRef.get();
	    enterProximityListener.remove();
	    enterProximityListener = null;

            MicrophoneActiveAreaProximityListener activeAreaProximityListener =
                activeAreaProximityListenerRef.get();
	    activeAreaProximityListener.remove();
	    activeAreaProximityListener = null;

            component.removeProximityListener(enterProximityListener);
            component.removeProximityListener(activeAreaProximityListener);
        }

        // If we are making this component live, then add a listener to the proximity component.
        if (live == true) {
            Vector3f activeOrigin = new Vector3f((float) activeArea.activeAreaOrigin.getX(),
                    (float) activeArea.activeAreaOrigin.getY(),
                    (float) activeArea.activeAreaOrigin.getZ());

            BoundingVolume[] bounds = new BoundingVolume[1];

            if (activeArea.activeAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
                bounds[0] = cellRef.get().getLocalBounds();
		System.out.println("using mic cell bounds " + bounds[0]);
            } else if (activeArea.activeAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
                bounds[0] = new BoundingSphere((float) activeArea.activeAreaBounds.getX(), activeOrigin);
		System.out.println("using mic sphere " + bounds[0]);
            } else {
                bounds[0] = new BoundingBox(activeOrigin, (float) activeArea.activeAreaBounds.getX(),
                        (float) activeArea.activeAreaBounds.getY(),
                        (float) activeArea.activeAreaBounds.getZ());
		System.out.println("using mic sphere " + bounds[0]);
            }

            LOGGER.info("Active " + bounds[0]);

            MicrophoneActiveAreaProximityListener activeAreaProximityListener = 
		new MicrophoneActiveAreaProximityListener(cellRef.get(), name, volume);

	    activeAreaProximityListenerRef = AppContext.getDataManager().createReference(
		activeAreaProximityListener);

            component.addProximityListener(activeAreaProximityListener, bounds);

            bounds = new BoundingVolume[1];

            if (fullVolumeArea.boundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
                bounds[0] = cellRef.get().getLocalBounds();
                LOGGER.info("Microphone Using cell bounds:  " + bounds[0]);
                System.out.println("Microphone active Using cell bounds:  " + bounds[0]);
            } else if (fullVolumeArea.boundsType.equals(MicrophoneBoundsType.SPHERE)) {
                bounds[0] = new BoundingSphere((float) fullVolumeArea.bounds.getX(),
                        new Vector3f());
                LOGGER.info("Microphone Using radius:  " + bounds[0]);
                System.out.println("Microphone active Using radius:  " + bounds[0]);
            } else {
                bounds[0] = new BoundingBox(new Vector3f(), fullVolumeArea.bounds.getX(),
                        fullVolumeArea.bounds.getY(), fullVolumeArea.bounds.getZ());
                LOGGER.info("Microphone Using Box:  " + bounds[0]);
                System.out.println("Microphone active Using Box:  " + bounds[0]);
            }

            MicrophoneEnterProximityListener enterProximityListener = 
		new MicrophoneEnterProximityListener(cellRef.get(), name, volume);

	    enterProximityListenerRef = AppContext.getDataManager().createReference(
		enterProximityListener);

            component.addProximityListener(enterProximityListener, bounds);
        } 
    }

}
