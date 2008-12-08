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
import imi.character.CharacterMotionListener;
import imi.character.statemachine.GameContextListener;
import imi.scene.PMatrix;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.util.ArrayList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import imi.character.ninja.NinjaAvatar;
import imi.character.Character;
import imi.character.ninja.NinjaContext;
import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.ninja.PunchState;
import java.awt.event.KeyEvent;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.LoginManager;

/**
 * Renderer for Avatar, looks strangely like a teapot at the moment...
 * 
 * @author paulby
 */
@ExperimentalAPI
public class AvatarImiJME extends BasicRenderer {

    private AvatarCharacter avatarCharacter=null;

    public AvatarImiJME(Cell cell) {
        super(cell);
        assert(cell!=null);
    }
    
    @Override
    protected Entity createEntity() {
        Entity ret = createDemoEntities(ClientContextJME.getWorldManager());

        RenderComponent rc = (RenderComponent) ret.getComponent(RenderComponent.class);
        if (rc!=null)
            addDefaultComponents(ret, rc.getSceneRoot());
        else
            logger.warning("NO RenderComponent for Avatar");

        return ret;
    }

    @Override
    protected void addRenderState(Node node) {
        // Nothing to do
    }
    
    protected Entity createDemoEntities(WorldManager wm) {
        PMatrix origin = new PMatrix();
        CellTransform transform = cell.getLocalTransform();
        origin.setTranslation(transform.getTranslation(null));
        origin.setRotation(transform.getRotation(null));
        
//        Ninja avatar = new Ninja("Shadow Blade", origin, /*"assets/configurations/ninjaDude.xml",*/ 0.22f, wm);
        avatarCharacter = new AvatarCharacter("Avatar", wm);
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(avatarCharacter));
        avatarCharacter.selectForInput();
        control.getNinjaTeam().add(avatarCharacter);

//        JScene jscene = avatar.getJScene();
//        jscene.renderToggle();      // both renderers
//        jscene.renderToggle();      // jme renderer only
//        jscene.setRenderPRendererMesh(true);  // Force pRenderer to be instantiated
//        jscene.toggleRenderPRendererMesh();   // turn off mesh
//        jscene.toggleRenderBoundingVolume();  // turn off bounds

        // Listen for avatar movement and update the cell
        avatarCharacter.getController().addCharacterMotionListener(new CharacterMotionListener() {

            public void transformUpdate(Vector3f translation, PMatrix rotation) {
                cell.getComponent(MovableComponent.class).localMoveRequest(new CellTransform(rotation.getRotation(), translation));
            }
        });

        // Listen for game context changes
        // TODO this info will be sent to the other clients to animate the avatar
//        avatarCharacter.getContext().addGameContextListener(new GameContextListener() {
//
//            public void trigger(boolean pressed, int trigger, Vector3f translation, Quaternion rotation) {
//                System.err.println(pressed+" "+trigger+" "+translation);
//            }
//
//        });

        wm.removeEntity(avatarCharacter);
        
        return avatarCharacter;
    }

    public void triggerActionStart() {
        avatarCharacter.triggerActionStart(NinjaContext.TriggerNames.Punch);
    }

    public void triggerActionStop() {
        avatarCharacter.triggerActionStop(NinjaContext.TriggerNames.Punch);
    }

    public String[] getAnimations() {
        return avatarCharacter.getAnimations();
    }

    public void setAnimation(String str) {
        avatarCharacter.setAnimation(str);
    }

    @Override
    protected Node createSceneGraph(Entity entity) {
        // Nothing to do here
        return null;
    }

    class AvatarCharacter extends NinjaAvatar {

        public class MaleAvatarAttributes extends NinjaAvatar.NinjaAvatarAttributes
        {
            public MaleAvatarAttributes(String name) {
                super(name, true);
                // Animations are setup in the super class
//                setModelFile("assets/models/collada/Avatars/Male2/Male_Bind.dae");
//                ArrayList<String> anims = new ArrayList<String>();
//                anims.add("assets/models/collada/Avatars/MaleZip/Male_Idle.dae");
//                anims.add("assets/models/collada/Avatars/MaleZip/Male_StandToSit.dae");
//                anims.add("assets/models/collada/Avatars/MaleZip/Male_Wave.dae");
//                anims.add("assets/models/collada/Avatars/MaleZip/Male_Walk.dae");
//                anims.add("assets/models/collada/Avatars/MaleZip/Male_Sitting.dae");
//                if (false)
//                {
//                    anims.add("assets/models/collada/Avatars/MaleZip/Male_Run.dae");
//                    anims.add("assets/models/collada/Avatars/Male/Male_Bow.dae");
//                    anims.add("assets/models/collada/Avatars/Male/Male_Cheer.dae");
//                    anims.add("assets/models/collada/Avatars/Male/Male_Clap.dae");
//                    anims.add("assets/models/collada/Avatars/Male/Male_Follow.dae");
//                    anims.add("assets/models/collada/Avatars/Male/Male_Jump.dae");
//                    anims.add("assets/models/collada/Avatars/Male/Male_Laugh.dae");
//                }
//                setAnimations(anims.toArray(new String[anims.size()]));

                WonderlandSession session = cell.getCellCache().getSession();
                ServerSessionManager manager = LoginManager.find(session);
                String serverHostAndPort = manager.getServerNameAndPort();

                setBaseURL("wla://avatarbase@"+serverHostAndPort+"/");

//                try {
//                    URL simpleURL = new URL("wla://avatarbase@"+serverHostAndPort+"/CylinderMan.dae");
//                    SharedAsset colladaAsset = new SharedAsset(null,
//                        new AssetDescriptor(SharedAssetType.COLLADA_Mesh, simpleURL));
//
//                    ColladaEnvironment loader = new ColladaEnvironment(ClientContextJME.getWorldManager(), colladaAsset, "SimpleAvatar");
//                    setUseSimpleSphereModel(true, loader.getPScene());
//                } catch(MalformedURLException e) {
//                    logger.warning("Failed to load simple avatar");
//                }
            }

        }

        @Override
        protected void initKeyBindings()
        {
            m_keyBindings.put(KeyEvent.VK_SHIFT,        TriggerNames.Movement_Modifier.ordinal());
            m_keyBindings.put(KeyEvent.VK_LEFT,         TriggerNames.Move_Left.ordinal());
            m_keyBindings.put(KeyEvent.VK_RIGHT,        TriggerNames.Move_Right.ordinal());
            m_keyBindings.put(KeyEvent.VK_UP,           TriggerNames.Move_Forward.ordinal());
            m_keyBindings.put(KeyEvent.VK_DOWN,         TriggerNames.Move_Back.ordinal());
    //        m_keyBindings.put(KeyEvent.VK_W,        TriggerNames.Move_Forward.ordinal());
    //        m_keyBindings.put(KeyEvent.VK_S,        TriggerNames.Move_Back.ordinal());
            m_keyBindings.put(KeyEvent.VK_CONTROL,      TriggerNames.Punch.ordinal());
            m_keyBindings.put(KeyEvent.VK_ENTER,        TriggerNames.ToggleSteering.ordinal());
//            m_keyBindings.put(KeyEvent.VK_BACK_SPACE,   TriggerNames.PositionGoalPoint.ordinal());
//            m_keyBindings.put(KeyEvent.VK_HOME,         TriggerNames.SelectNearestGoalPoint.ordinal());
            m_keyBindings.put(KeyEvent.VK_ADD,          TriggerNames.Move_Down.ordinal());
            m_keyBindings.put(KeyEvent.VK_SUBTRACT,     TriggerNames.Move_Up.ordinal());
            m_keyBindings.put(KeyEvent.VK_COMMA,        TriggerNames.Reverse.ordinal());
            m_keyBindings.put(KeyEvent.VK_PERIOD,       TriggerNames.NextAction.ordinal());
            m_keyBindings.put(KeyEvent.VK_1,            TriggerNames.GoTo1.ordinal());
            m_keyBindings.put(KeyEvent.VK_2,            TriggerNames.GoTo2.ordinal());
            m_keyBindings.put(KeyEvent.VK_3,            TriggerNames.GoTo3.ordinal());
        }

        public void triggerActionStart(TriggerNames trigger) {
            m_context.triggerPressed(trigger.ordinal());
        }

        public void triggerActionStop(TriggerNames trigger) {
            m_context.triggerReleased(trigger.ordinal());
        }

        public String[] getAnimations() {
            return new String[] {"Male_Wave",
                         //        "Male_Run",
                          //       "Male_Bow",
                          //       "Male_Cheer",
                          //       "Male_Follow",
                          //       "Male_Jump",
                          //       "Male_Laugh",
                          //       "Male_Clap",
                              //   "Male_Idle",
                              //   "Male_Walk",
                              //   "Male_StandToSit",
                              //   "Male_Sitting",
                                };
        }

        public void setAnimation(String str) {
            PunchState punch = (PunchState) getContext().getStates().get(PunchState.class);
            punch.setAnimationSetBoolean(false);

            punch.setAnimationName(str);
        }

        public AvatarCharacter(String name, WorldManager wm) {
            super(name,wm);
        }

        @Override
        protected Attributes createAttributes(String name)
        {
            return new MaleAvatarAttributes(name);
        }
    }

}
