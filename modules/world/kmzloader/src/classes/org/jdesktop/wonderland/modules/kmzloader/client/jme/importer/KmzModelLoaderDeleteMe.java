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
package org.jdesktop.wonderland.modules.kmzloader.client.jme.importer;

import com.jme.math.Quaternion;
import com.jme.scene.Node;
import com.jme.util.resource.ResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.model.collada.ColladaImporter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.jdesktop.wonderland.client.protocols.wlzip.WlzipManager;
import org.jdesktop.wonderland.modules.kmzloader.client.KmlParser;
import org.jdesktop.wonderland.modules.kmzloader.client.KmlParser.KmlModel;

/**
 *
 * Loader for SketchUp .kmz files. Generates a list of model files and
 * textures.
 * 
 * @author paulby
 */
public class KmzModelLoaderDeleteMe {

    private static final Logger logger = Logger.getLogger(KmzModelLoaderDeleteMe.class.getName());
        
    private Map<URL, ZipEntry> textureFiles = new HashMap();
    
    // The original file the user loaded
    private File origFile;
    
    private List<String> modelFiles = new ArrayList();

    private Node modelNode = null;
    
    /**
     * Load a SketchUP KMZ file and return the graph root
     * @param file
     * @return
     */
    public Node importModel(File file) throws IOException {
        modelNode = null;
        origFile = file;
        
        try {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry docKmlEntry = zipFile.getEntry("doc.kml");
//            JAXBContext jc = JAXBContext.newInstance("org.jdesktop.wonderland.modules.kmzloader.client.kml_21",
//                                                     getClass().getClassLoader());
//            Unmarshaller u = jc.createUnmarshaller();
//            JAXBElement docKml = (JAXBElement) u.unmarshal(zipFile.getInputStream(docKmlEntry));
//
//            KmlType kml = (KmlType) docKml.getValue();
//
//            ArrayList<ModelType> models=new ArrayList();
//            FeatureType feature = kml.getFeature().getValue();
//            if (feature instanceof FolderType) {
//                findModels(models, (FolderType)feature);
//            }
            
            KmlParser parser = new KmlParser();
            InputStream in = zipFile.getInputStream(docKmlEntry);
            try {
                parser.decodeKML(in);
            } catch (Exception ex) {
                Logger.getLogger(KmzModelLoaderDeleteMe.class.getName()).log(Level.SEVERE, null, ex);
            }
            List<KmlParser.KmlModel> models = parser.getModels();
            if (models.size()==0) {
                logger.severe("No models found in KMZ File");
                return null;
            }
            
            if (models.size()==1) {
                modelNode = load(zipFile, models.get(0));
            } else {
                modelNode = new Node();
                for(KmlModel model : models) {
                    modelNode.attachChild(load(zipFile, model));
                }
            }
            
        } catch (ZipException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new IOException("Zip Error");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw ex;
        } 
        
        return modelNode;
    }

    public List<String> getModelFiles() {
        return modelFiles;
    }

    public Map<URL, ZipEntry> getTextureMap() {
        return textureFiles;
    }
    
    private Node load(ZipFile zipFile, KmlModel model) throws IOException {

        String filename = model.getHref();
        String zipHost = WlzipManager.getWlzipManager().addZip(zipFile);
        ZipResourceLocator zipResource = new ZipResourceLocator(zipHost, zipFile);
        ResourceLocatorTool.addResourceLocator(
                ResourceLocatorTool.TYPE_TEXTURE,
                zipResource);

        logger.info("Loading MODEL " + filename);
        modelFiles.add(filename);
        
        ZipEntry modelEntry = zipFile.getEntry(filename);
        BufferedInputStream in = new BufferedInputStream(zipFile.getInputStream(modelEntry));
        
        ColladaImporter.load(in, filename);
        modelNode = ColladaImporter.getModel();

        ColladaImporter.cleanUp();
        
        ResourceLocatorTool.removeResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, zipResource);
        WlzipManager.getWlzipManager().removeZip(zipHost, zipFile);

        // Correctly orient model - TODO get initial orientation from KML
        modelNode.setLocalRotation(new Quaternion(new float[] {-(float)Math.PI/2, 0f, 0f}));

        return modelNode;
    }

    /**
     * Search kmz folders adding any ModelTypes found to the models list
     * @param models
     * @param folder
     */
//    private void findModels(ArrayList<ModelType> models, FolderType folder) {
//        List<JAXBElement<? extends FeatureType>> features = folder.getFeature();
//        for(JAXBElement<? extends FeatureType> featureJAXB : features) {
//            FeatureType feature = featureJAXB.getValue();
//
//            if (feature instanceof FolderType) {
//                findModels(models, (FolderType)feature);
//            } else if (feature instanceof PlacemarkType) {
//                if (((PlacemarkType)feature).getGeometry()!=null) {
//                    GeometryType geometryType = ((PlacemarkType)feature).getGeometry().getValue();
//                    if (geometryType instanceof ModelType) {
//                        models.add((ModelType)geometryType);
//                    } else {
//                        logger.info("Unsupported GeometryType "+geometryType);
//                    }
//                }
//            } else
//                logger.info("Skipping feature "+feature);
//        }
//    }

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
                Logger.getLogger(KmzModelLoaderDeleteMe.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
}
