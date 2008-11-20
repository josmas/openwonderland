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

import org.jdesktop.wonderland.common.cell.setup.CellComponentSetup;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jdesktop.wonderland.common.cell.setup.spi.CellSetupSPI;

import java.io.Serializable;

/**
 * The component setup
 * @author jprovino
 */
@XmlRootElement(name="audio-treatment-component")
public class AudioTreatmentComponentSetup extends CellComponentSetup implements Serializable, CellSetupSPI {

    @XmlElement(name="treatment")
    public String treatment = null;

    @XmlElement(name="groupId")
    public String groupId = null;

    public AudioTreatmentComponentSetup() {
	System.out.println("AudioTreatmentComponentSetup");
    }

    public void setTreatment(String treatment) {
	this.treatment = treatment;
	System.out.println("Treatment:  " + treatment);
    }

    @XmlTransient
    public String getTreatment() {
	return treatment;
    }

    public void setGroupId(String groupId) {
	this.groupId = groupId;
    }

    @XmlTransient
    public String getGroupId() {
	return groupId;
    }

    public String getServerComponentClassName() {
	return "org.jdesktop.wonderland.modules.audiomanager.server.AudioTreatmentComponentMO";
    }

}
