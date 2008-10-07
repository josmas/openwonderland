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
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Renderer for Avatar, looks strangely like a teapot at the moment...
 * 
 * @author paulby
 */
@ExperimentalAPI
public class AvatarJME extends BasicRenderer {

    public AvatarJME(Cell cell) {
        super(cell);
    }

    @Override
    protected Entity createEntity() {
        Entity ret = super.createEntity();
        WorldManager wm = ClientContextJME.getWorldManager();
               
                                
        return ret;
    }

    @Override
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
        Node ret = createTeapotEntity(cell.getCellID().toString(), translation.x, translation.y, translation.z, buf, lightState, color);        

        return ret;
    }

    public Node createTeapotEntity(String name, float xoff, float yoff, float zoff, 
            ZBufferState buf, LightState ls, ColorRGBA color) {
        MaterialState matState = null;
        
        // The center teapot
        Node ret = new Node();
        Teapot teapot = new Teapot();
        teapot.resetData();
        ret.setLocalTranslation(xoff, yoff, zoff);
        ret.setLocalScale(0.2f);
        ret.attachChild(teapot);

        matState = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
        ret.setRenderState(matState);
        ret.setRenderState(buf);
        ret.setRenderState(ls);
        ret.setLocalTranslation(xoff, yoff, zoff);

        return ret;
    }

}
