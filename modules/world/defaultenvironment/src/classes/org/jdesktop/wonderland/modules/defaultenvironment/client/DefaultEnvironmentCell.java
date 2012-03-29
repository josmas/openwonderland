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
package org.jdesktop.wonderland.modules.defaultenvironment.client;

import com.jme.image.Texture;
import com.jme.light.DirectionalLight;
import com.jme.light.LightNode;
import com.jme.scene.Skybox;
import com.jme.util.TextureManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.EnvironmentCell;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.defaultenvironment.common.SharedDirectionLight;
import org.jdesktop.wonderland.modules.defaultenvironment.common.SharedSkybox;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 * Default environment cell
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 * @author JagWire 
 */
public class DefaultEnvironmentCell extends EnvironmentCell {
    private static final Logger LOGGER =
            Logger.getLogger(DefaultEnvironmentCell.class.getName());

    private Map<String, LightNode> globalLights = new LinkedHashMap<String, LightNode>();
    private List<MapReceivedListener> listeners = new CopyOnWriteArrayList<MapReceivedListener>();
    private Skybox skybox = null;
    
    @UsesCellComponent
    private SharedStateComponent ssc;
    private SharedMapCli stateMap = null;
    private SharedMapCli sharedLightMap = null;
    private SharedMapCli skyboxMap = null;
    private SharedListener listener = null;
    public DefaultEnvironmentCell(CellID cellID, CellCache cellCache) {
        super (cellID, cellCache);
        
        listener = new SharedListener();
        LOGGER.fine("Creating default environment cell");
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        
        switch(status) {
            case ACTIVE:
                //retrieve shared maps
                if (increasing) {
                    new Thread(new Runnable() {

                        public void run() {
                            LOGGER.fine("Acquiring maps for Environment Cell!");
                            stateMap = ssc.get("state");
                            sharedLightMap = ssc.get("lights");
                            skyboxMap = ssc.get("skyboxes");

                            //assign listener to shared maps
                            stateMap.addSharedMapListener(listener);
                            sharedLightMap.addSharedMapListener(listener);
                            skyboxMap.addSharedMapListener(listener);
                            
                            //handle initial states
                            handleState(stateMap);
                            handleLights(sharedLightMap);
                            handleSkyboxes(stateMap.getString("active"),skyboxMap);
                            fireMapReceived(sharedLightMap);
                            LOGGER.fine("Maps acquired!");
                        }
                    }).start();
                }
                
                break;
            case DISK:
                break;
        }
      
    }
    
    @Override
    public CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            LOGGER.fine("Creating default renderer!");
            return new DefaultEnvironmentRenderer(this);
        }

        return super.getCellRenderer(rendererType);
    }
    
    private void handleState(SharedMapCli map) {
        //handle existing state from a shared state component
        if(!map.containsKey("active")) {
            map.put("active", SharedString.valueOf("default"));
        } else {
            // not do anything?
        }
    }
    
    private void handleLights(SharedMapCli map) {
//        for(Map.Entry<String, SharedData> data: map.entrySet()) {
//            logger.warning("Loading light from state: "+data.getKey());
//            SharedDirectionLight state = (SharedDirectionLight)data.getValue();
//            
//            DirectionalLight light = new DirectionalLight();
//            LightNode lightNode = new LightNode();
//            
//            light.setAmbient(state.getAmbient());
//            light.setDiffuse(state.getDiffuse());
//            light.setSpecular(state.getSpecular());
//            light.setDirection(state.getDirection());
//            lightNode.setLight(light);
//            lightNode.setLocalTranslation(state.getTranslation());
//            globalLights.put(data.getKey(), lightNode);          
//            
//        }
    }
    
    private void handleSkyboxes(String activeName, SharedMapCli map) {
        
        if(map == null || activeName == null)
            return;
        
        if(!map.containsKey("default") && activeName.equals("default")) {
            map.put("default", new SharedSkybox("default",
                                                "Default Skybox",
                                                "wla://defaultenvironment/skybox1/1.jpg",
                                                "wla://defaultenvironment/skybox1/3.jpg",
                                                "wla://defaultenvironment/skybox1/2.jpg",
                                                "wla://defaultenvironment/skybox1/4.jpg",
                                                "wla://defaultenvironment/skybox1/6.jpg",
                                                "wla://defaultenvironment/skybox1/5.jpg"));
            return;
        }
        
        if(!map.containsKey(activeName)) {
            LOGGER.warning("Skybox map does not contain: "+activeName+""
                    + ". Defaulting.");
            
            SharedSkybox defaultSky = (SharedSkybox)map.get("default");
            updateSkybox(defaultSky.getNorth(),
                         defaultSky.getSouth(),
                         defaultSky.getEast(),
                         defaultSky.getWest(),
                         defaultSky.getUp(),
                         defaultSky.getDown());
        } else {
            LOGGER.fine("Activating skybox: "+activeName);
            SharedSkybox ssbox = (SharedSkybox)map.get(activeName);

            updateSkybox(ssbox.getNorth(),
                         ssbox.getSouth(),
                         ssbox.getEast(),
                         ssbox.getWest(),
                         ssbox.getUp(),
                         ssbox.getDown());
        }

    }
    
    public Skybox getSkybox() {
        if(skybox == null) {
            this.getRenderer().getSkybox();
        }
        return skybox;
    }
    
    public Collection<LightNode> getGlobalLights() {
        synchronized(globalLights) {
            if(globalLights == null)
                return null;
        
            return globalLights.values();
        }
    }
    
    /**
     * Get the map containing lights in the system.
     * @return 
     */
    public Map<String, LightNode> getLightMap() {
        return globalLights;
    }
    
    public void addLight(String key, LightNode light) {
        globalLights.put(key, light);
    }
    
    public void removeLight(String key) {
        globalLights.remove(key);        
    }
    
    
    private DefaultEnvironmentRenderer getRenderer() {
        return (DefaultEnvironmentRenderer)getCellRenderer(RendererType.RENDERER_JME);
    }
    
    
   public void addMapReceivedListener(MapReceivedListener l) {
       listeners.add(l);
   }
   
   public void removeMapReceivedListener(MapReceivedListener l) {
       listeners.remove(l);
   }
   
   public void fireMapReceived(SharedMapCli map) {
        LOGGER.fine("Firing map received!");
        for(MapReceivedListener l: listeners) {
            l.mapReceived(map);
        }
   }
    
   public SharedMapCli getSkyboxMap() {
       return skyboxMap;
   }
   
   public SharedMapCli getSharedLightMap() {
       return sharedLightMap;
   }
   

   public String getActiveSkyboxName() {
       return ((SharedString)stateMap.get("active")).getValue();
   }
   
    private void updateSkybox(String north,
                              String south,
                              String east,
                              String west,
                              String up,
                              String down) {
        final Skybox skybox = buildSkybox(north, south, east, west, up, down);
        final DefaultEnvironmentRenderer renderer =
                (DefaultEnvironmentRenderer)getCellRenderer(RendererType.RENDERER_JME);

        SceneWorker.addWorker(new WorkCommit() {

            public void commit() {
                renderer.updateSkybox(skybox);
            }
        });
    }
    
    private Skybox buildSkybox(String northURI,
                               String southURI,
                               String eastURI,
                               String westURI,
                               String upURI,
                               String downURI) {
        Skybox skybox = new Skybox("skybox", 1000, 1000, 1000);
        try {
//            Vector3f position = null;
//            Cell cell = ClientContextJME.getViewManager().getPrimaryViewCell();
//            
//          
//            CellTransform transform = cell.getLocalTransform();//getLocalCellTransform();
//            position = transform.getTranslation(null);
//            
//            //Unsure if we do this for the skybox or for the entire environment cell.
//            //Let's do it for the skybox for now, and decide later.
//            skybox.setLocalTranslation(position);
            
            LOGGER.fine("Acquiring URLs");
            final URL northURL = AssetUtils.getAssetURL(northURI);
            final URL southURL = AssetUtils.getAssetURL(southURI);
            final URL eastURL = AssetUtils.getAssetURL(eastURI);
            final URL westURL = AssetUtils.getAssetURL(westURI);
            final URL downURL = AssetUtils.getAssetURL(downURI);
            final URL upURL = AssetUtils.getAssetURL(upURI);
            
            LOGGER.fine("URLs acquired. Building textures.");
            Texture north = TextureManager.loadTexture(northURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture south = TextureManager.loadTexture(southURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture east = TextureManager.loadTexture(eastURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture west = TextureManager.loadTexture(westURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture up = TextureManager.loadTexture(upURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture down = TextureManager.loadTexture(downURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);                        
         
            LOGGER.fine("Textures built. Setting fields.");
            skybox.setTexture(Skybox.Face.North, north);
            skybox.setTexture(Skybox.Face.West, west);
            skybox.setTexture(Skybox.Face.South, south);
            skybox.setTexture(Skybox.Face.East, east);
            skybox.setTexture(Skybox.Face.Up, up);
            skybox.setTexture(Skybox.Face.Down, down);

            LOGGER.fine("Fields set. Skybox finished.");

        } catch (MalformedURLException ex) {
            Logger.getLogger(SkyboxEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return skybox;
    }
   
   
    
    public void updateActiveSkyboxName(String name) {
        stateMap.put("active", SharedString.valueOf(name));
        //The active skybox will take effect next time a change has been made to
        //the Skybox map
    }
    
    class SharedListener implements SharedMapListenerCli {

        public void propertyChanged(SharedMapEventCli event) {
            SharedMapCli map = event.getMap();
            if(map.getName().equals("state")) {
                //handle change in state here.
//                logger.warning("ACTIVE SKYBOX CHANGED TO: "+event.getNewValue().toString());
            } else if(map.getName().equals("lights")) {
                String key = event.getPropertyName();
                SharedDirectionLight light = (SharedDirectionLight)event.getNewValue();
                
                
                
                //CASE 1: edited light already exists in global lights 
                if(globalLights.containsKey(key)) {
                    LightNode oldLightNode = globalLights.get(key);
                    LightNode freshLightNode = buildLightFromState(light);
                    
                    getRenderer().updateLight(oldLightNode, freshLightNode);
                    LOGGER.fine("UPDATING LIGHT: "+key);
                //CASE 2: edited light does not exist in global lights
                } else {
                    LOGGER.fine("ADDING LIGHT: "+key);
                    LightNode freshLight = buildLightFromState(light);
                    globalLights.put(key, freshLight);
                    getRenderer().addLight(key, freshLight);
                    //needs to be added anew.
                }
                
                //CASE 3: there are global lights that don't exist in shared map
                //hint #1: is the global lights length greater than the shared map?
                if(globalLights.size()  > sharedLightMap.size()) {
                    String lightToAdd = findALightToAdd();
                    if(lightToAdd != null) {
                       LightNode node = globalLights.get(lightToAdd);
                       LOGGER.fine("SHARING LIGHT: "+key);
                       sharedLightMap.put(lightToAdd,buildStateFromLight(node)); 
                    }
                }
                
//                getRenderer().addLight(key, buildLightFromState(light));
            } else if(map.getName().equals("skyboxes")) {
                LOGGER.fine("Skybox sync request!");
                handleSkyboxes(stateMap.getString("active"),map);
            }
           
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
        
        private SharedDirectionLight buildStateFromLight(LightNode light) {
            
            
            return new SharedDirectionLight(light.getLight().getAmbient(),
                                            light.getLight().getDiffuse(),
                                            light.getLight().getSpecular(),
                                            light.getLocalTranslation(),
                    ((DirectionalLight)light.getLight()).getDirection(),
                                            false);
            
            
        }
        
        private String  findALightToAdd() {
            
            //for every light in global lights...
            for(String key: globalLights.keySet()) {
                //if the light doesn't exist in the shared map
                if(!sharedLightMap.containsKey(key)) {
//                    LightNode node = globalLights.get(key);
                    //return that name of that light
                    return key;
                    
                }
                //otherwise keep going...
            }
            //if no lights have been returned, return null;
            return null;
        }
      
    }
    
    public static interface MapReceivedListener {
        public void mapReceived(SharedMapCli map);
    }
}
