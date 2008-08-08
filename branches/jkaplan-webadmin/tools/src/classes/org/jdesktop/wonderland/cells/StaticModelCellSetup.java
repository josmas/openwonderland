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

package org.jdesktop.wonderland.cells;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The StaticModelCellSetup class is the cell component setup information that
 * describes model geomtetry to be loaded into the world. Cell setup classes
 * include this via the setCellSetupComponents() method on BasicCellSetup.
 * <p>
 * Within the XML file, this setup information appears as:
 * <static-model-component>
 *   <model>fubar</model>
 * </static-model-comoponent>
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="static-model")
public class StaticModelCellSetup extends BasicCellSetup implements Serializable {

    /* The URI of the static model file */
    @XmlElement(name="model")
    public String model = null;
    
    /** Default constructor */
    public StaticModelCellSetup() {
    }
    
    /**
     * Returns the model URI.
     * 
     * @return The model URI specification
     */
    @XmlTransient public String getModel() {
        return this.model;
    }
    
    /**
     * Sets the model URI. If null, then this property will not be written
     * out to the file.
     * 
     * @param model The model URI
     */
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.server.cell.StaticModelCellMO";
    }
}
