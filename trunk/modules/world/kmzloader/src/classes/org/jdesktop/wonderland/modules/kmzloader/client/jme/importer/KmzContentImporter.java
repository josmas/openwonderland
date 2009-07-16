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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.ImportSettings;
import org.jdesktop.wonderland.client.jme.artimport.ImportedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
import org.jdesktop.wonderland.client.jme.content.AbstractContentImporter;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.FileUtils;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;

/**
 * A content importer handler for Google Earth (kmz) files
 *
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class KmzContentImporter extends AbstractContentImporter {

    private static Logger logger = Logger.getLogger(KmzContentImporter.class.getName());
    private ServerSessionManager loginInfo = null;

    /** Constructor, takes the login information */
    public KmzContentImporter(ServerSessionManager loginInfo) {
        this.loginInfo = loginInfo;
    }

    /**
     * @inheritDoc()
     */
    @Override
    public boolean isContentExists(File file) {
        String fileName = file.getName();
        ContentCollection userRoot = getUserRoot();
        try {
            boolean exists = (userRoot.getChild(fileName) != null);
            return exists;
        } catch (ContentRepositoryException excp) {
            logger.log(Level.WARNING, "Error while try to find " + fileName +
                    " in content repository", excp);
            return false;
        }
    }

    /**
     * @inheritDoc()
     */
    @Override
    public String uploadContent(File file) throws IOException {
        URL url = file.toURI().toURL();
        ModelLoader loader = LoaderManager.getLoaderManager().getLoader(url);
        ImportSettings importSettings = new ImportSettings(url);
        ImportedModel importedModel = loader.importModel(importSettings);

        File tmpDir = File.createTempFile("dndart", null);
        tmpDir.mkdir();

        DeployedModel deployedModel = loader.deployToModule(tmpDir, importedModel);

        System.err.println("DONE deploy, now copy ------------------------------------------");

        // Now copy the temporarte files into webdav

        // Create the directory to hold the contents of the model. We place it
        // in a directory named after the kmz file. If the directory already
        // exists, then just use it.
        ContentCollection modelRoot = null;
        String fileName = file.getName();
        try {
            ContentCollection root = getUserRoot();
            modelRoot = (ContentCollection)root.getChild(fileName);
            if (modelRoot == null) {
                modelRoot = (ContentCollection) root.createChild(fileName, Type.COLLECTION);
            }
        } catch (ContentRepositoryException excp) {
            logger.log(Level.WARNING, "Unable to create content directory for" +
                    " model " + fileName, excp);
            throw new IOException("Unable to create content directory for " +
                    "model " + fileName);
        }
        try {
            copyFiles(tmpDir, modelRoot);
        } catch (ContentRepositoryException ex) {
            Logger.getLogger(KmzContentImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.err.println("DEPLOYED TO "+deployedModel.getDeployedURL());

        String modelFile = null;

        return "wlcontent://users/" + loginInfo.getUsername() + "/" +
                fileName + "/" + modelFile;
        
//        KmzModelLoader loader = new KmzModelLoader();
//        loader.importModel(file);
//        Map<URL, ZipEntry> textureMap = loader.getTextureMap();
//        ZipFile zipFile = new ZipFile(file);
//
//        // Create the directory to hold the contents of the model. We place it
//        // in a directory named after the kmz file. If the directory already
//        // exists, then just use it.
//        ContentCollection modelRoot = null;
//        String fileName = file.getName();
//        try {
//            ContentCollection root = getUserRoot();
//            modelRoot = (ContentCollection)root.getChild(fileName);
//            if (modelRoot == null) {
//                modelRoot = (ContentCollection) root.createChild(fileName, Type.COLLECTION);
//            }
//        } catch (ContentRepositoryException excp) {
//            logger.log(Level.WARNING, "Unable to create content directory for" +
//                    " model " + fileName, excp);
//            throw new IOException("Unable to create content directory for " +
//                    "model " + fileName);
//        }
//
//        // Deploy the models and the textures beneath this root directory
//        deployModels(modelRoot, zipFile);
//        deployTextures(modelRoot, zipFile, textureMap);
//
//        // We fetch the model file from the loader and form up the URL based
//        // upon that
//        String modelFile = loader.getModelFiles().get(0);
//
//        // Returns the
//        return "wlcontent://users/" + loginInfo.getUsername() + "/" +
//                fileName + "/" + modelFile;
    }

    private void copyFiles(File f, ContentCollection n) throws ContentRepositoryException, IOException {
        if (f.isDirectory()) {
            System.err.println("CREATE DIR "+f.getName());
            ContentCollection dir = (ContentCollection) n.createChild(f.getName(), Type.COLLECTION);
            File[] subdirs = f.listFiles();
            if (subdirs!=null) {
                for(File child : subdirs)
                    copyFiles(child, dir);
            }
        } else {
            System.err.println("CREATE FILE "+f.getName());
            ContentResource r = (ContentResource) n.createChild(f.getName(), Type.COLLECTION);
            r.put(f);
        }
    }

    /**
     * @inheritDoc()
     */
    public String[] getExtensions() {
        return new String[] { "kmz" };
    }

    /**
     * Returns the content repository root for the current user, or null upon
     * error.
     */
    private ContentCollection getUserRoot() {
        ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
        ContentRepository repo = registry.getRepository(loginInfo);
        try {
            return repo.getUserRoot();
        } catch (ContentRepositoryException excp) {
            logger.log(Level.WARNING, "Unable to find repository root", excp);
            return null;
        }
    }

    /**
     * Given the content collection root, returns the node representing the
     * given file beneath the root, creating all subdirectories as
     * necessary. This assumes the final element in the given path is the file
     * name.
     */
    private ContentResource mkfile(ContentCollection root, String path)
            throws ContentRepositoryException {
        
        // First parse the path into a collection of directory names. Loop
        // through each and attempt to fetch the subdirectory. If it does not
        // exist then create it.
        ContentCollection subdir = root;
        String paths[] = path.split("/");
        for (int i = 0; i < paths.length - 1; i++) {
            ContentCollection newdir = (ContentCollection)subdir.getChild(paths[i]);
            if (newdir == null) {
                newdir = (ContentCollection)subdir.createChild(paths[i], Type.COLLECTION);
            }
            subdir = newdir;
        }

        // Create the file resource and return it
        String fileName = paths[paths.length - 1];
        ContentResource resource = (ContentResource)subdir.getChild(fileName);
        if (resource == null) {
            resource = (ContentResource)subdir.createChild(fileName, Type.RESOURCE);
        }
        return resource;
    }

    /**
     * Utility method that saves the contents of an entry in a zip file to the
     * content repository. Overwrites any existing file
     */
    private void write(ZipFile zipFile, ZipEntry zipEntry,
            ContentCollection root, String fileName) throws IOException {

        // Fetch the file in which we should put this new resource beneath
        // the root creating subdirectories as necessary.
        ContentResource contentFile;
        try {
            contentFile = mkfile(root, fileName);
        } catch (ContentRepositoryException excp) {
            logger.log(Level.WARNING, "Unable to create file for " +
                    fileName + " in the repository", excp);
            throw new IOException("Unable to create file for " + fileName +
                    " in the repository");
        }

        // Fetch the input stream for the zip entry and write to the content
        // resource
        InputStream is = zipFile.getInputStream(zipEntry);
        File tmpFile = File.createTempFile("uploader", ".file");
        tmpFile.deleteOnExit();
        FileUtils.copyFile(is, new FileOutputStream(tmpFile));
        try {
            contentFile.put(tmpFile);
        } catch (ContentRepositoryException excp) {
            logger.log(Level.WARNING, "Unable to write content from " + fileName +
                    " to resource", excp);
            throw new IOException("Unable to write content from " + fileName +
                    " to resource");
        }
    }

    /**
     * Copy all of the .dae files to the content repository
     */
    private void deployModels(ContentCollection root, ZipFile zipFile) {

        // Loop through each file in the zip file and see which have a .dae
        // extension. Write them to the content repository
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements() == true) {
                ZipEntry zipEntry = entries.nextElement();
                String zipEntryName = zipEntry.getName();
                if (zipEntryName.endsWith(".dae") == true) {
                    write(zipFile, zipEntry, root, zipEntryName);
                }
            }
        } catch (ZipException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Deploys the textures into the content repository, placing them in a
     * directory with the name of the original model.
     */
    private void deployTextures(ContentCollection root, ZipFile zipFile,
            Map<URL, ZipEntry> textureMap) {

        // Loop through each of the texture files in the map and write them
        // to the content repository
        try {
            for (Map.Entry<URL, ZipEntry> t : textureMap.entrySet()) {
                String texturePath = t.getKey().getPath();
                ZipEntry zipEntry = t.getValue();
                write(zipFile, zipEntry, root, texturePath);
            }
        } catch (ZipException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
