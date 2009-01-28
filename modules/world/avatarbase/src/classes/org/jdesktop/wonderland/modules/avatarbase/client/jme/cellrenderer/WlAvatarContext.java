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

import imi.character.avatar.Avatar;
import imi.character.statemachine.corestates.ActionInfo;
import imi.character.statemachine.corestates.ActionState;
import imi.character.statemachine.corestates.CycleActionState;
import java.util.HashMap;

/**
 *
 * Overload AvatarContext so we can install our own WlAvatarController
 *
 * @author paulby
 */
public class WlAvatarContext extends imi.character.avatar.AvatarContext {

    private HashMap<String, ActionInfo> actionMap = new HashMap();
    private ActionInfo currentActionInfo = null;

    public WlAvatarContext(Avatar avatar) {
        super(avatar);

//        for(ActionInfo actionInfo : getGenericAnimations()) {
//            actionMap.put(actionInfo.getAnimationName(), actionInfo);
//            System.err.println("Found Animation "+actionInfo.getAnimationName());
//        }
    }

    @Override
    protected imi.character.avatar.AvatarController instantiateController() {
        return new WlAvatarController(getavatar());
    }

    Iterable<String> getAnimationNames() {
        return actionMap.keySet();
    }

    void playAnimation(String name) {
        currentActionInfo = actionMap.get(name);
        ActionState action = (ActionState) gameStates.get(CycleActionState.class);
        action.setAnimationSetBoolean(false);
        currentActionInfo.apply(action);
        setCurrentState(action);
    }
}
