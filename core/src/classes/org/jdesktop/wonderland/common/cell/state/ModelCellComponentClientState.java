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
package org.jdesktop.wonderland.common.cell.state;

/**
 * A cell component that encapsulates all the information required to load a
 * collada model.
 */
public class ModelCellComponentClientState extends CellComponentClientState {
    /* The unique URL that describes the model data */
    private String deployedModelURL;
    private boolean pickable=true;
    private boolean collidable=true;
    private boolean lightingEnabled=true;
    private boolean backfaceCullingEnabled = true;
    
    /** Default constructor */
    public ModelCellComponentClientState() {
    }
    
    /**
     * Returns the unique model URI, null if none.
     *
     * @return The unique model URI
     */
    public String getDeployedModelURL() {
        return deployedModelURL;
    }

    /**
     * Sets the unique model URI, null for none.
     *
     * @param modelURI The unique model URI
     */
    public void setDeployedModelURL(String deployedModelURL) {
        this.deployedModelURL = deployedModelURL;
    }

    /**
     * @return the pickable
     */
    public boolean isPickingEnabled() {
        return pickable;
    }

    /**
     * @param pickable the pickable to set
     */
    public void setPickingEnabled(boolean pickable) {
        this.pickable = pickable;
    }

    /**
     * @return the collidable
     */
    public boolean isCollisionEnabled() {
        return collidable;
    }

    /**
     * @param collidable the collidable to set
     */
    public void setCollisionEnabled(boolean collidable) {
        this.collidable = collidable;
    }

    /**
     * @return the lightingEnabled
     */
    public boolean isLightingEnabled() {
        return lightingEnabled;
    }

    /**
     * @param lightingEnabled the lightingEnabled to set
     */
    public void setLightingEnabled(boolean lightingEnabled) {
        this.lightingEnabled = lightingEnabled;
    }

    public void setBackfaceCullingEnabled(boolean backfaceCullingEnabled) {
        this.backfaceCullingEnabled = backfaceCullingEnabled;
    }

    public boolean isBackfaceCullingEnabled() {
        return backfaceCullingEnabled;
    }
}
