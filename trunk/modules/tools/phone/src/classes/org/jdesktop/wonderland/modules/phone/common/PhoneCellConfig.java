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
package org.jdesktop.wonderland.modules.phone.common;


import java.util.ArrayList;
import java.util.HashMap;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.common.cell.state.spi.CellServerStateSPI;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * The PhoneCellSetup class is the cell that renders a phone cell in
 * world.
 * 
 * @author jkaplan
 */
public class PhoneCellConfig extends CellClientState {

    private boolean locked;
    private boolean simulateCalls;
    private String phoneNumber;
    private String password;
    private String phoneLocation;
    private double zeroVolumeRadius;
    private double fullVolumeRadius;

    /** Default constructor */
    public PhoneCellConfig() {
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

    public void setPhoneNumber(String phoneNumber) {
	this.phoneNumber = phoneNumber;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public String getPassword() {
	return password;
    }

    public void setPhoneLocation(String phoneLocation) {
	this.phoneLocation = phoneLocation;
    }

    public String getPhoneLocation() {
	return phoneLocation;
    }

    public double getZeroVolumeRadius() {
	return zeroVolumeRadius;
    }

    public void setZeroVolumeRadius(double zeroVolumeRadius) {
	this.zeroVolumeRadius = zeroVolumeRadius;
    }

    public double getFullVolumeRadius() {
        return fullVolumeRadius;
    }

    public void setFullVolumeRadius(double fullVolumeRadius) {
        this.fullVolumeRadius = fullVolumeRadius;
    }

}
