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

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * The component client state
 * @author jprovino
 */
public class AudioTreatmentComponentClientState extends AudioParticipantComponentClientState {

    public String[] treatments;

    public String groupId = null;

    public double fullVolumeRadius = 2;

    public double zeroVolumeRadius = 10;

    public boolean useFullVolumeSpatializer;

    public double volume = 1;

    public boolean startImmediately = true;

    public AudioTreatmentComponentClientState() {
	super(false, false);
    }

    @XmlElement
    public String[] getTreatments() {
	return treatments;
    }

    @XmlElement
    public String getGroupId() {
	return groupId;
    }

    @XmlElement
    public double getFullVolumeRadius() {
	return fullVolumeRadius;
    }

    @XmlElement
    public double getZeroVolumeRadius() {
	return zeroVolumeRadius;
    }

    @XmlElement
    public boolean getUseFullVolumeSpatializer() {
	return useFullVolumeSpatializer;
    }

    @XmlElement
    public double getVolume() {
	return volume;
    }
   
    @XmlElement
    public boolean getStartImmediately() {
	return startImmediately;
    }

}
