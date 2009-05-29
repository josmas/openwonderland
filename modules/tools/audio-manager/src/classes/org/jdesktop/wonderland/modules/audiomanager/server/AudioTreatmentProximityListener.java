/** * Project Wonderland * * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved * * Redistributions in source code form must reproduce the above * copyright and this condition.
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

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioTreatmentEnterExitMessage;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupListener;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.AudioGroupSetup;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.sgs.app.AppContext;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import com.jme.bounding.BoundingVolume;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;

import java.io.Serializable;

/**
 * @author jprovino
 */
public class AudioTreatmentProximityListener implements ProximityListenerSrv, Serializable {

    private static final Logger logger =
            Logger.getLogger(AudioTreatmentProximityListener.class.getName());

    CellID cellID;
    String name;

    public AudioTreatmentProximityListener(CellMO cellMO) {
	cellID = cellMO.getCellID();
        name = cellMO.getName();
    }

    public void viewEnterExit(boolean entered, CellID cellID,
            CellID viewCellID, BoundingVolume proximityVolume,
            int proximityIndex) {

	logger.info("viewEnterExit:  " + entered + " cellID " + cellID
	    + " viewCellID " + viewCellID);

	if (entered) {
	    cellEntered(viewCellID);
	} else {
	    cellExited(viewCellID);
	}
    }

    private void cellEntered(CellID softphoneCellID) {
        cellEntered(CallID.getCallID(softphoneCellID));
    }

    public void cellEntered(String callId) {
        /*
         * The avatar has entered the audio treatment cell.
         * Set the public and incoming spatializers for the avatar to be
         * the zero volume spatializer.
         * Set a private spatializer for the given fullVolume radius
         * for all the other avatars in the cell.
         * For each avatar already in the cell, set a private spatializer
         * for this avatar.
         */
        logger.info(callId + " entered audio treatment " + name + " avatar cell ID " + callId);

        VoiceManager vm = AppContext.getManager(VoiceManager.class);
    }

    private void cellExited(CellID softphoneCellID) {
        cellExited(CallID.getCallID(softphoneCellID));
    }

    public void cellExited(String callId) {
        logger.info(callId + " exited audio treatment " + name + " avatar cell ID " + callId);

        VoiceManager vm = AppContext.getManager(VoiceManager.class);
    }

}
