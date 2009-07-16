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

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Translation;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Rotation;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Scale;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * The ColladaCellSetup class is the cell that renders a collada model cell in
 * world.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="jme-collada-cell-component")
@ServerState
public class JmeColladaCellComponentServerState extends CellComponentServerState implements Serializable {
    
    /* The URI of the static model file */
    @XmlElement(name="model")
    public String model = null;

    @XmlElement(name="model-group")
    public String modelGroup = null;

    @XmlElement(name="model-author")
    public String modelAuthor = null;

    /* The translation for the geometry -- really should be done on the cell level */
    @XmlElement(name="model-translation")
    public PositionComponentServerState.Translation modelTranslation = null;

    @XmlElement(name="model-scale")
    public PositionComponentServerState.Scale modelScale = null;

    /* The rotation for the geometry -- really should be done on the cell level */
    @XmlElement(name="model-rotation")
    public PositionComponentServerState.Rotation modelRotation = null;

    @XmlElement(name="model-loader-classname")
    private String modelLoaderClassname = null;

    /** Default constructor */
    public JmeColladaCellComponentServerState() {
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

    @XmlTransient public Rotation getModelRotation() {
        return modelRotation;
    }

    public void setModelRotation(Rotation geometryRotation) {
        this.modelRotation = geometryRotation;
    }

    @XmlTransient public Translation getModelTranslation() {
        return modelTranslation;
    }

    public void setModelTranslation(Translation geometryTranslation) {
        this.modelTranslation = geometryTranslation;
    }
    
    @XmlTransient public Scale getModelScale() {
        return modelScale;
    }

    public void setModelScale(Scale geometryScale) {
        this.modelScale = geometryScale;
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

    /**
     * @return the modelAuthor
     */
    @XmlTransient public String getModelAuthor() {
        return modelAuthor;
    }

    /**
     * @param modelAuthor the modelAuthor to set
     */
    public void setModelAuthor(String modelAuthor) {
        this.modelAuthor = modelAuthor;
    }

    @Override
    @XmlTransient public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.jmecolladaloader.server.cell.JmeColladaCellComponentMO";
    }

    public CellComponentServerState clone(CellComponentServerState state) {
        JmeColladaCellComponentServerState ret = (JmeColladaCellComponentServerState) state;
        if (ret == null)
            ret = new JmeColladaCellComponentServerState();

        ret.model = this.model;
        ret.modelAuthor = this.modelAuthor;
        ret.modelGroup = this.modelGroup;
        ret.modelRotation = this.modelRotation;
        ret.modelScale = this.modelScale;
        ret.modelTranslation = this.modelTranslation;
        ret.setModelLoaderClassname(this.getModelLoaderClassname());

        return ret;
    }

    public CellComponentClientState setClientState(JmeColladaCellComponentClientState state) {
        state.setModelGroupURI(modelGroup);
        state.setModelRotation((modelRotation==null ? null : modelRotation.asQuaternion()));
        state.setModelScale((modelScale==null ? null : modelScale.asVector3f()));
        state.setModelTranslation((modelTranslation==null ? null : modelTranslation.asVector3f()));
        state.setModelURI(model);
        state.setModelLoaderClassname(getModelLoaderClassname());

        return state;
    }

    /**
     * @return the modelLoaderClassname
     */
    @XmlTransient public String getModelLoaderClassname() {
        return modelLoaderClassname;
    }

    /**
     * @param modelLoaderClassname the modelLoaderClassname to set
     */
    public void setModelLoaderClassname(String modelLoaderClassname) {
        this.modelLoaderClassname = modelLoaderClassname;
    }
}
