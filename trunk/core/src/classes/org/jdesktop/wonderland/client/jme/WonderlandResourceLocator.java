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
import org.jdesktop.wonderland.client.datamgr.Repository;
import org.jdesktop.wonderland.common.AssetType;

/**
 * Resource Locator for wonderland.
 * This uses the AssetManager to locate a resource and returns an
 * asset URL, one of wltexture: ....
 * 
 * @author paulby
 */
public class WonderlandResourceLocator implements ResourceLocator {

    private Repository repository;

    public WonderlandResourceLocator() {
        try {
//                repository = new Repository(new URL("file:///home/paulby/local-code/java.net/lg3d/trunk/lg3d-wonderland-art/compiled_models"));
//                repository = new Repository(new URL("file:///home/paulby/local-code/java.net/wonderland/branches/bringup/core"));
            repository = new Repository(new URL("http://192.18.37.42/compiled_models/"));
        } catch (MalformedURLException ex) {
            Logger.getAnonymousLogger().log(Level.SEVERE, null, ex);
        }
    }

    public URL locateResource(String filename) {
        URL ret;

        int trim = 0;
        while(filename.charAt(trim)=='/')
            trim++;
        if (trim!=0)
            filename = filename.substring(trim);

        AssetManager assetManager = AssetManager.getAssetManager();
        Asset asset = assetManager.getAsset(AssetType.IMAGE, repository, filename, null);

        try {
            ret = new URL("wltexture://"+repository.getOriginalRepository().getHost()+"/"+filename+"#"+repository.getOriginalRepository().getFile());
        } catch (MalformedURLException ex) {
            Logger.getAnonymousLogger().log(Level.SEVERE, null, ex);
            ret = null;
        }

        return ret;              
    }

}
