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
package org.jdesktop.wonderland.client.protocols.wltexture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.datamgr.Asset;
import org.jdesktop.wonderland.client.datamgr.AssetManager;
import org.jdesktop.wonderland.client.datamgr.Repository;
import org.jdesktop.wonderland.common.AssetType;

/**
 * URLConnection for the wlasset protocol
 * 
 * @author paulby
 */
public class WlTextureURLConnection extends URLConnection {

    // wltexture://host/<asset path and filename>#host path
    
    // http://host/host path/asset path and filename
    
    public WlTextureURLConnection(URL url) {
        super(url);
    }
    
    @Override
    public void connect() throws IOException {
        System.out.println("Connect to "+url);
    }
    
    @Override
    public InputStream getInputStream() {
        try {
            System.out.println("Asset "+url);
            
            Repository repository = new Repository(new URL("http://" + url.getHost()+"/"+trimSlash(url.getRef())));

            String fileStr = url.getFile();
            trimSlash(fileStr);
            
            System.out.println("ASSET "+repository.getOriginalRepository()+"   "+fileStr+"   orig "+url.getFile());
            
            Asset asset = AssetManager.getAssetManager().getAsset(AssetType.IMAGE, repository, fileStr, null);
            if (!AssetManager.getAssetManager().waitForAsset(asset))
                return null;
            
            
            return new FileInputStream(asset.getLocalCacheFile());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WlTextureURLConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(WlTextureURLConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }
    
    /**
     * Trim leading / from str and return str.
     * @param str
     * @return
     */
    private String trimSlash(String str) {
        int trim = 0;
        while(str.charAt(trim)=='/')
            trim++;
        if (trim!=0)
            str = str.substring(trim);
        return str;
    }

}
