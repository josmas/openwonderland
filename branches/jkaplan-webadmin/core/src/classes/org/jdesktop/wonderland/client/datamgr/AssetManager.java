/**
 * Project Wonderland
 *
 * $RCSfile:$
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.client.datamgr;

import org.jdesktop.wonderland.client.repository.Repository;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.datamgr.TrackingInputStream.ProgressListener;
import org.jdesktop.wonderland.client.repository.RepositoryFactory;
import org.jdesktop.wonderland.common.AssetType;
import org.jdesktop.wonderland.common.AssetURI;
import org.jdesktop.wonderland.common.Checksum;
import org.jdesktop.wonderland.common.ChecksumSha1;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.config.WonderlandConfigUtil;

/**
 * AssetManager provides services for downloading and maintaining the latest
 * version of asset data for the system. Primary use is for Images (Textures) and
 * geometry files of various types.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class AssetManager {
    
    /*
     * The version of the asset manager, controls the location of the cache file:
     * Wonderland v0.3-v0.4: No version
     * Wonderland v0.4: Version 2
     */
    private static final int AM_VERSION = 2;
    
    /* The error logger for this class */
    private Logger logger = Logger.getLogger(AssetManager.class.getName());
    
    private final static String CHECKSUM_ALGORITHM = "SHA-1";
    private AssetDB assetDB;
    private File cacheDir = null;
    
    private HashMap<String, Class<? extends Asset>> userDefinedAssetTypes = null;

    // Assets currently being loaded
    private HashMap<String, AssetLoader> loadingAssets;
    
    // Assets that are already loaded
    private HashMap<String, Asset> loadedAssets;
    
    /* The number of threads to use for each of the two downloading services */
    private static final int NUMBER_THREADS = 10;
    
    private ExecutorService downloadService = Executors.newFixedThreadPool(AssetManager.NUMBER_THREADS);
    private ExecutorService localloadService = Executors.newFixedThreadPool(AssetManager.NUMBER_THREADS);
    
    /* Receive updates every 1 MB during downloads */
    private static final int UPDATE_BYTE_INTERVAL = 1024 * 1024;
    
    /* Number of bytes to read as chunks from the network */
    private static final int NETWORK_CHUNK_SIZE = 2 * 1024;
    
    private AssetManager() {
        assetDB = new AssetDB();

        /* Open the cache directory */
        cacheDir = new File(this.getCacheDirectory());
        logger.warning("AssetManager: cacheDir = " + cacheDir);
        
        loadingAssets = new HashMap();
        loadedAssets = new HashMap();
        
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        logger.addHandler(ch);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }
    
    /**
     * AssetManagerHolder holds the single instance of the AssetManager class.
     * It is loaded upon the first call to AssetManager.getAssetManager(). 
     */
    private static class AssetManagerHolder {
        private final static AssetManager assetManager = new AssetManager();
    }
    
    /**
     * Return the singleton AssetManager.
     * 
     * @return An instance of the AssetManager class
     */
    public static AssetManager getAssetManager() {
        return AssetManagerHolder.assetManager;
    }
    
    /**
     * Returns the version of the asset manager.
     * 
     * @return The asset manager version
     */
    public int getVersion() {
        return AssetManager.AM_VERSION;
    }
    
    /**
     * Returns the name of the directory in which the assets are cache.
     * 
     * @return The asset manager cache directory
     */
    public String getCacheDirectory() {
        String defaultName = WonderlandConfigUtil.getWonderlandDir() +
                File.separatorChar + "v" + this.getVersion() + File.separatorChar +
                "cache";
        return System.getProperty("wonderland.cache.dir", defaultName);       
    }
    
    /**
     * Get the asset. If the asset is not in the local cache then it will be
     * downloaded. This call returns immediately with an Asset object that
     * represents the asset downloaded or currently downloading. Register an
     * AssetReadyListener on the Asset object to receive notification when
     * the asset is ready to be used.
     * 
     * @param assetURI The unique URI of the asset to load
     * @param assetType The type of the asset being loaded
     */
    public Asset getAsset(AssetURI assetURI, AssetType assetType) {
        String uri = assetURI.toString();
        Asset asset = null; 
        Checksum checksum = null; // XXX for now
        
        // XXX Some profiling shows this method takes a while -- hmmm, is it
        // because of the synchronized calls? Can we do this better?
        
        /* Log an initial message to the log at method start */
        logger.fine("In getAsset(), looking for, uri=" + uri);
        
        synchronized(loadingAssets) {
            if (loadingAssets.containsKey(uri)) {
                // Currently downloading
                asset = loadingAssets.get(uri).getAsset();
            } else {
                synchronized(loadedAssets) {
                    if (loadedAssets.containsKey(uri)) {
                        // Asset already loaded
                        logger.info("Asset already loaded "+assetURI.toString());
                        asset = loadedAssets.get(uri);
                    } else {
                        Asset tmp = assetDB.getAsset(uri);
        
                        logger.warning("CHECK LOCAL CACHE found "+tmp);
                        
                        /* Fetch the repository where the asset is stored */
                        Repository repository = RepositoryFactory.getRepository(assetURI);
                        
                        if (tmp==null) {
                            // Asset is not in local cache, so get if from the server
                            asset = downloadFromServer(assetURI, assetType, repository);
                        } else {
                            if (checksum==null || checksum.equals(tmp.getLocalChecksum())) {
                                // Asset is in cache, so load it from there
                                logger.info("Asset in local cache");
                                asset = tmp;
                                asset.setLocalCacheFile(new File(this.getAssetCacheFileName(assetURI)));
                                AssetLoader loader = new AssetLoader(asset, repository, false);
                                loadingAssets.put(assetURI.toString(), loader);

                                Future f = localloadService.submit(loader);
                                loader.setFuture(f);
                            } else {
                                // Local cache is out of date, get from server
                                logger.info("Asset checksum out of date");
                                asset = downloadFromServer(assetURI, assetType, repository);
                            }
                        }
                    }
                }
            }
        }
        
        return asset;
    }
    
    /**
     * Wait for the specified asset to load. This method will return once
     * the asset is either loaded, or the load fails.
     * 
     * If the load is successful true is returned, otherwise false is returned
     * 
     * @param asset
     * @return true if asset is ready, false if there was a failure
     */
    public boolean waitForAsset(Asset asset) {
        try {
            AssetLoader loader;

            synchronized (loadingAssets) {
                loader = loadingAssets.get(asset.getAssetURI().toString());
            }

            logger.fine("Waiting for asset: loader=" + loader);
            if (loader == null) {
                return true;
            }
            
            Object o = loader.getFuture().get();
            logger.fine("Finished waiting for asset, o=" + o);
            
            if (o==null) {
                // Load failed
                return false;
            }
            
            return true;
        } catch (InterruptedException ex) {
            //Logger.getLogger(AssetManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            //Logger.getLogger(AssetManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
        
    /**
     * Initiate download of asset from the server given the unique uri of the asset,
     * the asset type, and the repository to use to look for servers. This method
     * assumes the proper locks have been obtained to update the list of loading
     * assets.
     * 
     * @param assetType
     * @param repository
     * @param filename
     * @return
     */
    public Asset downloadFromServer(AssetURI assetURI, AssetType assetType, Repository repository) {
        logger.info("Asset download from server, uri=" + assetURI.toString());

        /* Create a new asset for the given type and uri */
        Asset asset = assetFactory(assetType, assetURI);
        
        /* Create a new loader for it and add to the list of assets being loaded */
        AssetLoader loader = new AssetLoader(asset, repository, true);
        loadingAssets.put(assetURI.toString(), loader);

        /* Spawn off an asynchronous request to download the asset */
        Future f = downloadService.submit(loader);
        loader.setFuture(f);
        
        return asset;
    }
    
    /**
     * Unload the asset from memory
     * @param asset
     */
    public void unloadAsset(Asset asset) {
        synchronized(loadedAssets) {
            loadedAssets.remove(asset.getAssetURI().toString());
            asset.unloaded();
        }
    }
    
    /**
     * Delete the asset from the local cache
     * @param asset
     */
    public void deleteAsset(Asset asset) {
        synchronized(loadedAssets) {
            loadedAssets.remove(asset.getAssetURI().toString());
            asset.unloaded();
            assetDB.deleteAsset(asset.getAssetURI().toString());
            asset.getLocalCacheFile().delete();
        }
    }
    
    /**
     * Factory for creating assets of the required type, takes the type of
     * asset desired (given by the AssetType enumeration) and the unique URI
     * that describes the asset
     * 
     * @param assetType The type of the asset
     * @param assetURI The unique URI describing the asset
     * @return The new Asset, or null upon error
     */
    Asset assetFactory(AssetType assetType, AssetURI assetURI) {
        switch(assetType) {
            case FILE :
                return new AssetFile(assetURI);
            case IMAGE :
                return new AssetImage(assetURI);
            case MODEL :
                return new AssetBranchGroup(assetURI);
            case OTHER :
                throw new RuntimeException("Not implemented");
        }
        
        return null;
    }
    
    /**
     * Register a new asset type, for use by developer who want to add new asset
     * types. The assetType must be other, and the file extension of the string
     * return by assetType.getFilename() will be bound to the asset type.
     * 
     * So in a request to get assetFactory the assetType must be OTHER and the 
     * filename of the Asset object must contain a filename with a known extension
     * 
     * TODO Test this....
     * 
     * @param assetType
     * @param asset
     * @throws IllegalArumentException if fileExtension is already registered.
     */
    public void registerAssetType(String fileExtension, Class<? extends Asset> assetClass) {
        if (userDefinedAssetTypes==null)
            userDefinedAssetTypes = new HashMap<String, Class<? extends Asset>>();
        
        if (userDefinedAssetTypes.containsKey(fileExtension))
            throw new IllegalArgumentException("Duplicate file extension "+fileExtension);
        
        userDefinedAssetTypes.put(fileExtension, assetClass);   
    }
    
    /**
     * Given the unique URI for the asset, return the name of its cache file.
     * This method accounts for the structure of the cache imposed because of
     * different sorts of uri's. For example, all assets part of some module
     * should be cached in a subdirectory pertaining only to that module, so
     * that the file does not conflict with similarly-named files in other
     * modules.
     */
    private String getAssetCacheFileName(AssetURI assetURI) {
        String relativePath = assetURI.getRelativeCachePath();
        String cacheFile = cacheDir.getAbsolutePath() + File.separator + relativePath;
        return cacheFile;
    }
    
    /**
     * Synchronously fetches an asset from a repository, failing over to
     * secondary servers for the repository if some are unreachable. Returns
     * true upon success, false upon failure. This method uses getAssetFromServer()
     * to download the asset from each server.
     */
    private boolean getAssetFromRepository(Asset asset, Repository repository) {        
        /* Fetch an (ordered) array of urls to look for the asset */
        String uri = asset.getAssetURI().toString();
        String[] urls = repository.getAllURLs(asset.getAssetURI());

        System.out.println("Looking at all possible repositories: " + urls);
        /*
         * Try each url in turn and return true when one succeeds. Save the
         * asset to the local cache.
         */
        for (String url : urls) {
            /* Log a message for this attempt to download from the next source */
            System.out.println("Attempting to load from location, url=" + url);

            /*
             * Try to synchronously download the asset. Upon failure log
             * a message and continue to the next one.
             */
            if (getAssetFromServer(asset, url, null) == false) {
                System.out.println("Loading of asset uri=" + uri + " failed for, url=" + url);
                continue;
            }

            /*
             * If we've reached here, we have successfully loaded the asset
             * from the repository, so perform any post-processing needed and
             * return true.
             */
            asset.postProcess();
            return true;
        }
        return false;
    }

    /**
     * Synchronously download an asset from a server, given the asset and the
     * url of the server to look for the asset. The asset object will be updated
     * with the local file containing the cached asset and the locally computed 
     * checksum. Returns true upon success, false upon failure
     * 
     * @param asset The asset to download
     * @param url The full URL to the asset
     * @param progressListener Notified of updates in the loading
     * @return True upon success, false upon failure 
     */
    private boolean getAssetFromServer(Asset asset, String url, ProgressListener progressListener) {        
        try {
            System.out.println("Downloading: " + url.toString());
            /* Open up all of the connections to the remote server */
            URLConnection connection = new URL(url).openConnection();
            TrackingInputStream track = new TrackingInputStream(connection.getInputStream());
            InputStream in = new BufferedInputStream(track);

            /* Receive notifcation after every N bytes during the download */
            if (progressListener != null) {
                track.setListener(progressListener, AssetManager.UPDATE_BYTE_INTERVAL, connection.getContentLength());
            }
            
            /* Open the cache file, create directories if necessary */
            String cacheFile = this.getAssetCacheFileName(asset.getAssetURI());
            System.out.println("AssetManager: Writing asset to local cache file: " + cacheFile);
            File file = new File(cacheFile);
            System.out.println("Can write file? " + file.canWrite());
            if (!file.canWrite())
                makeDirectory(file);
            System.out.println("Does dir exsit? " + file.getAbsolutePath());

            /* Create the output stream, through a digest to compute the checksum */
            byte[] buf = new byte[AssetManager.NETWORK_CHUNK_SIZE];
            MessageDigest digest = MessageDigest.getInstance(CHECKSUM_ALGORITHM);
            OutputStream out = new DigestOutputStream(new BufferedOutputStream(new FileOutputStream(file), buf.length), digest);

            /* Read from the server, write to the cache file */
            int c = in.read(buf);
            while(c>0) {
                out.write(buf, 0, c);
                c = in.read(buf);
            } 
            
            /* Close everything up since we done */
            in.close();
            out.close();
            track.close();
            
            /* Compute the checksum and set in the asset */
            asset.setLocalChecksum(new ChecksumSha1(digest.digest()));
            digest.reset();

            /* Point the asset to the local cache file */
            asset.setLocalCacheFile(file);
            return true;
        } catch(java.lang.Exception ex) {
            /* Log an error and return false */
            logger.log(Level.SEVERE, "Unable to load asset url=" + url);
            return false;
        }
    }
    
    /**
     * Make the directory in which this file will go.
     *
     * Removes the trailing filename from File and creates the directory
     * 
     */
    private synchronized void makeDirectory(File file) throws IOException {
        // Method synchronized to avoid problems where lots of calls can cause
        // a failure of the canWrite() check
        String f = file.getAbsolutePath();
        File dir = new File(f.substring(0, f.lastIndexOf(File.separator)));
        dir.mkdirs();
        if (!dir.canWrite()) {
            logger.severe("Unable to create cache dir "+dir.getAbsolutePath());
            throw new IOException("Failed to Create cache dir "+dir.getAbsolutePath());
        }
    }
    
    /**
     * Attempts to load the file from the local cache synchronously. Returns
     * true upon success, false upon failure.
     * 
     * @param asset The asset to load from the local cache
     * @return True upon success, false upon failure
     */
    private boolean getAssetFromCache(Asset asset) {
        System.out.println("Fetching asset from cache, file=" + asset.getLocalCacheFile().getAbsolutePath());
        try {
            /* Attempt to load the asset, return false if we cannot */
            if (asset.loadLocal() == false) {
                return false;
            }

            /* Otherwise update the list of loading and loaded assets */
            synchronized (loadingAssets) {
                synchronized (loadedAssets) {
                    String uri = asset.getAssetURI().toString();
                    loadingAssets.remove(uri);
                    loadedAssets.put(uri, asset);
                }
            }
            return true;
        } catch (java.lang.Exception excp) {
            /* Catch any exception and return false */
            logger.warning("Unable to fetch asset from local cache, uri=" +
                    asset.getAssetURI().toString());
            logger.warning(excp.toString());
            return false;
        }
    }
    

    /**
     * Used to load assets in parallel. This class implements the Callable
     * interface and is run inside of a Java Executer. The class can load
     * assets from both a remote repository and the local file cache, as given
     * by the 'server' flag.
     */
    class AssetLoader implements Callable {
        /* The asset to load */
        private Asset asset = null;
        
        /* True to load from a remote repository, false to load from the cache */
        private boolean server = true;
        
        /* The repository from which to fetch the asset */
        private Repository repository = null;
        
        /* Object reflecting the results of the asynchronous operation */
        private Future future = null;
        
        /**
         * Load a given asset, either from local cache or the server.
         * 
         * @param asset The asset to load
         * @param repository The repository from which to fetch the asset
         * @param server true loads from server, false for client local cache
         */
        public AssetLoader(Asset asset, Repository repository, boolean server) {
            this.asset = asset;
            this.repository = repository;
            this.server = server;
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
                String uri = this.asset.getAssetURI().toString();

                /* Log a message when this asynchronous task kicks off */
                System.out.println("Start asset fetch, uri=" + uri + ", server=" + this.server);

                /*
                 * First see if we wish to load the asset from the local cache. If
                 * so, then synchronously load the asset. If it fails to load, then
                 * drop through and try to load the asset from the server
                 */
                if (this.server == false) {
                    /* If we can load the asset, then all is well and return */
                    if (getAssetFromCache(this.asset) == true) {
                        System.out.println("Loaded asset from local cache, uri=" + uri);
                        this.asset.setFailureInfo(null);
                        this.asset.notifyAssetReadyListeners();
                        return this.asset;
                    }

                    /* Otherwise, log a warning and try to load from the server */
                    System.out.println("Unable to load asset from local cache, uri=" + uri);
                    if (getAssetFromRepository(this.asset, this.repository) == false) {
                        System.out.println("Loading asset from repository failed, uri=" + uri);
                        this.asset.setFailureInfo(new String("Failed to load asset from repository, uri=" + uri));
                        this.asset.notifyAssetReadyListeners();
                        return this.asset;
                    }

                    /*
                     * Update the cache information locally. If server == false,
                     * then we presume the asset exists in the database
                     */
                    if (assetDB.updateAsset(asset) == false) {
                        // XXX This is a bit more than a warning situation
                        logger.warning("Failed to update asset to cache db, uri=" + uri);
                    }
                }
                else {
                    /* Load the asset from the remote repository */
                    if (getAssetFromRepository(this.asset, this.repository) == false) {
                        System.out.println("Loading asset from repository failed, uri=" + uri);
                        this.asset.setFailureInfo(new String("Failed to load asset from repository, uri=" + uri));
                        this.asset.notifyAssetReadyListeners();
                        return this.asset;
                    }

                    /*
                     * If we've reached here, we have successfully loaded the asset
                     * from the repository, so add it to the cache. If server == true,
                     * we assume the asset does not exist in the database.
                     */
                    if (assetDB.addAsset(asset) == false) {
                        // XXX This is a bit more than a warning situation
                        logger.warning("Failed to add new asset to cache db, uri=" + uri);
                    }
                }

                /*
                 * At this point the asset exists locally in the cache, whether
                 * it was download just now or already exists. Attempt to open
                 * the cached version.
                 */
                if (getAssetFromCache(this.asset) == true) {
                    logger.fine("Loaded asset from local cache, uri=" + uri);
                    asset.setFailureInfo(null);
                    asset.notifyAssetReadyListeners();
                    return this.asset;
                }

                /*
                 * If we have reached here, we were unable to open the local cache
                 * copy for some reason...
                 * */
                logger.warning("Unable to load asset from local cache, uri=" + uri);
                asset.setFailureInfo("Unable to load asset from local cache, uri=" + uri);
                asset.notifyAssetReadyListeners();
                return null;
            } catch (java.lang.Exception excp) {
                excp.printStackTrace();
                throw excp;
            }
        }
    }
    
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
}
