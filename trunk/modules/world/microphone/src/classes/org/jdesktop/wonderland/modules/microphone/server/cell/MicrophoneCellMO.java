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
package org.jdesktop.wonderland.modules.microphone.server.cell;

import java.util.logging.Logger;

import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;

import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;


import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState.FullVolumeArea;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState.ActiveArea;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellClientState;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;

import com.jme.math.Vector3f;

import org.jdesktop.wonderland.server.comms.WonderlandClientID;

import com.sun.sgs.app.ManagedReference;
import java.util.Arrays;
import org.jdesktop.wonderland.modules.microphone.common.messages.MicrophoneEnterCellMessage;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;

/**
 * A server cell that provides conference microphone functionality
 * @author jprovino
 */
public class MicrophoneCellMO extends CellMO {

    private static final Logger logger =
            Logger.getLogger(MicrophoneCellMO.class.getName());

    private String modelFileName;
    private String microphoneName;
    private double volume;
    private FullVolumeArea fullVolumeArea;
    private ActiveArea activeArea;
    
    private MicrophoneProximityListener proxListener;

    @UsesCellComponentMO(ProximityComponentMO.class)
    private ManagedReference<ProximityComponentMO> proxRef;

    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelRef;

    public MicrophoneCellMO() {
    }

    public MicrophoneCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size),
                new CellTransform(null, center));
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        if (live) {
            channelRef.getForUpdate().addMessageReceiver(MicrophoneEnterCellMessage.class,
                                                         new MicrophoneMessageHandler(this));

            BoundingVolume[] bounds = new BoundingVolume[2];
            if (fullVolumeArea.areaType.equalsIgnoreCase("Sphere")) {
                bounds[0] = new BoundingSphere((float) fullVolumeArea.xExtent / 2, new Vector3f());
            } else {
                bounds[0] = new BoundingBox(new Vector3f(), (float) fullVolumeArea.xExtent,
                (float) fullVolumeArea.yExtent, (float) fullVolumeArea.zExtent);
            }

            Vector3f activeOrigin = new Vector3f((float) activeArea.origin.x,
                                                 (float) activeArea.origin.y,
                                                 (float) activeArea.origin.z);
            if (activeArea.areaType.equalsIgnoreCase("Sphere")) {
                bounds[1] = new BoundingSphere((float) 2, activeOrigin);
            } else {
                bounds[1] = new BoundingBox(activeOrigin, (float) activeArea.xExtent,
                                                          (float) activeArea.yExtent,
                                                          (float) activeArea.zExtent);
            }

            System.out.println("Microphone bounds: " + Arrays.toString(bounds));

            proxListener =
                new MicrophoneProximityListener(microphoneName, volume, bounds);
            proxRef.getForUpdate().addProximityListener(proxListener, bounds);
        } else {

            channelRef.getForUpdate().removeMessageReceiver(MicrophoneEnterCellMessage.class);

            if (proxListener != null) {
		proxListener.remove();
                proxRef.getForUpdate().removeProximityListener(proxListener);
                proxListener = null;
            }
        }
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        return "org.jdesktop.wonderland.modules.microphone.client.cell.MicrophoneCell";
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        if (cellClientState == null) {
            cellClientState = new MicrophoneCellClientState(microphoneName, volume, fullVolumeArea,
                    activeArea);
        }

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setServerState(CellServerState cellServerState) {
        super.setServerState(cellServerState);

        MicrophoneCellServerState microphoneCellServerState = (MicrophoneCellServerState) cellServerState;

	if (microphoneName != null && microphoneName.equals(microphoneCellServerState.getMicrophoneName()) == false) {
	    if (proxListener != null) {
		proxListener.changeMicrophoneName(microphoneCellServerState.getMicrophoneName());
	    }
	}

        microphoneName = microphoneCellServerState.getMicrophoneName();

	volume = microphoneCellServerState.getVolume();
        fullVolumeArea = microphoneCellServerState.getFullVolumeArea();
        activeArea = microphoneCellServerState.getActiveArea();

        logger.fine("setServerState fva " + fullVolumeArea.areaType
            + " x " + fullVolumeArea.xExtent
            + " y " + fullVolumeArea.yExtent
            + " z " + fullVolumeArea.zExtent);

        logger.fine("setServerState active area" 
	    + "origin (" + activeArea.origin.x + ","
	    + activeArea.origin.y + "," + activeArea.origin.z + ")"
	    + " type " + activeArea.areaType
            + " x " + activeArea.xExtent
            + " y " + activeArea.yExtent
            + " z " + activeArea.zExtent);
    }

    /**
     * Return a new CellServerState Java bean class that represents the current
     * state of the cell.
     *
     * @return a CellServerState representing the current state
     */
    @Override
    public CellServerState getServerState(CellServerState cellServerState) {
        /* Create a new BasicCellState and populate its members */
        if (cellServerState == null) {
            cellServerState = new MicrophoneCellServerState(microphoneName, volume, 
		fullVolumeArea, activeArea);
        }

        return super.getServerState(cellServerState);
    }

}
