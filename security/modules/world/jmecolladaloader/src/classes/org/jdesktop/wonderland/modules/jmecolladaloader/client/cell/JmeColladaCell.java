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
package org.jdesktop.wonderland.modules.jmecolladaloader.client.cell;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.jme.cellrenderer.JmeColladaRenderer;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellClientState;

/**
 * Client side cell for rendering JME content
 * 
 * @author paulby
 */
public class JmeColladaCell extends Cell {
    
    /* The URI of the model asset */
    private String modelURI = null;
    private String modelGroupURI = null;
    private Vector3f geometryTranslation;
    private Quaternion geometryRotation;
    private Vector3f geometryScale;
    
    public JmeColladaCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    /**
     * Called when the cell is initially created and any time there is a 
     * major configuration change. The cell will already be attached to it's parent
     * before the initial call of this method
     * 
     * @param config the cell config object
     */
    @Override
    public void setClientState(CellClientState config) {
        super.setClientState(config);
        JmeColladaCellClientState colladaConfig = (org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellClientState)config;
        this.modelURI = colladaConfig.getModelURI();
        this.modelGroupURI = colladaConfig.getModelGroupURI();
        this.geometryRotation = colladaConfig.getGeometryRotation();
        this.geometryTranslation = colladaConfig.getGeometryTranslation();
        this.geometryScale = colladaConfig.getGeometryScale();
        logger.info("[CELL] JME COLLADA CELL " + this.modelURI);
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch(rendererType) {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
                ret= new JmeColladaRenderer(this);
                break;                
        }
        
        return ret;
    }
    
    /**
     * Returns the URI of the model asset.
     * 
     * TODO shouldn't this be a URL instead of a String ?
     * 
     * @return The asset URI
     */
    public String getModelURI() {
        return this.modelURI;
    }

    public String getModelGroupURI() {
        return modelGroupURI;
    }
    
    public Vector3f getGeometryTranslation() {
        return geometryTranslation;
    }

    public Quaternion getGeometryRotation() {
        return geometryRotation;
    }

    public Vector3f getGeometryScale() {
        return geometryScale;
    }
}
