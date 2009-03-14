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
package org.jdesktop.wonderland.client.jme.content;

import com.jme.math.Vector3f;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.content.spi.ContentImporterSPI;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Rotation;

/**
 * An abstract base class that content importers may use to help implement the
 * ContentImporterSPI. This class takes care of querying whether the asset
 * already exists and utility methods to create the cell based upon the asset.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public abstract class AbstractContentImporter implements ContentImporterSPI {

    private static Logger logger = Logger.getLogger(AbstractContentImporter.class.getName());

    /**
     * @inheritDoc()
     */
    public String importFile(File file, String extension) {
        // First check to see whether the given extension matches one of the
        // extension specified. This should never be false!
        // Doesn't work for default importers!!!
//        boolean isFound = false;
//        for (String lookAtExtension : getExtensions()) {
//            if (extension.equals(lookAtExtension) == true) {
//                isFound = true;
//                break;
//            }
//        }
//        if (isFound == false) {
//            logger.warning("Internal error: asked to import a file with an " +
//                    extension + " extension, but this does not handle it");
//            return null;
//        }


        // Next check whether the content already exists and ask the user if
        // the upload should still proceed.
        JFrame frame = JmeClientMain.getFrame().getFrame();
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

        // Next, do the actual upload of the file. This should display a
        // progress dialog if the upload is going to take a long time.
        String uri = null;
        try {
            uri = uploadContent(file);
        } catch (java.io.IOException excp) {
            logger.log(Level.WARNING, "Failed to upload content file " +
                    file.getAbsolutePath(), excp);
            JOptionPane.showMessageDialog(frame,
                    "Failed to upload content file " + file.getName(),
                    "Upload Failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // XXX Need to handle prep phase here?

        // Finally, go ahead and create the cell.
        createCell(uri, extension);
        return uri;
    }

    /**
     * Returns true if content already exists upload, false if not. This method
     * should return false if the content already exists, but should and can
     * be overwritten.
     *
     * @param file The File of the imported content
     * @return True if the content has already been uploaded, false if not.
     */
    public abstract boolean isContentExists(File file);

    /**
     * Uploads the content file. Throws IOException upon error. Returns a URI
     * that describes the location of the content
     *
     * @param file The content file to upload
     * @return A URI that represents the uploaded content
     * @throw IOException Upon upload error
     */
    public abstract String uploadContent(File file) throws IOException;

    /**
     * Create a cell based upon the uri of the content and the file extension
     * of the uploaded file.
     *
     * @param uri The URI of the uploaded content
     * @param extension The file extension of the content
     */
    public void createCell(String uri, String extension) {
        // Next look for a cell type that handles content with this file
        // extension and create a new cell with it.
        CellRegistry registry = CellRegistry.getCellRegistry();
        Set<CellFactorySPI> factories = registry.getCellFactoriesByExtension(extension);
        if (factories == null) {
            logger.warning("Could not find cell factory for " + extension);
        }
        CellFactorySPI factory = factories.iterator().next();
        CellServerState state = factory.getDefaultCellServerState();

        // Get the meta data and set
        Map<String, String> metadata = state.getMetaData();
        metadata.put("content-uri", uri);
        state.setMetaData(metadata);

        // Fetch the current transform from the view manager. Find the current
        // position of the camera and its look direction.
        ViewManager manager = ViewManager.getViewManager();
        Vector3f cameraPosition = manager.getCameraPosition(null);
        Vector3f cameraLookDirection = manager.getCameraLookDirection(null);

        // Compute the new vector away from the camera position to be a certain
        // number of scalar units away
        float lengthSquared = cameraLookDirection.lengthSquared();
        float factor = (5.0f * 5.0f) / lengthSquared;
        Vector3f origin = cameraPosition.add(cameraLookDirection.mult(factor));

        // Create a position component that will set the initial origin
        PositionComponentServerState position = new PositionComponentServerState();
        position.setOrigin(new Origin(origin));
        position.setRotation(new Rotation(new Vector3f(0.0f, 1.0f, 0.0f), Math.PI));
        state.addComponentServerState(position);

        // Send the message to the server
        WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
        CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
        CellCreateMessage msg = new CellCreateMessage(null, state);
        connection.send(msg);
    }
}
