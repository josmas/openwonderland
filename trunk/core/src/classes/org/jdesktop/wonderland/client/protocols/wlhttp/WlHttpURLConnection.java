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
package org.jdesktop.wonderland.client.protocols.wlhttp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.assetmgr.Asset;
import org.jdesktop.wonderland.client.assetmgr.AssetManager;
import org.jdesktop.wonderland.common.WlHttpURI;

/**
 * The WlHttpURLConnection class is the URL connection to URLs that have the
 * 'wlhttp'. There are used to handle 'http' url's in the asset manager.
 * 
 * @author paulby
 */
public class WlHttpURLConnection extends URLConnection {

    /**
     * Constructor, takes the 'wla' protocol URL as an argument
     * 
     * @param url A URL with a 'wla' protocol
     */
    public WlHttpURLConnection(URL url) {
        super(url);
    }
    
    @Override
    public void connect() throws IOException {
    }
    
    @Override
    public InputStream getInputStream() {
        try {
            // First create a factory to handle the loading from a url
            WlHttpURI uri = new WlHttpURI(this.url.toExternalForm());

            // Next, we ask the asset manager for the asset and wait for it to
            // be loaded
            Asset asset = AssetManager.getAssetManager().getAsset(uri);
            if (asset == null || AssetManager.getAssetManager().waitForAsset(asset) == false) {
                return null;
            }
            return new FileInputStream(asset.getLocalCacheFile());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WlHttpURLConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException excp) {
            Logger.getLogger(WlHttpURLConnection.class.getName()).log(Level.SEVERE, null, excp);
        } catch (MalformedURLException excp) {
            Logger.getLogger(WlHttpURLConnection.class.getName()).log(Level.SEVERE, null, excp);
        }
        return null;
        
    }
}
