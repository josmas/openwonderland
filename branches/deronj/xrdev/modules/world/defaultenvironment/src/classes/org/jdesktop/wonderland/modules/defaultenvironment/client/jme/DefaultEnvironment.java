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
package org.jdesktop.wonderland.modules.defaultenvironment.client.jme;

import com.jme.image.Texture;
import com.jme.light.LightNode;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Skybox;
import com.jme.scene.Spatial;
import com.jme.scene.Spatial.TextureCombineMode;
import com.jme.scene.state.CullState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.SkyboxComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.Environment;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * @author paulby
 */
public class DefaultEnvironment implements Environment {

    private Skybox skybox = null;

    private ServerSessionManager loginManager;

    public DefaultEnvironment(ServerSessionManager loginManager) {
        this.loginManager = loginManager;
    }

    /**
     * @{@inheritDoc}
     */
    public void setGlobalLights() {             
        LightNode globalLight1 = new LightNode();
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.95f, 0.95f, 0.95f, 1.0f));
        //light.setAmbient(new ColorRGBA(0.85f, 0.85f, 0.85f, 1.0f));
        light.setAmbient(new ColorRGBA(0.25f, 0.25f, 0.25f, 1.0f));
        light.setEnabled(true);
        globalLight1.setLight(light);
        globalLight1.setLocalTranslation(0.0f, 500.0f, 500.0f);

        LightNode globalLight2 = new LightNode();
        light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 1.0f));
        light.setAmbient(new ColorRGBA(0.25f, 0.25f, 0.25f, 1.0f));
        light.setEnabled(true);
        globalLight2.setLight(light);
        globalLight2.setLocalTranslation(0.0f, -500.0f, -500.0f);
        ClientContextJME.getWorldManager().getRenderManager().addLight(globalLight1);
        ClientContextJME.getWorldManager().getRenderManager().addLight(globalLight2);
    }

    /**
     * @{@inheritDoc}
     */
    public void setSkybox() {

        if (skybox==null) {
            Entity skyboxEnt = createSkyboxEntity();
            ClientContextJME.getWorldManager().addEntity(skyboxEnt);
        }

        ViewManager.getViewManager().addViewManagerListener(new ViewManagerListener() {

            public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {
            //Keep the skybox centered on the view
            newViewCell.addTransformChangeListener(new TransformChangeListener() {
                private Vector3f translation = new Vector3f();

                public void transformChanged(Cell cell, ChangeSource source) {
                    skybox.setLocalTranslation(cell.getWorldTransform().getTranslation(translation));
                }

            });
            }
        });

    }

    private Entity createSkyboxEntity() {
        try {
            /* Form the asset URIs */
            String server = loginManager.getServerNameAndPort();

            URL northURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/1.jpg", server);
            URL southURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/3.jpg", server);
            URL eastURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/2.jpg", server);
            URL westURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/4.jpg", server);
            URL downURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/5.jpg", server);
            URL upURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/6.jpg", server);

            WorldManager wm = ClientContextJME.getWorldManager();
            skybox = new Skybox("skybox", 1000, 1000, 1000);
            Texture north = TextureManager.loadTexture(northURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture south = TextureManager.loadTexture(southURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture east = TextureManager.loadTexture(eastURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture west = TextureManager.loadTexture(westURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture up = TextureManager.loadTexture(upURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture down = TextureManager.loadTexture(downURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            skybox.setTexture(Skybox.Face.North, north);
            skybox.setTexture(Skybox.Face.West, west);
            skybox.setTexture(Skybox.Face.South, south);
            skybox.setTexture(Skybox.Face.East, east);
            skybox.setTexture(Skybox.Face.Up, up);
            skybox.setTexture(Skybox.Face.Down, down);
            //skybox.preloadTextures();
            CullState cullState = (CullState) wm.getRenderManager().createRendererState(RenderState.RS_CULL);
            cullState.setEnabled(true);
            skybox.setRenderState(cullState);
            ZBufferState zState = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
            //zState.setEnabled(false);
            skybox.setRenderState(zState);
            FogState fs = (FogState) wm.getRenderManager().createRendererState(RenderState.RS_FOG);
            fs.setEnabled(false);
            skybox.setRenderState(fs);
            skybox.setLightCombineMode(Spatial.LightCombineMode.Off);
            skybox.setCullHint(Spatial.CullHint.Never);
            skybox.setTextureCombineMode(TextureCombineMode.Replace);
            skybox.updateRenderState();
            skybox.lockBounds();
            //skybox.lockMeshes();
            Entity e = new Entity("Skybox");
//            e.addComponent(ProcessorComponent.class, new TextureAnimationProcessor(up));
            SkyboxComponent sbc = wm.getRenderManager().createSkyboxComponent(skybox, true);
            e.addComponent(SkyboxComponent.class, sbc);

            return e;

        } catch (MalformedURLException ex) {
            Logger.getLogger(DefaultEnvironment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

//    class TextureAnimationProcessor extends ProcessorComponent {
//
//        private Texture top;
//
//        public TextureAnimationProcessor(Texture top) {
//            this.top = top;
//            top.setTranslation(new Vector3f());
//        }
//
//        @Override
//        public void compute(ProcessorArmingCollection arg0) {
//            // Do nothing
//        }
//
//        @Override
//        public void commit(ProcessorArmingCollection arg0) {
//            Vector3f v3f = top.getTranslation();
//            v3f.y += 0.1f;
//            if (v3f.y>1)
//                v3f.y = 0;
//            top.setTranslation(v3f);
//        }
//
//        @Override
//        public void initialize() {
//            setArmingCondition(new NewFrameCondition(this));
//        }
//
//    }

}
