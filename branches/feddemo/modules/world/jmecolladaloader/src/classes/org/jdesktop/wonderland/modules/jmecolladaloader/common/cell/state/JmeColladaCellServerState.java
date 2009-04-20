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
package org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state;

import org.jdesktop.wonderland.common.cell.state.CellServerState;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Rotation;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Scale;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * The ColladaCellSetup class is the cell that renders a collada model cell in
 * world.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="jme-collada-cell")
@ServerState
public class JmeColladaCellServerState extends CellServerState implements Serializable {
    
    /* The URI of the static model file */
    @XmlElement(name="model")
    public String model = null;

    @XmlElement(name="model-group")
    public String modelGroup = null;

    /* The translation for the geometry -- really should be done on the cell level */
    @XmlElement(name="geometry-translation")
    public PositionComponentServerState.Origin geometryTranslation = null;

    @XmlElement(name="geometry-scale")
    public PositionComponentServerState.Scale geometryScale = null;

    /* The rotation for the geometry -- really should be done on the cell level */
    @XmlElement(name="geometry-rotation")
    public PositionComponentServerState.Rotation geometryRotation = null;

    /** Default constructor */
    public JmeColladaCellServerState() {
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

    @XmlTransient public Rotation getGeometryRotation() {
        return geometryRotation;
    }

    public void setGeometryRotation(Rotation geometryRotation) {
        this.geometryRotation = geometryRotation;
    }

    @XmlTransient public Origin getGeometryTranslation() {
        return geometryTranslation;
    }

    public void setGeometryTranslation(Origin geometryTranslation) {
        this.geometryTranslation = geometryTranslation;
    }
    
    @XmlTransient public Scale getGeometryScale() {
        return geometryScale;
    }

    public void setGeometryScale(Scale geometryScale) {
        this.geometryScale = geometryScale;
    }

    @Override
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

    /**
     * @return the modelGroup
     */
    @XmlTransient public String getModelGroup() {
        return modelGroup;
    }

    /**
     * @param modelGroup the modelGroup to set
     */
    public void setModelGroup(String modelGroup) {
        this.modelGroup = modelGroup;
    }

}
