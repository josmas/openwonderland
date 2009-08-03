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
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import com.jme.bounding.BoundingSphere;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveSource;
import org.jdesktop.wonderland.client.jme.cellrenderer.*;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Geometry;
import com.jme.scene.shape.Box;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.resource.ResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;
import imi.character.CharacterAnimationProcessor;
import imi.character.CharacterController;
import imi.character.CharacterMotionListener;
import imi.character.CharacterParams;
import imi.character.CharacterProcessor;
import imi.character.MaleAvatarParams;
import imi.character.avatar.Avatar;
import imi.character.avatar.AvatarController;
import imi.character.statemachine.GameContextListener;
import imi.character.statemachine.GameState;
import imi.character.statemachine.corestates.CycleActionState;
import imi.collision.CollisionController;
import imi.input.DefaultCharacterControls;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonModelInstance;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;
import javolution.util.FastList;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.CollisionSystem;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.MovableAvatarComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveListener;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.cell.view.AvatarCell.AvatarActionTrigger;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.utils.graphics.GraphicsUtils;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.Math3DUtils;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.avatarbase.client.cell.AvatarConfigComponent;
import org.jdesktop.wonderland.modules.avatarbase.client.cell.AvatarConfigComponent.AvatarConfigChangeListener;
import org.jdesktop.wonderland.modules.avatarbase.client.loader.AvatarLoaderRegistry;
import org.jdesktop.wonderland.modules.avatarbase.client.loader.spi.AvatarLoaderFactorySPI;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.AvatarConfigInfo;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigMessage;


/**
 * Cell renderer for Avatars, using the IMI avatar system.
 * 
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class AvatarImiJME extends BasicRenderer implements AvatarActionTrigger {

    private WlAvatarCharacter pendingAvatar = null;
    private WlAvatarCharacter avatarCharacter = null;

    private boolean selectedForInput = false;

//    private AvatarRendererChangeRequestEvent.AvatarQuality quality = AvatarRendererChangeRequestEvent.AvatarQuality.High;
    private CharacterMotionListener characterMotionListener;
    private GameContextListener gameContextListener;
    private int currentTrigger = -1;
    private boolean currentPressed = false;
    private float positionMinDistanceForPull = 0.1f;
    private float positionMaxDistanceForPull = 3.0f;
    private String username;
    private DefaultCharacterControls controlScheme = null;
    private NameTagNode nameTag;

    private ProcessorComponent cameraChainedProcessor = null;  // The processor to which the camera is chained

    private CellMoveListener cellMoveListener = null;

    private Entity rootEntity = null;

    private CollisionController collisionController = null;
    private CollisionChangeRequestListener collisionChangeRequestListener;
    
    /** Collection of listeners **/
    private final List<WeakReference<AvatarChangedListener>> avatarChangedListeners
            = new FastList();

    public AvatarImiJME(Cell cell) {
        super(cell);
        assert (cell != null);
        final Cell c = cell;

        // Listen for avatar configuration changes.
        AvatarConfigComponent comp = cell.getComponent(AvatarConfigComponent.class);
        comp.addAvatarConfigChangeListener(new AvatarChangeListener());

        // XXX NPC HACK XXX
        if (cell instanceof AvatarCell)
            username = ((AvatarCell) cell).getIdentity().getUsername();
        else
            username = "npc"; // HACK !

        characterMotionListener = new CharacterMotionListener() {
            Vector3f prevTrans;
            PMatrix prevRot;
            
            public void transformUpdate(Vector3f translation, PMatrix rotation) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Transform update: translation: prev: " +
                            prevTrans + " cur: " + translation +
                            " rotation: prev: " + prevRot + " cur: " +
                            rotation);
                }
                
                if (prevTrans == null || !Math3DUtils.epsilonEquals(prevTrans, translation, 0.001f) ||
                    prevRot == null || !prevRot.epsilonEquals(rotation, 0.001f))
                {
                    ((MovableAvatarComponent) c.getComponent(MovableComponent.class)).localMoveRequest(new CellTransform(rotation.getRotation(), translation));

                    prevTrans = translation.clone();
                    prevRot = new PMatrix(rotation);
                }
            };   
        };

        // This info will be sent to the other clients to animate the avatar
        gameContextListener = new GameContextListener() {

            public void trigger(boolean pressed, int trigger, Vector3f translation, Quaternion rotation) {
                synchronized (this) {
                    currentTrigger = trigger;
                    currentPressed = pressed;
                }
                GameState state = avatarCharacter.getContext().getCurrentState();
                String animationName=null;
                if (state instanceof CycleActionState) {
                    animationName = avatarCharacter.getContext().getState(CycleActionState.class).getAnimationName();
                }
                if (c.getComponent(MovableComponent.class)==null)
                    System.err.println("!!!! NULL MovableComponent");
                else
                    ((MovableAvatarComponent) c.getComponent(MovableComponent.class)).localMoveRequest(new CellTransform(rotation, translation), trigger, pressed, animationName, null);
            }
        };

        ClientContext.getInputManager().addGlobalEventListener(new EventClassListener() {

            private Class[] consumeClasses = new Class[]{
                AvatarRendererChangeRequestEvent.class,
                AvatarNameEvent.class
            };

            @Override
            public Class[] eventClassesToConsume() {
                return consumeClasses;
            }

            @Override
            public void commitEvent(Event event) {
                if (event instanceof AvatarNameEvent) {
                    AvatarNameEvent e = (AvatarNameEvent) event;

                    if (e.getUsername().equals(username)) {
                        if (nameTag == null) {
                            logger.warning("[AvatarImiJME] warning: setting " +
                                "avatar name when name tag is null. " + e);
                            return;
                        }

                        nameTag.setNameTag(e.getEventType(), username, 
                                           e.getUsernameAlias(),
                                           e.getForegroundColor(), e.getFont());
                    }
                } else if (event instanceof AvatarRendererChangeRequestEvent) {
                    handleAvatarRendererChangeRequest((AvatarRendererChangeRequestEvent)event);
                }
            }

            @Override
            public void computeEvent(Event evtIn) {
            }
        });

        collisionChangeRequestListener = new CollisionChangeRequestListener();
        ClientContext.getInputManager().addGlobalEventListener(collisionChangeRequestListener);

    }

    /**
     * Returns the avatar renderer for the primary view cell, or null if none
     * exists.
     *
     * @return An instance of this class that is the avatar cell renderer
     */
    public static AvatarImiJME getPrimaryAvatarRenderer() {
        // Fetch the primary view cell, make sure it is an avatar and then get
        // its cell renderer.
        ViewCell cell = ClientContextJME.getViewManager().getPrimaryViewCell();
        if (cell instanceof AvatarCell) {
            AvatarCell avatarCell = (AvatarCell) cell;
            return (AvatarImiJME) avatarCell.getCellRenderer(ClientContext.getRendererType());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        // If we are increasing to the ACTIVE state, then turn everything on.
        // Add the listeners to the avatar Cell and set the avatar character
        if (status == CellStatus.ACTIVE && increasing == true) {

            if (cellMoveListener != null) {
                cell.getComponent(MovableComponent.class).removeServerCellMoveListener(cellMoveListener);
                cellMoveListener = null;
            }

            // If we have not creating the avatar yet, then look for the config
            // component on the cell. Fetch the avatar configuration.
            if (avatarCharacter == null) {
                AvatarConfigComponent configComp = cell.getComponent(AvatarConfigComponent.class);
                AvatarConfigInfo avatarConfigInfo = null;
                if (configComp != null) {
                    avatarConfigInfo = configComp.getAvatarConfigInfo();
                }
                pendingAvatar = loadAvatar(avatarConfigInfo);
            }
            else {
                // Otherwise remove the existing avatar from the world
                ClientContextJME.getWorldManager().removeEntity(avatarCharacter);
                pendingAvatar = null;
            }

            // Go ahead and change the avatar
            changeAvatar(pendingAvatar);

            if (cellMoveListener == null) {
                cellMoveListener = new CellMoveListener() {
                    public void cellMoved(CellTransform transform, CellMoveSource source) {
                        if (source == CellMoveSource.REMOTE) {
                            //                            System.err.println("REMOTE MOVE "+transform.getTranslation(null));
                            if (avatarCharacter != null) {
                                if (avatarCharacter.getModelInst() == null) {  // Extra debug check
                                    logger.severe("MODEL INST IS NULL !");
                                    Thread.dumpStack();
                                    return;
                                }
                                avatarCharacter.getModelInst().setTransform(new PTransform(transform.getRotation(null), transform.getTranslation(null), new Vector3f(1, 1, 1)));
                            }
                        }
                    }
                };
            }
            cell.getComponent(MovableComponent.class).addServerCellMoveListener(cellMoveListener);
        }
        else if (status == CellStatus.RENDERING) {
            // Only if increasing? XXX
            if (((AvatarCell) cell).isSelectedForInput())
                selectForInput(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Entity createEntity() {
        assert (rootEntity == null);
        rootEntity = new Entity("AvatarRoot");
        return rootEntity;
    }

    private void handleAvatarRendererChangeRequest(AvatarRendererChangeRequestEvent event) {
        switch (event.getQuality()) {
            case High :
                // Fetch the avatar configuration information and change to the
                // current avatar
                AvatarConfigComponent comp = cell.getComponent(AvatarConfigComponent.class);
                AvatarConfigInfo avatarConfigInfo = comp.getAvatarConfigInfo();
                changeAvatar(loadAvatar(avatarConfigInfo));
                break;
            case Medium :
                changeAvatar(loadAvatar(null));
                break;
            case Low :
                changeAvatar(loadAvatar(null));
                break;
        }
    }

    /**
     * Changes the avatar to the given avatar on the MT Game Render Thread
     */
    public void changeAvatar(final WlAvatarCharacter avatar) {
        RenderUpdater updater = new RenderUpdater() {
            public void update(Object arg0) {
                changeAvatarInternal(avatar);
            }
        };
        WorldManager wm = ClientContextJME.getWorldManager();
        wm.addRenderUpdater(updater, null);
    }

    /**
     * Change the current avatar to the newAvatar
     * XXX Does this need to happen in a render thread? XXX
     * @param newAvatar
     */
    private void changeAvatarInternal(WlAvatarCharacter newAvatar) {
        
        synchronized(this) {
            WorldManager wm = ClientContextJME.getWorldManager();

            LoadingInfo.startedLoading(cell.getCellID(), newAvatar.getName());

            PMatrix currentLocation = null;

            if (avatarCharacter != null) {
                currentLocation = avatarCharacter.getModelInst().getTransform().getWorldMatrix(true);
                rootEntity.removeEntity(avatarCharacter);
                if (nameTag!=null) { // THis must be done after the entity is no longer live
                    avatarCharacter.getJScene().getExternalKidsRoot().detachChild(nameTag);
                }

                enableInputListeners(false);
                avatarCharacter.destroy();
            }

            avatarCharacter = newAvatar;

            if (newAvatar==null)
                return;

            RenderComponent rc = (RenderComponent) avatarCharacter.getComponent(RenderComponent.class);

            if (rc != null) {
                addDefaultComponents(avatarCharacter, rc.getSceneRoot());
                avatarCharacter.removeComponent(CollisionComponent.class); // We don't want collision as we use the collision graph
            } else {
                logger.warning("NO RenderComponent for Avatar");
            }

            if (currentLocation != null && avatarCharacter.getModelInst()!=null) {
                avatarCharacter.getModelInst().setTransform(new PTransform(currentLocation));
            }

            if (nameTag!=null) {
                avatarCharacter.getJScene().getExternalKidsRoot().attachChild(nameTag);
            }

            rootEntity.addEntity(avatarCharacter);

            selectForInput(selectedForInput);
            
            // Notify listeners
            for (WeakReference<AvatarChangedListener> listenerRef : avatarChangedListeners) {
                AvatarChangedListener listener = listenerRef.get();
                if (listener != null)
                    listener.avatarChanged(avatarCharacter);
                else
                    avatarChangedListeners.remove(listenerRef);
            }
            // update the bounds if necessary
            if (avatarCharacter.getJScene() != null) {
                // Some of these ops must be done on the render thread
                ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
                    public void update(Object arg0) {
                        avatarCharacter.getPScene().submitTransformsAndGeometry(true); // Make sure the geometry is attached to the jscene
                        avatarCharacter.getJScene().setModelBound(new BoundingSphere()); // No more null bounding volumes
                        avatarCharacter.getJScene().updateModelBound();
                        avatarCharacter.getJScene().updateWorldBound();
                    }
                }, null);
            }
            LoadingInfo.finishedLoading(cell.getCellID(), newAvatar.getName());
        }
    }

    @Override
    protected void addRenderState(Node node) {
        // Nothing to do
    }

    @Override
    public void cellTransformUpdate(CellTransform transform) {
        // Don't call super, we don't use a MoveProcessor for avatars

        if (!selectedForInput && avatarCharacter != null && avatarCharacter.getContext().getController().getModelInstance()!=null ) {
            // If the user is being steered by AI, do not mess it up
            // (objects that the AI is dealing with gota be synced)
//            System.err.println("Steering "+avatarCharacter.getContext().getSteering().isEnabled()+"  "+avatarCharacter.getContext().getSteering().getCurrentTask());
            if (avatarCharacter.getContext().getBehaviorManager().isEnabled()
                    && avatarCharacter.getContext().getBehaviorManager().getCurrentTask() != null) {
                System.err.println("Avatar steering !");
            } else {
                Vector3f pos = transform.getTranslation(null);
                Vector3f dir = new Vector3f(0, 0, -1);
                transform.getRotation(null).multLocal(dir);
//                System.err.println("Setting pos "+pos);
                PMatrix local = avatarCharacter.getContext().getController().getModelInstance().getTransform().getLocalMatrix(true);
                final Vector3f currentPosition = local.getTranslation();
                float currentDistance = currentPosition.distance(pos);
                if (currentDistance < positionMaxDistanceForPull) {
                    pos.set(currentPosition);
                }

            }
        }
    }

    public void loadAndChangeAvatar(final AvatarConfigInfo avatarConfigInfo) {
        logger.warning("Loading avatar info");
        WlAvatarCharacter avatar = loadAvatar(avatarConfigInfo);
        logger.warning("Changing avatar character");
        changeAvatar(avatar);
        logger.warning("Done changing avatar character");
    }

    /**
     * Load and return an avatar given its configuration information.
     *
     * @param avatarConfigInfo The avatar configuration info
     * @return The Avatar character
     */
    private WlAvatarCharacter loadAvatar(AvatarConfigInfo avatarConfigInfo) {
        // Load the avatar configuration information, placing a loading
        // message until it is finished
        LoadingInfo.startedLoading(cell.getCellID(), username);
        try {
            if (avatarConfigInfo != null) {
                logger.warning("Loading avatar with config info url " +
                        avatarConfigInfo.getAvatarConfigURL() + " with loader " +
                        avatarConfigInfo.getLoaderFactoryClassName());
            }
            else {
                logger.warning("Loading default avatar.");
            }

            return loadAvatarInternal(avatarConfigInfo);
        } catch (java.lang.Exception excp) {
            // Loger and error and return null
            logger.log(Level.WARNING, "Failed to load avatar character for " +
                    "url " + avatarConfigInfo.getAvatarConfigURL(), excp);
            return null;
        } finally {
            LoadingInfo.finishedLoading(cell.getCellID(), username);
        }
    }

    /**
     * Load and return the avatar. To make this the current avatar changeAvatar()
     * must be called
     * 
     * @param avatarConfigURL
     * @return
     */
    private WlAvatarCharacter loadAvatarInternal(AvatarConfigInfo avatarConfigInfo)
            throws MalformedURLException, IOException {
        
        WlAvatarCharacter ret = null;
        WorldManager wm = ClientContextJME.getWorldManager();
        PMatrix origin = new PMatrix();
        CellTransform transform = cell.getLocalTransform();
        origin.setTranslation(transform.getTranslation(null));
        origin.setRotation(transform.getRotation(null));

        // Create the character
        String avatarDetail = System.getProperty("avatar.detail", "high");

        // Check to see if the system supports OpenGL 2.0. If not, then
        // always use the low-detail avatar character
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        if (rm.supportsOpenGL20() == false) {
            avatarDetail = "low";
        }

        if (avatarConfigInfo == null || avatarDetail.equalsIgnoreCase("low")) {
            CharacterParams attributes = new MaleAvatarParams(username);

            // Set the base URL
            WonderlandSession session = cell.getCellCache().getSession();
            ServerSessionManager manager = session.getSessionManager();
            String serverHostAndPort = manager.getServerNameAndPort();
            String baseURL = "wla://avatarbaseart@" + serverHostAndPort + "/";

            // Setup simple model, needs to actually have something to
            // play well with the system
            PScene simpleScene = new PScene(ClientContextJME.getWorldManager());
            simpleScene.addMeshInstance(new PPolygonMesh("PlaceholderMesh"), new PMatrix());
            attributes.setUseSimpleStaticModel(true, simpleScene);
            attributes.setBaseURL(baseURL);

            // don't add the entity to wm
            ret = new WlAvatarCharacter.WlAvatarCharacterBuilder(attributes, wm).addEntity(false).build();

            URL url = new URL(baseURL + "assets/models/collada/Avatars/StoryTeller.kmz/models/StoryTeller.wbm");
            ResourceLocator resourceLocator = new RelativeResourceLocator(url);

            ResourceLocatorTool.addThreadResourceLocator(
                    ResourceLocatorTool.TYPE_TEXTURE,
                    resourceLocator);
            Spatial placeHolder = (Spatial) BinaryImporter.getInstance().load(url);
            ResourceLocatorTool.removeThreadResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, resourceLocator);

            //checkBounds(placeHolder);
            //placeHolder.updateModelBound();
            //placeHolder.updateWorldBound();

            //System.out.println("Default Model Bounds: " + placeHolder.getWorldBound());
            //placeHolder.lockBounds();
            ret.getJScene().getExternalKidsRoot().attachChild(placeHolder);
            ret.getJScene().setExternalKidsChanged(true);
        } else {
            // If the avatar has a non-null configuration information, then
            // ask the loader factory to generate a new loader for this avatar
            String className = avatarConfigInfo.getLoaderFactoryClassName();

            logger.warning("Loading avatar with class name " + className);

            if (className == null) {
                logger.warning("No class name given for avatar configuration" +
                        " with url " + avatarConfigInfo.getAvatarConfigURL());
                return null;
            }

            // Find the avatar factory, if it does not exist, return an error
            AvatarLoaderRegistry registry = AvatarLoaderRegistry.getAvatarLoaderRegistry();
            AvatarLoaderFactorySPI factory = registry.getAvatarLoaderFactory(className);
            if (factory == null) {
                logger.warning("No avatar loader factory for the class name " +
                        className + " with url " + avatarConfigInfo.getAvatarConfigURL());
                return null;
            }

            // Ask the avatar loader to create and return an avatar character
            ret = factory.getAvatarLoader().getAvatarCharacter(avatarConfigInfo);

            logger.warning("Done loading avatar, ret="+ ret);
        }

        ret.getModelInst().getTransform().getLocalMatrix(true).set(origin);

        // XXX NPC HACK XXX
        // TODO - remove hardcoded npc support
//        if (username.equals("npc") && avatarConfigURL != null) {
//            String u = avatarConfigURL.getFile();
//            username = u.substring(u.lastIndexOf('/') + 1, u.lastIndexOf('.'));
//        }

        Node external = ret.getJScene().getExternalKidsRoot();
        ZBufferState zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        external.setRenderState(zbuf);

        NameTagComponent nameTagComp = cell.getComponent(NameTagComponent.class);
        if (nameTagComp == null) {
            nameTagComp = new NameTagComponent(cell, username, 2);
            cell.addComponent(nameTagComp);
        }
        nameTag = nameTagComp.getNameTagNode();
        external.attachChild(nameTag);
        external.setModelBound(new BoundingSphere());
        external.updateModelBound();
        external.updateGeometricState(0, true);

        // JSCENE HAS NOT CHILDREN, so this does nothing
        ret.getJScene().updateGeometricState(0, true);
        GraphicsUtils.printGraphBounds(ret.getJScene());

        //        JScene jscene = avatar.getJScene();
        //        jscene.renderToggle();      // both renderers
        //        jscene.renderToggle();      // jme renderer only
        //        jscene.setRenderPRendererMesh(true);  // Force pRenderer to be instantiated
        //        jscene.toggleRenderPRendererMesh();   // turn off mesh
        //        jscene.toggleRenderBoundingVolume();  // turn off bounds

        // Set up the collision for the avatar
        Spatial collisionGraph = new Box("AvatarCollision", new Vector3f(0f, 0.92f, 0f), 0.4f, 0.6f, 0.3f);
        collisionGraph.setModelBound(new BoundingSphere());
        collisionGraph.updateModelBound();

        ServerSessionManager manager = cell.getCellCache().getSession().getSessionManager();
        CollisionSystem collisionSystem = ClientContextJME.getCollisionSystem(manager, "Default");

        collisionController = new CollisionController(collisionGraph, (JMECollisionSystem) collisionSystem);
        collisionChangeRequestListener.setCollisionController(collisionController);
        ((AvatarController) ret.getContext().getController()).setCollisionController(collisionController);

        return ret;
    }

    void checkBounds(Spatial placeHolder) {
        traverseGraph(placeHolder, 0);
    }

    void traverseGraph(Spatial s, int level) {
        //for (int i=0; i<level; i++) {
        //    System.out.print("\t");
        //}

        if (s instanceof Geometry) {
            Geometry g = (Geometry)s;
            g.lockBounds();
            //System.out.println("Bounds for " + g + " is : " + g.getWorldBound());
        }
        if (s instanceof Node) {
            Node n = (Node)s;
            //n.setModelBound(new BoundingSphere());
            for (int i=0; i<n.getQuantity(); i++) {
                traverseGraph(n.getChild(i), level+1);
            }
            //n.updateWorldBound();
            //System.out.println("Bounds for " + n + " is : " + n.getWorldBound());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node createSceneGraph(Entity entity) {
        // Nothing to do here
        return null;
    }

    /**
     * Returns the WlAvatarCharacter object for this renderer. The WlAvatarCharacter
     * provides the control points in the avatar system.
     * @return
     */
    public WlAvatarCharacter getAvatarCharacter() {
        return avatarCharacter;
    }

    public void selectForInput(boolean selected) {
        selectedForInput = selected;
        enableInputListeners(selected);
    }

    private void enableInputListeners(boolean enabled) {
        if (avatarCharacter!=null) {
             WorldManager wm = ClientContextJME.getWorldManager();

            ((WlAvatarContext) avatarCharacter.getContext()).getBehaviorManager().setEnable(false);

            if (controlScheme == null && enabled) {
                controlScheme = new DefaultCharacterControls(ClientContextJME.getWorldManager());
                ((AvatarControls)wm.getUserData(AvatarControls.class)).setDefault(controlScheme);
                avatarCharacter.selectForInput();
                controlScheme.addCharacterToTeam(avatarCharacter);
                controlScheme.setCharacter(avatarCharacter);
//                controlScheme = (AvatarControls) ((JSceneEventProcessor) wm.getUserData(JSceneEventProcessor.class)).setDefault(new AvatarControlScheme(avatarCharacter));
            }
            if (enabled) {
                // Listen for avatar movement and update the cell
                avatarCharacter.getContext().getController().addCharacterMotionListener(characterMotionListener);

                // Listen for game context changes
                avatarCharacter.getContext().addGameContextListener(gameContextListener);
                avatarCharacter.selectForInput();
                controlScheme.addCharacterToTeam(avatarCharacter);
                controlScheme.setCharacter(avatarCharacter);

                // Chain the camera processor to the avatar motion processor for
                // smooth animation. For animated avatars we use CharacterAnimationProcessor for the simple
                // avatar CharacterProcessor
                ProcessorCollectionComponent pcc = avatarCharacter.getComponent(ProcessorCollectionComponent.class);
                ProcessorComponent characterProcessor = null;
                ProcessorComponent characterAnimationProcessor = null;
                for(ProcessorComponent pc : pcc.getProcessors()) {
                    if (pc instanceof CharacterProcessor)
                        characterProcessor = pc;
                    else if (pc instanceof CharacterAnimationProcessor) {
                        characterAnimationProcessor = pc;
                        break;
                    }
                }

                cameraChainedProcessor=null;
                if (characterAnimationProcessor!=null) {
                    cameraChainedProcessor = characterAnimationProcessor;
                } else if (characterProcessor!=null)
                    cameraChainedProcessor = characterProcessor;

                if (cameraChainedProcessor!=null) {
                    cameraChainedProcessor.addToChain(ViewManager.getViewManager().getCameraProcessor());
                    cameraChainedProcessor.setRunInRenderer(true);
                }

            } else {
                avatarCharacter.getContext().getController().removeCharacterMotionListener(characterMotionListener);
                avatarCharacter.getContext().removeGameContextListener(gameContextListener);
                if (controlScheme!=null) {
                    controlScheme.clearCharacterTeam();
                }

                if (cameraChainedProcessor!=null) {
                    cameraChainedProcessor.removeFromChain(ViewManager.getViewManager().getCameraProcessor());
                    cameraChainedProcessor = null;
                }
            }
        } else {
//            System.out.println("The avatar was null during enableInputListeners().");
        }
    }

    public void trigger(int trigger, boolean pressed, String animationName) {
        if (!selectedForInput && avatarCharacter != null) {
            // Sync to avoid concurrent updates of currentTrigger and currentPressed
            synchronized (this) {
                if (currentTrigger == trigger && currentPressed == pressed) {
                    return;
                }

                try {
                    if (pressed) {
                        if (animationName != null) {
                            ((WlAvatarContext) avatarCharacter.getContext()).setMiscAnimation(animationName);
                        }

                        avatarCharacter.getContext().triggerPressed(trigger);
                    } else {
                        avatarCharacter.getContext().triggerReleased(trigger);
                    }

                    currentTrigger = trigger;
                    currentPressed = pressed;
                } catch(Exception e) {
                    // We can get this if a user is viewing a female avatar but
                    // has not yet set female as the default. 
                }
            }
        }
    }

    public void triggerGoto(Vector3f position, Quaternion look) {
        CharacterController cc = avatarCharacter.getContext().getController();
        PPolygonModelInstance body = cc.getModelInstance();

        PMatrix newPosition = new PMatrix(body.getTransform().getLocalMatrix(false));
        newPosition.setTranslation(position);
        newPosition.setRotation(look);

        body.getTransform().getLocalMatrix(true).set(newPosition);
        cc.notifyTransfromUpdate(position, newPosition);
    }

    /**
     * Avatar model configuration listener, re-loads the avatar whenever a
     * reconfiguration happens
     */
    private class AvatarChangeListener implements AvatarConfigChangeListener {
        public void avatarConfigChanged(AvatarConfigMessage message) {
            // Fetch the new config info and try to load it. The null case is
            // handled by loadAvatar, and loads the system default avatar.
            loadAndChangeAvatar(message.getAvatarConfigInfo());
//            changeAvatar(loadAvatar(message.getAvatarConfigInfo()));
        }
    }

    /**
     * Add an avatar changed listener to the list. Duplicate checking is not 
     * performed. This method is thread-safe.
     * @param listener A non-null listener
     * @throws NullPointerException If listener == null
     */
    public synchronized void addAvatarChangedListener(AvatarChangedListener listener) {
        if (listener == null)
            throw new NullPointerException("Null listener provided!");
        avatarChangedListeners.add(new WeakReference<AvatarChangedListener>(listener));
    }
    
    /**
     * Remove an avatar changed listener from the list. This method will remove
     * erroneously added duplicates if any exist.
     * @param remove A non-null listener to remove
     * @throws NullPointerException If (remove == null)
     */
    public synchronized void removeAvatarChangedListener(AvatarChangedListener remove) {
        if (remove == null)
            throw new NullPointerException("Null listener provided!");
        for (WeakReference<AvatarChangedListener> listenerRef : avatarChangedListeners) {
            AvatarChangedListener listener = listenerRef.get();
            if (listener == null || listener == remove)
                avatarChangedListeners.remove(listenerRef);
        }
    }

    /**
     * This interface is used to receive call-backs whenever the underlying avatar
     * is changed
     */
    public static interface AvatarChangedListener {
        /**
         * The avatar has changed.
         * @param newAvatar The newly assigned avatar.
         */
        public void avatarChanged(Avatar newAvatar);
    }

    /**
     * Hack for the binary loader, this will need to be made general purpose once
     * we implement a core binary loader
     */
    class RelativeResourceLocator implements ResourceLocator {

        private String modulename;
        private String path;
        private String protocol;

        /**
         * Locate resources for the given file
         * @param url
         */
        public RelativeResourceLocator(URL url) {
            // The modulename can either be in the "user info" field or the
            // "host" field. If "user info" is null, then use the host name.
//            System.out.println("ASSET RESOURCE LOCATOR FOR URL " + url.toExternalForm());

            if (url.getUserInfo() == null) {
                modulename = url.getHost();
            }
            else {
                modulename = url.getUserInfo();
            }
            path = url.getPath();
            path = path.substring(0, path.lastIndexOf('/')+1);
            protocol = url.getProtocol();

//            System.out.println("MODULE NAME " + modulename + " PATH " + path);
        }

        public URL locateResource(String resource) {
//            System.err.println("Looking for resource "+resource);
//            System.err.println("Module "+modulename+"  path "+path);
            try {

                    String urlStr = trimUrlStr(protocol + "://"+modulename+path+".." + resource);

                    URL url = getAssetURL(urlStr);
//                    System.err.println("Using " + url.toExternalForm());
                    return url;

            } catch (MalformedURLException ex) {
                logger.log(Level.SEVERE, "Unable to locateResource "+resource, ex);
                return null;
            }
        }

        /**
         * Trim ../ from url
         * @param urlStr
         */
        private String trimUrlStr(String urlStr) {
            int pos = urlStr.indexOf("/../");
            if (pos==-1)
                return urlStr;

            StringBuilder buf = new StringBuilder(urlStr);
            int start = pos;
            while(buf.charAt(--start)!='/') {}
            buf.replace(start, pos+4, "/");
//            System.out.println("Trimmed "+buf.toString());

           return buf.toString();
        }

    }

    class DebugNode extends Node {
        @Override
        public void draw(Renderer r) {
            System.err.println("START**********************************");
            super.draw(r);
            System.err.println("END ***********************************");
        }
    }

    class CollisionChangeRequestListener extends EventClassListener {

        private CollisionController collisionController;
        private AvatarCollisionChangeRequestEvent evt=null;

        private Class[] consumeClasses = new Class[]{
            AvatarCollisionChangeRequestEvent.class,
        };

        public void setCollisionController(CollisionController collisionController) {
            synchronized(this) {
                this.collisionController = collisionController;
                if (collisionController!=null) {
                    if (evt!=null) {
                        collisionController.setCollisionEnabled(evt.isCollisionEnabled());
                        collisionController.setGravityEnabled(evt.isGravityEnabled());
                    } else {
                        collisionController.setCollisionEnabled(false);
                        collisionController.setGravityEnabled(true);
                    }
                }
            }
        }

        @Override
        public Class[] eventClassesToConsume() {
            return consumeClasses;
        }

        @Override
        public void commitEvent(Event event) {
        }

        @Override
        public void computeEvent(Event evtIn) {
            synchronized(this) {
                evt = (AvatarCollisionChangeRequestEvent) evtIn;
                if (collisionController!=null) {
                    collisionController.setCollisionEnabled(evt.isCollisionEnabled());
                    collisionController.setGravityEnabled(evt.isGravityEnabled());
                }
            }
        }
    }
}
