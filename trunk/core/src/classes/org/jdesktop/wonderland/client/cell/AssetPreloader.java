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
package org.jdesktop.wonderland.client.cell;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * A cell that implements AssetPreloader provides a list of assets to
 * download before the cell is loaded. This speeds up cell loading since
 * assets can be downloaded in parallel.
 * 
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public interface AssetPreloader {
    /**
     * Provide an initial list of assets to load
     * @return a list of the assets for this cell, or an empty list
     * if no assets are available.
     */
    public List<URL> getAssets();
    
    /**
     * Called whenever an asset is finished loading. This method returns an
     * additional list of assets to load based on the contents of the newly
     * added file.
     * @url the url that the asset was loaded from
     * @param loaded the InputStream from the loaded URL
     * @return a list of additional URLs to load, or null if there
     * are no more URLs that need loading
     */
    public List<URL> assetLoaded(URL url, InputStream loaded);
}
