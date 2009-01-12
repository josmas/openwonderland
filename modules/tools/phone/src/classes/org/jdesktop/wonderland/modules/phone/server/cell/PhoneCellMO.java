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
package org.jdesktop.wonderland.modules.phone.server.cell;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.AudioGroupSetup;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.mpk20.voicelib.app.ZeroVolumeSpatializer;

import com.sun.sgs.app.ManagedReference;

import com.sun.voip.CallParticipant;
import com.sun.voip.client.connector.CallStatus;

import java.lang.String;
import java.util.logging.Logger;



import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.modules.phone.common.PhoneCellSetup;
import org.jdesktop.wonderland.modules.phone.common.PhoneCellConfig;

import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;

import com.jme.bounding.BoundingBox;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.server.cell.ChannelComponentImplMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell that provides conference phone functionality
 * @author jprovino
 */
public class PhoneCellMO extends CellMO {

    private static final Logger logger =
            Logger.getLogger(PhoneCellMO.class.getName());
    private String modelFileName;
    private final static double PRIVATE_DAMPING_COEFFICIENT = 0.5;
    private boolean locked;
    private boolean simulateCalls;
    private String phoneNumber;
    private String password;
    private String phoneLocation;
    private double zeroVolumeRadius;
    private double fullVolumeRadius;
    private boolean keepUnlocked = true;
    private int callNumber = 0;

    public PhoneCellMO() {
        addComponent(new ChannelComponentImplMO(this), ChannelComponentMO.class);

        new PhoneMessageHandler(this);
    }

    public PhoneCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size),
                new CellTransform(null, center));

        addComponent(new ChannelComponentImplMO(this), ChannelComponentMO.class);

        new PhoneMessageHandler(this);
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        return "org.jdesktop.wonderland.modules.phone.client.cell.PhoneCell";
    }

    @Override
    public CellClientState getCellClientState(CellClientState cellClientState, WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        if (cellClientState == null) {
          cellClientState = new PhoneCellConfig();
        }

        ((PhoneCellConfig)cellClientState).setLocked(locked);
        ((PhoneCellConfig)cellClientState).setSimulateCalls(simulateCalls);
        ((PhoneCellConfig)cellClientState).setPhoneNumber(phoneNumber);
        ((PhoneCellConfig)cellClientState).setPassword(password);
        ((PhoneCellConfig)cellClientState).setPhoneLocation(phoneLocation);
        ((PhoneCellConfig)cellClientState).setZeroVolumeRadius(zeroVolumeRadius);
        ((PhoneCellConfig)cellClientState).setFullVolumeRadius(fullVolumeRadius);

        cellClientState.addClientComponentClasses(new String[]{
                    "org.jdesktop.wonderland.client.cell.ChannelComponent"
                });

        return super.getCellClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setCellServerState(CellServerState setup) {
        super.setCellServerState(setup);

        PhoneCellSetup pcs = (PhoneCellSetup) setup;

        locked = pcs.getLocked();
        simulateCalls = pcs.getSimulateCalls();
        phoneNumber = pcs.getPhoneNumber();
        password = pcs.getPassword();
        phoneLocation = pcs.getPhoneLocation();
        zeroVolumeRadius = pcs.getZeroVolumeRadius();
    }

    /**
     * Return a new CellServerState Java bean class that represents the current
     * state of the cell.
     *
     * @return a JavaBean representing the current state
     */
    @Override
    public CellServerState getCellServerState(CellServerState cellServerState) {
        /* Create a new BasicCellState and populate its members */
        if (cellServerState == null) {
            cellServerState= new PhoneCellSetup();
        }
        return super.getCellServerState(cellServerState);
    }

    public boolean getLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean getSimulateCalls() {
        return simulateCalls;
    }

    public void setSimulateCalls(boolean simulateCalls) {
        this.simulateCalls = simulateCalls;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneLocation() {
        return phoneLocation;
    }

    public double getZeroVolumeRadius() {
        return zeroVolumeRadius;
    }

    public double getFullVolumeRadius() {
        return fullVolumeRadius;
    }

    public boolean getKeepUnlocked() {
        return keepUnlocked;
    }

    public void setKeepUnlocked(boolean keepUnlocked) {
        this.keepUnlocked = keepUnlocked;
    }

    protected void addParentCell(ManagedReference parent) {
        IncomingCallHandler incomingCallHandler = IncomingCallHandler.getInstance();

    //vector3f translation = new Vector3f();
    //getOriginWorld().get(translation);

    //incomingCallHandler.addPhone(getCellID(), translation, phoneNumber,
    //    phoneLocation, zeroVolumeRadius, fullVolumeRadius);
    }
}
