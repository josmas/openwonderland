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

package org.jdesktop.wonderland.modules.coneofsilence.common;

import java.util.ArrayList;
import java.util.HashMap;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.cell.setup.spi.CellSetupSPI;

/**
 * The ConeOfSilenceCellSetup class is the cell that renders a coneofsilence cell in
 * world.
 * 
 * @author jprovino
 */
@XmlRootElement(name="ConeOfSilence-cell")
public class ConeOfSilenceCellSetup extends BasicCellSetup 
        implements Serializable, CellSetupSPI {

    @XmlElement(name="name")
    private String name;

    @XmlElement(name="fullVolumeRadius")
    private double fullVolumeRadius;

    /** Default constructor */
    public ConeOfSilenceCellSetup() {
    }
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.coneofsilence.server.cell.ConeOfSilenceCellMO";
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

}
