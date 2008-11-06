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
import imi.character.Character;
import java.util.ArrayList;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author paulby
 */
public class AvatarCharacter extends NinjaAvatar {

    public class MaleAvatarAttributes extends Character.AnimatedAvatarAttributes
    {
        public MaleAvatarAttributes(String name) {
            super(name);
            setModelFile("assets/models/collada/Avatars/Male2/Male_Bind.dae");
            ArrayList<String> anims = new ArrayList<String>();
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Idle.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_StandToSit.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Wave.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Walk.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Sitting.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Run.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_Bow.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_Cheer.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_Clap.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_Follow.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_Jump.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_Laugh.dae");
            setAnimations(anims.toArray(new String[anims.size()]));

            setBaseURL("wla://avatarbase/");
        }

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
