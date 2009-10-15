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

package org.jdesktop.wonderland.client.assetmgr.http;

import java.util.List;
import org.jdesktop.wonderland.client.assetmgr.Asset;
import org.jdesktop.wonderland.client.assetmgr.AssetCache;
import org.jdesktop.wonderland.client.assetmgr.AssetCacheException;
import org.jdesktop.wonderland.client.assetmgr.AssetManager;
import org.jdesktop.wonderland.client.assetmgr.AssetRepository;
import org.jdesktop.wonderland.client.assetmgr.AssetRepositoryFactory;
import org.jdesktop.wonderland.common.AssetURI;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.WlHttpURI;

/**
 * A factory that returns an ordered list of asset repositories for an asset
 * that is contained within a module.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@InternalAPI
public class WlHttpAssetRepositoryFactory extends AssetRepositoryFactory {

    private long lastModified = 0;

    public WlHttpAssetRepositoryFactory(AssetURI assetURI) {
        super(assetURI);
        lastModified = getAsLastModified(getChecksumFromDB(assetURI));
        isAlwaysDownload = true;
    }

    /**
     * @inheritDoc()
     */
    @Override
    public String getDesiredChecksum() {
        return "" + lastModified;
    }

    /**
     * @inheritDoc()
     */
    @Override
    public AssetRepository[] getAssetRepositories() {
        // Create the one asset repository for the base URL of the server
        AssetRepository[] assetRepositories = new AssetRepository[1];
        WlHttpURI uri = (WlHttpURI)getAssetURI();
        String baseURL = uri.getBaseURL();
        String checksum = getDesiredChecksum();
        assetRepositories[0] = new WlHttpAssetRepository(baseURL, getAsLastModified(checksum));

        return assetRepositories;
    }

    /**
     * Convert a string checksum into a last modified date, returns 0 if the
     * string is not properly formatted.
     */
    private long getAsLastModified(String checksum) {
        // First check whether it is null or an empty string, return -1
        if (checksum == null || checksum.equals("") == true) {
            return 0;
        }

        try {
            return Long.parseLong(checksum);
        } catch (java.lang.NumberFormatException excp) {
            logger.warning("Checksum from asset is not a valid last modified" +
                    " time " + checksum);
            return 0;
        }
    }

    /**
     * Asks the asset database for the checksum
     */
    private String getChecksumFromDB(AssetURI assetURI) {
        // Query the asset database for an entry with this URI, looking for
        // the most recent checksum. This assumes that, although we are returned
        // a list of assets, there is only one asset, because it is the policy
        // to have only one wlhttp entry per cache.
        AssetCache assetCache = AssetManager.getAssetManager().getAssetCache();
        List<Asset> assetList = null;
        try {
            assetList = assetCache.getAssetList(assetURI);
        } catch (AssetCacheException excp) {
        }
        
        if (assetList.size() > 1) {
            logger.warning("Found more than one asset for " +
                    assetURI.toExternalForm());
        }
        if (assetList.size() == 0) {
            logger.fine("Unable to find asset in cache for " +
                    assetURI.toExternalForm());
            return null;
        }
        Asset asset = assetList.get(0);
        return asset.getChecksum();
    }
}
