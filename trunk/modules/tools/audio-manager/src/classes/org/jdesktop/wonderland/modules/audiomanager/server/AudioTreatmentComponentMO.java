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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;

import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.cell.setup.CellComponentSetup;

import org.jdesktop.wonderland.server.TimeManager;
import org.jdesktop.wonderland.server.WonderlandContext;

import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;

import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentSetup;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioTreatmentMessage;

import com.sun.voip.client.connector.CallStatus;
import com.sun.voip.client.connector.CallStatusListener;

import com.sun.voip.CallParticipant;

import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Treatment;
import com.sun.mpk20.voicelib.app.TreatmentGroup;
import com.sun.mpk20.voicelib.app.TreatmentSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author jprovino
 */
public class AudioTreatmentComponentMO extends CellComponentMO implements ManagedCallStatusListener {

    private static final Logger logger =
            Logger.getLogger(AudioTreatmentComponentMO.class.getName());

    private ManagedReference<ChannelComponentMO> channelComponentRef = null;
    
    private String groupId;
    private String[] treatments;
    private double x;
    private double y;
    private double z;

    /**
     * Create a AudioTreatmentComponent for the given cell. The cell must already
     * have a ChannelComponent otherwise this method will throw an IllegalStateException
     * @param cell
     */
    public AudioTreatmentComponentMO(CellMO cell) {
        super(cell);
        
        ChannelComponentMO channelComponent = (ChannelComponentMO) 
	    cell.getComponent(ChannelComponentMO.class);

        if (channelComponent == null) {
            logger.warning("Cell does not have a ChannelComponent");
	    return;
	}

        channelComponentRef = AppContext.getDataManager().createReference(channelComponent); 
                
        channelComponent.addMessageReceiver(AudioTreatmentMessage.class, new ComponentMessageReceiverImpl(this));
    }
    
    @Override
    public void setupCellComponent(CellComponentSetup setup) {
	AudioTreatmentComponentSetup accs = (AudioTreatmentComponentSetup) setup;

	treatments = accs.getTreatments();

	groupId = accs.getGroupId();
    }

    @Override
    public void setLive(boolean live) {
	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	TreatmentGroup group = vm.createTreatmentGroup(groupId);

	for (int i = 0; i < treatments.length; i++) {
	    TreatmentSetup setup = new TreatmentSetup();

	    setup.treatment = treatments[i];
	    
	    if (setup.treatment == null || setup.treatment.length() == 0) {
		logger.warning("Invalid treatment '" + setup.treatment + "'");
		continue;
	    }

	    Vector3f location = cellRef.get().getLocalTransform(null).getTranslation(null);

	    setup.x = -location.getX();
	    setup.y = location.getY();
	    setup.z = location.getZ();

	    logger.info("Starting treatment " + setup.treatment 
		+ " at (" + setup.x + ":" + setup.y + ":" + setup.z + ")");

	    try {
	        group.addTreatment(vm.createTreatment(setup.treatment, setup));
	    } catch (IOException e) {
	        logger.warning("Unable to create treatment " + setup.treatment
		    + e.getMessage());
    	        return;
	    }
	}
    }
    
    private static class ComponentMessageReceiverImpl implements ComponentMessageReceiver {

        private ManagedReference<AudioTreatmentComponentMO> compRef;
        
        public ComponentMessageReceiverImpl(AudioTreatmentComponentMO comp) {
            compRef = AppContext.getDataManager().createReference(comp);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID,
		CellMessage message) {

            AudioTreatmentMessage msg = (AudioTreatmentMessage) message;

	    logger.fine("Got AudioTreatmentMessage, startTreatment=" + msg.startTreatment());    
        }
    }

    public void callStatusChanged(CallStatus callStatus) {
	String callId = callStatus.getCallId();

	switch (callStatus.getCode()) {
	case CallStatus.ESTABLISHED:
	    break;

	case CallStatus.TREATMENTDONE:
	    break;
	}

    }

}
