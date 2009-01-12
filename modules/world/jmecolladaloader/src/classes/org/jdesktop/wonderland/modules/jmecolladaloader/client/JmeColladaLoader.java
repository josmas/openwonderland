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
package org.jdesktop.wonderland.modules.jmecolladaloader.client;

import com.jme.scene.Node;
import com.jme.util.resource.ResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.jmex.model.collada.ColladaImporter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
import org.jdesktop.wonderland.common.cell.state.CellServerState.Origin;
import org.jdesktop.wonderland.common.cell.state.CellServerState.Rotation;
import org.jdesktop.wonderland.common.cell.state.CellServerState.Scaling;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JMEColladaCellServerState;

/**
 *
 * Loader for Collada files using JME loader
 * 
 * @author paulby
 */
class JmeColladaLoader implements ModelLoader {

    private static final Logger logger = Logger.getLogger(JmeColladaLoader.class.getName());
        
    private HashMap<URL, ZipEntry> textureFiles = new HashMap();
    
    // The original file the user loaded
    private File origFile;
    
    private ArrayList<String> modelFiles = new ArrayList();

    private Node rootNode = null;

    /**
     * Load a SketchUP KMZ file and return the graph root
     * @param file
     * @return
     */
    public Node importModel(File file) throws IOException {
        rootNode = null;
        origFile = file;
        
//        ZipResourceLocator zipResource = new ZipResourceLocator(zipHost, zipFile);
        SimpleResourceLocator resourceLocator = new SimpleResourceLocator(file.toURI());
        ResourceLocatorTool.addResourceLocator(
                ResourceLocatorTool.TYPE_TEXTURE,
                resourceLocator);

        
        logger.info("Loading MODEL " + file.getName());
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        
        ColladaImporter.load(in, file.getName());
        rootNode = ColladaImporter.getModel();

        ColladaImporter.cleanUp();
        
        ResourceLocatorTool.removeResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, resourceLocator);

        return rootNode;
    }
    

    
    public ModelDeploymentInfo deployToModule(File moduleRootDir) throws IOException {
        try {
            String modelName = origFile.getName();
            ZipFile zipFile = new ZipFile(origFile);
            
            // TODO replace getName with getModuleName(moduleRootDir)
            String moduleName = moduleRootDir.getName();

            String targetDirName = moduleRootDir.getAbsolutePath()+File.separator+"art"+ File.separator + modelName;
            File targetDir = new File(targetDirName);
            targetDir.mkdir();

            deployTextures(zipFile, targetDir);
            deployModels(zipFile, targetDir);

            if (modelFiles.size() > 1) {
                logger.warning("Multiple models not supported during deploy");
            }
            JMEColladaCellServerState setup = new JMEColladaCellServerState();
            setup.setModel("wla://"+moduleName+"/art/"+modelFiles.get(0));
            setup.setOrigin(new Origin(rootNode.getLocalTranslation()));
            setup.setRotation(new Rotation(rootNode.getLocalRotation()));
            setup.setScaling(new Scaling(rootNode.getLocalScale()));

            ModelDeploymentInfo deploymentInfo = new ModelDeploymentInfo();
            deploymentInfo.setCellSetup(setup);
            return deploymentInfo;            
        } catch (ZipException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new IOException("Zip error");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw ex;
        }        
    }
    
    /**
     * KMZ files keep all the models in the /models directory, copy all the
     * models into the module
     * @param moduleArtRootDir
     */
    private void deployModels(ZipFile zipFile, File targetDir) {
        
        // TODO update collada files with module relative texture paths
        
        try {
            String targetDirName = targetDir.getAbsolutePath();
            
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".dae")) {
                    File target = new File(targetDirName+File.separator+entry.getName());
                    target.getParentFile().mkdirs();
                    target.createNewFile();
                    
                    copyAsset(zipFile, entry, target);
                }
            }
            
            
        } catch (ZipException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Deploys the textures into the art directory, placing them in a directory
     * with the name of the original model.
     * @param moduleArtRootDir
     */
    private void deployTextures(ZipFile zipFile, File targetDir) {
        try {
            // TODO generate checksums to check for image duplication
            String targetDirName = targetDir.getAbsolutePath();

            for (Map.Entry<URL, ZipEntry> t : textureFiles.entrySet()) {
                File target = new File(targetDirName + File.separator + t.getKey().getPath());
                target.getParentFile().mkdirs();
                target.createNewFile();
//                logger.fine("Texture file " + target.getAbsolutePath());
                copyAsset(zipFile, t.getValue(), target);
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
    private void copyAsset(ZipFile zipFile, ZipEntry zipEntry, File target) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(zipFile.getInputStream(zipEntry));
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
        
        public ZipResourceLocator(String zipHost, ZipFile zipFile) {
            this.zipHost = zipHost;
            this.zipFile = zipFile;
        }
        
        public URL locateResource(String filename) {
            // Texture paths seem to be relative to the model directory....
            if (filename.startsWith("../")) {
                filename = filename.substring(3);
            }
            if (filename.startsWith("/")) {
                filename = filename.substring(1);
            }
            
            ZipEntry entry = zipFile.getEntry(filename);
            if (entry==null) {
                logger.severe("Unable to locate texture "+filename);
                return null;
            }
            
            try {
                URL url = new URL("wlzip", zipHost, "/"+filename);
                textureFiles.put(url, entry);
                return url;
            } catch (MalformedURLException ex) {
                Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
    

}
