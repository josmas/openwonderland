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
package org.jdesktop.wonderland.modules.phone.client.cell;

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
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.common.cell.CellTransform;

import javax.media.opengl.GLContext;

import java.lang.reflect.Method;

/**
 * @author jkaplan
 */
public class PhoneCellRenderer extends BasicRenderer {
    
    public PhoneCellRenderer(Cell cell) {
        super(cell);
    }
    
    protected Node createSceneGraph(Entity entity) {
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

	new MyMouseListener().addToEntity(entity);
        return createWireframeEntity();
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

	logger.fine("WIRE FRAME ENTITY CREATED");
        return node;
    }

    class MyMouseListener extends EventClassListener {

	public Class[] eventClassesToConsume () {
	    return new Class[] { MouseEvent3D.class };
	}

	public void commitEvent (Event event) {
	    if (event instanceof MouseButtonEvent3D) {
		// Linux-specific workaround: On Linux JOGL holds the SunToolkit AWT lock in mtgame commit methods.
		// In order to avoid deadlock with any threads which are already holding the AWT lock and which
		// want to acquire the lock on the dirty rectangle so they can draw (e.g Embedded Swing threads)
		// we need to temporarily release the AWT lock before we lock the dirty rectangle and then reacquire
		// the AWT lock afterward.
		GLContext glContext = null;
		if (isAWTLockHeldByCurrentThreadMethod != null) {
                    try {
                        Boolean ret = (Boolean) isAWTLockHeldByCurrentThreadMethod.invoke(null);
                        if (ret.booleanValue()) {
                            glContext = GLContext.getCurrent();
                            glContext.release();
                        }
                    } catch (Exception ex) {}
                }

		try {
		    MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
		    if (buttonEvent.isPressed()) {
		        ((PhoneCell) cell).phoneSelected();
		    }
		    return;
		} finally {
		    //Linux-specific workaround: Reacquire the lock if necessary.
                    if (glContext != null) {
                        glContext.makeCurrent();
                    }
		}
	    } 
	}
    }

    // We need to call this method reflectively because it isn't available in Java 5
    // BTW: we don't support Java 5 on Linux, so this is okay.
    private static boolean isLinux = System.getProperty("os.name").equals("Linux");
    private static Method isAWTLockHeldByCurrentThreadMethod;
    static {
        if (isLinux) {
            try {
                Class awtToolkitClass = Class.forName("sun.awt.SunToolkit");
                isAWTLockHeldByCurrentThreadMethod =
                    awtToolkitClass.getMethod("isAWTLockHeldByCurrentThread");
            } catch (ClassNotFoundException ex) {
            } catch (NoSuchMethodException ex) {
            }
        }
    }

}
