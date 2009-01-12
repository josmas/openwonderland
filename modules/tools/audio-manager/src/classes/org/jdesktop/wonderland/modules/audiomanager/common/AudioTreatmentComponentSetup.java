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
package org.jdesktop.wonderland.modules.audiomanager.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jdesktop.wonderland.common.cell.state.spi.CellServerStateSPI;

import java.io.Serializable;

/**
 * The component setup
 * @author jprovino
 */
@XmlRootElement(name="audio-treatment-component")
public class AudioTreatmentComponentSetup extends CellComponentServerState implements Serializable, CellServerStateSPI {

    @XmlElements({
	@XmlElement(name="treatment")
    })
    public String[] treatments = null;

    @XmlElement(name="groupId")
    public String groupId = null;

    @XmlElement(name="lowerLeftX")
    public double lowerLeftX;

    @XmlElement(name="lowerLeftY")
    public double lowerLeftY;

    @XmlElement(name="lowerLeftZ")
    public double lowerLeftZ;

    @XmlElement(name="upperRightX")
    public double upperRightX;

    @XmlElement(name="upperRightY")
    public double upperRightY;

    @XmlElement(name="upperRightZ")
    public double upperRightZ;

    public AudioTreatmentComponentSetup() {
    }

    @XmlTransient
    public String[] getTreatments() {
	return treatments;
    }

    public void setGroupId(String groupId) {
	this.groupId = groupId;
    }

    @XmlTransient
    public String getGroupId() {
	return groupId;
    }

    @XmlTransient
    public double getLowerLeftX() {
	return lowerLeftX;
    }

    @XmlTransient
    public double getLowerLeftY() {
	return lowerLeftY;
    }

    @XmlTransient
    public double getLowerLeftZ() {
	return lowerLeftZ;
    }

    @XmlTransient
    public double getUpperRightX() {
	return upperRightX;
    }

    @XmlTransient
    public double getUpperRightY() {
	return upperRightY;
    }

    @XmlTransient
    public double getUpperRightZ() {
	return upperRightZ;
    }

    public String getServerComponentClassName() {
	return "org.jdesktop.wonderland.modules.audiomanager.server.AudioTreatmentComponentMO";
    }

}
