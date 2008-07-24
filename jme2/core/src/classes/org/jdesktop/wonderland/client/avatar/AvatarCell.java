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
package org.jdesktop.wonderland.client.avatar;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Teapot;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.SceneComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Floor;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveListener;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveSource;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author paulby
 */
public class AvatarCell extends ViewCell {

    public AvatarCell(CellID cellID) {
        super(cellID);
    }
    
    @Override
    protected Entity createEntity() {
        Entity ret = new Entity("StaticModelCell "+getCellID());
        BoundingSphere b = (BoundingSphere) getLocalBounds();
        
        // HACK ! TODO once entities support other bounds types remove this
//        BoundingBox b2 = new BoundingBox(b.getCenter(), b.getRadius(), b.getRadius(), b.getRadius());
//        ret.setBounds(b2);
        
        WorldManager wm = JmeClientMain.getWorldManager();
        CellTransform t = getTransform();
//        ret.setTransform(t.getRotation(null), t.getTranslation(null), t.getScaling(null));
//        Vector3f v3f = new Vector3f();
//        ret.getPosition(v3f);
        
        addBoundsGeometry(ret, wm);
        
        return ret;
    }
    
    private void addBoundsGeometry(Entity entity, WorldManager wm) {
        ColorRGBA color = new ColorRGBA();

        ZBufferState buf = (ZBufferState) wm.createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

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
        createTeapotEntity(getCellID().toString(), translation.x, translation.y, translation.z, buf, lightState, color, wm);        
    }

    public void createTeapotEntity(String name, float xoff, float yoff, float zoff, 
            ZBufferState buf, LightState ls, ColorRGBA color, WorldManager wm) {
        MaterialState matState = null;
        
        // The center teapot
        Node node = new Node();
        Teapot teapot = new Teapot();
        teapot.resetData();
        teapot.setLocalTranslation(xoff, yoff, zoff);
        node.attachChild(teapot);

        matState = (MaterialState) wm.createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
        node.setRenderState(matState);
        node.setRenderState(buf);
        node.setRenderState(ls);
        node.setLocalTranslation(xoff, yoff, zoff);

        Entity te = new Entity(name + "Teapot");
        SceneComponent sc = new SceneComponent();
        sc.setSceneRoot(node);
        te.addComponent(SceneComponent.class, sc);
        
        MoveProcessor moveProc = new MoveProcessor(JmeClientMain.getWorldManager(), node);
        MovableComponent mc = getComponent(MovableComponent.class);
        mc.addServerCellMoveListener(moveProc);
        
//        RotationProcessor rp = new RotationProcessor(name + "Teapot Rotator", wm, 
//                node, (float) (6.0f * Math.PI / 180.0f));
//        //rp.setRunInRenderer(true);
        te.addComponent(ProcessorComponent.class, moveProc);
        wm.addEntity(te);        
    }
    
    class MoveProcessor extends ProcessorComponent implements CellMoveListener {

        private CellTransform cellTransform;
        private boolean dirty = false;
        private Node node;
        private WorldManager worldManager;
        
        public MoveProcessor(WorldManager worldManager, Node node) {
            this.node = node;
            this.worldManager = worldManager;
        }
        
        @Override
        public void compute(ProcessorArmingCollection arg0) {
        }

        @Override
        public void commit(ProcessorArmingCollection arg0) {
            synchronized(this) {
                if (dirty) {
                    node.setLocalTranslation(cellTransform.getTranslation(null));
                    dirty = false;
                    worldManager.addToUpdateList(node);
                }
            }
        }

        @Override
        public void initialize() {
            setArmingCondition(new NewFrameCondition(this));           
        }

        public void cellMoved(CellTransform transform, CellMoveSource source) {
            synchronized(this) {
                this.cellTransform = transform;
                dirty = true;
            }
        }


    }
}
