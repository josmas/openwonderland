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

import com.jme.bounding.BoundingSphere;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.util.resource.ResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.model.collada.ColladaImporter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.datamgr.Asset;
import org.jdesktop.wonderland.client.datamgr.AssetManager;
import org.jdesktop.wonderland.client.datamgr.Repository;
import org.jdesktop.wonderland.common.AssetType;

/**
 *
 * @author paulby
 */
public class CellModule implements RenderModule {

    private static final Logger logger = Logger.getLogger(CellModule.class.getName());
    
    public void init(RenderInfo info) {
        try {
            Node model = loadStaticCollada(new URL("file:////home/paulby/local-code/java.net/wonderland/branches/bringup/core/mpk20.dae"), new Vector3f());
//            Node model = loadStaticCollada(new URL("file:////home/paulby/local-code/java.net/wonderland/branches/bringup/core/tmp-avatar.dae"), new Vector3f());
            model.setModelBound(new BoundingSphere());
            model.updateModelBound();
            model.lock();
            info.getRoot().attachChild(model);
        } catch (IOException ex) {
            Logger.getLogger(CellModule.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    public void update(RenderInfo info, float interpolation) {
        // do nothing
    }

    public void render(RenderInfo info, float interpolation) {
        // do nothing
    }
    
    private Node loadStaticCollada(URL asset, Vector3f origin) throws IOException {
        ResourceLocatorTool.addResourceLocator(
                ResourceLocatorTool.TYPE_TEXTURE,
                new WonderlandResourceLocator());

        InputStream mobboss = asset.openStream();
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
        
        Node node = new Node();
        node.setLocalTranslation(origin);
        node.setLocalRotation(new Quaternion().fromAngleAxis((float)-Math.PI/2, new Vector3f(1,0,0)));
//        node.setLocalScale(0.5f);
        //attach the skeleton and the skin to the rootnode. Skeletons could possibly
        //be used to update multiple skins, so they are seperate objects.
        node.attachChild(ColladaImporter.getModel());
        
        node.setModelBound(new BoundingSphere());
        node.updateModelBound();
        
        //clean up the importer as we are about to use it again.
        ColladaImporter.cleanUp();
        
        return node;
        
    }

    class WonderlandResourceLocator implements ResourceLocator {

        private Repository repository;
        private HashMap<String, URL> loadedTextures = new HashMap();
        
        public WonderlandResourceLocator() {
            try {
//                repository = new Repository(new URL("file:///home/paulby/local-code/java.net/lg3d/trunk/lg3d-wonderland-art/compiled_models"));
//                repository = new Repository(new URL("file:///home/paulby/local-code/java.net/wonderland/branches/bringup/core"));
                repository = new Repository(new URL("http://192.18.37.42/compiled_models/"));
            } catch (MalformedURLException ex) {
                Logger.getLogger(CellModule.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public URL locateResource(String filename) {
            //Check if we already processed this texture
            URL ret=loadedTextures.get(filename);
              
            if (ret==null) {
                AssetManager assetManager = AssetManager.getAssetManager();
                Asset asset = assetManager.getAsset(AssetType.IMAGE, repository, filename, null);
                assetManager.waitForAsset(asset);

                System.out.println("Looking for Texture " + filename);

                try {
                    File f = asset.getLocalCacheFile();
                    if (f==null)
                        return null;
                    ret = f.toURI().toURL();
                    loadedTextures.put(ret.getFile(), ret);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(CellModule.class.getName()).log(Level.SEVERE, null, ex);
                    ret = null;
                }
            }


            return ret;
              
        }
        
    }
}
