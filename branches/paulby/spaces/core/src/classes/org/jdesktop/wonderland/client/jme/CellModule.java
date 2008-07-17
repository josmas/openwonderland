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
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Spatial;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureKey;
import com.jme.util.TextureManager;
import com.jme.util.export.binary.BinaryExporter;
import com.jme.util.export.binary.BinaryImporter;
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
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.common.AssetType;

/**
 *
 * @author paulby
 */
public class CellModule extends RenderModule {

    private static final Logger logger = Logger.getLogger(CellModule.class.getName());
    private Node model;
    
    public void init(RenderInfo info) {
        try {
            Repository repository = new Repository(new URL("http://192.18.37.42/"));

            Asset asset = AssetManager.getAssetManager().getAsset(AssetType.FILE, repository, "mpk20.jme", null);
            AssetManager.getAssetManager().waitForAsset(asset);
            URL url = asset.getLocalCacheFile().toURI().toURL();
            
            model = Loaders.loadJMEBinary(url, new Vector3f());
//            model = Loaders.loadJMEBinary(new URL("file:///home/paulby/local-code/java.net/wonderland/branches/bringup/core/mpk20.jme"), new Vector3f());
//            model = loadStaticCollada(new URL("file:///home/paulby/local-code/java.net/wonderland/branches/bringup/core/mpk20.dae"), new Vector3f());
//            model = loadStaticCollada(new URL("file:///home/paulby/local-code/java.net/wonderland/branches/bringup/core/mannikin.dae"), new Vector3f());

//            fixTextureKeys(model);
            model.setModelBound(new BoundingSphere());
            model.updateModelBound();
//            File file = new File("tmp.jme");
//            BinaryExporter.getInstance().save(model, file);

            model.lock();
            
            TextureManager.preloadCache(info.getDisplay().getRenderer());
        } catch (IOException ex) {
            Logger.getLogger(CellModule.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private void printTextureKeys(Node node) {
        TreeScan.findNode(node, new ProcessNodeInterface() {

            public boolean processNode(SceneElement node) {
                if (node instanceof Spatial) {
                    TextureState ts = (TextureState)node.getRenderState(RenderState.RS_TEXTURE);
                    if (ts==null)
                        return true;
                    Texture t = ts.getTexture();
                    if (t==null)
                        return true;
                    TextureKey key = t.getTextureKey();
                    System.out.println("key "+key.getLocation());
                    
                }
                
                return true;
            }
            
        });
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

    public void setActiveImpl(boolean active, RenderInfo info) {
        if (active)
            info.getRoot().attachChild(model);
        else
            info.getRoot().detachChild(model);
        
    }

  
}
