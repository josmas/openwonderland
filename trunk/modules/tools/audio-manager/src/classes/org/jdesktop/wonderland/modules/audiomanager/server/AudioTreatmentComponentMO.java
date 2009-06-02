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

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;

import com.jme.math.Vector3f;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;

import java.io.InputStreamReader;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentClientState;
import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioTreatmentMessage;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import com.sun.voip.client.connector.CallStatus;

import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.Treatment;
import com.sun.mpk20.voicelib.app.TreatmentGroup;
import com.sun.mpk20.voicelib.app.TreatmentSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;

import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

import org.jdesktop.wonderland.common.checksums.Checksum;
import org.jdesktop.wonderland.common.checksums.ChecksumList;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author jprovino
 */
public class AudioTreatmentComponentMO extends AudioParticipantComponentMO implements
        ManagedCallStatusListener {

    private static final Logger logger =
            Logger.getLogger(AudioTreatmentComponentMO.class.getName());

    private static final String ASSET_PREFIX = "wonderland-web-asset/asset/";

    private String groupId;
    private String[] treatments;
    private double x;
    private double y;
    private double z;
    private double fullVolumeRadius;
    private double zeroVolumeRadius;
    private double volume;
    private boolean useFullVolumeSpatializer = false;
    private boolean startImmediately = true;

    private static String serverURL;

    static {
        serverURL = System.getProperty("wonderland.web.server.url");
    }

    /**
     * Create a AudioTreatmentComponent for the given cell. The cell must already
     * have a ChannelComponent otherwise this method will throw an IllegalStateException
     * @param cell
     */
    public AudioTreatmentComponentMO(CellMO cellMO) {
        super(cellMO);

        // The AudioTreatment Component depends upon the Proximity Component.
        // We add this component as a dependency if it does not yet exist
        if (cellMO.getComponent(ProximityComponentMO.class) == null) {
            cellMO.addComponent(new ProximityComponentMO(cellMO));
        }
    }

    @Override
    public void setServerState(CellComponentServerState serverState) {
	super.setServerState(serverState);

        AudioTreatmentComponentServerState state = (AudioTreatmentComponentServerState) serverState;

        treatments = state.getTreatments();

        groupId = state.getGroupId();

        fullVolumeRadius = state.getFullVolumeRadius();
        zeroVolumeRadius = state.getZeroVolumeRadius();
        useFullVolumeSpatializer = state.getUseFullVolumeSpatializer();
	volume = state.getVolume();
	startImmediately = state.getStartImmediately();

	addProximityListener(isLive());

	if (isLive()) {
	    initialize();
	}
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState serverState) {
        AudioTreatmentComponentServerState state = (AudioTreatmentComponentServerState) serverState;

        if (state == null) {
            state = new AudioTreatmentComponentServerState();

            state.treatments = treatments;
            state.setGroupId(groupId);
            state.setFullVolumeRadius(fullVolumeRadius);
            state.setZeroVolumeRadius(zeroVolumeRadius);
            state.setUseFullVolumeSpatializer(useFullVolumeSpatializer);
	    state.setStartImmediately(startImmediately);
        }

        return super.getServerState(state);
    }

    @Override
    public CellComponentClientState getClientState(
            CellComponentClientState clientState,
            WonderlandClientID clientID,
            ClientCapabilities capabilities) {

	AudioTreatmentComponentClientState state = (AudioTreatmentComponentClientState) clientState;

	if (state == null) {
	    state = new AudioTreatmentComponentClientState();

	    state.treatments = treatments;
	    state.groupId = groupId;
	    state.fullVolumeRadius = fullVolumeRadius;
	    state.zeroVolumeRadius = zeroVolumeRadius;
	    state.useFullVolumeSpatializer = useFullVolumeSpatializer;
	    state.volume = volume;
	    state.startImmediately = startImmediately;
	}

        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channelComponent = (ChannelComponentMO) cellRef.get().getComponent(ChannelComponentMO.class);

        if (live == false) {
            channelComponent.removeMessageReceiver(AudioTreatmentMessage.class);
            return;
        }

        ComponentMessageReceiverImpl receiver = new ComponentMessageReceiverImpl(cellRef, this);

        channelComponent.addMessageReceiver(AudioTreatmentMessage.class, receiver);

	initialize();
    }

    private void initialize() {
	if (groupId == null || treatments.length == 0) {
	    /*
	     * The AudioTreatmentComponent hasn't been configured yet.
	     */
	    logger.info("Not starting treatment:  groupID " + groupId + " treatments.length " 
		+ treatments.length);

	    return;
	}

        VoiceManager vm = AppContext.getManager(VoiceManager.class);

        TreatmentGroup group = vm.createTreatmentGroup(groupId);

        for (int i = 0; i < treatments.length; i++) {
            TreatmentSetup setup = new TreatmentSetup();

            if (useFullVolumeSpatializer == false) {
                DefaultSpatializer spatializer = new DefaultSpatializer();

                setup.spatializer = spatializer;

                spatializer.setFullVolumeRadius(fullVolumeRadius);

                if (zeroVolumeRadius != 0) {
                    spatializer.setZeroVolumeRadius(zeroVolumeRadius);
                }
            } else {
                setup.spatializer = new FullVolumeSpatializer(fullVolumeRadius);
            }

            String treatment = treatments[i];

            logger.fine("Processing " + treatment);

            String treatmentId = CallID.getCallID(cellRef.get().getCellID());

            if (treatment.startsWith("wls://")) {
                /*
                 * We need to create a URL from wls:<module>/path
                 */
                treatment = treatment.substring(6);  // skip past wls://

                int ix = treatment.indexOf("/");

                if (ix < 0) {
                    logger.warning("Bad treatment:  " + treatments[i]);
                    continue;
                }

                String moduleName = treatment.substring(0, ix);

                String path = treatment.substring(ix + 1);

                logger.fine("Module:  " + moduleName + " treatment " + treatment);

                URL url;

                try {
                    url = new URL(new URL(serverURL),
                            "webdav/content/modules/installed/" + moduleName + "/audio/" + path);

                    treatment = url.toString();
                    logger.fine("Treatment: " + treatment);
                } catch (MalformedURLException e) {
                    logger.warning("bad url:  " + e.getMessage());
                    continue;
                }
            }

            setup.treatment = treatment;
	    setup.managedListenerRef = 
		AppContext.getDataManager().createReference((ManagedCallStatusListener) this);

            if (setup.treatment == null || setup.treatment.length() == 0) {
                logger.warning("Invalid treatment '" + setup.treatment + "'");
                continue;
            }

            Vector3f location = cellRef.get().getLocalTransform(null).getTranslation(null);

            setup.x = location.getX();
            setup.y = location.getY();
            setup.z = location.getZ();

            logger.info("Starting treatment " + setup.treatment + " at (" + setup.x 
		+ ":" + setup.y + ":" + setup.z + ")");

            try {
		Treatment t = vm.createTreatment(treatmentId, setup);
                group.addTreatment(t);

		if (startImmediately == false) {
		    t.pause(true);
		}
            } catch (IOException e) {
                logger.warning("Unable to create treatment " + setup.treatment + e.getMessage());
                return;
            }
        }
    }

    public String[] getTreatments() {
        return treatments;
    }

    public CellMO getCell() {
        return cellRef.get();
    }

    public double getFullVolumeRadius() {
        return fullVolumeRadius;
    }

    public double getZeroVolumeRadius() {
        return zeroVolumeRadius;
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.audiomanager.client.AudioTreatmentComponent";
    }

    private static class ComponentMessageReceiverImpl extends AbstractComponentMessageReceiver {
        private ManagedReference<AudioTreatmentComponentMO> compRef;

        private CellID cellID;

        public ComponentMessageReceiverImpl(ManagedReference<CellMO> cellRef,
                AudioTreatmentComponentMO comp) {

            super(cellRef.get());

	    cellID = cellRef.get().getCellID();

            compRef = AppContext.getDataManager().createReference(comp);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID,
                CellMessage message) {

            if (message instanceof AudioTreatmentMessage) {
                AudioTreatmentMessage msg = (AudioTreatmentMessage) message;
                logger.fine("Got AudioTreatmentMessage, startTreatment=" + msg.startTreatment());

            	String treatmentId = CallID.getCallID(cellID);

        	Treatment treatment = null;

		try {
		    treatment = AppContext.getManager(VoiceManager.class).getTreatment(treatmentId);
		} catch (IOException e) {
		}

		if (treatment == null) {
		    System.out.println("Can't find treatment " + treatmentId);
		    return;
		}

		if (msg.startTreatment()) {
		    treatment.pause(false);		    
		    System.out.println("resume treatment");
		} else {
		    treatment.restart(true);
		    System.out.println("pause treatment");
		}
                return;
            }

            logger.warning("Unknown message:  " + message);
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

    private void addProximityListener(boolean live) {
        if (live == false) {
	    return;
	}

        // Fetch the proximity component, we will need this below. If it does
        // not exist (it should), then log an error
        ProximityComponentMO component = cellRef.get().getComponent(ProximityComponentMO.class);

        if (component == null) {
            logger.warning("The AudioTreatment Component does not have a " +
                    "Proximity Component for Cell ID " + cellRef.get().getCellID());
            return;
        }

        // We are making this component live, add a listener to the proximity component.
	BoundingVolume[] bounds = new BoundingVolume[1];
        bounds[0] = new BoundingSphere((float) zeroVolumeRadius, new Vector3f());

        AudioTreatmentProximityListener proximityListener = 
	    new AudioTreatmentProximityListener(cellRef.get());

        component.addProximityListener(proximityListener, bounds);
    }

    /**
     * Asks the web server for the module's checksum information given the
     * unique name of the module and a particular asset type, returns null if
     * the module does not exist or upon some general I/O error.
     * 
     * @param serverURL The base web server URL
     * @param moduleName The unique name of a module
     * @param assetType The name of the asset type (art, audio, client, etc.)
     * @return The checksum information for a module
     */
    public static ChecksumList fetchAssetChecksums(String serverURL,
            String moduleName, String assetType) {

        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            String uriPart = moduleName + "/checksums/get/" + assetType;
            URL url = new URL(new URL(serverURL), ASSET_PREFIX + uriPart);
            logger.fine("fetchAssetChecksums:  " + url.toString());
            return ChecksumList.decode(new InputStreamReader(url.openStream()));
        } catch (java.lang.Exception e) {
            /* Log an error and return null */
            logger.warning("[MODULES] FETCH CHECKSUMS Failed " + e.getMessage());
            return null;
        }
    }
}
