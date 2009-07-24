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

package org.jdesktop.wonderland.client.assetmgr;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.AssetURI;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The AssetRepositoryFactory class is an abstract base class that is used by
 * the Asset Manager to fetch a list of repositories from which to attempt to
 * download an asset. This class returns an ordered list of asset repositories
 * to try.
 * <p>
 * This class is typically extended to support loading assets from a particular
 * kind of location. For example, this class is subclassed to load assets from
 * modules on a server, or from HTTP URLs over the network
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@InternalAPI
public abstract class AssetRepositoryFactory {

    protected static Logger logger = Logger.getLogger(AssetRepositoryFactory.class.getName());
    private AssetURI assetURI = null;

    /**
     * Constructor that takes the AssetURI to be downloaded
     */
    public AssetRepositoryFactory(AssetURI assetURI) {
        this.assetURI = assetURI;
    }

    /**
     * Returns the asset URI to download using this factory.
     *
     * @return An AssetURI object
     */
    public AssetURI getAssetURI() {
        return assetURI;
    }

    /**
     * Returns the desired checksum information of the asset.
     */
    public abstract String getDesiredChecksum();

    /**
     * Returns an ordered list of AssetRepository objects that can be used
     * to fetch assets.
     *
     * @param An array of AssetRepository objects
     */
    public abstract AssetRepository[] getAssetRepositories();
}
