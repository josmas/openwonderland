/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellSelectionRegistry;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.cell.utils.spi.CellSelectionSPI;
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

    private static final Logger LOGGER = Logger.getLogger(
            URLDataFlavorHandler.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/client/jme/dnd/Bundle");

    /**
     * @inheritDoc()
     */
    public DataFlavor[] getDataFlavors() {
        try {
            return new DataFlavor[]{
                        new DataFlavor(
                                "application/x-java-url; class=java.net.URL")
                    };
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Unable to find DataFlavor for URL", ex);
            return new DataFlavor[]{};
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
            url = (URL) transferable.getTransferData(dataFlavor);
        } catch (java.io.IOException excp) {
            LOGGER.log(Level.WARNING, "Unable to complete drag and drop", excp);
            return false;
        } catch (UnsupportedFlavorException excp) {
            LOGGER.log(Level.WARNING, "Unable to complete drag and drop", excp);
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
    public void handleDrop(Transferable transferable, DataFlavor dataFlavor,
            Point dropLocation) {
        // Fetch the url from the transferable using the flavor it is provided
        // (assuming it is a URL data flavor)
        URL url = null;
        try {
            url = (URL) transferable.getTransferData(dataFlavor);
        } catch (java.io.IOException excp) {
            LOGGER.log(Level.WARNING, "Unable to complete drag and drop", excp);
        } catch (UnsupportedFlavorException excp) {
            LOGGER.log(Level.WARNING, "Unable to complete drag and drop", excp);
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

        // First look for the SPI that tells us which Cell to use. If there
        // is none, then it is a fairly big error. (There should be at least
        // one registered in the system).
        CellSelectionSPI spi = CellSelectionRegistry.getCellSelectionSPI();
        if (spi == null) {
            final JFrame frame = JmeClientMain.getFrame().getFrame();
            LOGGER.warning("Could not find the CellSelectionSPI factory");
            String message = BUNDLE.getString("Launch_Failed_Message");
            message = MessageFormat.format(message, url.toExternalForm());
            JOptionPane.showMessageDialog(frame, message,
                    BUNDLE.getString("Launch_Failed"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Next look for a cell type that handles content with this file
        // extension and create a new cell with it.
        CellFactorySPI factory = null;
        try {
            factory = spi.getCellSelection(extension);
        } catch (CellCreationException excp) {
            LOGGER.log(Level.WARNING,
                    "Could not find cell factory for " + extension, excp);
            JFrame frame = JmeClientMain.getFrame().getFrame();
            String message = BUNDLE.getString("Unsupported_Content_Type");
            message = MessageFormat.format(message, url.toExternalForm());
            JOptionPane.showMessageDialog(frame, message);
            return;
        }

        // If the returned factory is null, it means that the user has cancelled
        // the action, so we just return
        if (factory == null) {
            return;
        }

        // Get the cell server state, injecting the content URI into it via
        // the properties
        Properties props = new Properties();
        props.put("content-uri", url.toExternalForm());
        CellServerState state = factory.getDefaultCellServerState(props);

        // Create the new cell at a distance away from the avatar
        try {
            CellUtils.createCell(state);
        } catch (CellCreationException excp) {
            LOGGER.log(Level.WARNING,
                    "Unable to create cell for uri " + url, excp);
        }
    }
}
