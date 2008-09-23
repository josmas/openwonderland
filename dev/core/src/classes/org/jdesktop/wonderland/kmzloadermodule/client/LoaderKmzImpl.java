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
package org.jdesktop.wonderland.kmzloadermodule.client;

import com.jme.scene.Node;
import com.jme.util.resource.ResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.model.collada.ColladaImporter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.jdesktop.wonderland.client.jme.artimport.ImportSessionFrame;
import org.jdesktop.wonderland.client.protocols.wlzip.WlzipManager;
import org.jdesktop.wonderland.kmzloadermodule.client.kml_21.FeatureType;
import org.jdesktop.wonderland.kmzloadermodule.client.kml_21.FolderType;
import org.jdesktop.wonderland.kmzloadermodule.client.kml_21.GeometryType;
import org.jdesktop.wonderland.kmzloadermodule.client.kml_21.KmlType;
import org.jdesktop.wonderland.kmzloadermodule.client.kml_21.ModelType;
import org.jdesktop.wonderland.kmzloadermodule.client.kml_21.PlacemarkType;

/**
 *
 * Loader for SketchUp .kmz files
 * 
 * @author paulby
 */
class LoaderKmzImpl {

    private static final Logger logger = Logger.getLogger(LoaderKmzImpl.class.getName());
        
    /**
     * Load a SketchUP KMZ file and return the graph root
     * @param file
     * @return
     */
    Node load(File file) {
        Node ret = null;
        
        try {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry docKmlEntry = zipFile.getEntry("doc.kml");
            JAXBContext jc = JAXBContext.newInstance("org.jdesktop.wonderland.kmzloadermodule.client.kml_21");
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement docKml = (JAXBElement) u.unmarshal(zipFile.getInputStream(docKmlEntry));
            
            KmlType kml = (KmlType) docKml.getValue();
            
            ArrayList<ModelType> models=new ArrayList();
            FeatureType feature = kml.getFeature().getValue();
            if (feature instanceof FolderType) {
                findModels(models, (FolderType)feature);
            }
            
            if (models.size()==0) {
                logger.severe("No models found in KMZ File");
                return null;
            }
            
            if (models.size()==1) {
                ret = load(zipFile, models.get(0).getLink().getHref());
            } else {
                ret = new Node();
                for(ModelType model : models) {
                    ret.attachChild(load(zipFile, model.getLink().getHref()));
                }
            }
            
        } catch (ZipException ex) {
            Logger.getLogger(ImportSessionFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportSessionFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(ImportSessionFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ret;
    }
    
    private Node load(ZipFile zipFile, String filename) throws IOException {
        
        String zipHost = WlzipManager.getWlzipManager().addZip(zipFile);
        ZipResourceLocator zipResource = new ZipResourceLocator(zipHost, zipFile);
        ResourceLocatorTool.addResourceLocator(
                ResourceLocatorTool.TYPE_TEXTURE,
                zipResource);

        
        logger.info("Loading MODEL " + filename);
        ZipEntry modelEntry = zipFile.getEntry(filename);
        BufferedInputStream in = new BufferedInputStream(zipFile.getInputStream(modelEntry));
        
        Node ret;
        ColladaImporter.load(in, filename);
        ret = ColladaImporter.getModel();

        ColladaImporter.cleanUp();
        
        ResourceLocatorTool.removeResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, zipResource);
        WlzipManager.getWlzipManager().removeZip(zipHost, zipFile);

        return ret;
    }
    
    /**
     * Search folders adding any ModelTypes found to the models list
     * @param models
     * @param folder
     */
    private void findModels(ArrayList<ModelType> models, FolderType folder) {
        List<JAXBElement<? extends FeatureType>> features = folder.getFeature();
        for(JAXBElement<? extends FeatureType> featureJAXB : features) {
            FeatureType feature = featureJAXB.getValue();
            
            if (feature instanceof FolderType) {
                findModels(models, (FolderType)feature);
            } else if (feature instanceof PlacemarkType) {
                if (((PlacemarkType)feature).getGeometry()!=null) {
                    GeometryType geometryType = ((PlacemarkType)feature).getGeometry().getValue();
                    if (geometryType instanceof ModelType) {
                        models.add((ModelType)geometryType);
                    } else {
                        logger.info("Unsupported GeometryType "+geometryType);
                    }
                }
            } else
                logger.info("Skipping feature "+feature);
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
            
            ZipEntry entry = zipFile.getEntry(filename);
            if (entry==null) {
                logger.severe("Unable to locate texture "+filename);
                return null;
            }
            
            try {
                return new URL("wlzip", zipHost, filename);
            } catch (MalformedURLException ex) {
                Logger.getLogger(LoaderKmzImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
    

}
