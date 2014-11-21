/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

import com.sun.mpk20.voicelib.app.Treatment;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;
import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.ManagedReference;

import java.io.Serializable;
import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState.PlayWhen;

/**
 * @author jprovino
 * @author Abhishek Upadhyay
 */
public class AudioTreatmentProximityListener implements ProximityListenerSrv,
	ManagedObject, Serializable {

    private static final Logger logger =
            Logger.getLogger(AudioTreatmentProximityListener.class.getName());

    private String treatmentId;
    private int numberInRange;
    private boolean treatmentDone = true;
    private ManagedReference<CellMO> cellMO;

    public AudioTreatmentProximityListener(ManagedReference<CellMO> cellMO, Treatment treatment) {
	this.cellMO = cellMO;
	treatmentId = treatment.getId();
    }

    public void viewEnterExit(boolean entered, CellID cellID,
            CellID viewCellID, BoundingVolume proximityVolume,
            int proximityIndex) {

        AudioTreatmentComponentMO atc = cellMO.get().getComponent(AudioTreatmentComponentMO.class);
        PlayWhen playWhen = atc.getPlayWhen();
        
	logger.fine("viewEnterExit:  " + entered + " cellID " + cellID
	    + " viewCellID " + viewCellID);

        if(playWhen.equals(PlayWhen.FIRST_IN_RANGE)) {
            if (entered) {
                cellEntered();
            } else {
                cellExited();
            }
        }
    }

    public void cellEntered() {
	numberInRange++;

//	if (numberInRange > 1) {
//	    return;
//	}

	logger.warning("Restarting treatment...");

	Treatment treatment = AppContext.getManager(VoiceManager.class).getTreatment(treatmentId);

	if (treatment == null) {
	    logger.warning("No treatment for " + treatmentId);
	    return;
	}

        if(treatmentDone) {
            treatment.restart(false);
            treatmentDone = false;
        }
    }

    public void cellExited() {
	numberInRange--;

	if (numberInRange != 0) {
	    return;
	}

	logger.warning("Pausing treatment...");

	Treatment treatment = AppContext.getManager(VoiceManager.class).getTreatment(treatmentId);

	if (treatment == null) {
	    logger.warning("No treatment for " + treatmentId);
	    return;
	}
        
        treatmentDone = true;
	treatment.restart(true);
    }

    public void setTreatmentDone(boolean td) {
        this.treatmentDone = td;
    }
    
    public void stopTreatment() {
        logger.warning("Pausing treatment...");
        Treatment treatment = AppContext.getManager(VoiceManager.class).getTreatment(treatmentId);
        treatment.restart(true);
    }

}
