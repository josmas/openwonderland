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
package org.jdesktop.wonderland.modules.appbase.client.gui.guidefault;

import com.jme.light.PointLight;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.appbase.client.AppCell;
import org.jdesktop.wonderland.modules.appbase.client.AppCellRenderer;
import org.jdesktop.wonderland.modules.appbase.client.WindowView;

/**
 * A cell renderer which uses JME to render app cell contents. It creates
 * a rootNode which it hands off to mtgame. It allows window views to be
 * attached. When a window view is attached the base node of the window is 
 * added as a child of this rootNode.
 *
 * @author dj
 */
public class AppCellRendererJME extends AppCellRenderer {

    /** The root node of the cell. */
    protected Node rootNode;

    /** The Z buffer state. */
    protected ZBufferState zBufferState;
    
    /** The light state. */
    protected LightState lightState;
	    
    /** 
     * Create a new instance of AppCellRendererJME.
     * @param cell The cell to be rendered.
     */
    public AppCellRendererJME (AppCell cell) {
        super(cell);

	rootNode = new Node();
        rootNode.setName("Root node for cell " + cell.getCellID().toString());

	initZBufferState();
	initLightState();
	rootNode.setRenderState(zBufferState);
	rootNode.setRenderState(lightState);
	
	// TODO: workaround: flip the window 180 degrees around Y axis until initial
	// avatar look direction can be flipped.
	//hack(rootNode);
    }
    
    /* TODO: delete: didn't work
    private void hack (Node node) {
	Quaternion nodeQuat = node.getLocalRotation();
	Quaternion flipRotQuat = new Quaternion();
        flipRotQuat.fromAngles(0f, 180f *(float)Math.PI/180.0f, 0.0f);
        nodeQuat.multLocal(flipRotQuat);
	node.setLocalRotation(nodeQuat);
    }
    */

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node createSceneGraph (Entity entity) {
        applyTransform(rootNode, cell.getLocalTransform());
	return rootNode;
    }

    /**
     * Initialize the Z buffer state.
     */
    protected void initZBufferState () {
	zBufferState = (ZBufferState) 
	    ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
	zBufferState.setEnabled(true);
	zBufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
    }

    /**
     * Initialize the light state.
     */
    protected void initLightState () {
	PointLight light = new PointLight();
	light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
	light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
	light.setLocation(new Vector3f(100, 100, 100));
	light.setEnabled(true);
	lightState = (LightState) 
	    ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_LIGHT);
	lightState.setEnabled(true);
	lightState.attach(light);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void attachView (WindowView view) {
	ViewWorldDefault viewWorld = (ViewWorldDefault) view;
	rootNode.attachChild(viewWorld.getBaseNode());

        FrameWorldDefault frame = viewWorld.getFrame();
	if (frame != null) {
	    rootNode.attachChild(frame.getBaseNode());
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void detachView (WindowView view) {
	ViewWorldDefault viewWorld = (ViewWorldDefault) view;
	rootNode.detachChild(viewWorld.getBaseNode());

        FrameWorldDefault frame = viewWorld.getFrame();
	rootNode.detachChild(frame.getBaseNode());
    }
}
