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

import com.sun.sgs.app.ManagedReference;

import java.util.logging.Logger;

import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.server.cell.CellMO;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;

import org.jdesktop.wonderland.server.comms.WonderlandClientID;

import org.jdesktop.wonderland.modules.phone.common.PhoneCellClientState;
import org.jdesktop.wonderland.modules.phone.common.PhoneCellServerState;

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
    }

    public PhoneCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size),
	    new CellTransform(null, center));
    }

    public void setLive(boolean live) {
	System.out.println("Phone set live! " + live);

	if (live == false) {
	    return;
	}

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
	    cellClientState = new PhoneCellClientState();
        }

        ((PhoneCellClientState)cellClientState).setLocked(locked);
        ((PhoneCellClientState)cellClientState).setSimulateCalls(simulateCalls);
        ((PhoneCellClientState)cellClientState).setPhoneNumber(phoneNumber);
        ((PhoneCellClientState)cellClientState).setPassword(password);
        ((PhoneCellClientState)cellClientState).setPhoneLocation(phoneLocation);
        ((PhoneCellClientState)cellClientState).setZeroVolumeRadius(zeroVolumeRadius);
        ((PhoneCellClientState)cellClientState).setFullVolumeRadius(fullVolumeRadius);

        return super.getCellClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setCellServerState(CellServerState cellServerState) {
        super.setCellServerState(cellServerState);

        PhoneCellServerState phoneCellServerState = (PhoneCellServerState) cellServerState;

        locked = phoneCellServerState.getLocked();
        simulateCalls = phoneCellServerState.getSimulateCalls();
        phoneNumber = phoneCellServerState.getPhoneNumber();
        password = phoneCellServerState.getPassword();
        phoneLocation = phoneCellServerState.getPhoneLocation();
        zeroVolumeRadius = phoneCellServerState.getZeroVolumeRadius();
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
            cellServerState= new PhoneCellServerState();
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
