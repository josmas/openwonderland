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
package org.jdesktop.wonderland.client.jme.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.dnd.spi.DataFlavorHandlerSPI;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

/**
 * A handler to support drag-and-drop from a URL (perhaps from a web browser).
 * The data flavor supported has the mime type "application/x-java-url". This
 * simply looks for a Cell that can handle the data type and launches it.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class URLDataFlavorHandler implements DataFlavorHandlerSPI {

    private static Logger logger = Logger.getLogger(URLDataFlavorHandler.class.getName());

    /**
     * @inheritDoc()
     */
    public DataFlavor[] getDataFlavors() {
        try {
            return new DataFlavor[] {
                new DataFlavor("application/x-java-url; class=java.net.URL")
            };
        } catch (ClassNotFoundException ex) {
            logger.log(Level.WARNING, "Unable to find DataFlavor for URL", ex);
            return new DataFlavor[] {};
        }
    }

    /**
     * @inheritDoc()
     */
    public boolean accept(Transferable transferable, DataFlavor dataFlavor) {
        // We will accept all transferables, except those with the "file"
        // protocol. We kick those out to the "flie list" data flavor handler
        URL url = null;
        try {
            url = (URL)transferable.getTransferData(dataFlavor);
        } catch (java.io.IOException excp) {
            logger.log(Level.WARNING, "Unable to complete drag and drop", excp);
            return false;
        } catch (UnsupportedFlavorException excp) {
            logger.log(Level.WARNING, "Unable to complete drag and drop", excp);
            return false;
        }

        String protocol = url.getProtocol();
        if (protocol.equals("file") == true) {
            return false;
        }
        return true;
    }

    /**
     * @inheritDoc()
     */
    public void handleDrop(Transferable transferable, DataFlavor dataFlavor, Point dropLocation) {
        // Fetch the url from the transferable using the flavor it is provided
        // (assuming it is a URL data flavor)
        URL url = null;
        try {
            url = (URL)transferable.getTransferData(dataFlavor);
        } catch (java.io.IOException excp) {
            logger.log(Level.WARNING, "Unable to complete drag and drop", excp);
        } catch (UnsupportedFlavorException excp) {
            logger.log(Level.WARNING, "Unable to complete drag and drop", excp);
        }
        URLDataFlavorHandler.launchCellFromURL(url);
    }

    /**
     * Launches a cell based upon a given URL. This method assumes the URL
     * refers to some generally-available web content that all clients can fetch
     *
     * @param url The URL to launch a Cell with
     */
    public static void launchCellFromURL(URL url) {
        // Fetch the file extension of the URL to figure out which Cell to
        // create
        String extension = DragAndDropManager.getFileExtension(url.getFile());

        // Next look for a cell type that handles content with this file
        // extension and create a new cell with it.
        CellRegistry registry = CellRegistry.getCellRegistry();
        Set<CellFactorySPI> factories = registry.getCellFactoriesByExtension(extension);
        if (factories == null) {
            logger.warning("Could not find cell factory for " + extension);
            JFrame frame = JmeClientMain.getFrame().getFrame();
            JOptionPane.showMessageDialog(frame,
                    "The content type for this URL is not supported: " +
                    url.toExternalForm());
            return;
        }
        CellFactorySPI factory = factories.iterator().next();

        // Get the cell server state, injecting the content URI into it via
        // the properties
        Properties props = new Properties();
        props.put("content-uri", url.toExternalForm());
        CellServerState state = factory.getDefaultCellServerState(props);

        // Create the new cell at a distance away from the avatar
        try {
            CellUtils.createCell(state);
        } catch (CellCreationException excp) {
            logger.log(Level.WARNING, "Unable to create cell for uri " + url, excp);
        }
    }
}
