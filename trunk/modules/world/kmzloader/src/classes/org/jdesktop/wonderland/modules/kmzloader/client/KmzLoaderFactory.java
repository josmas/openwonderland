/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

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
package org.jdesktop.wonderland.modules.kmzloader.client;

import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.content.ContentImportManager;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
import org.jdesktop.wonderland.client.jme.artimport.ModelLoaderFactory;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.ModelDndContentImporter;

/**
 *
 * @author paulby
 */
public class KmzLoaderFactory extends ModelLoaderFactory
    implements ClientPlugin
{
    /** A BaseClientPlugin that delegate activate and deactivate to the parent */
    private BaseClientPlugin plugin;
    private ModelDndContentImporter importer;

    public void initialize(ServerSessionManager loginManager) {
        LoaderManager.getLoaderManager().registerLoader(this);
        this.importer = new ModelDndContentImporter(loginManager, new String[] {getFileExtension()});
        this.plugin = new BaseClientPlugin() {
            @Override
            protected void activate() {
                KmzLoaderFactory.this.register();
            }

            @Override
            protected void deactivate() {
                KmzLoaderFactory.this.unregister();
            }
        };

        plugin.initialize(loginManager);
    }

    public void cleanup() {
        LoaderManager.getLoaderManager().unregisterLoader(this);
        plugin.cleanup();
    }

    protected void register() {
        LoaderManager.getLoaderManager().activateLoader(this);
        ContentImportManager cim = ContentImportManager.getContentImportManager();
        cim.registerContentImporter(importer);
    }

    protected void unregister() {
        LoaderManager.getLoaderManager().deactivateLoader(this);
        ContentImportManager cim = ContentImportManager.getContentImportManager();
        cim.unregisterContentImporter(importer);
    }

    public String getFileExtension() {
        return "kmz";
    }

    public ModelLoader getLoader() {
        return (ModelLoader) new KmzLoader();
    }

    @Override
    public String getLoaderClassname() {
        return KmzLoader.class.getName();
    }
    
    /**
     * Find a relative path between two files
     * @param model the model path
     * @param texture the texture path
     * @return the location of the texture relative to the model
     */
    public static String getRelativePath(String model, String texture) {
        String[] modelParts = model.split("/");
        String[] textureParts = texture.split("/");
        
        StringBuilder out = new StringBuilder();
        
        // eliminate all common directories
        int index = 0;
        while (index < Math.min(modelParts.length, textureParts.length) &&
               modelParts[index].equals(textureParts[index])) 
        {
            index++;
        }
        
        // how many segments are left in the model?
        if (index == modelParts.length - 1) {
            // we are at the model file
            out.append("./");
        } else {
            // we need to back up some
            for (int i = index; i < modelParts.length - 1; i++) {
                out.append("../");
            }
        }
        
        // add anything left in the image path
        for (int i = index; i < textureParts.length - 1; i++) {
            out.append(textureParts[i]).append("/");
        }
        out.append(textureParts[textureParts.length - 1]);
        
        return out.toString();
    }
}
