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

import imi.character.CharacterAttributes;
import imi.character.ninja.NinjaAvatarAttributes;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * @author paulby
 */
public class SimpleAvatarAttributes extends CharacterAttributes {

    public SimpleAvatarAttributes(Cell cell) {
        super(cell.getName());
        // Animations are setup in the super class

        WonderlandSession session = cell.getCellCache().getSession();
        ServerSessionManager manager = LoginManager.find(session);
        String serverHostAndPort = manager.getServerNameAndPort();

        setBaseURL("wla://avatarbase@"+serverHostAndPort+"/");

        String avatarDetail = System.getProperty("avatar.detail", "medium");

        setUseSimpleStaticModel(true, null);

    }

}