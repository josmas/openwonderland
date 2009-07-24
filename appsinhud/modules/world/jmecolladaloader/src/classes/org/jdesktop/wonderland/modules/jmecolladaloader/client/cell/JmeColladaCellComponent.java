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

import com.jme.scene.Node;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ModelCellComponent;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
import org.jdesktop.wonderland.client.jme.cellrenderer.ModelRenderer;
import org.jdesktop.wonderland.common.cell.ComponentLookupClass;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellComponentClientState;

/**
 *
 * @author paulby
 */
@ComponentLookupClass(ModelCellComponent.class)
public class JmeColladaCellComponent extends ModelCellComponent {

    private DeployedModel deployedModel=null;

    public JmeColladaCellComponent(Cell cell) {
        super(cell);
    }

    @Override
    public Node loadModel() {
        Node ret = new Node();
        ModelLoader loader = deployedModel.getModelLoader();
        Node model = loader.loadDeployedModel(deployedModel);
        if (model != null) {
            deployedModel.applyModelTransform(ret);
            model.setName(deployedModel.getDeployedURL());
            ret.attachChild(model);
        }
        return ret;
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);

        JmeColladaCellComponentClientState jmeState = (JmeColladaCellComponentClientState) clientState;

        deployedModel = getDeployedModel(jmeState);
    }

    private DeployedModel getDeployedModel(JmeColladaCellComponentClientState state) {
        DeployedModel ret = new DeployedModel(state.getModelLoaderClassname());
        ret.setDeployedURL(state.getModelURI());
        ret.setModelRotation(state.getModelRotation());
        ret.setModelScale(state.getModelScale());
        ret.setModelTranslation(state.getModelTranslation());

        return ret;
    }

    @Override
    public CellRenderer getCellRenderer(RendererType type, Cell cell) {
        if (type.equals(RendererType.RENDERER_JME))
            return new ModelRenderer(cell, this);
        return null;
    }

}
