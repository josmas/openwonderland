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
package org.jdesktop.wonderland.modules.mtgloader.client;

import com.jme.scene.Node;
import com.jme.util.resource.ResourceLocator;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ConfigInstance;
import org.jdesktop.mtgame.WorldManager.ConfigLoadListener;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.JmeColladaLoader;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.ImportSettings;
import org.jdesktop.wonderland.client.jme.artimport.ImportedModel;

/**
 *
 * Loader for SketchUp .kmz files
 * 
 * @author paulby
 */
class MtgLoader extends JmeColladaLoader {

    private static final Logger logger = Logger.getLogger(MtgLoader.class.getName());
        
    private HashMap<URL, ZipEntry> textureFiles = new HashMap();
    
    
    private ArrayList<String> modelFiles = new ArrayList();

    /**
     * Load a SketchUP KMZ file and return the ImportedModel object
     * @param file
     * @return
     */
    @Override
    public ImportedModel importModel(ImportSettings settings) throws IOException {
        URL modelURL = settings.getModelURL();
        ImportedModel importedModel=new ImportedModel(modelURL, null);
        load(modelURL);

        importedModel.setModelLoader(this);
        importedModel.setImportSettings(settings);
        importedModel.setModelBG(new Node("Fake"));
        
        return importedModel;
    }
    
    private Entity load(URL url) {

        ClientContextJME.getWorldManager().loadConfiguration(url, new ConfigLoadListener() {

            public void configLoaded(ConfigInstance ci) {
                //System.out.println("Loaded: " + ci.getEntity());
            }
        });

        ConfigInstance ci[] = ClientContextJME.getWorldManager().getAllConfigInstances();
        for (int i=0; i<ci.length; i++) {
            ClientContextJME.getWorldManager().addEntity(ci[i].getEntity());
        }

        return null;
    }

    @Override
    protected ResourceLocator getDeployedResourceLocator(Map<String, String> deployedTextures, String baseURL) {
        return new RelativeResourceLocator(baseURL);
    }

    protected String getLoaderDataURL(DeployedModel model) {
        String str = model.getDeployedURL()+".ldr";

        // For kmz files the dep file is in the directory above the dae file
        return str.replaceFirst("/model", "");
    }

    @Override
    protected void deployModels(File targetDir,
            String moduleName,
            DeployedModel deployedModel,
            ImportedModel importedModel,
            HashMap<String, String> deploymentMapping) {
        URL modelURL = importedModel.getImportSettings().getModelURL();
        

    }


    @Override
    protected void deployTextures(File targetDir, Map<String, String> deploymentMapping, ImportedModel importedModel) {
        URL modelURL = importedModel.getImportSettings().getModelURL();

        if (!modelURL.getProtocol().equalsIgnoreCase("file")) {
            final String modelURLStr = modelURL.toExternalForm();
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JOptionPane.showConfirmDialog(null,
                            "Unable to deploy KMZ from this url "+modelURLStr+
                            "\nPlease use a local kmz file.",
                            "Deploy Error", JOptionPane.OK_OPTION);
                }
            });
            return;
        }
        try {
            ZipFile zipFile = new ZipFile(new File(modelURL.toURI()));
            deployZipTextures(zipFile, targetDir);
        } catch (ZipException ex) {
            Logger.getLogger(MtgLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MtgLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(MtgLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Deploys the textures into the art directory, placing them in a directory
     * with the name of the original model.
     * @param moduleArtRootDir
     */
    private void deployZipTextures(ZipFile zipFile, File targetDir) {
        try {
            // TODO generate checksums to check for image duplication
//            String targetDirName = targetDir.getAbsolutePath();

            for (Map.Entry<URL, ZipEntry> t : textureFiles.entrySet()) {
                File target = new File(targetDir, "/"+t.getKey().getPath());
                target.getParentFile().mkdirs();
                target.createNewFile();
//                logger.fine("Texture file " + target.getAbsolutePath());
                copyAsset(zipFile, t.getValue(), target, false);
            }
        } catch (ZipException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Copy the asset from the zipEntry to the target file
     * @param zipFile the zipFile that contains the zipEntry
     * @param zipEntry entry to copy from
     * @param target file to copy to
     */
    private void copyAsset(ZipFile zipFile, ZipEntry zipEntry, File target, boolean compress) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(zipFile.getInputStream(zipEntry));
            if (compress)
                out = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(target)));
            else
                out = new BufferedOutputStream(new FileOutputStream(target));
            
            org.jdesktop.wonderland.common.FileUtils.copyFile(in, out);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (in!=null)
                    in.close();
                if (out!=null)
                    out.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    class ZipResourceLocator implements ResourceLocator {

        private String zipHost;
        private ZipFile zipFile;
        private Map<URL, String> resourceSet;
        
        public ZipResourceLocator(String zipHost, ZipFile zipFile, Map<URL, String> resourceSet) {
            this.zipHost = zipHost;
            this.zipFile = zipFile;
            this.resourceSet = resourceSet;
        }
        
        public URL locateResource(String resourceName) {
            // Texture paths seem to be relative to the model directory....
            if (resourceName.startsWith("../")) {
                resourceName = resourceName.substring(3);
            }
            if (resourceName.startsWith("/")) {
                resourceName = resourceName.substring(1);
            }

            ZipEntry entry = zipFile.getEntry(resourceName);
            if (entry==null) {
                logger.severe("Unable to locate texture "+resourceName);
                return null;
            }
            
            try {
                URL url = new URL("wlzip", zipHost, "/"+resourceName);
                try {
                    url.openStream();
                } catch (IOException ex) {
                    Logger.getLogger(MtgLoader.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
                textureFiles.put(url, entry);
                if (!resourceSet.containsKey(url)) {
                    resourceSet.put(url, resourceName);
                }
                return url;
            } catch (MalformedURLException ex) {
                Logger.getLogger(MtgLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }

    class RelativeResourceLocator implements ResourceLocator {

        private String baseURL;
        private HashMap<String, URL> processed = new HashMap();

        /**
         * Locate resources for the given file
         * @param url
         */
        public RelativeResourceLocator(String baseURL) {
            this.baseURL = baseURL;
        }

        public URL locateResource(String resource) {
            try {
                URL url = processed.get(resource);
                if (url!=null)
                    return url;
                
                String urlStr = trimUrlStr(baseURL+"/" + resource);

                url = AssetUtils.getAssetURL(urlStr);
                processed.put(url.getPath(), url);

                return url;

            } catch (MalformedURLException ex) {
                logger.log(Level.SEVERE, "Unable to locateResource "+resource, ex);
                return null;
            }
        }

        /**
         * Trim ../ from url
         * @param urlStr
         */
        private String trimUrlStr(String urlStr) {
            // replace /dir/../ with /
            return urlStr.replaceAll("/[^/]*/\\.\\./", "/");
        }
    }
    
    class KmzImportedModel extends ImportedModel {
        private String primaryModel;

        /**
         *
         * @param originalFile
         * @param primaryModel  the name of the primary dae file in the kmz.
         * @param textureFilesMapping
         */
        public KmzImportedModel(URL originalFile, String primaryModel, Map<URL, String> textureFilesMapping) {
            super(originalFile, textureFilesMapping);
            this.primaryModel = primaryModel;
        }

        /**
         * @return the primaryModel
         */
        public String getPrimaryModel() {
            return primaryModel;
        }
    }
}
