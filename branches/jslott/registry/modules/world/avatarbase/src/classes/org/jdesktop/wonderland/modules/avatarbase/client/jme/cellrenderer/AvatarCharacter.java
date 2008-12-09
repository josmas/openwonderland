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

import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.ninja.PunchState;
import imi.character.statemachine.GameContext;
import java.awt.event.KeyEvent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author paulby
 */
public class AvatarCharacter extends NinjaAvatar {


    public AvatarCharacter(AvatarAttributes attributes, WorldManager wm) {
        super(attributes,wm);
    }

    @Override
    protected GameContext instantiateContext() {
        return new AvatarContext(this);
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


}
