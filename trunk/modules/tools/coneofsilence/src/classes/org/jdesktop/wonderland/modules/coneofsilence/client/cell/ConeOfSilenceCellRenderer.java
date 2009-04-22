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
package org.jdesktop.wonderland.modules.coneofsilence.client.cell;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Cone;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;

import com.jme.scene.state.RenderState;
import com.jme.scene.shape.Cylinder;
import com.jme.renderer.ColorRGBA;
import org.jdesktop.mtgame.RenderManager;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;

/**
 * @author jkaplan
 */
public class ConeOfSilenceCellRenderer extends BasicRenderer {
    
    public ConeOfSilenceCellRenderer(Cell cell) {
        super(cell);
    }
    
    protected Node createSceneGraph(Entity entity) {

        /* Fetch the basic info about the cell */
        String name = cell.getCellID().toString();

        /* Create the scene graph object and set its wireframe state */
        float radius = ((BoundingSphere)cell.getLocalBounds()).getRadius();
        Cone cone = new Cone(name, 30, 30, radius, (float) 1.0 * radius);
        Node node = new Node();
        node.attachChild(cone);

	float height = 2.3f;

	Node cylinder = createCylinderNode("Cylinder", radius, height);
	cylinder.setLocalTranslation(new Vector3f(0.0f, -height, 0.0f));
	node.attachChild(cylinder);

        // Raise the cone off of the floor, and rotate it about the +x axis 90
        // degrees so it faces the proper way
        Vector3f translation = new Vector3f(0.0f, height, 0.0f);
        Vector3f axis = new Vector3f(1.0f, 0.0f, 0.0f);
        float angle = (float)Math.toRadians(90);
        Quaternion rotation = new Quaternion().fromAngleAxis(angle, axis);
        node.setLocalTranslation(translation);
        node.setLocalRotation(rotation);
        node.setModelBound(new BoundingSphere());
        node.updateModelBound();

        WireframeState wiState = (WireframeState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
        wiState.setEnabled(true);
        node.setRenderState(wiState);
        node.setName("Cell_"+cell.getCellID()+":"+cell.getName());

        return node;
    }

    private Node createCylinderNode(String name, float radius, float height) {
        // Create the new node and Cylinder primitive
        Node cylinderNode = new Node();
        Cylinder cylinder = new Cylinder(name, 30, 30, radius, height);
        cylinderNode.attachChild(cylinder);

        // Set the color to black and the transparency
        cylinder.setSolidColor(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.5f));
        //cylinderNode.setRenderState(zbuf);
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        MaterialState matState = (MaterialState) rm.createRendererState(RenderState.RS_MATERIAL);
        cylinderNode.setRenderState(matState);
        matState.setDiffuse(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.5f));
        matState.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.5f));
//        matState.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.5f));
        matState.setShininess(128.0f);
        matState.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.5f));
        matState.setEnabled(true);

        BlendState alphaState = (BlendState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_BLEND);
        alphaState.setBlendEnabled(true);
        alphaState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alphaState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alphaState.setTestEnabled(true);
        alphaState.setTestFunction(BlendState.TestFunction.GreaterThan);
        alphaState.setEnabled(true);
        cylinder.setRenderState(alphaState);

        // Remove the back faces of the object so transparency works properly
        CullState cullState = (CullState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_CULL);
        cullState.setCullFace(CullState.Face.Back);
        cylinderNode.setRenderState(cullState);

        // Set the bound so this node can be pickable
        //cylinder.setModelBound(new BoundingSphere());
        //cylinder.updateModelBound();
        return cylinderNode;
    }

}
