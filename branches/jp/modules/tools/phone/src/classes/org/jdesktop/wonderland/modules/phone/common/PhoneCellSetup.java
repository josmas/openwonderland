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

package org.jdesktop.wonderland.modules.phone.common;

import java.util.ArrayList;
import java.util.HashMap;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.cell.setup.spi.CellSetupSPI;

/**
 * The PhoneCellSetup class is the cell that renders a phone cell in
 * world.
 * 
 * @author jkaplan
 */
@XmlRootElement(name="phone-cell")
public class PhoneCellSetup extends BasicCellSetup 
        implements Serializable, CellSetupSPI {

    @XmlElement(name="locked")
    private boolean locked;

    @XmlElement(name="simulateCalls")
    private boolean simulateCalls;

    @XmlElement(name="phoneNumber")
    private String phoneNumber;

    @XmlElement(name="password")
    private String password;

    @XmlElement(name="phoneLocation")
    private String phoneLocation;

    @XmlElement(name="zeroVolumeRadius")
    private double zeroVolumeRadius;

    @XmlElement(name="fullVolumeRadius")
    private double fullVolumeRadius;

    /** Default constructor */
    public PhoneCellSetup() {
	System.out.println("BAR PHONECELLSETUP");
    }
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.phone.server.cell.PhoneCellMO";
    }

    @XmlTransient
    public boolean getLocked() {
  	return locked;
    }

    public void setLocked(boolean locked) {
	this.locked = locked;
    }

    @XmlTransient
    public boolean getSimulateCalls() {
 	return simulateCalls;
    }

    public void setSimulateCalls(boolean simulateCalls) {
	this.simulateCalls = simulateCalls;
    }

    @XmlTransient
    public String getPhoneNumber() {
	return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
	this.phoneNumber = phoneNumber;
    }

    @XmlTransient
    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    @XmlTransient
    public String getPhoneLocation() {
	return phoneLocation;
    }

    public void setPhoneLocation(String phoneLocation) {
	this.phoneLocation = phoneLocation;
    }

    @XmlTransient
    public double getZeroVolumeRadius() {
	return zeroVolumeRadius;
    }

    public void setZeroVolumeRadius(double zeroVolumeRadius) {
	this.zeroVolumeRadius = zeroVolumeRadius;
    }

    @XmlTransient
    public double getFullVolumeRadius() {
        return fullVolumeRadius;
    }

    public void setFullVolumeRadius(double fullVolumeRadius) {
        this.fullVolumeRadius = fullVolumeRadius;
    }

}
