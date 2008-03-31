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
package org.jdesktop.wonderland.client.jme;

import com.jme.animation.AnimationController;
import com.jme.animation.Bone;
import com.jme.animation.BoneAnimation;
import com.jme.animation.SkinNode;
import com.jme.bounding.BoundingSphere;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.jmex.model.collada.ColladaImporter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author paulby
 */
public class Loaders {
    
    private static final Logger logger = Logger.getLogger(Loaders.class.getName());

    public static Node loadJMEBinary(URL asset, Vector3f origin) {
        try {
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new WonderlandResourceLocator());

            Node ret;

            ret = (Node) BinaryImporter.getInstance().load(asset);

            return ret;
        } catch (IOException ex) {
            Logger.getLogger(CellModule.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static Node loadColladaAvatar(Vector3f origin) throws IOException {
        AnimationController ac;
        
        try {
            ResourceLocatorTool.addResourceLocator(
                    ResourceLocatorTool.TYPE_TEXTURE,
                    new SimpleResourceLocator(Loaders.class.getClassLoader().getResource(
                    "org/jdesktop/wonderland/client/resources/collada/")));
        } catch (URISyntaxException e1) {
            logger.warning("Unable to add texture directory to RLT: " + e1.toString());
        }

        KeyBindingManager.getKeyBindingManager().set("bones", KeyInput.KEY_SPACE);

        //this stream points to the model itself.
        InputStream mobboss = Loaders.class.getClassLoader().getResourceAsStream("org/jdesktop/wonderland/client/resources/collada/man.dae");
//        InputStream mobboss = JmeTest.class.getClassLoader().getResourceAsStream("org/jdesktop/wonderland/client/resources/collada/man_waving.dae");
//        InputStream mobboss = new URL("file:///media/TRAVELDRIVE/Idle-test.dae").openStream();
        //this stream points to the animation file. Note: You don't necessarily
        //have to split animations out into seperate files, this just helps.
        InputStream animation = Loaders.class.getClassLoader().getResourceAsStream("org/jdesktop/wonderland/client/resources/collada/man_walk.dae");
//        InputStream animation = JmeTest.class.getClassLoader().getResourceAsStream("org/jdesktop/wonderland/client/resources/collada/man_waving.dae");
        if (mobboss == null) {
            logger.info("Unable to find file, did you include jme-test.jar in classpath?");
            System.exit(0);
        }
        //tell the importer to load the mob boss
        ColladaImporter.load(mobboss, "model");
        //we can then retrieve the skin from the importer as well as the skeleton
        
//        for(String s : ColladaImporter.getSkinNodeNames())
//            System.out.println("Skin "+s);
//        for(String s : ColladaImporter.getSkeletonNames())
//            System.out.println("Skel "+s);
        
        SkinNode sn = ColladaImporter.getSkinNode(ColladaImporter.getSkinNodeNames().get(0));
        Bone skel = ColladaImporter.getSkeleton(ColladaImporter.getSkeletonNames().get(0));
        //clean up the importer as we are about to use it again.
        ColladaImporter.cleanUp();

        //load the animation file.
        ColladaImporter.load(animation, "anim");
        //this file might contain multiple animations, (in our case it's one)
        ArrayList<String> animations = ColladaImporter.getControllerNames();
        if (animations != null) {
            logger.info("Number of animations: " + animations.size());
            for (int i = 0; i < animations.size(); i++) {
                logger.info(animations.get(i));
            }
            //Obtain the animation from the file by name
            BoneAnimation anim1 = ColladaImporter.getAnimationController(animations.get(0));

            //set up a new animation controller with our BoneAnimation
            ac = new AnimationController();
            ac.addAnimation(anim1);
            ac.setRepeatType(Controller.RT_WRAP);
            ac.setActive(true);
            ac.setActiveAnimation(anim1);

            //assign the animation controller to our skeleton
            skel.addController(ac);
        }

        
        Node node = new Node();
        node.setLocalTranslation(origin);
        node.setLocalRotation(new Quaternion().fromAngleAxis((float)-Math.PI/2, new Vector3f(1,0,0)));
        node.setLocalScale(0.5f);
        //attach the skeleton and the skin to the rootnode. Skeletons could possibly
        //be used to update multiple skins, so they are seperate objects.
        node.attachChild(sn);
        node.attachChild(skel);
        
        node.setModelBound(new BoundingSphere());
        node.updateModelBound();
        
        ColladaImporter.cleanUp();
        
        return node;
    }
}
