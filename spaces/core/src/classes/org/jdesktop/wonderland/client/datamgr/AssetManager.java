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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.datamgr.TrackingInputStream.ProgressListener;
import org.jdesktop.wonderland.common.AssetType;
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
    
    private Logger logger = Logger.getLogger(AssetManager.class.getName());
    
    private final static String CHECKSUM_ALGORITHM = "SHA-1";
    private AssetDB assetDB;
    private File cacheDir = null;
    
    private static AssetManager assetManager = null;
    private HashMap<String, Class<? extends Asset>> userDefinedAssetTypes = null;

    // Assets currently being loaded
    private HashMap<String, AssetLoader> loadingAssets;
    
    // Assets that are already loaded
    private HashMap<String, Asset> loadedAssets;
    
    private ExecutorService downloadService = Executors.newFixedThreadPool(4);
    private ExecutorService localloadService = Executors.newFixedThreadPool(4);
    
    private AssetManager() {
        assetDB = new AssetDB();
        
        String cacheDirName = System.getProperty("wonderland.cache.dir", WonderlandConfigUtil.getWonderlandDir()+File.separatorChar+"cache");
        cacheDir = new File(cacheDirName);
        
        loadingAssets = new HashMap();
        loadedAssets = new HashMap();
    }
    
    /**
     * Return the singleton AssetManager
     * @return
     */
    public static AssetManager getAssetManager() {
        if (assetManager==null)
            assetManager = new AssetManager();
        return assetManager;
    }
    
    /**
     * Get the asset. If the asset is not in the local cache then it will be
     * downloaded. This call returns immediately, use assetReadyListener
     * to determine when the asset is ready for use.
     * 
     * @param T
     * @param r
     * @param filename
     * @param checksum
     * @return
     */
    public Asset getAsset(AssetType assetType, Repository repository, String filename, Checksum checksum) {
        Asset asset;      
        
        assert(checksum!=null);
        assert(filename!=null);
        
        synchronized(loadingAssets) {
            if (loadingAssets.containsKey(filename)) {
                // Currently downloading
                asset = loadingAssets.get(filename).getAsset();
            } else {
                synchronized(loadedAssets) {
                    if (loadedAssets.containsKey(filename)) {
                        // Asset already loaded
                        logger.info("Asset already loaded "+filename);
                        asset = loadedAssets.get(filename);
                    } else {
                        Asset tmp = assetDB.getAsset(filename);
        
                        logger.warning("CHECK LOCAL CACHE found "+tmp);
                        if (tmp==null) {
                            // Asset is not in local cache, so get if from the server
                            asset = downloadFromServer(assetType, repository, filename);
                        } else {
                            if (checksum==null || checksum.equals(tmp.getLocalChecksum())) {
                                // Asset is in cache, so load it from there
                                logger.info("Asset in local cache");
                                asset = tmp;
                                try {
                                    asset.setLocalCacheFile(new File(new URL(cacheDir.toURI().toURL().toExternalForm() + "/" + asset.getFilename()).toURI()));
                                } catch(MalformedURLException e) {
                                    logger.log(Level.WARNING, "Cache problem ", e);
                                } catch(URISyntaxException e) {
                                    logger.log(Level.WARNING, "Cache problem ", e);                                    
                                }
                                AssetLoader loader = new AssetLoader(asset, false);
                                loadingAssets.put(filename, loader);

                                Future f = localloadService.submit(loader);
                                loader.setFuture(f);
                            } else {
                                // Local cache is out of date, get from server
                                logger.info("Asset checksum out of date");
                                asset = downloadFromServer(assetType, repository, filename);
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
                loader = loadingAssets.get(asset.getFilename());
            }

            if (loader == null) {
                return true;
            }
            
            Object o = loader.getFuture().get();
            
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
     * Initiate download of asset from the server
     * @param assetType
     * @param repository
     * @param filename
     * @return
     */
    private Asset downloadFromServer(AssetType assetType, Repository repository, String filename) {
        logger.info("downloadFromServer "+repository.getPreferedRepository()+" "+filename);
        Asset asset = assetFactory(assetType, repository, filename);
        AssetLoader loader = new AssetLoader(asset, true);
        loadingAssets.put(filename, loader);

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
            loadedAssets.remove(asset.getFilename());
            asset.unloaded();
        }
    }
    
    /**
     * Delete the asset from the local cache
     * @param asset
     */
    public void deleteAsset(Asset asset) {
        synchronized(loadedAssets) {
            loadedAssets.remove(asset.getFilename());
            asset.unloaded();
            assetDB.deleteAsset(asset.getFilename());
            asset.getLocalCacheFile().delete();
        }
    }
    
    /**
     * Factory for creating assets of the required type
     * 
     * @param assetType
     * @return
     */
    Asset assetFactory(AssetType assetType, Repository repository, String filename) {
        switch(assetType) {
            case FILE :
                return new AssetFile(repository, filename);
            case IMAGE :
                return new AssetImage(repository, filename);
            case MODEL :
                return new AssetBranchGroup(repository, filename);
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
     * Download the asset from the repository. The asset object will be updated
     * with the local file containing the cached asset and the locally computed 
     * checksum
     * 
     * @param asset the asset to download
     */
    private void getAssetFromRepository(Asset asset, ProgressListener progressListener) throws IOException {
        URL url = new URL(asset.getRepository().getPreferedRepository().toExternalForm()+"/"+asset.getFilename());        
        logger.info("Loading file "+asset.getFilename()+" from URL "+url);
        File file;
        
        try {
            URLConnection connection = url.openConnection();
            TrackingInputStream track = new TrackingInputStream(connection.getInputStream());
            InputStream in;

            in = new BufferedInputStream(track);

            if (progressListener!=null)
                track.setListener(progressListener, 1024*1024, connection.getContentLength());

            file = new File(new URL(cacheDir.toURI().toURL().toExternalForm() + "/" + asset.getFilename()).toURI());
            if (!file.canWrite())
                makeDirectory(file);

            byte[] buf = new byte[1024*2];
            MessageDigest digest = MessageDigest.getInstance(CHECKSUM_ALGORITHM);
            OutputStream out = new DigestOutputStream(new BufferedOutputStream(new FileOutputStream(file), buf.length), digest);

            int c = in.read(buf);
            while(c>0) {
                out.write(buf, 0, c);
                c = in.read(buf);
            } 
            in.close();
            out.close();
            track.close();
            asset.setLocalChecksum(new ChecksumSha1(digest.digest()));
            digest.reset();

            asset.setLocalCacheFile(file);
        } catch(Exception ex) {
            //ex.printStackTrace();
            logger.log(Level.SEVERE, "Unable to load asset "+asset.getFilename()+
                       " from " + url, ex);
            throw new IOException("Failed to retrieve asset");
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
     * Return the file extension of the give filename. If there is no extension
     * null is returned;
     */
    String getFileExtension(String filename) {
        try {
            return filename.substring(filename.lastIndexOf('.')+1);        
        } catch(Exception ex) {
            return null;
        }
    }
    
    /**
     * TODO move to common package
     */
    public static abstract class Checksum {
        public abstract boolean equals(Checksum checksum);        
    }
    
    /**
     * TODO move to common package
     */
    public static class ChecksumSha1 extends Checksum {
        private byte[] checksum;
        
        public ChecksumSha1(byte[] checksum) {
            this.checksum = checksum.clone();
        }
        
        /**
         * Returns true if this checksum is equals to the supplied checksum
         * @param checksum
         * @return
         */
        public boolean equals(Checksum checksum2) {
            if (!(checksum2 instanceof ChecksumSha1))
                return false;
            
            ChecksumSha1 c2 = (ChecksumSha1)checksum2;
            if (c2.checksum.length!=this.checksum.length)
                return false;
            
            for(int i=0; i<checksum.length; i++) {
                if (checksum[i]!=c2.checksum[i])
                    return false;
            }
            
            return true;
        }
        
        /**
         * HACK ALERT !
         * toString must return a toHexString as it's used
         * to store this in the db
         * @return
         */
        @Override
        public String toString() {
            return AssetDB.toHexString(checksum);
        }
    }
    


    /**
     * Used to load assets in parallel.
     */
    class AssetLoader implements Callable {
        private Asset asset;
        private boolean server;
        private Future future;
        
        /**
         * Load an asset, either from local cache or the server
         * @param asset
         * @param server true loads from server, false for client
         */
        public AssetLoader(Asset asset, boolean server) {
            this.asset = asset;
            this.server = server;
        }
        
        /**
         * Return the asset this loader is loading
         * @return
         */
        public Asset getAsset() {
            return asset;
        }
        
        public Object call() throws Exception {
            try {
                if (server) {
                    getFromServer();
                }

                try {
                    getLocal();
                } catch(Exception e) {
                    if (server) {
                        getFromServer();
                        getLocal();
                    }
                }

                if (server)
                    assetDB.addAsset(asset);

                asset.notifyAssetReadyListeners();
            } catch(Exception e) {
                e.printStackTrace();
                logger.log(Level.INFO, "Loader FAILED ", e);
                asset.setFailureInfo("Load Failed "+e.getMessage());
                asset.notifyAssetReadyListeners();
                
                // Return null to indicate failure to Future object
                return null;
            }
            
            return asset.getAsset();
        }
        
        private void getFromServer() throws IOException {
            try {
                getAssetFromRepository(asset, null);
                asset.postProcess();
            } catch (IOException ex) {
                // TODO try different repositories
                Logger.getLogger(AssetManager.class.getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }
            
        }
        
        private void getLocal() {
            asset.loadLocal();

            synchronized(loadingAssets) {              
                synchronized(loadedAssets) {
                    loadingAssets.remove(asset.getFilename());
                    loadedAssets.put(asset.getFilename(), asset);
                }
            }            
        }

        Future getFuture() {
            return future;
        }

        void setFuture(Future future) {
            this.future = future;
        }
        
    }
    
    /**
     * Used to recieve notification when an asset load has been completed.
     */
    @ExperimentalAPI
    public interface AssetReadyListener {
        /**
         * Called when the asset is ready for use
         * @param asset
         */
        public void assetReady(Asset asset);
        
        public void assetFailure(Asset Asset, String reason);
    }
}
