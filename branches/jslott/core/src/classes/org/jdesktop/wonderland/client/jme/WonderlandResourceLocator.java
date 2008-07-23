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

import com.jme.util.resource.ResourceLocator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.datamgr.Asset;
import org.jdesktop.wonderland.client.datamgr.AssetManager;
import org.jdesktop.wonderland.common.AssetType;
import org.jdesktop.wonderland.common.AssetURI;

/**
 * The WonderlandResourceLocator 
 * Resource Locator for wonderland.
 * This uses the AssetManager to locate a resource and returns an
 * asset URL, one of wltexture: ....
 * 
 * @author paulby
 */
public class WonderlandResourceLocator implements ResourceLocator {

    private String assetURI;

    public WonderlandResourceLocator() {
//        try {
//                repository = new Repository(new URL("file:///home/paulby/local-code/java.net/lg3d/trunk/lg3d-wonderland-art/compiled_models"));
//                repository = new Repository(new URL("file:///home/paulby/local-code/java.net/wonderland/branches/bringup/core"));
            assetURI = "http://192.18.37.42/compiled_models/";
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(CellModule.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public URL locateResource(String filename) {
        System.out.println("LOCATING TEXTURE: " + filename);
        
        URL ret;

        int trim = 0;
        while(filename.charAt(trim)=='/')
            trim++;
        if (trim!=0)
            filename = filename.substring(trim);

 

        try {
            AssetURI uri = new AssetURI(assetURI + "/" + filename);
            AssetManager assetManager = AssetManager.getAssetManager();
            Asset asset = assetManager.getAsset(uri, AssetType.IMAGE);
            //ret = new URL("wltexture://"+repository.getOriginalRepository().getHost()+"/"+filename+"#"+repository.getOriginalRepository().getFile());
            ret = new URL(""); // XXX
        } catch (Exception ex) {
            Logger.getLogger(CellModule.class.getName()).log(Level.SEVERE, null, ex);
            ret = null;
        }

        return ret;              
    }

}
