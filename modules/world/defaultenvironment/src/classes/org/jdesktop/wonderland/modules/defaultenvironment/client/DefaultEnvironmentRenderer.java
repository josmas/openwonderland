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

import com.jme.image.Texture;
import com.jme.light.DirectionalLight;
import com.jme.light.LightNode;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.SkyboxComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.defaultenvironment.client.DefaultEnvironmentCell.MapReceivedListener;
import org.jdesktop.wonderland.modules.defaultenvironment.common.SharedDirectionLight;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 * Renders the default environment, including lights and skybox. Adapted from
 * DefaultEnvironment.
 * @author paulby
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 * @author JagWire
 */
public class DefaultEnvironmentRenderer implements CellRendererJME,
        ViewManagerListener, TransformChangeListener, MapReceivedListener
{
    private static final Logger LOGGER =
            Logger.getLogger(DefaultEnvironmentRenderer.class.getName());

    private final DefaultEnvironmentCell cell;

    private CellStatus status = CellStatus.DISK;

    private Skybox skybox = null;
    private Entity skyboxEntity = null;
    private Vector3f translation = new Vector3f();

    private ViewCell curViewCell = null;
    private SkyboxProcessor skyboxProcessor;

    
    /**
     * This field is used to hold default 'magic lights' created within
     * addGlobalLights(). Those lights will be placed here and added to the
     * cell's mapping on RENDERING if no lights currently exist in the cell's
     * map.
     */
    private Map<String, LightNode> initialLights = new LinkedHashMap<String, LightNode>();
    
    public DefaultEnvironmentRenderer(Cell cell) {
        this.cell = (DefaultEnvironmentCell) cell;
        this.cell.addMapReceivedListener(this);
        LOGGER.fine("Registered map received listener in renderer!");
    }

    public synchronized Entity getEntity() {
        if (skyboxEntity == null) {
            addGlobalLights();
            createSkybox();
        }
        
        return skyboxEntity;
    }

    public Skybox getSkybox() {
        return skybox;
    }
    
    public void cellTransformUpdate(CellTransform localTransform) {
        // ignore -- shouldn't happen
    }

    public void setStatus(CellStatus status, boolean increasing) {
        this.status = status;

        if (status == CellStatus.RENDERING && increasing) {
            //Re-add lights from state

            Map<String, LightNode> m = cell.getLightMap();

            //if our cell lightmap is null, promptly return as there is an error.
            if(m == null) {
                LOGGER.warning("CELL LIGHT MAP IS NULL!");
                return;
                //otherwise, it's just empty.
            } else if(m.isEmpty()) {
                LOGGER.warning("CELL LIGHT MAP IS EMPTY!!");
                //if the cell lightmap is empty, and the temp lightmap is empty, return as there is an error.
                if(initialLights.isEmpty()) {
                    LOGGER.warning("RENDERER TMP MAP IS EMPTY!");
                    return;
                } else {
                    //if the tempt lightmap isn't empty, populate the cell light map with their values.
                    m.putAll(initialLights);
//                    logger.warning("CREATING DEFAULT LIGHTS!");

                }
            }
            
//            for(LightNode value: tmpLights.values()) {
//                addLightToRenderer(value);
//            }
            
            if(cell.getSharedLightMap() != null) {
                LOGGER.fine("POPULATING LIGHTS WITH SHARED VALUES!");
                SharedMapCli smc = cell.getSharedLightMap();
                for(Map.Entry<String, SharedData> data:smc.entrySet()) {
                    LOGGER.fine("POPULATING LIGHT: "+data.getKey());
                    if (initialLights.containsKey(data.getKey())) {
                        LightNode node = initialLights.get(data.getKey());
                        SharedDirectionLight light = (SharedDirectionLight) data.getValue();

                        updateLight(node, buildLightFromState(light));
                    } else {
                        //there's a light in the shared map that isn't in the
                        //tmp lights map.
                        LOGGER.warning("LIGHT: "+data.getKey()+" DOES NOT EXIST IN ENVIRONMENT!");
                    }

                }
            } else {
                LOGGER.warning("CELL SHARED LIGHT MAP IS NULL!");
            }
            
            
            
            
            
            //clear the lighting so we can add our own.
//            removeGlobalLights();
//            for(LightNode value: m.values()) {                
//                addLightToRenderer(value);
//            }
            
            
        } else if (status == CellStatus.INACTIVE && increasing) {                                                
            ClientContextJME.getWorldManager().addEntity(getEntity());
        } else if (status == CellStatus.INACTIVE && !increasing) {
            removeGlobalLights();
            removeSkybox();
        }
    }

    public void addLight(String key, LightNode value) {
//        ClientContextJME.getWorldManager().getRenderManager().addLight(key, value);
        addLightToRenderer(value);
    }
    
    public void updateLight(final LightNode lightToBeUpdated, final LightNode updatedLightInfo) {
//        removeLightFromRenderer(lightToBeReplaced);
//        addLightToRenderer(lightToBeInserted);
        final DirectionalLight originalLight = (DirectionalLight)lightToBeUpdated.getLight();
        final DirectionalLight freshLight = (DirectionalLight)updatedLightInfo.getLight();
        
        SceneWorker.addWorker(new WorkCommit() { 
            public void commit() {
                originalLight.setAmbient(freshLight.getAmbient());
                originalLight.setDiffuse(freshLight.getDiffuse());
                originalLight.setSpecular(freshLight.getSpecular());
                originalLight.setShadowCaster(freshLight.isShadowCaster());
                originalLight.setDirection(freshLight.getDirection());
                lightToBeUpdated.setLocalTranslation(updatedLightInfo.getLocalTranslation());
            }
        });
    }
    
    public void removeLight(String key) {
//        ClientContextJME.getWorldManager().getRenderManager().removeLight(key);
    }
    
    public HashMap<String, SharedDirectionLight> 
            getMapWithSharedDataFromLights(Map<String, LightNode> lights) {
        
        HashMap<String, SharedDirectionLight> sharedLights =
                new HashMap<String, SharedDirectionLight>();
        
        for(Map.Entry<String, LightNode> light: lights.entrySet()) {
            sharedLights.put(light.getKey(), buildSharedLightFromLightNode(light.getValue()));
        }
        return null;
    }
    
    private SharedDirectionLight buildSharedLightFromLightNode(LightNode node) {
        return SharedDirectionLight.valueOf(node.getLight().getAmbient(),
                                            node.getLight().getDiffuse(),
                                            node.getLight().getSpecular(),
                                            node.getLocalTranslation(),
                                            ((DirectionalLight)node.getLight()).getDirection(),
                                            false);//ColorRGBA.blue, ColorRGBA.yellow, translation, translation, true)
    }
    
    public CellStatus getStatus() {
        return status;
    }

    private LightNode buildLightFromState(SharedDirectionLight state) {
            DirectionalLight light = new DirectionalLight();
            LightNode lightNode = new LightNode();
            
            light.setAmbient(state.getAmbient());
            light.setDiffuse(state.getDiffuse());
            light.setSpecular(state.getSpecular());
            light.setDirection(state.getDirection());
            light.setShadowCaster(state.isCastShadows());
            lightNode.setLight(light);
            lightNode.setLocalTranslation(state.getTranslation());
            
            return lightNode;
        }
    
    /**
     * Add global lights
     */
    protected void addGlobalLights() {
        LightNode globalLight1;
        LightNode globalLight2;
        LightNode globalLight3;

        float radius = 75.0f;
        float lheight = 30.0f;
        float x = (float)(radius*Math.cos(Math.PI/6));
        float z = (float)(radius*Math.sin(Math.PI/6));
        globalLight1 = createLight(x, lheight, z);
        x = (float)(radius*Math.cos(5*Math.PI/6));
        z = (float)(radius*Math.sin(5*Math.PI/6));
        globalLight2 = createLight(x, lheight, z);
        x = (float)(radius*Math.cos(3*Math.PI/2));
        z = (float)(radius*Math.sin(3*Math.PI/2));
        globalLight3 = createLight(x, lheight, z);

        initialLights.put("LIGHT-1", globalLight1);
        initialLights.put("LIGHT-2", globalLight2);
        initialLights.put("LIGHT-3", globalLight3);
        
        LOGGER.fine("ADDING GLOBAL LIGHTS!");
        for(LightNode value: initialLights.values()) {
            addLightToRenderer(value);
        }
        

    }

    private LightNode createLight(float x, float y, float z) {
        LightNode lightNode = new LightNode();
        DirectionalLight light = new DirectionalLight();
        light.setDiffuse(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setAmbient(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
        light.setSpecular(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
        light.setEnabled(true);
        lightNode.setLight(light);
        lightNode.setLocalTranslation(x, y, z);
        light.setDirection(new Vector3f(-x, -y, -z));
        return (lightNode);
    }

    /**
     * @{inheritDoc}
     */
    protected void removeGlobalLights() {
        for(LightNode node: getLightsFromRenderer()) {
            removeLightFromRenderer(node);
        }
    }

    /**
     * @{@inheritDoc}
     */
    protected void createSkybox() {
        LOGGER.fine("[DefaultEnvironment] add skybox to " + this);

        if (skyboxEntity == null) {
            skyboxEntity = createSkyboxEntity();
        }

        ViewManager.getViewManager().addViewManagerListener(this);
    }

    public void removeSkybox() {
        LOGGER.fine("[DefaultEnvironment] remove skybox from " + this +
                    " curViewCell: " + curViewCell);

        ClientContextJME.getWorldManager().removeEntity(skyboxEntity);
        ViewManager.getViewManager().removeViewManagerListener(this);

        if (curViewCell != null) {
            curViewCell.removeTransformChangeListener(this);
            curViewCell = null;
        }

        skyboxEntity = null;
        skybox = null;
    }

    public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {
        LOGGER.fine("[DefaultEnvironment] primary view changed for " + this +
                    ".  Old: " + oldViewCell + " new: " + newViewCell);

        if (curViewCell != null) {
            curViewCell.removeTransformChangeListener(this);
        }

        //Keep the skybox centered on the view
        curViewCell = newViewCell;
        if (curViewCell!=null) {
            curViewCell.addTransformChangeListener(this);
            transformChanged(curViewCell, ChangeSource.LOCAL);
        }
    }

    public void transformChanged(Cell cell, ChangeSource source) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("[DefaultEnvironment] transform changed for " + this);
        }

        skyboxProcessor.viewMoved(cell.getWorldTransform().getTranslation(translation));

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater(){
            public void update(Object arg0) {

            }
        }, cell);
    }
    /**
     * Apply a new skybox to our entity. Method assumes that CullState,
     * ZBufferState, and FogState have already been attached.
     * 
     * @param skybox
     * @return Entity reflecting new skybox. 
     */
    private Entity createSkyboxEntity(Skybox skybox) {
        WorldManager wm = ClientContextJME.getWorldManager();
        
        skybox.setLightCombineMode(Spatial.LightCombineMode.Off);
        skybox.setCullHint(Spatial.CullHint.Never);
        skybox.setTextureCombineMode(TextureCombineMode.Replace);
        skybox.updateRenderState();
        skybox.lockBounds();    
    
        Entity e = new Entity("Skybox");
        SkyboxComponent sbc = wm.getRenderManager().createSkyboxComponent(skybox, true);
        e.addComponent(SkyboxComponent.class, sbc);

        skyboxProcessor = new SkyboxProcessor(wm, skybox);
        e.addComponent(SkyboxProcessor.class, skyboxProcessor);
        return e;

    }
    
    public void updateSkybox(Skybox skybox) {
//        Entity update = createSkyboxEntity(skybox);
        ClientContextJME.getWorldManager().removeEntity(skyboxEntity);
        skyboxEntity = createSkyboxEntity(skybox);
        ClientContextJME.getWorldManager().addEntity(skyboxEntity);
        
    }
    private Entity createSkyboxEntity() {
        try {
            /* Form the asset URIs */
            
            URL northURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/1.jpg", cell);
            URL southURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/3.jpg", cell);
            URL eastURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/2.jpg", cell);
            URL westURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/4.jpg", cell);
            URL downURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/5.jpg", cell);
            URL upURL = AssetUtils.getAssetURL("wla://defaultenvironment/skybox1/6.jpg", cell);

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

            skyboxProcessor = new SkyboxProcessor(wm, skybox);
            e.addComponent(SkyboxProcessor.class, skyboxProcessor);

            return e;

        } catch (MalformedURLException ex) {
            Logger.getLogger(DefaultEnvironmentRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private List<LightNode> getLightsFromRenderer() {
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        
        int numberOfLights = rm.numLights();
        List<LightNode> lights = new ArrayList<LightNode>();
        for(int i = 0; i < numberOfLights; i++) {
            lights.add(rm.getLight(i));
        }
        
        return lights;        
    }
    
    private void addLightToRenderer(LightNode lightNode) {
        ClientContextJME.getWorldManager().getRenderManager().addLight(lightNode);
    }
    
    private void removeLightFromRenderer(LightNode lightNode) {
        ClientContextJME.getWorldManager().getRenderManager().removeLight(lightNode);        
    }
    
    public void mapReceived(SharedMapCli map) {
        LOGGER.fine("MAP RECEIVED: "+map.getName());
        //process the shared data from the map first.
        Map<String, LightNode> processed = new LinkedHashMap<String, LightNode>();
  
//            if(map == null) {
//                return;
//            } else if(map.isEmpty()) {
//                if(tmpLights.isEmpty()) {
//                    return;
//                } else {
//                    
//                }
//            }
//            for(Map.Entry<String, SharedData> e: map.entrySet()) {
//                SharedDirectionLight sdl = (SharedDirectionLight)e.getValue();
//                tmpLights.put(e.getKey(), sdl.toLightNode());
//            }
//            
//            
//            //clear the lighting so we can add our own.
//            removeGlobalLights();
//            for(LightNode value: tmpLights.values()) {                
//                addLightToRenderer(value);
//            }
    }
    
    class SkyboxProcessor extends ProcessorComponent {

        private Vector3f translation=new Vector3f();
        private boolean translationDirty = false;
        private final WorldManager worldManager;
        private Skybox skybox;
        
        
        public SkyboxProcessor(WorldManager worldManager, Skybox skybox) {
            this.worldManager = worldManager;
            this.skybox = skybox;
        }

        @Override
        public void compute(ProcessorArmingCollection arg0) {
        }

        @Override
        public synchronized void commit(ProcessorArmingCollection arg0) {
            if (translationDirty) {
                skybox.setLocalTranslation(translation);
                worldManager.addToUpdateList(skybox);
                translationDirty = false;
            }
        }

        @Override
        public void initialize() {
            setArmingCondition(new NewFrameCondition(this));
        }

        private synchronized void viewMoved(Vector3f translation) {
            this.translation.set(translation);
            translationDirty = true;
        }

    }

}
