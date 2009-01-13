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

import imi.character.CharacterController;
import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaContext;
import imi.character.ninja.NinjaController;

/**
 *
 * Overload NinjaContext so we can install our own AvatarController
 *
 * @author paulby
 */
public class AvatarContext extends NinjaContext {

    public AvatarContext(NinjaAvatar avatar) {
        super(avatar);
    }

    @Override
    protected NinjaController instantiateController() {
        return new AvatarController(getNinja());
    }
}
