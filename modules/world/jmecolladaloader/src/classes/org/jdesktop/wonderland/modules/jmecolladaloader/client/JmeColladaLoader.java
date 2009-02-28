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
package org.jdesktop.wonderland.modules.jmecolladaloader.client;

import com.jme.scene.Node;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Rotation;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Scale;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.jme.cellrenderer.JmeColladaRenderer;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellServerState;

/**
 *
 * Loader for Collada files using JME loader
 * 
 * @author paulby
 */
class JmeColladaLoader implements ModelLoader {

    private static final Logger logger = Logger.getLogger(JmeColladaLoader.class.getName());
        
    // The original file the user loaded
    private File origFile;
    
//    private ArrayList<String> modelFiles = new ArrayList();

    private Node rootNode = null;

    private HashMap<URL, String> resourceSet = new HashMap();

    /**
     * Load a SketchUP KMZ file and return the graph root
     * @param file
     * @return
     */
    public Node importModel(File file) throws IOException {
        rootNode = null;
        origFile = file;

        SimpleResourceLocator resourceLocator = new RecordingResourceLocator(file.toURI());
        ResourceLocatorTool.addResourceLocator(
                ResourceLocatorTool.TYPE_TEXTURE,
                resourceLocator);

        
        logger.info("Loading MODEL " + file.getName());
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

        rootNode = JmeColladaRenderer.loadModel(in, file.getName());
        in.close();
        
        ResourceLocatorTool.removeResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, resourceLocator);

        return rootNode;
    }


    public ModelDeploymentInfo deployToModule(File moduleRootDir) throws IOException {
        try {
            String modelName = origFile.getName();
            
            // TODO replace getName with getModuleName(moduleRootDir)
            String moduleName = moduleRootDir.getName();

            String targetDirName = moduleRootDir.getAbsolutePath()+File.separator+"art"+ File.separator + modelName;
            File targetDir = new File(targetDirName);
            targetDir.mkdirs();

            deployTextures(targetDir);
            deployModels(origFile.toURI().toURL(), targetDir);

//            if (modelFiles.size() > 1) {
//                logger.warning("Multiple models not supported during deploy");
//            }

            JmeColladaCellServerState setup = new JmeColladaCellServerState();
            setup.setModel("wla://"+moduleName+"/"+modelName+"/"+modelName);

            PositionComponentServerState position = new PositionComponentServerState();
            position.setOrigin(new Origin(rootNode.getLocalTranslation()));
            position.setRotation(new Rotation(rootNode.getLocalRotation()));
            position.setScaling(new Scale(rootNode.getLocalScale()));
            position.setBounds(rootNode.getWorldBound());
            setup.addComponentServerState(position);

            ModelDeploymentInfo deploymentInfo = new ModelDeploymentInfo();
            deploymentInfo.setCellSetup(setup);
            return deploymentInfo;            
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
    private void deployModels(URL source, File targetDir) {
        File targetFile = new File(targetDir, origFile.getName());
        try {
            targetFile.createNewFile();
            copyAsset(source, targetFile); // TODO handle multiple dae files
        } catch (IOException ex) {
            Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, "Unable to create file "+targetFile.getAbsolutePath(), ex);
        }

    }
    
    /**
     * Deploys the textures into the art directory, placing them in a directory
     * with the name of the original model.
     * @param moduleArtRootDir
     */
    private void deployTextures(File targetDir) {
        try {
            // TODO generate checksums to check for image duplication
            String targetDirName = targetDir.getAbsolutePath();

            for (Map.Entry<URL, String> t : resourceSet.entrySet()) {
                File target=null;
                String targetFilename = t.getValue();
                if (targetFilename.startsWith("/")) {
                    targetFilename = targetFilename.substring(targetFilename.lastIndexOf(File.separatorChar));
                    if (targetFilename==null) {
                        targetFilename = t.getValue();
                    }
                } else {
                    // Relative path
                    if (targetFilename.startsWith("..")) {
                        target = new File(targetDir.getParentFile(), targetFilename.substring(3));
                    }
                }

                if (target==null)
                    target = new File(targetDirName + File.separator + targetFilename);

//                logger.fine("Texture file " + target.getAbsolutePath());
                target.getParentFile().mkdirs();
                target.createNewFile();
                copyAsset(t.getKey(), target);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Copy the asset from the source url to the target file
     * @param source the source file to copy from
     * @param target file to copy to
     */
    private void copyAsset(URL source, File targetFile) {
        InputStream in = null;
        OutputStream out = null;
        try {

            in = new BufferedInputStream(source.openStream());
            out = new BufferedOutputStream(new FileOutputStream(targetFile));
            
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

//    class ZipResourceLocator implements ResourceLocator {
//
//        private String zipHost;
//        private ZipFile zipFile;
//
//        public ZipResourceLocator(String zipHost, ZipFile zipFile) {
//            this.zipHost = zipHost;
//            this.zipFile = zipFile;
//        }
//
//        public URL locateResource(String filename) {
//            // Texture paths seem to be relative to the model directory....
//            if (filename.startsWith("../")) {
//                filename = filename.substring(3);
//            }
//            if (filename.startsWith("/")) {
//                filename = filename.substring(1);
//            }
//
//            ZipEntry entry = zipFile.getEntry(filename);
//            if (entry==null) {
//                logger.severe("Unable to locate texture "+filename);
//                return null;
//            }
//
//            try {
//                URL url = new URL("wlzip", zipHost, "/"+filename);
//                textureFiles.put(url, entry);
//                return url;
//            } catch (MalformedURLException ex) {
//                Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            return null;
//        }
//    }
    

    class RecordingResourceLocator extends SimpleResourceLocator {
        public RecordingResourceLocator(URI baseDir) {
            super(baseDir);
        }

        public RecordingResourceLocator(URL baseDir) throws URISyntaxException {
            super(baseDir);
        }

        @Override
        public URL locateResource(String resourceName) {
            URL ret = locateResourceImpl(resourceName);

            if (!resourceSet.containsKey(ret)) {
//                System.err.println("Looking for "+resourceName+"   found "+ret.toExternalForm());
                resourceSet.put(ret, resourceName);
            }

            return ret;
        }

        // Copied directly from SimpleResourceLocator
        public URL locateResourceImpl(String resourceName) {
            // Trim off any prepended local dir.
            while (resourceName.startsWith("./") && resourceName.length() > 2) {
                resourceName = resourceName.substring(2);
            }
            while (resourceName.startsWith(".\\") && resourceName.length() > 2) {
                resourceName = resourceName.substring(2);
            }

            // Try to locate using resourceName as is.
            try {
                String spec = URLEncoder.encode( resourceName, "UTF-8" );
                //this fixes a bug in JRE1.5 (file handler does not decode "+" to spaces)
                spec = spec.replaceAll( "\\+", "%20" );

                URL rVal = new URL( baseDir.toURL(), spec );
                // open a stream to see if this is a valid resource
                // XXX: Perhaps this is wasteful?  Also, what info will determine validity?
                rVal.openStream().close();
                return rVal;
            } catch (IOException e) {
                // URL wasn't valid in some way, so try up a path.
            } catch (IllegalArgumentException e) {
                // URL wasn't valid in some way, so try up a path.
            }

            resourceName = trimResourceName(resourceName);
            if (resourceName == null) {
                return null;
            } else {
                return locateResourceImpl(resourceName);
            }
        }
    }
}
