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
package org.jdesktop.wonderland.client.cell;

import com.jme.app.mtgame.WorldManager;
import com.jme.app.mtgame.entity.Entity;
import com.jme.app.mtgame.entity.ProcessorComponent;
import com.jme.app.mtgame.entity.RotationProcessor;
import com.jme.app.mtgame.entity.SceneComponent;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Teapot;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * Client side class for world root cells.
 * 
 * @author paulby
 */
public class StaticModelCell extends Cell {
    
    public StaticModelCell(CellID cellID) {
        super(cellID);
    }
    
    @Override
    protected Entity createEntity() {
        Entity ret = new Entity("StaticModelCell "+getCellID(), null);
        ret.setBounds((BoundingBox) getLocalBounds());
        
        WorldManager wm = JmeClientMain.getWorldManager();
        CellTransform t = getTransform();
        ret.setTransform(t.getRotation(null), t.getTranslation(null), t.getScaling(null));
        Vector3f v3f = new Vector3f();
        ret.getPosition(v3f);
        
        addBoundsGeometry(ret, wm);
        
        return ret;
    }
    
    private void addBoundsGeometry(Entity entity, WorldManager wm) {
        ColorRGBA color = new ColorRGBA();

        ZBufferState buf = (ZBufferState) wm.createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.CF_LEQUAL);

        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3f(100, 100, 100));
        light.setEnabled(true);
        LightState lightState = (LightState) wm.createRendererState(RenderState.RS_LIGHT);
        lightState.setEnabled(true);
        lightState.attach(light);

        Vector3f translation = getTransform().getTranslation(null);
        
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;
        createFloorEntity(getCellID().toString(), translation.x, translation.y, translation.z, buf, lightState, color, wm);        
    }

    public void createTeapotEntity(String name, float xoff, float yoff, float zoff, 
            ZBufferState buf, LightState ls, ColorRGBA color, WorldManager wm) {
        MaterialState matState = null;
        
        // The center teapot
        Node node = new Node();
//        Teapot teapot = new Teapot();
//        teapot.resetData();
        Floor teapot = new Floor("Floor", 10, 10);
        teapot.setLocalTranslation(xoff, yoff, zoff);
//        Box teapot = new Box("Box", new Vector3f(0f,0f,0f), 1f, 1f, 1f);
        node.attachChild(teapot);

        matState = (MaterialState) wm.createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
        node.setRenderState(matState);
        node.setRenderState(buf);
        node.setRenderState(ls);
        node.setLocalTranslation(xoff, yoff, zoff);

        Entity te = new Entity(name + "Teapot", null);
        SceneComponent sc = new SceneComponent();
        sc.setSceneRoot(node);
        te.addComponent(SceneComponent.class, sc);
        
        RotationProcessor rp = new RotationProcessor(name + "Teapot Rotator", wm, 
                node, (float) (6.0f * Math.PI / 180.0f));
        //rp.setRunInRenderer(true);
        te.addComponent(ProcessorComponent.class, rp);
        wm.addEntity(te);        
    }
    
    public void createFloorEntity(String name, float xoff, float yoff, float zoff, 
            ZBufferState buf, LightState ls, ColorRGBA color, WorldManager wm) {
        MaterialState matState = null;
        
        Node node = new Node();
        BoundingVolume b = getLocalBounds();
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
        node.attachChild(floor);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();

        matState = (MaterialState) wm.createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
//        node.setRenderState(matState);
        node.setRenderState(buf);
//        node.setRenderState(ls);
        node.setLocalTranslation(xoff, yoff, zoff);

        Entity te = new Entity(name + "Entity", null);
        SceneComponent sc = new SceneComponent();
        sc.setSceneRoot(node);
        te.addComponent(SceneComponent.class, sc);
        
        wm.addEntity(te);        
    }
}
