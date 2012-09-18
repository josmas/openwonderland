/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.assetmgr;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.assetmgr.AssetStream.AssetResponse;
import org.jdesktop.wonderland.common.AssetURI;

/**
 * Used to load assets in parallel. This class implements the Callable
 * interface and is run inside of a Java Executer. The class can load
 * assets from both a remote repository and the local file cache, as given
 * by the 'server' flag.
 */
public class AssetLoader implements Callable {
    /* The asset to load */
    private final Asset asset;
    /* The factory which tells us how to download the asset */
    private final AssetRepositoryFactory factory;
    /* Object reflecting the results of the asynchronous operation */
    private Future future = null;
    private final AssetManager outer;

    
    private static final Logger logger = Logger.getLogger(AssetLoader.class.getName());
    
    /**
     * Load a given asset, either from local cache or the server.
     *
     * @param asset The asset to load
     * @param server true loads from server, false for client local cache
     */
    /**
     * Load a given asset, either from local cache or the server.
     *
     * @param asset The asset to load
     * @param server true loads from server, false for client local cache
     */
    public AssetLoader(Asset asset, AssetRepositoryFactory factory, final AssetManager outer) {
        this.outer = outer;
        this.asset = asset;
        this.factory = factory;
    }

    /**
     * Return the asset this loader is loading
     *
     * @return The asset
     */
    public Asset getAsset() {
        return this.asset;
    }

    /**
     * Returns the object representing the state of the asynchronous task
     *
     * @return The Future status object
     */
    Future getFuture() {
        return this.future;
    }

    /**
     * Sets the object representing the state of the asynchronous task.
     * Typically this is called by the thread that kicks off the task and
     * sets the Future object returns by the Java Executer service.
     *
     * @param future The Future status object
     */
    void setFuture(Future future) {
        this.future = future;
    }

    /**
     * Called by the asynchronous task service to attempt to load the asset.
     * Returns the asset upon success, null upn failure.
     *
     * @return Upon success returns the asset, null upon failure
     * @throws java.lang.Exception
     */
    public Object call() throws Exception {
        try {
            // Do the asset download from the server. If the asset is
            // already cached then doAssetDownload() will detect this. The
            // failure information of the asset download is set here as is
            // notifying the asset ready listeners.
            Object ret = doAssetDownload();
            return ret;
        } catch (java.lang.Exception excp) {
            logger.log(Level.WARNING, "Exception in call()", excp);
            throw excp;
        }
    }

    /**
     * Downloads the asset from the server and returns the asset upon success
     * or null upon failure
     */
    private Object doAssetDownload() {
        AssetURI assetURI = asset.getAssetURI();
        String uriString = assetURI.toExternalForm();
        // Keep a copy of the original asset checksum. Sometimes (e.g. in
        // the case of HTTP if-modified-since) the "checksum" can change
        // after we have downloaded. We need to make sure we remove the
        // proper thing from the "loading" and "loaded" lists.
        String originalChecksum = asset.getChecksum();
        // Using the repository factory, fetch the list of repositories
        // from which to fetch the asset. It is up to each of the
        // individual repositories to determine whether the asset is
        // already cached or not.
        AssetRepository[] repositories = factory.getAssetRepositories();
        logger.fine("Got a list of repositories " + repositories + " for asset " + uriString);
        for (AssetRepository repository : repositories) {
            logger.fine("Seeing if repository " + repository.toString() + " has asset " + assetURI);
            long startTime = System.currentTimeMillis();
            // Try to open the output stream. If the repository tells
            // us we already have the most up-to-date version, then we
            // simply return that. Otherwise, we attempt to download
            // the asset.
            AssetStream stream = repository.openAssetStream(assetURI);
            AssetResponse response = stream.getResponse();
            logger.fine("Got an asset stream with response " + response + " for asset " + uriString);
            // record statistic
            long streamTime = System.currentTimeMillis() - startTime;
            outer.getStatsProvider().assetStatistic(assetURI, AssetManager.AssetStat.OPEN_STREAM, streamTime);
            startTime = System.currentTimeMillis();
            if (response == AssetResponse.ASSET_CACHED) {
                // The asset is already cache, so we just return that
                // version. We first need to set up the location of the
                // cache file first
                AssetID assetID = new AssetID(assetURI, asset.getChecksum());
                asset.setLocalCacheFile(new File(outer.getAssetCache().getAssetCacheFileName(assetID)));
                Asset out = outer.loadAssetFromCache(asset, originalChecksum);
                // statistic
                long cacheTime = System.currentTimeMillis() - startTime;
                outer.getStatsProvider().assetStatistic(assetURI, AssetManager.AssetStat.GET_FROM_CACHE, cacheTime);
                return out;
            } else if (response == AssetResponse.STREAM_READY) {
                // The asset stream is ready to be downloaded, so we go
                // ahead and download the asset. Once we do that we
                // need to add the asset to the cache and then fetch
                // it from the cache.
                try {
                    stream.open();
                    outer.loadAssetFromServer(asset, stream);
                    outer.getAssetCache().addAsset(asset, stream.getCachePolicy());
                    stream.close();
                    Asset out = outer.loadAssetFromCache(asset, originalChecksum);
                    // statistic
                    long serverTime = System.currentTimeMillis() - startTime;
                    outer.getStatsProvider().assetStatistic(assetURI, AssetManager.AssetStat.GET_FROM_SERVER, serverTime);
                    return out;
                } catch (java.io.IOException excp) {
                    logger.log(Level.WARNING, "Failed to download asset " + "from this stream " + uriString, excp);
                    continue;
                } catch (AssetCacheException excp) {
                    logger.log(Level.WARNING, "Failed to cache downloaded" + " asset " + uriString, excp);
                    continue;
                }
            } else {
                // We did not find a valid repository to load from,
                // so we will just go into the next one
                continue;
            }
        }
        // if we got here, the asset was not loaded from any of the
        // repositories, so it has failed
        asset.setDownloadFailure("Unable to load from any repositories");
        return null;
    }
    
}
