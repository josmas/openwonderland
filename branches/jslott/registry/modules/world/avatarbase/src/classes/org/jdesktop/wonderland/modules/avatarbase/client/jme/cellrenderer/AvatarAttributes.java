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

import imi.character.ninja.NinjaAvatarAttributes;
import imi.scene.PScene;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * @author paulby
 */
public class AvatarAttributes extends NinjaAvatarAttributes {

    public AvatarAttributes(String name) {
        super(name, false, false);
        // Animations are setup in the super class

        String avatarDetail = System.getProperty("avatar.detail", "medium");

        if (avatarDetail.equalsIgnoreCase("medium")) {
            setUseSimpleStaticModel(true, null);

//                    try {
//                        URL model = new URL("wla://avatarbase@"+serverHostAndPort+"/CylinderMan.dae");
//
//                        ColladaEnvironment loader = new ColladaEnvironment(ClientContextJME.getWorldManager(), model, "SimpleAvatar");
//                        System.err.println("--------------> LOADED <----------------");
//                        setUseSimpleSphereModel(true, loader.getPScene());
//                    } catch(MalformedURLException e) {
//                        logger.warning("Unable to load model");
//                        e.printStackTrace();
//                        setUseSimpleSphereModel(true, null);
//                    }
        }
    }

    public void setCell(Cell cell) {
        WonderlandSession session = cell.getCellCache().getSession();
        ServerSessionManager manager = LoginManager.find(session);
        String serverHostAndPort = manager.getServerNameAndPort();

        setBaseURL("wla://avatarbase@"+serverHostAndPort+"/");
    }

}
