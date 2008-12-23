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
package org.jdesktop.wonderland.modules.orb.client.cell;

import com.jme.animation.SpatialTransformer;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.light.PointLight;
import com.jme.light.SimpleLightNode;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.LightState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;

import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;

import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * @author jprovino
 */
public class OrbCellRenderer extends BasicRenderer {
    
    private Entity entity;
    private MyMouseListener listener;

    public OrbCellRenderer(Cell cell) {
        super(cell);
    }
    
    protected Node createSceneGraph(Entity entity) {
	this.entity = entity;

        ColorRGBA color = new ColorRGBA();

        ZBufferState buf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3f(100, 100, 100));
        light.setEnabled(true);
        LightState lightState = (LightState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_LIGHT);
        lightState.setEnabled(true);
        lightState.attach(light);
        
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;
        //return createWireframeEntity();

	listener = new MyMouseListener();
	listener.addToEntity(entity);
	//System.out.println("Added mouse listener to " + cell.getCellID());
	return createAnimationEntity();
    }

    /**
     * Creates a wireframe box or sphere with the same size as the bounds.
     */
    private Node createWireframeEntity() {
        /* Fetch the basic info about the cell */
        String name = cell.getCellID().toString();
        CellTransform transform = cell.getLocalTransform();
        Vector3f translation = transform.getTranslation(null);
        Vector3f scaling = transform.getScaling(null);
        Quaternion rotation = transform.getRotation(null);
        
        /* Create the new object -- either a Box or Sphere */
        TriMesh mesh = null;
        if (cell.getLocalBounds() instanceof BoundingBox) {
            Vector3f extent = ((BoundingBox)cell.getLocalBounds()).getExtent(null);
            mesh = new Box(name, new Vector3f(), extent.x, extent.y, extent.z);
        }
        else if (cell.getLocalBounds() instanceof BoundingSphere) {
            float radius = ((BoundingSphere)cell.getLocalBounds()).getRadius();
            mesh = new Sphere(name, new Vector3f(), 10, 10, radius);
        }
        else {
            logger.warning("Unsupported Bounds type " +cell.getLocalBounds().getClass().getName());
            return new Node();
        }
        
        /* Create the scene graph object and set its wireframe state */
        Node node = new Node();
        node.attachChild(mesh);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        node.setLocalTranslation(translation);
        node.setLocalScale(scaling);
        node.setLocalRotation(rotation);

        WireframeState wiState = (WireframeState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
        wiState.setEnabled(true);
        node.setRenderState(wiState);
        node.setName("Cell_"+cell.getCellID()+":"+cell.getName());

        return node;
    }

    private Node createAnimationEntity() {
	Sphere s=new Sphere("My sphere",30,30,(float).5);
	// I will rotate this pivot to move my light
	Node pivot=new Node("Pivot node");
	// This light will rotate around my sphere. Notice I don't
	// give it a position
	PointLight pl=new PointLight();
	// Color the light red
	pl.setDiffuse(ColorRGBA.red);
	// Enable the light
	pl.setEnabled(true);
	// Remove the default light and attach this one
        LightState lightState = (LightState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_LIGHT);
	lightState.detachAll();
	lightState.attach(pl);
	// This node will hold my light
	SimpleLightNode ln=
	 new SimpleLightNode("A node for my pointLight",pl);
	// I set the light's position thru the node
	ln.setLocalTranslation(new Vector3f(0,10,0));
	// I attach the light's node to my pivot
	pivot.attachChild(ln);
	// I create a box and attach it too my lightnode.
	// This lets me see where my light is
	Box b=new Box("Blarg", new Vector3f(-.3f,-.3f,-.3f),
	new Vector3f(.3f,.3f,.3f));
	ln.attachChild(b);
	// I create a controller to rotate my pivot
	SpatialTransformer st=new SpatialTransformer(1);
	// I tell my spatial controller to change pivot
	st.setObject(pivot,0,-1);
	// Assign a rotation for object 0 at time 0 to rotate 0
	// degrees around the z axis
	Quaternion x0=new Quaternion();
	x0.fromAngleAxis(0,new Vector3f(0,0,1));
	st.setRotation(0,0,x0);
	// Assign a rotation for object 0 at time 2 to rotate 180
	// degrees around the z axis
	Quaternion x180=new Quaternion();
	x180.fromAngleAxis(FastMath.DEG_TO_RAD*180,new Vector3f(0,0,1));
	st.setRotation(0,2,x180);
	// Assign a rotation for object 0 at time 4 to rotate 360
	// degrees around the z axis
	Quaternion x360=new Quaternion();
	x360.fromAngleAxis(FastMath.DEG_TO_RAD*360,new Vector3f(0,0,1));
	st.setRotation(0,4,x360);
	// Prepare my controller to start moving around
	st.interpolateMissing();
	// Tell my pivot it is controlled by st
	pivot.addController(st);

        Node node = new Node();
	// Attach pivot and sphere to graph
	node.attachChild(pivot);
	node.attachChild(s);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();

        CellTransform transform = cell.getLocalTransform();
	logger.fine("transform:  " + transform);
        Vector3f translation = transform.getTranslation(null);
        Vector3f scaling = transform.getScaling(null);
        Quaternion rotation = transform.getRotation(null);

        node.setLocalTranslation(translation);
        node.setLocalScale(scaling);
        node.setLocalRotation(rotation);

        node.setName("Cell_"+cell.getCellID()+":"+cell.getName());

	logger.fine("ANIMATION ENTITY CREATED");
	return node;
    }

    public void removeMouseListener() {
	//System.out.println("Removing mouse listener for " + cell.getCellID());
	listener.removeFromEntity(entity);
    }

    class MyMouseListener extends EventClassListener {

	public Class[] eventClassesToConsume () {
	    return new Class[] { MouseEvent3D.class };
	}

	public void commitEvent (Event event) {
	    if (event instanceof MouseButtonEvent3D) {
		MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
		if (buttonEvent.isPressed()) {
		    ((OrbCell) cell).orbSelected();
		}
		return;
	    } 
	}
    }

}
