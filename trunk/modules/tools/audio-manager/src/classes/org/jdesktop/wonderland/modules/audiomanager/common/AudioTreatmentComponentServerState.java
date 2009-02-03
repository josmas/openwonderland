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
package org.jdesktop.wonderland.modules.audiomanager.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

/**
 * The component server state
 * @author jprovino
 */
@XmlRootElement(name="audio-treatment-component")
@ServerState
public class AudioTreatmentComponentServerState extends CellComponentServerState {

    @XmlElements({
	@XmlElement(name="treatment")
    })
    public String[] treatments = null;

    @XmlElement(name="groupId")
    public String groupId = null;

    @XmlElement(name="fullVolumeRadius")
    public double fullVolumeRadius;

    @XmlElement(name="zeroVolumeRadius")
    public double zeroVolumeRadius;

    @XmlElement(name="spatialize")
    public boolean spatialize;

    public AudioTreatmentComponentServerState() {
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

    public void setFullVolumeRadius(double fullVolumeRadius) {
	this.fullVolumeRadius = fullVolumeRadius;
    }

    @XmlTransient
    public double getFullVolumeRadius() {
	return fullVolumeRadius;
    }

    public void setZeroVolumeRadius(double zeroVolumeRadius) {
	this.zeroVolumeRadius = zeroVolumeRadius;
    }

    @XmlTransient
    public double getZeroVolumeRadius() {
	return zeroVolumeRadius;
    }

    public void setSpatialize(boolean spatialize) {
	this.spatialize = spatialize;
    }

    @XmlTransient
    public boolean getSpatialize() {
	return spatialize;
    }

    public String getServerComponentClassName() {
	return "org.jdesktop.wonderland.modules.audiomanager.server.AudioTreatmentComponentMO";
    }

}
