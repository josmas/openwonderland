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
package org.jdesktop.wonderland.client.assetmgr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.assetmgr.TrackingInputStream.ProgressListener;
import org.jdesktop.wonderland.client.modules.Checksum;
import org.jdesktop.wonderland.client.modules.ModuleCache;
import org.jdesktop.wonderland.client.modules.ModuleCacheList;
import org.jdesktop.wonderland.client.modules.ChecksumList;
import org.jdesktop.wonderland.client.modules.RepositoryList;
import org.jdesktop.wonderland.client.modules.RepositoryList.Repository;
import org.jdesktop.wonderland.client.modules.RepositoryUtils;
import org.jdesktop.wonderland.common.AssetType;
import org.jdesktop.wonderland.common.ResourceURI;
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

    /*
     * A Hash map of repositories and the list of checksums for the assets in
     * that repository. The list is ordered so that the last element in the
     * list is the oldest. The length of this list is limited to the value
     * MAX_REPOSITORY_CHECKSUMS. This list is synchronized so that multiple
     * threads may interact with it safely
     */
    private Map<Repository, ChecksumList> checksums = null;
    private static final int MAX_REPOSITORY_CHECKSUMS = 100;
    
    /*
     * A map of assets currently being loaded, where the key is the unique ID
     * of the asset and the value is the loader reponsible for loading it
     */
    private HashMap<AssetID, AssetLoader> loadingAssets;
    
    /*
     * A map of assets already loaded by the asset manager, where the key is the
     * unique ID of the asset and the value is the Asset object itself.
     */
    private HashMap<AssetID, Asset> loadedAssets;
    
    /* The number of threads to use for each of the two downloading services */
    private static final int NUMBER_THREADS = 10;
    
    private ExecutorService downloadService = Executors.newFixedThreadPool(AssetManager.NUMBER_THREADS);
    private ExecutorService localloadService = Executors.newFixedThreadPool(AssetManager.NUMBER_THREADS);
    
    /* Receive updates every 1 MB during downloads */
    private static final int UPDATE_BYTE_INTERVAL = 1024 * 1024;
    
    /* Number of bytes to read as chunks from the network */
    private static final int NETWORK_CHUNK_SIZE = 2 * 1024;
    
    /* The maximum size of the data cache */
    private static final int MAX_DATA_CACHE = 0; // XXX
    
    private AssetManager() {
        assetDB = new AssetDB();
        
        /* Create a synchronized list of cached checksums information */
        this.checksums = Collections.synchronizedMap(new LinkedHashMap<Repository, ChecksumList>());
        
        /* Open the cache directory */
        cacheDir = new File(this.getCacheDirectory());
        logger.warning("AssetManager: cacheDir = " + cacheDir);
        
        loadingAssets = new HashMap<AssetID, AssetLoader>();
        loadedAssets = new HashMap<AssetID, Asset>();
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
        return defaultName;
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
    public Asset getAsset(ResourceURI assetURI, AssetType assetType) {
        String uri = assetURI.toString();
        
        /* Log a bunch of informative messages */
        logger.fine("[ASSET] GET asset " + uri + " [" + assetType + "]");
        logger.fine("[ASSET] GET module name " + assetURI.getModuleName());
        logger.fine("[ASSET] GET module relative path " + assetURI.getRelativePath());
        
        /*
         * First construct an Asset object that represents the asset we want.
         * This consists of both the Asset URI and the desired checksum.
         */
        String checksum = null;
        ModuleCache cache = ModuleCacheList.getModuleCacheList().getModuleCache("server");
        if (cache != null) {
            ChecksumList checksums = cache.getModuleChecksums(assetURI.getModuleName());
            logger.fine("[ASSET] GET checksums for module " + checksums);
            if (checksums != null) {
                Checksum c = checksums.getChecksums().get(assetURI.getRelativePath());
                if (c != null) {
                    checksum = c.getChecksum();
                }
            }
        }
        AssetID assetID = new AssetID(assetURI, checksum);
        
        /* Log a bunch of informative messages */
        logger.fine("[ASSET] GET module cache: " + cache);
        logger.fine("[ASSET] GET checksum for asset: " + checksum);
        
        /*
         * If the asset does not have a checksum, it means the module is bad.
         * Print an error message here -- we cannot load the asset.
         */
        if (checksum == null) {
            logger.warning("[ASSET] GET Checksum is null for asset");
            logger.warning("[ASSET] GET Unable to load, recheck module installation");
            return null;
        }
        
        // XXX Some profiling shows this method takes a while -- hmmm, is it
        // because of the synchronized calls? Can we do this better?
        
        synchronized(loadingAssets) {
            /*
             * Check to see if the asset is currently being downloaded. We use
             * the Asset object as a key -- which lets us uniquely identify an
             * asset based upon its URI and checksum.
             */
            if (loadingAssets.containsKey(assetID) == true) {
                logger.fine("[ASSET] GET asset is currently being downloaded: " + uri);
                return loadingAssets.get(assetID).getAsset();
            }

            synchronized (loadedAssets) {
                /*
                 * Otherwise, see if the asset has already been loaded. An
                 * equivalent Asset object is in the list of loaded assets.
                 */
                if (loadedAssets.containsKey(assetID) == true) {
                    logger.info("[ASSET] GET asset has been downloaded: " + uri);
                    return loadedAssets.get(assetID);
                }
                
                /*
                 * Next, check if the asset is in the local cache. If not, then
                 * ask to download the asset asynchronously.
                 */
                Asset asset = assetDB.getAsset(assetID);
                if (asset == null) {
                    logger.info("[ASSET] GET attempt to download asset: " + uri);
                    return this.downloadFromServer(assetID, assetType);
                }

                /*
                 * Otherwise, fetch from the local cache.
                 */
                logger.info("[ASSET] GET asset is in local cache: " + uri);
                asset.setLocalCacheFile(new File(this.getAssetCacheFileName(assetID)));
                AssetLoader loader = new AssetLoader(asset, false);
                loadingAssets.put(assetID, loader);

                Future f = localloadService.submit(loader);
                loader.setFuture(f);
                return asset;
            }
        }
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
                AssetID assetID = new AssetID(asset.getAssetURI(), asset.getChecksum());
                loader = loadingAssets.get(assetID);
            }

            /*
             * Fetch the class that is currently loading an asset. If it is null,
             * there is none, so return true. This is situation is a bit odd,
             * but happens when the asset has already been downloaded. Hence
             * we return true.
             */
            logger.fine("[ASSET] WAIT Waiting for Loader " + loader);
            if (loader == null) {
                return true;
            }
            
            Object o = loader.getFuture().get();
            logger.fine("[ASSET] WAIT Finished got " + o);
           
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
     * Initiate download of asset from the server given the unique ID of the asset,
     * the asset type, and the repository to use to look for servers. This method
     * assumes the proper locks have been obtained to update the list of loading
     * assets.
     * 
     * @param assetType
     * @param filename
     * @return
     */
    public Asset downloadFromServer(AssetID assetID, AssetType assetType) {
        /* Create a new asset for the given type and uri */
        Asset asset = assetFactory(assetType, assetID);
        
        /* Create a new loader for it and add to the list of assets being loaded */
        AssetLoader loader = new AssetLoader(asset, true);
        loadingAssets.put(assetID, loader);

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
            AssetID assetID = new AssetID(asset.getAssetURI(), asset.getChecksum());
            loadedAssets.remove(assetID);
            asset.unloaded();
        }
    }
    
    /**
     * Delete the asset from the local cache
     * @param asset
     */
    public void deleteAsset(Asset asset) {
        synchronized(loadedAssets) {
            AssetID assetID = new AssetID(asset.getAssetURI(), asset.getChecksum());
            loadedAssets.remove(assetID);
            asset.unloaded();
            assetDB.deleteAsset(assetID);
            asset.getLocalCacheFile().delete();
        }
    }
    
    /**
     * Factory for creating assets of the required type, takes the type of
     * asset desired (given by the AssetType enumeration) and the unique URI
     * that describes the asset
     * 
     * @param assetType The type of the asset
     * @param assetID The unique ID describing the asset
     * @return The new Asset, or null upon error
     */
    Asset assetFactory(AssetType assetType, AssetID assetID) {
        switch(assetType) {
            case FILE :
                return new AssetFile(assetID);
            case IMAGE :
                return new AssetImage(assetID);
            case MODEL :
                return new AssetBranchGroup(assetID);
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
     * Given the unique ID for the asset, return the name of its cache file.
     * This method accounts for the structure of the cache imposed because of
     * different sorts of uri's. For example, all assets part of some module
     * should be cached in a subdirectory pertaining only to that module, so
     * that the file does not conflict with similarly-named files in other
     * modules.
     */
    private String getAssetCacheFileName(AssetID assetID) {
        String basePath = cacheDir.getAbsolutePath();
        String relativePath = assetID.getAssetURI().getRelativeCachePath();
        String checksum = assetID.getChecksum();
        return basePath + File.separator + relativePath + "/" + checksum;
    }
    
    /**
     * Synchronously fetches an asset from a repository, failing over to
     * secondary servers for the repository if some are unreachable. Returns
     * true upon success, false upon failure. This method uses getAssetFromServer()
     * to download the asset from each server.
     */
    private boolean getAssetFromRepository(Asset asset) {
        logger.fine("[ASSET] FETCH asset: " + asset.getAssetURI() + " [" + asset.checksum + "]");
        
        /*
         * Fetch an (ordered) array of urls to look for the asset. We fetch
         * this information from the module in which the asset is contained.
         */
        String moduleName = asset.getAssetURI().getModuleName();
        ModuleCache cache = ModuleCacheList.getModuleCacheList().getModuleCache("server");
        RepositoryList list = cache.getModuleRepositoryList(moduleName);
        if (list == null) {
            logger.warning("[ASSET] FETCH unable to locate repository list, cache: " + cache);
            return false;
        }

        /*
         * Try each repository in turn and return true when one succeeds. Save
         * the asset to the local cache.
         */
        for (Repository repository : list.getAllRepositories()) {
            /* Log a message for this attempt to download from the next source */
            logger.fine("[ASSET] FETCH Attempting to load from location, url=" + repository.toString());

            /*
             * See if the checksum of the asset stored in the repository matches
             * the desired asset checksum. If not, continue looking. If so,
             * fetch it. We skip the checksum if we know that the repository is
             * the same server that hosts the module
             */
            if (repository.isServer == false) {
                ChecksumList mc = this.getChecksums(repository);
                if (mc == null) {
                    continue;
                }
                Checksum c = mc.getChecksums().get(asset.getAssetURI().getRelativePath());
                if (c == null || c.equals(asset.getChecksum()) == false) {
                    continue;
                }
            }
            
            /* Form the URL of where to find the asset */
            String url = RepositoryUtils.getAssetURL(repository, asset.getAssetURI());
            
            /*
             * Try to synchronously download the asset. Upon failure log
             * a message and continue to the next one.
             */
            if (getAssetFromServer(asset, url, null) == false) {
                logger.warning("[ASSET] FETCH Loading of asset url=" + url + " failed");
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
            logger.fine("[ASSET] DOWNLOAD " + url.toString());
            
            /* Open up all of the connections to the remote server */
            URLConnection connection = new URL(url).openConnection();
            TrackingInputStream track = new TrackingInputStream(connection.getInputStream());
            InputStream in = new BufferedInputStream(track);

            /* Receive notifcation after every N bytes during the download */
            if (progressListener != null) {
                track.setListener(progressListener, AssetManager.UPDATE_BYTE_INTERVAL, connection.getContentLength());
            }
            
            /* Open the cache file, create directories if necessary */
            AssetID assetID = new AssetID(asset.getAssetURI(), asset.getChecksum());
            String cacheFile = this.getAssetCacheFileName(assetID);
            File file = new File(cacheFile);
            if (!file.canWrite())
                makeDirectory(file);

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
            asset.setChecksum(Checksum.toHexString(digest.digest()));
            digest.reset();

            /* Point the asset to the local cache file */
            asset.setLocalCacheFile(file);
            asset.setURL(url);
            
            logger.fine("[ASSET] DOWNLOAD done " + url.toString());
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
        logger.fine("[ASSET] CACHE FETCH " + asset.getLocalCacheFile().getAbsolutePath());
        try {
            /* Attempt to load the asset, return false if we cannot */
            if (asset.loadLocal() == false) {
                return false;
            }

            /* Otherwise update the list of loading and loaded assets */
            synchronized (loadingAssets) {
                synchronized (loadedAssets) {
                    AssetID assetID = new AssetID(asset.getAssetURI(), asset.getChecksum());
                    loadingAssets.remove(assetID);
                    loadedAssets.put(assetID, asset);
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
     * Returns the collection of checksums for a given repository, null if the
     * checksum information does not exist, or if the repository is unreachable.
     * 
     * @param repository The repository to look for checksums
     * @return The collection of checksums for the repository
     */
    private ChecksumList getChecksums(Repository repository) {
        /*
         * First check whether the checksum information is cached for the
         * repository. Note that we do not make this entire method atomic. If
         * we did, then only a single thread can invoke it at once -- there is
         * a potentially long delay since if the checksum information is not
         * cached, it must be downloaded. The implementation here lets many
         * methods operate at once, even if the same information is downloaded
         * more than once. (Access to the "checksums" map is synchronized, so
         * it will never be messed up).
         */
        if (this.checksums.containsKey(repository) == true) {
            return this.checksums.get(repository);
        }
        
        /*
         * Otherwise, download the checksum information and cache it.
         */
        try {
            URL url = new URL(RepositoryUtils.getChecksumURL(repository));
            ChecksumList c = ChecksumList.decode(new InputStreamReader(url.openStream()));
            if (c != null) {
                this.checksums.put(repository, c);
            }
            return c;
        } catch (Exception excp) {
            // XXX log error
            return null;
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
        
        /* Object reflecting the results of the asynchronous operation */
        private Future future = null;
        
        /**
         * Load a given asset, either from local cache or the server.
         * 
         * @param asset The asset to load
         * @param server true loads from server, false for client local cache
         */
        public AssetLoader(Asset asset, boolean server) {
            this.asset = asset;
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
                logger.fine("[ASSET] CALL fetch asset: " + uri + " [" + this.server + "]");

                /*
                 * First see if we wish to load the asset from the local cache. If
                 * so, then synchronously load the asset. If it fails to load, then
                 * drop through and try to load the asset from the server
                 */
                if (this.server == false) {
                    /* If we can load the asset, then all is well and return */
                    if (getAssetFromCache(this.asset) == true) {
                        logger.warning("[ASSET] CALL Loaded asset from local cache, uri=" + uri);
                        this.asset.setFailureInfo(null);
                        this.asset.notifyAssetReadyListeners();
                        return this.asset;
                    }

                    /* Otherwise, log a warning and try to load from the server */
                    logger.fine("[ASSET] CALL Unable to load asset from local cache, uri=" + uri);
                    if (getAssetFromRepository(this.asset) == false) {
                        logger.warning("[ASSET] CALL Loading asset from repository failed, uri=" + uri);
                        this.asset.setFailureInfo(new String("Failed to load asset from repository, uri=" + uri));
                        this.asset.notifyAssetReadyListeners();
                        return null;
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
                    logger.fine("[ASSET] CALL fetch asset from server: " + asset.toString());
                    
                    /* Load the asset from the remote repository */
                    if (getAssetFromRepository(this.asset) == false) {
                        logger.warning("[ASSET] CALL Loading asset from repository failed, uri=" + uri);
                        this.asset.setFailureInfo(new String("Failed to load asset from repository, uri=" + uri));
                        this.asset.notifyAssetReadyListeners();
                        return null;
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
