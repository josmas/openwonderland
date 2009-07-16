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
package org.jdesktop.wonderland.client.jme.artimport;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.net.URL;
import java.util.Map;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

/**
 *
 * @author paulby
 */
public class DeployedModel extends Model {

    private String deployedURL = null;

    private Vector3f modelBGScale = null;
    private Vector3f modelBGTranslation = null;
    private Quaternion modelBGRotation = null;

    private String modelLoaderClassname;
    private CellServerState cellServerState;

    private ModelLoader modelLoader;

    private Object loaderData=null;

    public DeployedModel(URL originalFile, ModelLoader modelLoader) {
        super(originalFile);
        this.modelLoaderClassname = modelLoader.getClass().getName();
    }

    public DeployedModel(String modelLoaderClassname) {
        super(null);
        this.modelLoaderClassname = modelLoaderClassname;
    }

    /**
     * @return the deployedURL
     */
    public String getDeployedURL() {
        return deployedURL;
    }

    /**
     * @param deployedURL the deployedURL to set
     */
    public void setDeployedURL(String deployedURL) {
        this.deployedURL = deployedURL;
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();

        ret.append(super.toString()+"\n");
        ret.append("Deployed to "+deployedURL+"\n");

        return ret.toString();
    }

    /**
     * Record the local transform of the modelBG
     * @param modelBG
     */
    public void recordModelBGTransform(Node modelBG) {
        setModelScale(modelBG.getLocalScale());
        setModelTranslation(modelBG.getLocalTranslation());
        setModelRotation(modelBG.getLocalRotation());
    }

    /**
     * Return the classname of the model loader used to load this model.
     * @return
     */
    public String getModelLoaderClassname() {
        return modelLoaderClassname;
    }

    /**
     * Apply the model transform the modelBG node
     */
    public void applyModelTransform(Node modelBG) {
        if (getModelScale()!=null)
            modelBG.setLocalScale(getModelScale());
        if (getModelTranslation()!=null)
            modelBG.setLocalTranslation(getModelTranslation());
        if (getModelRotation()!=null)
            modelBG.setLocalRotation(getModelRotation());
    }

    public void addCellServerState(CellServerState cellServerState) {
        this.cellServerState = cellServerState;
    }

    public CellServerState getCellServerState() {
        return cellServerState;
    }

    public ModelLoader getModelLoader() {
        if (modelLoader==null) {
           modelLoader = LoaderManager.getLoaderManager().getLoader(this);
        }

        return modelLoader;
    }

    /**
     * @return the modelBGScale
     */
    public Vector3f getModelScale() {
        return modelBGScale;
    }

    /**
     * @param modelBGScale the modelBGScale to set
     */
    public void setModelScale(Vector3f modelBGScale) {
        this.modelBGScale = modelBGScale;
    }

    /**
     * @return the modelBGTranslation
     */
    public Vector3f getModelTranslation() {
        return modelBGTranslation;
    }

    /**
     * @param modelBGTranslation the modelBGTranslation to set
     */
    public void setModelTranslation(Vector3f modelBGTranslation) {
        this.modelBGTranslation = modelBGTranslation;
    }

    /**
     * @return the modelBGRotation
     */
    public Quaternion getModelRotation() {
        return modelBGRotation;
    }

    /**
     * @param modelBGRotation the modelBGRotation to set
     */
    public void setModelRotation(Quaternion modelBGRotation) {
        this.modelBGRotation = modelBGRotation;
    }

    /**
     * @return the loaderDeploymentData
     */
    public Object getLoaderData() {
        return loaderData;
    }

    /**
     * @param loaderDeploymentData the loaderDeploymentData to set
     */
    public void setLoaderData(Object loaderDeploymentData) {
        this.loaderData = loaderDeploymentData;
    }
}
