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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.jdesktop.wonderland.common.AssetType;
import org.jdesktop.wonderland.common.AssetURI;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The Asset class represents an asset (e.g. artwork) in the system. An asset
 * is uniquely identified by a combination of its URI (see AssetURI class) and
 * an optional checksum. Assets with no checksum are considered the same asset.
 * <p>
 * Each asset has a type: typically, either file, image, or model and given by
 * the AssetType enumeration.
 * <p>
 * Each asset 
 * The url gives the full URL from which the asset was downloaded
 * <p>
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public abstract class Asset<T> {
    protected AssetType type = null;
    protected AssetURI assetURI = null;
    protected File localCacheFile = null;
    protected String checksum = null;
    protected String baseURL = null;
    protected ArrayList<AssetReadyListener> listeners = null;
    protected String failureInfo = null;

    /**
     * Constructor that takes the unique URI as an argument.
     * 
     * @param assetURI The unique identifying asset URI.
     */
    public Asset(AssetID assetID) {
        this.assetURI = assetID.getAssetURI();
        this.checksum = assetID.getChecksum();
    }

    /**
     * Returns the asset type, typically either a file, image, or model.
     * 
     * @return The type of asset
     */
    public AssetType getType() {
        return type;
    }

    /**
     * Returns the unique URI describing the asset.
     * 
     * @return The unique URI describing the asset
     */
    public AssetURI getAssetURI() {
        return this.assetURI;
    }
    
    /**
     * Return the file containing the local cache of the asset
     * 
     * @return
     */
    public File getLocalCacheFile() {
        return localCacheFile;
    }

    void setLocalCacheFile(File localCacheFile) {
        this.localCacheFile = localCacheFile;
    }

    /**
     * Returns the local cache file as a URL, or null if the asset is not
     * cached.
     *
     * @return The local cache file as a URL
     * @throw MalformedURLException If the URL is malformed
     */
    public URL getLocalCacheFileAsURL() throws MalformedURLException {
        if (localCacheFile != null) {
            String fname = AssetManager.encodeSpaces(localCacheFile.getAbsolutePath());
            return new URL("file://" + fname);
        }
        return null;
    }


    /**
     * Get the checksum of this file in the local cache.
     * @return
     */
    public String getChecksum() {
        return checksum;
    }

    void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * Returns the base URL from which the asset was downloaded, null if unknown
     *
     * @return The base URL
     */
    public String getBaseURL() {
        return baseURL;
    }

    void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
    
    @Override
    public String toString() {
        return "(" + this.getAssetURI().toString() + " @ " + this.checksum + ")";
    }
    
    /**
     * Called whenever the asset has been downloaded from the server
     */
    abstract void postProcess();

    /**
     * Load and return an asset from the local cache. Multiple instances
     * of the same asset can be shared this call will implement the necessary
     * sharing.
     * 
     * Returns true if load was succesful, otherwise returns false.
     */
    abstract boolean loadLocal();
    
    /**
     * Asset has been unloaded, cleanup.
     */
    abstract void unloaded();
    
    /**
     * Return the asset
     * @return
     */
    public abstract T getAsset();
    
    /**
     * Used to recieve notification when an asset load has been completed or
     * has failed. Register with Asset.addAssetReadyListener().
     */
    @ExperimentalAPI
    public interface AssetReadyListener {
        /**
         * Called when the asset is ready for use
         * @param asset The asset loaded
         */
        public void assetReady(Asset asset);

        /**
         * Called when loading the asset has failed, with the given reason
         * @param asset The asset that has failed to load
         * @param reason The reason why the asset has failed to load
         */
        public void assetFailure(Asset asset, String reason);
    }

    /**
     * Notify listeners waiting for asset to be downloaded, if failureInfo
     * is set will call assetFailure, otherwise it will call assetReady
     * @param asset
     */
    void notifyAssetReadyListeners() {
        if (listeners == null)
            return;

        synchronized (listeners) {
            if (failureInfo == null) {
                for (AssetReadyListener listener : listeners)
                    listener.assetReady(this);
            } else {
                for (AssetReadyListener listener : listeners)
                    listener.assetFailure(this, failureInfo);
            }
        }
    }

    public void addAssetReadyListener(AssetReadyListener listener) {
        if (listeners == null)
            listeners = new ArrayList();
        synchronized (listeners) {
            listeners.add(listener);
            if (localCacheFile != null)
                listener.assetReady(this);
        }
    }

    public void removeAssetReadyListener(AssetReadyListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public String getFailureInfo() {
        return failureInfo;
    }

    public void setFailureInfo(String failureInfo) {
        this.failureInfo = failureInfo;
    }  
}
