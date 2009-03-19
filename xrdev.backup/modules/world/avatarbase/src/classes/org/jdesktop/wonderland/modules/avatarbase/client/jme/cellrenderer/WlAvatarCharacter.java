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

import com.jme.math.Vector3f;
import imi.character.CharacterAttributes;
import imi.character.avatar.Avatar;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.statemachine.GameContext;
//import imi.character.statemachine.corestates.ActionState;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import java.net.URL;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author paulby
 */
public class WlAvatarCharacter extends Avatar {


    /**
     * Create the avatar character, but don't add it to wm
     */
    public WlAvatarCharacter(CharacterAttributes attributes, WorldManager wm) {
        super(attributes,wm, false);
//        bigHeadMode(this);
    }

    public WlAvatarCharacter(URL configURL, WorldManager wm, String baseURL) {
        super(configURL, wm, baseURL);
    }

    @Override
    protected GameContext instantiateContext() {
        return new WlAvatarContext(this);
    }

    public void triggerActionStart(TriggerNames trigger) {
        m_context.triggerPressed(trigger.ordinal());
    }

    public void triggerActionStop(TriggerNames trigger) {
        m_context.triggerReleased(trigger.ordinal());
    }

    public void playAnimation(String name) {
        ((WlAvatarContext)getContext()).playAnimation(name);
    }

    public Iterable<String> getAnimationNames() {
        return ((WlAvatarContext)getContext()).getAnimationNames();
    }

    // TESTING
    private void bigHeadMode(WlAvatarCharacter avatar)
    {
        SkinnedMeshJoint joint = avatar.getSkeleton().getSkinnedMeshJoint("Head");
        avatar.getSkeleton().displaceJoint("Head", new Vector3f(0, 0.07f, 0));
        joint.getBindPose().setScale(2.0f);

        joint = avatar.getSkeleton().getSkinnedMeshJoint("rightHand");
        joint.getBindPose().setScale(2.0f);

        joint = avatar.getSkeleton().getSkinnedMeshJoint("leftHand");
        joint.getBindPose().setScale(2.0f);
    }
}
