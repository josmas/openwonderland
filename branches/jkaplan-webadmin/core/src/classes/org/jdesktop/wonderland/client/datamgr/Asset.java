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
package org.jdesktop.wonderland.client.datamgr;

import java.io.File;
import java.util.ArrayList;
import org.jdesktop.wonderland.client.datamgr.AssetManager.AssetReadyListener;
import org.jdesktop.wonderland.client.datamgr.AssetManager.Checksum;
import org.jdesktop.wonderland.common.AssetType;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An asset in the system
 * 
 * @author paulby
 */
@ExperimentalAPI
public abstract class Asset<T> {
    protected AssetType type=null;
    protected Repository r=null;
    protected String filename=null;
    protected File localCacheFile=null;
    protected Checksum localChecksum=null;
    
    protected ArrayList<AssetReadyListener> listeners = null;
    
    protected String failureInfo = null;
    
    public Asset(Repository r, 
            String filename) {
        this.r = r;
        this.filename = filename;
    }

    public AssetType getType() {
        return type;
    }

    public Repository getRepository() {
        return r;
    }

    /**
     * Path and filename of the asset. This is appended to the URL
     * so use / not File.seperator
     * @return
     */
    public String getFilename() {
        return filename;
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
     * Get the checksum of this file in the local cache.
     * @return
     */
    public Checksum getLocalChecksum() {
        return localChecksum;
    }

    void setLocalChecksum(Checksum checksum) {
        this.localChecksum = checksum;
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
     * Notify listeners waiting for asset to be downloaded, if failureInfo
     * is set will call assetFailure, otherwise it will call assetReady
     * @param asset
     */
    void notifyAssetReadyListeners() {
        if (listeners==null)
            return;
        
        synchronized(listeners) {
            if (failureInfo==null) {
                for(AssetReadyListener listener : listeners)
                    listener.assetReady(this);
            } else {
                for(AssetReadyListener listener : listeners)
                    listener.assetFailure(this, failureInfo);
            }
        }
    }
    
    public void addAssetReadyListener(AssetReadyListener listener) {
        if (listeners==null)
            listeners = new ArrayList();
        synchronized(listeners) {
            listeners.add(listener);
            if (localCacheFile!=null)
                listener.assetReady(this);
        }
    }
    
    public void removeAssetReadyListener(AssetReadyListener listener) {
        synchronized(listeners) {
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
