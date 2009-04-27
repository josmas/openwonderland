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

import com.jme.math.Matrix3f;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.cellrenderer.*;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import imi.character.CharacterAttributes;
import imi.character.CharacterMotionListener;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.statemachine.GameContextListener;
import imi.character.statemachine.corestates.CycleActionState;
import imi.scene.PMatrix;
import imi.scene.PTransform;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.PMathUtils;
import imi.utils.input.AvatarControlScheme;
import java.io.File;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.MovableAvatarComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.AvatarControls.AvatarActionTrigger;
import org.jdesktop.wonderland.client.jme.AvatarControls.AvatarInputSelector;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.avatarbase.client.AvatarConfigManager;
import org.jdesktop.wonderland.modules.avatarbase.client.cell.AvatarConfigComponent;
import org.jdesktop.wonderland.modules.avatarbase.client.cell.AvatarConfigComponent.AvatarConfigChangeListener;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigMessage;

/**
 * Renderer for Avatars, using the new avatar system
 * 
 * @author paulby
 */
@ExperimentalAPI
public class AvatarImiJME extends BasicRenderer implements AvatarInputSelector, AvatarActionTrigger {

    private WlAvatarCharacter avatarCharacter = null;
    private boolean selectedForInput = false;

//    private AvatarRendererChangeRequestEvent.AvatarQuality quality = AvatarRendererChangeRequestEvent.AvatarQuality.High;
    private CharacterMotionListener characterMotionListener;
    private CharacterMotionListener nameTagMover;
    private GameContextListener gameContextListener;
    private int currentTrigger = -1;
    private boolean currentPressed = false;
    private float positionMinDistanceForPull = 0.1f;
    private float positionMaxDistanceForPull = 3.0f;
    private String username;
    private AvatarControlScheme controlScheme = null;
    private NameTag nameTag;

    public AvatarImiJME(Cell cell) {
        super(cell);
        assert (cell != null);
        final Cell c = cell;

        cell.getComponent(AvatarConfigComponent.class).addAvatarConfigChageListener(new AvatarConfigChangeListener() {

            public void AvatarConfigChanged(AvatarConfigMessage msg) {
                URL configURL=null;
                try {
                    configURL = new URL(msg.getModelConfigURL());
                    System.err.println("Config " + configURL + "  user="+username+"  "+selectedForInput);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(AvatarImiJME.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }

                WonderlandSession session = c.getCellCache().getSession();
                ServerSessionManager manager = session.getSessionManager();
                String serverHostAndPort = manager.getServerNameAndPort();
                final WlAvatarCharacter avatarCharacter = new WlAvatarCharacter(configURL,
                        ClientContextJME.getWorldManager(),
                        "wla://avatarbaseart@" + serverHostAndPort + "/");

                changeAvatar(avatarCharacter);
            }
        });

        username = ((AvatarCell) cell).getIdentity().getUsername();

        characterMotionListener = new CharacterMotionListener() {
            public void transformUpdate(Vector3f translation, PMatrix rotation) {
                ((MovableAvatarComponent) c.getComponent(MovableComponent.class)).localMoveRequest(new CellTransform(rotation.getRotation(), translation));
                };
            
        };

        // Temporary workaround until we can attach the name tag directly
        // to the avatar
        nameTagMover = new CharacterMotionListener() {
            public void transformUpdate(Vector3f translation, PMatrix rotation) {
                if (nameTag != null) {
                    nameTag.setLocalTranslation(translation);
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
                String animationName = avatarCharacter.getContext().getState(CycleActionState.class).getAnimationName();
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
                    if (nameTag == null) {
                        System.out.println("[AvatarImiJME] warning: setting " +
                                "avatar name when name tag is null");
                        return;
                    }

                    AvatarNameEvent e = (AvatarNameEvent) event;

                    if (e.getUsername().equals(username)) {
                        nameTag.setNameTag(e.getEventType(), username, 
                                           e.getUsernameAlias(),
                                           e.getForegroundColor(), e.getFont());
                    }
                }
            }

            @Override
            public void computeEvent(Event evtIn) {
                //System.err.println("TODO - GOT EVENT "+evtIn);
            }
        });

    }

    /**
     * TODO remove once we attach the nametag to the avatar scene
     * @param status
     */
    @Override
    public void setStatus(CellStatus status) {
        super.setStatus(status);
        switch(status) {
            case DISK :
                if (nameTag!=null)
                    nameTag.done();
                if (entity!=null)
                    ClientContextJME.getWorldManager().removeEntity(entity);
                break;
        }
    }

    @Override
    protected Entity createEntity() {
        avatarCharacter = (WlAvatarCharacter) createAvatarEntities(ClientContextJME.getWorldManager());

        RenderComponent rc = (RenderComponent) avatarCharacter.getComponent(RenderComponent.class);

        if (rc != null) {
            addDefaultComponents(avatarCharacter, rc.getSceneRoot());
        } else {
            logger.warning("NO RenderComponent for Avatar");
        }

        // Remove the entity, it will be added when the cell status changes
        ClientContextJME.getWorldManager().removeEntity(avatarCharacter);

        System.out.println("[AvatarImiJME] setting name tag");
        nameTag = new NameTag(cell, username, 2, avatarCharacter);
        return avatarCharacter;
    }

    void changeAvatar(WlAvatarCharacter newAvatar) {
        LoadingInfo.startedLoading(cell.getCellID(), newAvatar.getName());
        ViewManager viewManager = ViewManager.getViewManager();

        if (viewManager.getPrimaryViewCell()==cell) {
            ViewManager.getViewManager().detach();
        }
        PMatrix currentLocation = null;

        WorldManager wm = ClientContextJME.getWorldManager();
        if (avatarCharacter != null) {
            currentLocation = avatarCharacter.getModelInst().getTransform().getWorldMatrix(true);
            wm.removeEntity(avatarCharacter);
            avatarCharacter.destroy();
        }

        avatarCharacter = newAvatar;
        
        avatarCharacter.getController().addCharacterMotionListener(nameTagMover);

        RenderComponent rc = (RenderComponent) avatarCharacter.getComponent(RenderComponent.class);

        if (rc != null) {
            addDefaultComponents(avatarCharacter, rc.getSceneRoot());
        } else {
            logger.warning("NO RenderComponent for Avatar");
        }
        if (currentLocation != null) {
            avatarCharacter.getModelInst().setTransform(new PTransform(currentLocation));
        }

        wm.addEntity(avatarCharacter);

        entity = newAvatar;     // This needs to be set before calls to the viewmanager

        if (viewManager.getPrimaryViewCell()==cell) {
            ViewManager.getViewManager().attach(cell);
        }

        // reset the control scheme before selecting the new avatar
        controlScheme = null;

        selectForInput(selectedForInput);
//        System.err.println("Change Avatar to " + entity);
        LoadingInfo.finishedLoading(cell.getCellID(), newAvatar.getName());
    }

    @Override
    protected void addRenderState(Node node) {
        // Nothing to do
    }

    @Override
    public void cellTransformUpdate(CellTransform transform) {
        // Don't call super, we don't use a MoveProcessor for avatars

        if (!selectedForInput && avatarCharacter != null && avatarCharacter.getController().getModelInstance()!=null ) {
            Vector3f pos = transform.getTranslation(null);
            Vector3f dir = new Vector3f(0, 0, -1);
            transform.getRotation(null).multLocal(dir);
            PMatrix local = avatarCharacter.getController().getModelInstance().getTransform().getLocalMatrix(true);
            final Vector3f currentPosition = local.getTranslation();
            float currentDistance = currentPosition.distance(pos);
            if (currentDistance < positionMaxDistanceForPull) {
                pos.set(currentPosition);
            }

            PMatrix look = PMathUtils.lookAt(pos.add(dir), pos, Vector3f.UNIT_Y);
            avatarCharacter.getModelInst().getTransform().getLocalMatrix(true).set(look);

            SceneWorker.addWorker(new WorkCommit() {

                public void commit() {
                    if (nameTag != null) {
                        nameTag.setLocalTranslation(currentPosition);
                    }
                }
            });
        }

    }

    protected Entity createAvatarEntities(WorldManager wm) {
        PMatrix origin = new PMatrix();
        CellTransform transform = cell.getLocalTransform();
        origin.setTranslation(transform.getTranslation(null));
        origin.setRotation(transform.getRotation(null));

        String name = ((ViewCell) cell).getIdentity().getUsername();


        // Set the base URL
        WonderlandSession session = cell.getCellCache().getSession();
        ServerSessionManager manager = session.getSessionManager();
        String serverHostAndPort = manager.getServerNameAndPort();
        String baseURL = "wla://avatarbaseart@" + serverHostAndPort + "/";

        URL avatarConfigURL = cell.getComponent(AvatarConfigComponent.class).getAvatarConfigURL();

        System.out.println("[AvatarImiJme] AVATAR CONFIG URL "+avatarConfigURL);

        LoadingInfo.startedLoading(cell.getCellID(), username);
        try {
        // Create the character, but don't add the entity to wm
        // TODO this will change to take the config
        if (avatarConfigURL == null) {
//            File defaultConfig = AvatarConfigManager.getDefaultAvatarConfigFile();
//            if (defaultConfig.exists()) {
//                try {
//                    avatarCharacter = new WlAvatarCharacter(defaultConfig.toURI().toURL(), ClientContextJME.getWorldManager(), baseURL);
//                } catch (MalformedURLException ex) {
//                    avatarCharacter = null;
//                    Logger.getLogger(AvatarImiJME.class.getName()).log(Level.SEVERE, "Error loading default config file" + defaultConfig, ex);
//                }
//            }

            if (avatarCharacter == null) {
                CharacterAttributes attributes = new MaleAvatarAttributes(name, true);
                attributes.setBaseURL(baseURL);
                avatarCharacter = new WlAvatarCharacter(attributes, wm);
            }

        } else {
            avatarCharacter = new WlAvatarCharacter(avatarConfigURL, wm, "wla://avatarbaseart@" + serverHostAndPort + "/");
        }

        avatarCharacter.getModelInst().getTransform().getLocalMatrix(true).set(origin);

        avatarCharacter.getController().addCharacterMotionListener(nameTagMover);

//        JScene jscene = avatar.getJScene();
//        jscene.renderToggle();      // both renderers
//        jscene.renderToggle();      // jme renderer only
//        jscene.setRenderPRendererMesh(true);  // Force pRenderer to be instantiated
//        jscene.toggleRenderPRendererMesh();   // turn off mesh
//        jscene.toggleRenderBoundingVolume();  // turn off bounds

//        wm.removeEntity(avatarCharacter);
        } catch(Exception e) {
            Logger.getLogger(AvatarImiJME.class.getName()).log(Level.SEVERE, "Error loading avatar "+avatarConfigURL, e);
        } finally {
            LoadingInfo.finishedLoading(cell.getCellID(), username);
        }
        return avatarCharacter;
    }

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
        WorldManager wm = ClientContextJME.getWorldManager();
        ((WlAvatarContext) avatarCharacter.getContext()).getSteering().setEnable(false);

        if (controlScheme == null && selected) {
            controlScheme = (AvatarControlScheme) ((JSceneEventProcessor) wm.getUserData(JSceneEventProcessor.class)).setDefault(new AvatarControlScheme(avatarCharacter));
        }

        selectedForInput = selected;

//        System.err.println("SelectForInput " + selected);

        if (selected) {
            // Listen for avatar movement and update the cell
            avatarCharacter.getController().addCharacterMotionListener(characterMotionListener);

            // Listen for game context changes
            avatarCharacter.getContext().addGameContextListener(gameContextListener);
            avatarCharacter.selectForInput();
            controlScheme.getAvatarTeam().add(avatarCharacter);
            controlScheme.setavatar(avatarCharacter);
        } else {
            avatarCharacter.getController().removeCharacterMotionListener(characterMotionListener);
            avatarCharacter.getContext().removeGameContextListener(gameContextListener);
            if (controlScheme!=null)
                controlScheme.getAvatarTeam().remove(avatarCharacter);
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
}
