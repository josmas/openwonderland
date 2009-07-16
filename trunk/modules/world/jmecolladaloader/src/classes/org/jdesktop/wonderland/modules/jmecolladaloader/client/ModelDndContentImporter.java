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
import org.jdesktop.wonderland.client.jme.artimport.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.content.spi.ContentImporterSPI;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.content.AbstractContentImporter;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.FileUtils;
import org.jdesktop.wonderland.common.cell.state.ModelCellServerState;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellComponentServerState;

/**
 * A content importer handler for ModelLoaders
 *
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModelDndContentImporter implements ContentImporterSPI {

    private static Logger logger = Logger.getLogger(ModelDndContentImporter.class.getName());
    private ServerSessionManager loginInfo = null;
    private String[] extensions;

    /** Constructor, takes the login information */
    public ModelDndContentImporter(ServerSessionManager loginInfo, String[] extensions) {
        this.loginInfo = loginInfo;
        this.extensions = extensions;
    }

    @Override
    public String importFile(File file, String extension) {

        final JFrame frame = JmeClientMain.getFrame().getFrame();

        // Next check whether the content already exists and ask the user if
        // the upload should still proceed.
        if (isContentExists(file) == true) {
            int result = JOptionPane.showConfirmDialog(frame,
                    "The file " + file.getName() + " already exists in the " +
                    "content repository. Do you wish to replace it and " +
                    "continue?", "Replace content?",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) {
                return null;
            }
        }

        // Display a dialog showing a wait message while we import. We need
        // to do this in the SwingWorker thread so it gets drawn
        JOptionPane waitMsg = new JOptionPane("Please wait while " +
                file.getName() + " is being uploaded");
        final JDialog dialog = waitMsg.createDialog(frame, "Uploading Content");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.setVisible(true);
            }
        });

        // Next, do the actual upload of the file. This should display a
        // progress dialog if the upload is going to take a long time.
        DeployedModel deployedModel;
        try {
            deployedModel = modelUploadContent(file);
        } catch (java.io.IOException excp) {
            logger.log(Level.WARNING, "Failed to upload content file " +
                    file.getAbsolutePath(), excp);

            final String fileName = file.getName();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dialog.setVisible(false);
                    JOptionPane.showMessageDialog(frame,
                            "Failed to upload content file " + fileName,
                            "Upload Failed", JOptionPane.ERROR_MESSAGE);
                }
            });
            return null;
        }

        // Close down the dialog indicating success
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.setVisible(false);
            }
        });

        // Finally, go ahead and create the cell.
        createCell(deployedModel);
        return deployedModel.getDeployedURL();
    }

    /**
     * @inheritDoc()
     */
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
    public DeployedModel modelUploadContent(File file) throws IOException {
        URL url = file.toURI().toURL();
        ModelLoader loader = LoaderManager.getLoaderManager().getLoader(url);
        ImportSettings importSettings = new ImportSettings(url);
        ImportedModel importedModel = loader.importModel(importSettings);

        File tmpDir = File.createTempFile("dndart", null);
        if (tmpDir.isDirectory()) {
            FileUtils.deleteDirContents(tmpDir);
        } else {
            tmpDir.delete();
        }
        tmpDir = new File(tmpDir, file.getName());
        tmpDir.mkdirs();

        // Create a fake entity, which will be used to calculate the model offset
        // from the cell
        Node cellRoot = new Node();
        cellRoot.attachChild(importedModel.getModelBG());
        cellRoot.updateWorldData(0f);
        cellRoot.updateWorldBound();
        Entity entity = new Entity("Fake");
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(cellRoot);
        entity.addComponent(RenderComponent.class, rc);
        importedModel.setEntity(entity);

        DeployedModel deployedModel = loader.deployToModule(tmpDir, importedModel);

        // Now copy the temporary files into webdav

        // Create the directory to hold the contents of the model. We place it
        // in a directory named after the kmz file. If the directory already
        // exists, then just use it.
        ContentCollection modelRoot = getUserRoot();

        try {
            // Copy from the art directory
            File artDir = FileUtils.findDir(tmpDir, "art");
            copyFiles(artDir, modelRoot);
        } catch (ContentRepositoryException ex) {
            Logger.getLogger(ModelDndContentImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        URL tmpUrl = new URL(deployedModel.getDeployedURL());
        String modelFile = "art"+tmpUrl.getPath();

        // THIS IS A HACK, should not be assuming JmeColladaCellComponentServerState, but
        // we don't have any other loaders yet (kmzloader is a subclass)
        ModelCellServerState cellState = (ModelCellServerState) deployedModel.getCellServerState();
        JmeColladaCellComponentServerState compState = (JmeColladaCellComponentServerState) cellState.getComponentServerState(JmeColladaCellComponentServerState.class);
        compState.setModel("wlcontent://users/" + loginInfo.getUsername() + "/" + modelFile);
        // END HACK

        deployedModel.setDeployedURL(compState.getModel()); // Make everything consistent with the state

        return deployedModel;
    }

    private void copyFiles(File f, ContentCollection n) throws ContentRepositoryException, IOException {
        if (f.isDirectory()) {
//            System.err.println("CREATE DIR "+f.getName());
            ContentCollection dir = (ContentCollection) ((ContentCollection)n).getChild(f.getName());
            if (dir==null) {
                dir = (ContentCollection) n.createChild(f.getName(), Type.COLLECTION);
            }
            File[] subdirs = f.listFiles();
            if (subdirs!=null) {
                for(File child : subdirs)
                    copyFiles(child, dir);
            }
        } else {
//            System.err.println("CREATE FILE "+f.getName());
            ContentResource r = (ContentResource) n.createChild(f.getName(), Type.RESOURCE);
            r.put(f);
        }
    }

    /**
     * @inheritDoc()
     */
    public String[] getExtensions() {
        return extensions;
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

    public void createCell(DeployedModel deployedModel) {
        try {
            CellUtils.createCell(deployedModel.getCellServerState(), 5.0f);
        } catch (CellCreationException excp) {
            logger.log(Level.WARNING, "Unable to create cell for uri " + deployedModel.getDeployedURL(), excp);
        }
    }
}
