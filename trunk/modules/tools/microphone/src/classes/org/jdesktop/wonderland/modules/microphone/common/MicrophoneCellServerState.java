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
package org.jdesktop.wonderland.modules.microphone.common;


import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * The MicrophoneCellServerState class is the cell that renders a microphone cell in
 * world.
 * 
 * @author jprovino
 */
@XmlRootElement(name="microphone-cell")
@ServerState
public class MicrophoneCellServerState extends CellServerState
        implements Serializable {

    @XmlElement(name="name")
    private String name;

    @XmlElement(name="fullVolumeRadius")
    private double fullVolumeRadius;

    @XmlElement(name="activeRadius")
    private double activeRadius;
    
    @XmlElement(name="activeRadiusType")
    private String activeRadiusType;

    /** Default constructor */
    public MicrophoneCellServerState() {
    }
    
    public MicrophoneCellServerState(String name, double fullVolumeRadius,
	    double activeRadius, String activeRadiusType) {

	this.name = name;
	this.fullVolumeRadius = fullVolumeRadius;
	this.activeRadius = activeRadius;
	this.activeRadiusType = activeRadiusType;
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.microphone.server.cell.MicrophoneCellMO";
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public String getName() {
        return name;
    }

    public void setFullVolumeRadius(double fullVolumeRadius) {
        this.fullVolumeRadius = fullVolumeRadius;
    }

    @XmlTransient
    public double getFullVolumeRadius() {
        return fullVolumeRadius;
    }

    public void setActiveRadius(double activeRadius) {
	this.activeRadius = activeRadius;
    }

    @XmlTransient
    public double getActiveRadius() {
	return activeRadius;
    }

    public void setActiveRadiusType(String activeRadiusType) {
	this.activeRadiusType = activeRadiusType;
    }

    @XmlTransient
    public String getActiveRadiusType() {
	return activeRadiusType;
    }

}
