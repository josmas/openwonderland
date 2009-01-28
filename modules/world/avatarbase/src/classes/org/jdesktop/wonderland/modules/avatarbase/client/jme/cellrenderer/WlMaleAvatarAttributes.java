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
import imi.character.avatar.FemaleAvatarAttributes;
import imi.character.avatar.MaleAvatarAttributes;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * @author paulby
 */
public class WlMaleAvatarAttributes extends MaleAvatarAttributes {

    public WlMaleAvatarAttributes(Cell cell) {
//        NinjaAvatarAttributes attribs = new NinjaAvatarAttributes("WeirdGuy", 2, 3, 5, 10, 1);
        //super(cell.getName(), 2, 3, 5, 10, 1);
        super(cell.getName(), true);
        // Animations are setup in the super class

        WonderlandSession session = cell.getCellCache().getSession();
        ServerSessionManager manager = LoginManager.find(session);
        String serverHostAndPort = manager.getServerNameAndPort();

        setBaseURL("wla://avatarbase@"+serverHostAndPort+"/");

        String avatarDetail = System.getProperty("avatar.detail", "high");

//        if (avatarDetail.equalsIgnoreCase("low")) {
//            setUseSimpleStaticModel(true, null);
//        } else if (avatarDetail.equalsIgnoreCase("medium")) {
////            try {
//////                URL model = new URL("wla://avatarbase@"+serverHostAndPort+"/TeapotAvatar.dae");
////                URL model = new URL("wla://avatarbase@"+serverHostAndPort+"/CylinderMan.dae");
////
////                ColladaEnvironment loader = new ColladaEnvironment(ClientContextJME.getWorldManager(), model, "SimpleAvatar");
////                System.err.println("--------------> LOADED <----------------");
////                setUseSimpleStaticModel(true, loader.getPScene());
////            } catch(MalformedURLException e) {
//////                logger.warning("Unable to load model");
////                e.printStackTrace();
////                setUseSimpleStaticModel(true, null);
////            }
//            setUseSimpleStaticModel(true, null);
//        } else {
//            // High is the default
//        }

    }


}
