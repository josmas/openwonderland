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
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import org.jdesktop.wonderland.client.jme.cellrenderer.*;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import imi.character.CharacterAttributes;
import imi.character.CharacterMotionListener;
import imi.character.statemachine.GameContextListener;
import imi.scene.PMatrix;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import imi.character.ninja.NinjaContext;
import imi.character.ninja.NinjaContext.TriggerNames;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.MovableAvatarComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.AvatarControls.AvatarActionTrigger;
import org.jdesktop.wonderland.client.jme.AvatarControls.AvatarInputSelector;

/**
 * Renderer for Avatars, using the new avatar system
 * 
 * @author paulby
 */
@ExperimentalAPI
public class AvatarImiJME extends BasicRenderer implements AvatarInputSelector, AvatarActionTrigger {

    private AvatarCharacter avatarCharacter=null;
    private AvatarCharacter simpleAvatar = null;
    private AvatarCharacter currentAvatar = null;
    private boolean selectedForInput = false;

    private AvatarRendererChangeRequestEvent.AvatarQuality quality = AvatarRendererChangeRequestEvent.AvatarQuality.High;

    private Entity rootEntity;
    private CharacterMotionListener characterMotionListener;
    private GameContextListener gameContextListener;

    public AvatarImiJME(Cell cell) {
        super(cell);
        assert(cell!=null);
        final Cell c = cell;

        characterMotionListener = new CharacterMotionListener() {
                public void transformUpdate(Vector3f translation, PMatrix rotation) {
                    ((MovableAvatarComponent)c.getComponent(MovableComponent.class)).localMoveRequest(new CellTransform(rotation.getRotation(), translation), -1, false, null);
                }
            };

        // TODO this info will be sent to the other clients to animate the avatar
        gameContextListener = new GameContextListener() {
                public void trigger(boolean pressed, int trigger, Vector3f translation, Quaternion rotation) {
                   ((MovableAvatarComponent)c.getComponent(MovableComponent.class)).localMoveRequest(new CellTransform(rotation, translation), trigger, pressed, null);
                }

            };

        ClientContext.getInputManager().addGlobalEventListener(new EventClassListener() {
            private Class[] consumeClasses = new Class[] { AvatarRendererChangeRequestEvent.class };

            @Override
            public Class[] eventClassesToConsume () {
                return consumeClasses;
            }

            @Override
            public void computeEvent(Event evtIn) {
                System.err.println("GOT EVENT "+evtIn);
                boolean sel = selectedForInput;
                if (sel)
                    selectForInput(false);

                AvatarRendererChangeRequestEvent evt  = (AvatarRendererChangeRequestEvent) evtIn;
                rootEntity.removeEntity(currentAvatar);
                switch(evt.getQuality()) {
                    case High :
                        currentAvatar = avatarCharacter;
                        break;
                    case Medium :
                        currentAvatar = simpleAvatar;
                        break;
                    case Low :
                        currentAvatar = simpleAvatar;
                        break;
                }

                PMatrix origin = new PMatrix();
                CellTransform transform = getCell().getLocalTransform();
                origin.setTranslation(transform.getTranslation(null));
                origin.setRotation(transform.getRotation(null));
                currentAvatar.getModelInst().getTransform().getLocalMatrix(true).set(origin);

                if (sel)
                    selectForInput(sel);

                rootEntity.addEntity(currentAvatar);
            }
        });
    }

    @Override
    protected Entity createEntity() {
        rootEntity = new Entity("AvatarRoot");
        Entity ret = createDemoEntities(ClientContextJME.getWorldManager());

        RenderComponent rc = (RenderComponent) ret.getComponent(RenderComponent.class);
        if (rc!=null)
            addDefaultComponents(ret, rc.getSceneRoot());
        else
            logger.warning("NO RenderComponent for Avatar");

//        rootEntity.addEntity(ret);
//
//        return rootEntity;

        return ret;
    }

    @Override
    protected void addRenderState(Node node) {
        // Nothing to do
    }

    @Override
    public void cellTransformUpdate(CellTransform transform) {
        super.cellTransformUpdate(transform);
        if (!selectedForInput && avatarCharacter!=null) {
            ((AvatarController)avatarCharacter.getController()).cellTransformUpdate(transform);
        }
    }
    
    protected Entity createDemoEntities(WorldManager wm) {
        PMatrix origin = new PMatrix();
        CellTransform transform = cell.getLocalTransform();
        origin.setTranslation(transform.getTranslation(null));
        origin.setRotation(transform.getRotation(null));

        CharacterAttributes attributes = new AvatarAttributes(cell);

        // Create the character, but don't add the entity to wm
        // TODO this will change to take the config
        avatarCharacter = new AvatarCharacter(attributes, wm);
        avatarCharacter.getModelInst().getTransform().getLocalMatrix(true).set(origin);

        simpleAvatar = new SimpleAvatarCharacter(new SimpleAvatarAttributes(cell), wm);
        simpleAvatar.getModelInst().getTransform().getLocalMatrix(true).set(origin);
        
//        try {
//            // Now load the config
//            avatarCharacter.loadConfiguration(new URL("file:////Users/paulby/src/java.net/avatars/trunk/assets/configurations/test1.xml"));
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(AvatarImiJME.class.getName()).log(Level.SEVERE, null, ex);
//        }

//        JScene jscene = avatar.getJScene();
//        jscene.renderToggle();      // both renderers
//        jscene.renderToggle();      // jme renderer only
//        jscene.setRenderPRendererMesh(true);  // Force pRenderer to be instantiated
//        jscene.toggleRenderPRendererMesh();   // turn off mesh
//        jscene.toggleRenderBoundingVolume();  // turn off bounds

//        wm.removeEntity(avatarCharacter);

        switch(quality) {
            case High :
                currentAvatar = avatarCharacter;
                break;
            case Medium :
                currentAvatar = simpleAvatar;
                break;
            case Low :
                currentAvatar = simpleAvatar;
                break;
            default :
                throw new RuntimeException("Unknown avatar quality");
        }

        return currentAvatar;
    }

    @Override
    protected Node createSceneGraph(Entity entity) {
        // Nothing to do here
        return null;
    }

    /**
     * Returns the AvatarCharacter object for this renderer. The AvatarCharacter
     * provides the control points in the avatar system.
     * @return
     */
    public AvatarCharacter getAvatarCharacter() {
        return avatarCharacter;
    }

    public void selectForInput(boolean selected) {
        WorldManager wm = ClientContextJME.getWorldManager();
        ((NinjaContext)currentAvatar.getContext()).getSteering().setEnable(false);
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(avatarCharacter));
        currentAvatar.selectForInput();
        control.getNinjaTeam().add(currentAvatar);
        ((AvatarController)currentAvatar.getController()).selectForInput(selected);
        selectedForInput = true;

        if (selected) {
            // Listen for avatar movement and update the cell
            currentAvatar.getController().addCharacterMotionListener(characterMotionListener);

            // Listen for game context changes
            // TODO this info will be sent to the other clients to animate the avatar
            currentAvatar.getContext().addGameContextListener(gameContextListener);
        } else {
            currentAvatar.getController().removeCharacterMotionListener(characterMotionListener);
            currentAvatar.getContext().removeGameContextListener(gameContextListener);
        }

    }

    public void trigger(int trigger, boolean pressed) {
        if (!selectedForInput) {
            System.err.println("Trigger "+trigger);
            if (pressed)
                avatarCharacter.triggerActionStart(TriggerNames.Move_Forward);
            else
                avatarCharacter.triggerActionStop(TriggerNames.Move_Forward);

        }
    }


}
