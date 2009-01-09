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

package org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.setup;

import org.jdesktop.wonderland.common.cell.setup.*;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.setup.spi.CellSetupSPI;

/**
 * The ColladaCellSetup class is the cell that renders a collada model cell in
 * world.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="jme-collada-cell")
public class JMEColladaCellSetup extends BasicCellSetup implements Serializable, CellSetupSPI {
    
    /* The URI of the static model file */
    @XmlElement(name="model")
    public String model = null;
    
    /** Default constructor */
    public JMEColladaCellSetup() {
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
        return "org.jdesktop.wonderland.modules.jmecolladaloader.server.cell.JmeColladaCellMO";
    }
    
    /**
     * Returns a string representation of this class
     *
     * @return The setup information as a string
     */
    @Override
    public String toString() {
        return super.toString() + " [ColladCellSetup] model: " + this.model;
    }

}
