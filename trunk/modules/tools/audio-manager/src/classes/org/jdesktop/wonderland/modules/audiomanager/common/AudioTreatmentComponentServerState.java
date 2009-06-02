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
//public class AudioTreatmentComponentServerState extends CellComponentServerState {
public class AudioTreatmentComponentServerState extends AudioParticipantComponentServerState {

    @XmlElements({
	@XmlElement(name="treatment")
    })
    public String[] treatments = new String[0];

    @XmlElement(name="groupId")
    public String groupId = null;

    @XmlElement(name="fullVolumeRadius")
    public double fullVolumeRadius = 2;

    @XmlElement(name="zeroVolumeRadius")
    public double zeroVolumeRadius = 10;

    @XmlElement(name="useFullVolumeSpatializer")
    public boolean useFullVolumeSpatializer;

    @XmlElement(name="volume")
    public double volume = 1;

    @XmlElement(name="startImmediately")
    public boolean startImmediately = true;

    @XmlElement(name="restartWhenFirstInRange")
    public boolean restartWhenFirstInRange = false;

    public AudioTreatmentComponentServerState() {
	super(false, false);
    }

    public void setTreatments(String[] treatments) {
	this.treatments = treatments;
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

    public void setUseFullVolumeSpatializer(boolean userFullVolumeSpatializer) {
	this.useFullVolumeSpatializer = userFullVolumeSpatializer;
    }

    @XmlTransient
    public boolean getUseFullVolumeSpatializer() {
	return useFullVolumeSpatializer;
    }

    public void setVolume(double Volume) {
	this.volume = volume;
    }

    @XmlTransient
    public double getVolume() {
	return volume;
    }
   
    public void setStartImmediately(boolean startImmediately) {
	this.startImmediately = startImmediately;
    }

    @XmlTransient
    public boolean getStartImmediately() {
	return startImmediately;
    }

    public void setRestartWhenFirstInRange(boolean restartWhenFirstInRange) {
	this.restartWhenFirstInRange = restartWhenFirstInRange;
    }

    @XmlTransient
    public boolean getRestartWhenFirstInRange() {
	return restartWhenFirstInRange;
    }

    public String getServerComponentClassName() {
	return "org.jdesktop.wonderland.modules.audiomanager.server.AudioTreatmentComponentMO";
    }

}
