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
package org.jdesktop.wonderland.client.jme.artimport;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.io.Serializable;
import org.jdesktop.mtgame.Entity;

public class ImportedModel implements Serializable {
    private String origModel;
    private String wonderlandName;
    private String texturePrefix;
    private Vector3f translation;
    private Vector3f orientation;
    private transient Node modelBG;
    private transient Node rootBG;
    private transient Entity entity;
    private transient ModelLoader modelLoader;

    public ImportedModel(String origModel,
                 String wonderlandName,
                 String texturePrefix,
                 Vector3f translation,
                 Vector3f orientation,
                 Node modelBG,
                 Node rootBG) {
        this.origModel = origModel;
        this.wonderlandName = wonderlandName;
        this.translation = translation;
        this.orientation = orientation;
        this.modelBG = modelBG;
        this.rootBG = rootBG;
        this.texturePrefix = texturePrefix;
    }

    public Node getModelBG() {
        return modelBG;
    }

    public void setModelBG(Node modelBG) {
        this.modelBG = modelBG;
    }

    public Node getRootBG() {
        return rootBG;
    }

    public void setRootBG(Node rootBG) {
        this.rootBG = rootBG;
    }

    public String getOrigModel() {
        return origModel;
    }

    public void setOrigModel(String origModel) {
        this.origModel = origModel;
    }

    public String getWonderlandName() {
        return wonderlandName;
    }

    public void setWonderlandName(String wonderlandName) {
        this.wonderlandName = wonderlandName;
    }

    public String getTexturePrefix() {
        return texturePrefix;
    }

    public void setTexturePrefix(String texturePrefix) {
        this.texturePrefix = texturePrefix;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    public Vector3f getOrientation() {
        return orientation;
    }

    public void setOrientation(Vector3f orientation) {
        this.orientation = orientation;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    /**
     * @return the modelLoader
     */
    public ModelLoader getModelLoader() {
        return modelLoader;
    }

    /**
     * @param modelLoader the modelLoader to set
     */
    public void setModelLoader(ModelLoader modelLoader) {
        this.modelLoader = modelLoader;
    }

}
