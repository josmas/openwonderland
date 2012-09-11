/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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
package org.jdesktop.wonderland.modules.defaultenvironment.client;

import com.jme.light.DirectionalLight;
import com.jme.light.LightNode;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Arrow;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;
import java.awt.Color;
import java.awt.Font;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * A visual Entity that displays a directional light as 3D arrow.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author JagWire
 */
public class DirectionalLightViewerEntity extends Entity {

    // The Cell to display the bounds
    private Cell cell = null;

    // The root node of the bounds viewer scene graph
    private Node rootNode = null;

    // Listener for changes in the transform of the cell
    private TransformChangeListener updateListener = null;

    // True if the bounds viewer Entity is visible, false if not
    private boolean isVisible = false;

    
    private static final Logger LOGGER = Logger.getLogger(DirectionalLightViewerEntity.class.getName());
    /**
     * Constructor that takes the root Node of the Cell's scene graph
     */
    public DirectionalLightViewerEntity(Cell cell) {
        super("Light Viewer");

        // Create a new Node that serves as the root for the bounds viewer
        // scene graph
        this.cell = cell;
    }

    public void showLight(final LightNode lightNode, final String name) {
	if (rootNode != null) {
	    dispose();
	}
        
        DirectionalLight directionalLight = (DirectionalLight)lightNode.getLight();

        rootNode = new Node("Light Viewer Node");
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        RenderComponent rc = rm.createRenderComponent(rootNode);
        this.addComponent(RenderComponent.class, rc);
        
        // Set the Z-buffer state on the root node
        ZBufferState zbuf = (ZBufferState)rm.createRendererState(StateType.ZBuffer);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
        rootNode.setRenderState(zbuf);

        // Set the wireframe state on the root node
//        WireframeState wf = (WireframeState)rm.createRendererState(StateType.Wireframe);
//        wf.setEnabled(true);
//        rootNode.setRenderState(wf);
        MaterialState ms = (MaterialState)rm.createRendererState(StateType.Material);
        ms.setAmbient(new ColorRGBA(0.25f, 0, 0.5f, 0.40f));
        ms.setDiffuse(new ColorRGBA(0.25f, 0, 0.5f, 0.40f));
        ms.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);;
        //ms.setSpecular(new ColorRGBA(1f, 1, 1f, 1f));
        
        ms.setEnabled(true);
        rootNode.setRenderState(ms);
        
        BlendState bs = (BlendState)rm.createRendererState(StateType.Blend);
        bs.setEnabled(true);
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        bs.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        bs.setTestEnabled(true);
        bs.setTestFunction(BlendState.TestFunction.GreaterThan);
        rootNode.setRenderState(bs);

        CellTransform transform = cell.getWorldTransform();
        // Draw an arrow that mimics the light position --direction still under
        //construction.
        TextLabel2D label = new TextLabel2D(name,
                                            Color.black, Color.white, 1.0f, true, Font.getFont("SANS_SERIF"));
        
        Vector3f direction = directionalLight.getDirection();
        Vector3f position = lightNode.getLocalTranslation();
        Vector3f labelPosition = new Vector3f(position.x, position.y + 3, position.z);
        
        Arrow arrow = new Arrow("Arrow", 3, 0.5f);
        Node rotatedOnX = new Node("Rotated For Arrow");
        rotatedOnX.setLocalRotation(new Quaternion().fromAngleAxis(90*FastMath.DEG_TO_RAD, new Vector3f(1, 0, 0)));
        rotatedOnX.attachChild(arrow);
        rotatedOnX.setLocalTranslation(position);
//        arrow.setLocalTranslation(position);
        label.setLocalTranslation(labelPosition);
        arrow.lookAt(direction, new Vector3f(0,1,0));
        rootNode.attachChild(label);
        rootNode.attachChild(rotatedOnX);
        // Fetch the world translation for the root node of the cell and set
        // the translation for this entity root node
        rootNode.setLocalTranslation(transform.getTranslation(null));
	rootNode.setLocalRotation(transform.getRotation(null));
        // OWL issue #61: make sure to take scale into account
        rootNode.setLocalScale(transform.getScaling(null));
        
        // Listen for changes to the cell's translation and apply the same
        // update to the root node of the bounds viewer. We also re-set the size
        // of the bounds: this handles the case where the bounds of the
        // scene graph has changed and we need to update the bounds viewer
        // accordingly.
        updateListener = new TransformChangeListener() {
            public void transformChanged(final Cell cell, ChangeSource source) {
                // We need to perform this work inside a proper updater, to
                // make sure we are MT thread safe
                final WorldManager wm = ClientContextJME.getWorldManager();
                RenderUpdater u = new RenderUpdater() {
                    public void update(Object obj) {
                        CellTransform transform = cell.getWorldTransform();
                        rootNode.setLocalTranslation(transform.getTranslation(null));
			rootNode.setLocalRotation(transform.getRotation(null));
                        rootNode.setLocalScale(transform.getScaling(null));
                        wm.addToUpdateList(rootNode);
                    }
                };
                wm.addRenderUpdater(u, this);
            }
        };
        cell.addTransformChangeListener(updateListener);
	setVisible(true);
    }

    /**
     * Returns the root Node for the bounds viewer Entity.
     *
     * @param The Entity root Node
     */
    public Node getRootNode() {
        return rootNode;
    }

    /**
     * Sets whether the bounds viewer is visible (true) or invisible (false).
     *
     * @param visible True to make the affordance visible, false to not
     */
    public synchronized void setVisible(boolean visible) {
        WorldManager wm = ClientContextJME.getWorldManager();

        // If we want to make the affordance visible and it already is not
        // visible, then make it visible. We do not need to put add/remove
        // Entities on the MT Game render thread, they are already thread safe.
        if (visible == true && isVisible == false) {
            wm.addEntity(this);
            isVisible = true;
            return;
        }

        // If we want to make the affordance invisible and it already is
        // visible, then make it invisible
        if (visible == false && isVisible == true) {
            wm.removeEntity(this);
            isVisible = false;
            return;
        }
    }

    public void dispose() {
        // First, to make sure the affordance is no longer visible
        setVisible(false);

        // Clean up all of the listeners so this class gets properly garbage
        // collected.
        cell.removeTransformChangeListener(updateListener);
        updateListener = null;
	rootNode = null;
    }

}
