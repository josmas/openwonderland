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
import imi.character.GoTo;
import imi.character.ninja.NinjaContext;
import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.ninja.PunchState;
import imi.character.statemachine.GameContext;
import imi.environments.ColladaEnvironment;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JFrame;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.AvatarControls.AvatarInputSelector;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.LoginManager;

/**
 * Renderer for Avatars, using the new avatar system
 * 
 * @author paulby
 */
@ExperimentalAPI
public class AvatarImiJME extends BasicRenderer implements AvatarInputSelector {

    private AvatarCharacter avatarCharacter=null;
    private boolean selectedForInput = false;

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

    @Override
    public void cellTransformUpdate(CellTransform transform) {
        super.cellTransformUpdate(transform);
        if (!selectedForInput && avatarCharacter!=null) {
//            avatarCharacter.triggerActionStart(TriggerNames.Move_Forward);
            ((AvatarController)avatarCharacter.getController()).cellTransformUpdate(transform);
        }
    }
    
    protected Entity createDemoEntities(WorldManager wm) {
        PMatrix origin = new PMatrix();
        CellTransform transform = cell.getLocalTransform();
        origin.setTranslation(transform.getTranslation(null));
        origin.setRotation(transform.getRotation(null));

        AvatarAttributes attributes = new AvatarAttributes("Avatar");
        attributes.setCell(cell);

        avatarCharacter = new AvatarCharacter(attributes, wm);
        avatarCharacter.getModelInst().getTransform().getLocalMatrix(true).set(origin);

        System.err.println("CREATING ENTITY "+avatarCharacter);
//        JScene jscene = avatar.getJScene();
//        jscene.renderToggle();      // both renderers
//        jscene.renderToggle();      // jme renderer only
//        jscene.setRenderPRendererMesh(true);  // Force pRenderer to be instantiated
//        jscene.toggleRenderPRendererMesh();   // turn off mesh
//        jscene.toggleRenderBoundingVolume();  // turn off bounds


        // The entity will be added by the cell
        wm.removeEntity(avatarCharacter);

        return avatarCharacter;
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

    public void selectForInput() {
        WorldManager wm = ClientContextJME.getWorldManager();
        ((NinjaContext)avatarCharacter.getContext()).getSteering().setEnable(false);
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(avatarCharacter));
        avatarCharacter.selectForInput();
        control.getNinjaTeam().add(avatarCharacter);
        ((AvatarController)avatarCharacter.getController()).selectForInput();
        selectedForInput = true;

        // Listen for avatar movement and update the cell
        avatarCharacter.getController().addCharacterMotionListener(new CharacterMotionListener() {

            public void transformUpdate(Vector3f translation, PMatrix rotation) {
                cell.getComponent(MovableComponent.class).localMoveRequest(new CellTransform(rotation.getRotation(), translation));
            }
        });

        // Listen for game context changes
        // TODO this info will be sent to the other clients to animate the avatar
        avatarCharacter.getContext().addGameContextListener(new GameContextListener() {

            public void trigger(boolean pressed, int trigger, Vector3f translation, Quaternion rotation) {
                System.err.println(pressed+" "+trigger+" "+translation);
            }

        });

    }


}
