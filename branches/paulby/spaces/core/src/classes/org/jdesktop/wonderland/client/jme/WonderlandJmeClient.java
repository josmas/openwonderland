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

import com.jme.app.BaseGame;
import com.jme.app.FixedFramerateGame;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.input.AbsoluteMouse;
import com.jme.input.ChaseCamera;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.MouseInputAction;
import com.jme.input.thirdperson.ThirdPersonMouseLook;
import com.jme.intersection.BoundingPickResults;
import com.jme.intersection.PickResults;
import com.jme.light.DirectionalLight;
import com.jme.math.FastMath;
import com.jme.math.Ray;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Skybox;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.system.JmeException;
import com.jme.util.TextureManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.datamgr.Asset;
import org.jdesktop.wonderland.client.datamgr.AssetManager;
import org.jdesktop.wonderland.client.datamgr.Repository;
import org.jdesktop.wonderland.client.jme.WonderlandJmeClient.PendingModuleAction.Action;
import org.jdesktop.wonderland.common.AssetType;

/**
 *
 * @author paulby
 */
public class WonderlandJmeClient extends FixedFramerateGame implements PluginAccessor {
    private boolean fullscreen;
    private int freq;
    private int depth;
    private int height;
    private int width;

    private Logger logger = Logger.getLogger(WonderlandJmeClient.class.getName());
    
    private RenderInfo renderInfo;
    
    private ArrayList<RenderModule> modules = new ArrayList();
    private ArrayList<PendingModuleAction> pendingActions = new ArrayList();
    
    private ToolFrame toolFrame;
    
    private boolean isRunning = false;
    
    private ModuleManager moduleManager = new ModuleManager(this);
    
    @Override
    protected void update(float interpolation) {
        // update the time to get the framerate
        renderInfo.updateTimer();
        
        synchronized(pendingActions) {
            if (pendingActions.size()!=0) {
                for(PendingModuleAction pending : pendingActions) {
                    RenderModule module = pending.getModule();
                    switch(pending.getAction()) {
                        case ADD :
                            module.init(renderInfo);
                            module.setActive(true, renderInfo);
                            modules.add(module);
                            renderInfo.getRoot().updateGeometricState(renderInfo.getTimer().getTime(), true);
                            renderInfo.getRoot().updateRenderState();
                            break;
                        case REMOVE :
                            modules.remove(module);
                            module.setActive(false, renderInfo);
                            
                            // Not sure we need to do both of these for remove
                            renderInfo.getRoot().updateGeometricState(renderInfo.getTimer().getTime(), true);
                            renderInfo.getRoot().updateRenderState();
                            break;
                        default :
                            throw new RuntimeException("Unknown Pending Action Type");
                    }
                }
            }
            pendingActions.clear();
        }
        
        interpolation = renderInfo.getTimer().getTimePerFrame();

        for(RenderModule mod : modules)
            mod.update(renderInfo, interpolation);
        
        // if escape was pressed, we exit
        if (KeyBindingManager.getKeyBindingManager().isValidCommand("exit")) {
            finished = true;
        }
        
        renderInfo.getRoot().updateGeometricState(interpolation, true);
    }

    @Override
    protected void render(float interpolation) {
        // Clear the screen
        display.getRenderer().clearBuffers();
        display.getRenderer().clearStatistics();
        display.getRenderer().draw(renderInfo.getRoot());
        
        for(RenderModule mod : modules)
            mod.render(renderInfo, interpolation);
    }

    @Override
    protected void initSystem() {
        Camera cam=null;
        
        // store the properties information
        width = properties.getWidth();
        height = properties.getHeight();
        depth = properties.getDepth();
        freq = properties.getFreq();
        fullscreen = properties.getFullscreen();
        
        try {
            display = DisplaySystem.getDisplaySystem(properties.getRenderer());
            display.createWindow(width, height, depth, freq, fullscreen);
            display.setTitle("Project Wonderland");

            cam = display.getRenderer().createCamera(width, height);
        } catch (JmeException e) {
            logger.log(Level.SEVERE, "Could not create displaySystem", e);
            System.exit(1);
        }

        // set the background to blafinishedck
        display.getRenderer().setBackgroundColor(ColorRGBA.black.clone());

        // initialize the camera
        cam.setFrustumPerspective(45.0f, (float) width / (float) height, 1,
                5000);
        
        /** Signal that we've changed our camera's location/frustum. */
        cam.update();

        display.getRenderer().setCamera(cam);

        KeyBindingManager.getKeyBindingManager().set("exit",
                KeyInput.KEY_ESCAPE);
        
        renderInfo = new RenderInfo();
        renderInfo.setCamera(cam);
        renderInfo.setDisplay(display);
        
        toolFrame = new ToolFrame(this);
        toolFrame.setLocation(width+10, 0);
        toolFrame.setVisible(true);
        
        // Gather rendering stats
        display.getRenderer().enableStatistics(true);

        // Enable hardware cursor
        MouseInput.get().setCursorVisible(true);
    }

    @Override
    protected void initGame() {
        display.setTitle("Wonderland Client Test");
        
        renderInfo.setRoot(new Node("Root"));
        /** Create a ZBuffer to display pixels closest to the camera above farther ones.  */
        ZBufferState buf = display.getRenderer().createZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.CF_LEQUAL);
        renderInfo.getRoot().setRenderState(buf);
        
        //Time for a little optimization. We don't need to render back face triangles, so lets
        //not. This will give us a performance boost for very little effort.
        CullState cs = display.getRenderer().createCullState();
        cs.setCullMode(CullState.CS_BACK);
        renderInfo.getRoot().setRenderState(cs);
     
        moduleManager.addModule(new WorldModule(), true);
        
        moduleManager.addModule(new LightModule(), true);
        
        moduleManager.addModule(new SkyBoxModule(), true);
        
        AvatarModule avatarModule = new AvatarModule();
        moduleManager.addModule(avatarModule, true);
                
        moduleManager.addModule(new ChaseCameraModule(avatarModule), true);
        moduleManager.addModule(new CursorModule(avatarModule), true);
        
        moduleManager.addModule(new HUDModule(), true);
        
        moduleManager.addModule(new CellModule(), false);
        
        moduleManager.addModule(new PortalModule(), true);
        
        // update the scene graph for rendering
        renderInfo.getRoot().updateGeometricState(0.0f, true);
        renderInfo.getRoot().updateRenderState();
        
        // Test case for problematic image format
//        try {
//        InputStream in = new FileInputStream("/Users/paulby/src/java.net/jme/trunk/jme/src/jmetest/data/texture/Decal.PNG");
//        
//        TextureManager.loadImage("png", in, false);
//        } catch(Exception e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
        
        isRunning = true;
    }
    
    public void addModule(RenderModule module) {
        if (isRunning) {
            synchronized(pendingActions) {
                pendingActions.add(new PendingModuleAction(Action.ADD, module));
            }
        } else {
            module.init(renderInfo);
            module.setActive(true, renderInfo);
            modules.add(module);
        }
    }

    public void removeModule(RenderModule module) {
        if (isRunning) {
            synchronized(pendingActions) {
                pendingActions.add(new PendingModuleAction(Action.REMOVE, module));
            }
        } else {
            module.setActive(false, renderInfo);
            modules.remove(module);
        }
    }
    
    @Override
    protected void reinit() {
        display.recreateWindow(width, height, depth, freq, fullscreen);
    }

    @Override
    protected void cleanup() {
        toolFrame.setVisible(false);
        toolFrame.dispose();
    }
    
    public void setFPS(int fps) {
        setFrameRate(fps);
    }
    
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public static void main(String args[]) {
//        try {
//            URL u = new URL("wlasset://server/test.a#/base/url");
//            u.openStream();
//        } catch (IOException ex) {
//            Logger.getLogger(WonderlandJmeClient.class.getName()).log(Level.SEVERE, null, ex);
//        } 
        WonderlandJmeClient app = new WonderlandJmeClient();
        app.setDialogBehaviour(FIRSTRUN_OR_NOCONFIGFILE_SHOW_PROPS_DIALOG, 
                WonderlandJmeClient.class.getClassLoader().getResource(
                        "wonderland-logo.png"));
        app.start();
        
    }
    
    class WorldModule extends RenderModule {

        public void init(RenderInfo info) {
        }

        public void update(RenderInfo info, float interpolation) {
        }

        public void render(RenderInfo info, float interpolation) {
        }

        @Override
        public void setActiveImpl(boolean active, RenderInfo info) {
        }
        
    }
    
    class LightModule extends RenderModule {
        private LightState lightState;

        public void init(RenderInfo info) {
            DirectionalLight light = new DirectionalLight();
            light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
            light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
            light.setDirection(new Vector3f(1,-1,0));
            light.setEnabled(true);

            DirectionalLight light2 = new DirectionalLight();
            light2.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
            light2.setDirection(new Vector3f(-1,-1,0));
            light2.setEnabled(true);
            
              /** Attach the light to a lightState and the lightState to rootNode. */
            lightState = info.getDisplay().getRenderer().createLightState();
            lightState.setEnabled(true);
            lightState.attach(light);
            lightState.attach(light2);
        }

        public void update(RenderInfo info, float interpolation) {
            // Nothing to do
        }

        public void render(RenderInfo info, float interpolation) {
            // Nothing to do
        }

        @Override
        public void setActiveImpl(boolean active, RenderInfo info) {
            if (active)
                info.getRoot().setRenderState(lightState);
            else
                System.out.println("Disabling lights not implemented");
        }
    }
    
    class SkyBoxModule extends RenderModule {

        private Skybox skybox;
        private Texture up;
        
        public void init(RenderInfo info) {
            skybox = new Skybox("skybox", 10, 10, 10);

            Texture north = TextureManager.loadTexture(
                getClass().getResource(
                "/org/jdesktop/wonderland/client/resources/jme/north.jpg"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
            Texture south = TextureManager.loadTexture(
                getClass().getResource(
                "/org/jdesktop/wonderland/client/resources/jme/south.jpg"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
            Texture east = TextureManager.loadTexture(
                getClass().getResource(
                "/org/jdesktop/wonderland/client/resources/jme/east.jpg"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
            Texture west = TextureManager.loadTexture(
                getClass().getResource(
                "/org/jdesktop/wonderland/client/resources/jme/west.jpg"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
            up = TextureManager.loadTexture(
                getClass().getResource(
                "/org/jdesktop/wonderland/client/resources/jme/top.jpg"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
            up.setWrap(Texture.WM_WRAP_S_WRAP_T);
            up.setTranslation(new Vector3f());
            
            Texture down = TextureManager.loadTexture(
                getClass().getResource(
                "/org/jdesktop/wonderland/client/resources/jme/bottom.jpg"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);

            skybox.setTexture(Skybox.NORTH, north);
            skybox.setTexture(Skybox.WEST, west);
            skybox.setTexture(Skybox.SOUTH, south);
            skybox.setTexture(Skybox.EAST, east);
            skybox.setTexture(Skybox.UP, up);
            skybox.setTexture(Skybox.DOWN, down);
            skybox.preloadTextures();
        }

        public void update(RenderInfo info, float interpolation) {
            
            // Update texture transform the make the clouds drift
            up.getTranslation().y += 0.3f * interpolation;
            if(up.getTranslation().y > 1) {
                up.getTranslation().y = 0;
            }
        
            // Keep skybox centered around user            
            skybox.setLocalTranslation(info.getCamera().getLocation());
        }

        public void render(RenderInfo info, float interpolation) {
            // Nothing to do
        }

        @Override
        public void setActiveImpl(boolean active, RenderInfo info) {
            if (active)
                info.getRoot().attachChild(skybox);        
            else
                info.getRoot().detachChild(skybox);        
        }
     
    }
    
    class AvatarModule extends RenderModule {

        private InputHandler input;
        private Node avatarRoot;

        public void init(RenderInfo info) {
//            float HEIGHT = 1.7f;
            
            try {
                //box stand in
//                Box b = new Box("box", new Vector3f(0f,HEIGHT/2f,0f), 0.65f, HEIGHT, 0.4f);
                Repository repository = new Repository(new URL("http://192.18.37.42/"));

                Asset asset = AssetManager.getAssetManager().getAsset(AssetType.FILE, repository, "mannikin.jme", null);
                AssetManager.getAssetManager().waitForAsset(asset);
                // new URL("file:///home/paulby/local-code/java.net/wonderland/branches/bringup/core/mannikin.jme")
                URL url = asset.getLocalCacheFile().toURI().toURL();
                
                Node model = Loaders.loadJMEBinary(url, new Vector3f());
                model.setModelBound(new BoundingBox());
                model.updateModelBound();
                
//                b.setModelBound(new BoundingBox());
//                b.updateModelBound();

                avatarRoot = new Node("Avatar Node");
                avatarRoot.setLocalTranslation(new Vector3f(0, 0, 0));
                avatarRoot.attachChild(model);
                avatarRoot.updateWorldBound();
                
                input = new WonderlandDefaultHandler(avatarRoot, properties.getRenderer());
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        public void update(RenderInfo info, float interpolation) {
            input.update(interpolation);
        }
        
        /**
         * Returns the root node of the avatar graph
         */
        public Node getAvatarRoot() {
            return avatarRoot;
        }
        
        /**
         * Return the input handler for this avatar
         * 
         * @return
         */
        public InputHandler getInputHandler() {
            return input;
        }

        public void render(RenderInfo info, float interpolation) {
            // Nothing to do
        }

        @Override
        public void setActiveImpl(boolean active, RenderInfo info) {
            if (active)
                info.getRoot().attachChild(avatarRoot);
            else
                info.getRoot().detachChild(avatarRoot);
        }
        
    }
    
    class ChaseCameraModule extends RenderModule {

        private ChaseCamera chaser;
        private AvatarModule avatarModule;
        
        public ChaseCameraModule(AvatarModule avatarModule) {
            this.avatarModule = avatarModule;
        }
        
        public void init(RenderInfo info) {
            Vector3f targetOffset = new Vector3f();
//            targetOffset.y = ((BoundingBox) avatarModule.getAvatarRoot().getWorldBound()).yExtent * 1.5f;
            targetOffset.y = 2.8f;
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put(ThirdPersonMouseLook.PROP_MAXROLLOUT, "6");
            props.put(ThirdPersonMouseLook.PROP_MINROLLOUT, "3");
            props.put(ThirdPersonMouseLook.PROP_MAXASCENT, ""+45 * FastMath.DEG_TO_RAD);
            props.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(5, 0, 30 * FastMath.DEG_TO_RAD));
            props.put(ChaseCamera.PROP_TARGETOFFSET, targetOffset);
            chaser = new ChaseCamera(info.getCamera(), avatarModule.getAvatarRoot(), props);
            chaser.setMaxDistance(8);
            chaser.setMinDistance(2);
        }

        public void update(RenderInfo info, float interpolation) {
            chaser.update(interpolation);
            info.getCamera().update();
        }

        public void render(RenderInfo info, float interpolation) {
            // Nothing to do
        }

        @Override
        public void setActiveImpl(boolean active, RenderInfo info) {
            // nothing to do
        }
        
    }
    
    public class CursorModule extends RenderModule {

        private BoundingPickResults pr;
        private AbsoluteMouse am;
        private InputHandler input;
        
        public CursorModule(AvatarModule avatarModule) {
            input = avatarModule.getInputHandler();
        }
        
        public void init(RenderInfo info) {
            pr = new BoundingPickResults();
            display = DisplaySystem.getDisplaySystem();

            // Create a new mouse. Restrict its movements to the display screen.
            am = new AbsoluteMouse("The Mouse", display.getWidth(), display
                    .getHeight());

            // Get a picture for my mouse.
            TextureState ts = display.getRenderer().createTextureState();
            URL cursorLoc = WonderlandJmeClient.class.getClassLoader().getResource(
                    "org/jdesktop/wonderland/client/resources/cursor1.png" );
            Texture t = TextureManager.loadTexture(cursorLoc, Texture.MM_LINEAR,
                    Texture.FM_LINEAR);
            ts.setTexture(t);
            am.setRenderState(ts);

            // Make the mouse's background blend with what's already there
            AlphaState as = display.getRenderer().createAlphaState();
            as.setBlendEnabled(true);
            as.setSrcFunction(AlphaState.SB_SRC_ALPHA);
            as.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
            as.setTestEnabled(true);
            as.setTestFunction(AlphaState.TF_GREATER);
            am.setRenderState(as);

            // Get the mouse input device and assign it to the AbsoluteMouse
            // Move the mouse to the middle of the screen to start with
            am.setLocalTranslation(new Vector3f(display.getWidth() / 2, display
                    .getHeight() / 2, 0));
            // Assign the mouse to an input handler
            am.registerWithInputHandler( input );
            
            input.addAction(new MousePick(renderInfo.getCamera(), 
                    renderInfo.getRoot(),
                    am));
            
            info.getRoot().attachChild(am);
        }

        public void update(RenderInfo info, float interpolation) {
            // do nothing
        }

        public void render(RenderInfo info, float interpolation) {
            // do nothing
         }

        @Override
        public void setActiveImpl(boolean active, RenderInfo info) {
            // nothing to do
        }
        
    }
    
    class MousePick extends MouseInputAction {

        private Camera camera;
        private Spatial scene;
        private AbsoluteMouse absMouse;
        
        private int hits;
        private String hitItems;
        
        public MousePick(Camera camera, Spatial scene, AbsoluteMouse absMouse) {
            this.camera = camera;
            this.scene = scene;
            this.absMouse = absMouse;
        }
        
        public void performAction(InputActionEvent evt) {
            if( MouseInput.get().isButtonDown(0)) {
                PickResults results = new BoundingPickResults();
                Vector2f screenPos = new Vector2f();
                
                // Get the position that the mouse is pointing to
                screenPos.set(absMouse.getHotSpotPosition().x, absMouse.getHotSpotPosition().y);
                // Get the world location of that X,Y value
                Vector3f worldCoords = display.getWorldCoordinates(screenPos, 1.0f);
                // Create a ray starting from the camera, and going in the direction
                // of the mouse's location
                Ray mouseRay = new Ray(camera.getLocation(), worldCoords
                                .subtractLocal(camera.getLocation()));
                mouseRay.getDirection().normalizeLocal();
                results.clear();

                scene.calculatePick(mouseRay, results);
                        
                hits += results.getNumber();
                hitItems = "";
                if(results.getNumber() > 0) {
                    for(int i = 0; i < results.getNumber(); i++) {
                        hitItems += results.getPickData(i).getTargetMesh().getParentGeom().getName() + " " + results.getPickData(i).getDistance();
                        if(i != results.getNumber() -1) {
                            hitItems += ", ";
                        }
                    }
                }
                results.clear();
                System.out.println("Ray "+mouseRay);
                System.out.println("Hits: " + hits + " : " + hitItems);
            }
        }
        
    }
    
    static class PendingModuleAction {
        private RenderModule module;
        
        public enum Action {ADD, REMOVE };

        private Action action;
        
        public PendingModuleAction(Action action, RenderModule module) {
            this.action = action;
            this.module = module;
        }
        
        public Action getAction() {
            return action;
        }
        
        public RenderModule getModule() {
            return module;
        }
    }

}
