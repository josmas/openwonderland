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
package org.jdesktop.wonderland.client.jme;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.TextureRenderer;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;

/**
 * Test for Portals using Render to Texture
 * 
 * @author paulby
 */
public class PortalModule extends RenderModule {

    private TextureRenderer tRenderer;
    private CameraNode portalCamera;
    private Texture rendTexture;
    private Node portalNode;
    private Vector3f portalLocation = new Vector3f(0,2,0);
    
    private float width = 3;
    private float height = 3;
    private float frameSize = 0.2f;
    
    public void init(RenderInfo info) {
//        tRenderer = info.getDisplay().createTextureRenderer(
//                256, 
//                256, 
//                TextureRenderer.RENDER_TEXTURE_2D);
//        portalCamera = new CameraNode("Portal Camera Node", tRenderer.getCamera());
//        portalCamera.setLocalTranslation(new Vector3f(5, 2, 5));
//        portalCamera.updateGeometricState(0, true);
//
//        portalNode = new Node("Portal Node");
//        Quad quad = new Quad("Portal");
//        quad.initialize(width, height);
//        portalNode.attachChild(quad);
//        
//        Quad top = new Quad("Portal Frame Top");
//        top.initialize(width+frameSize*2, frameSize);
//        top.setLocalTranslation(0, height/2+frameSize/2, 0);
//        portalNode.attachChild(top);
//
//        Quad bottom = new Quad("Portal Frame Bottom");
//        bottom.initialize(width+frameSize*2, frameSize);
//        bottom.setLocalTranslation(0, -(height/2+frameSize/2), 0);
//        portalNode.attachChild(bottom);
//
//        Quad right = new Quad("Portal Frame Right");
//        right.initialize(frameSize, height+frameSize*2);
//        right.setLocalTranslation(width/2+frameSize/2, 0, 0);
//        portalNode.attachChild(right);
//
//        Quad left = new Quad("Portal Frame Left");
//        left.initialize(frameSize, height+frameSize*2);
//        left.setLocalTranslation(-(width/2+frameSize/2), 0, 0);
//        portalNode.attachChild(left);
//                
//        portalNode.setLocalTranslation(portalLocation);
//    
//        tRenderer.setBackgroundColor(new ColorRGBA(0f, 0f, 0f, 1f));
//        rendTexture = new Texture();
//        rendTexture.setRTTSource(Texture.RTT_SOURCE_RGBA);
//        tRenderer.setupTexture(rendTexture);
//        TextureState screen = info.getDisplay().getRenderer().createTextureState();
//        screen.setTexture(rendTexture);
//        screen.setEnabled(true);
//        quad.setRenderState(screen);
//        
//        // Setup our params for the depth buffer
//        ZBufferState buf = info.getDisplay().getRenderer().createZBufferState();
//        buf.setEnabled(true);
//        buf.setFunction(ZBufferState.CF_LEQUAL);
//
//        portalNode.setRenderState(buf);
//    
//        portalNode.updateGeometricState(0.0f, true);
//        portalNode.updateRenderState();
//        portalNode.setLightCombineMode(LightState.OFF);
    }

    public void update(RenderInfo info, float interpolation) {
        // nothing to do
    }

    public void render(RenderInfo info, float interpolation) {  
        tRenderer.render(info.getRoot(), rendTexture);
    }

    @Override
    public void setActiveImpl(boolean active, RenderInfo info) {
        if (active)
            info.getRoot().attachChild(portalNode);
        else
            info.getRoot().detachChild(portalNode);
    }

}
