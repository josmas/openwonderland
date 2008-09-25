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
package org.jdesktop.wonderland.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.light.PointLight;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author paulby
 * @deprecated
 */
public class StaticModelRenderer extends BasicRenderer {
    
    public StaticModelRenderer(Cell cell) {
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

        Vector3f translation = cell.getLocalTransform().getTranslation(null);
        
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;
        return createFloorEntity(cell.getCellID().toString(), translation.x, translation.y, translation.z, buf, lightState, color);
//        return createWireframeEntity();
    }

    public Node createFloorEntity(String name, float xoff, float yoff, float zoff, 
            ZBufferState buf, LightState ls, ColorRGBA color) {
        MaterialState matState = null;
        
        Node ret = new Node();
        BoundingVolume b = cell.getLocalBounds();
        float width;
        float depth;
        if (b instanceof BoundingBox) {
            Vector3f extent = ((BoundingBox)b).getExtent(null);
            width = extent.x*2;
            depth = extent.z*2;
        } else if (b instanceof BoundingSphere) {
            width = ((BoundingSphere)b).getRadius();
            depth = width;
        } else {
            logger.warning("Unsupported Bounds type "+b.getClass().getName());
            width = depth = 5;
        }
        
        Floor floor = new Floor("Floor", width, depth);
        ret.attachChild(floor);
        ret.setModelBound(new BoundingBox());
        ret.updateModelBound();

        matState = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
//        node.setRenderState(matState);
        ret.setRenderState(buf);
//        node.setRenderState(ls);
        ret.setLocalTranslation(xoff, yoff, zoff);

        ret.setName("Cell_"+cell.getCellID()+":"+cell.getName());

        return ret;
    }
    
    /**
     * Creates a wireframe box or sphere with the same size as the bounds.
     */
    public Node createWireframeEntity() {
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
}
