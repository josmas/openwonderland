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
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.content.ContentImportManager;
import org.jdesktop.wonderland.client.content.spi.ContentImporterSPI;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.dnd.spi.DataFlavorHandlerSPI;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A handler to support drag-and-drop from the desktop. The data flavor supported
 * is the "java file list" type. This interacts with the content manager to
 * find the importer for the content and dispatch there.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class DesktopImportDataFlavorHandler implements DataFlavorHandlerSPI {

    private static Logger logger = Logger.getLogger(DesktopImportDataFlavorHandler.class.getName());

    public DataFlavor[] getDataFlavors() {
        return new DataFlavor[] { DataFlavor.javaFileListFlavor };
    }

    public void handleDrop(Transferable transferable, DataFlavor dataFlavor, Point dropLocation) {

        // Fetch the list of files from the transferable using the flavor
        // provided (assuming it is the java file list flavor).
        List<File> fileList = null;
        try {
            fileList = (List<File>) transferable.getTransferData(dataFlavor);
        } catch (java.io.IOException excp) {
        } catch (UnsupportedFlavorException excp) {
        }

        // Check to see that we have at least one file. If not signal an
        // error and return
        if (fileList.size() < 1) {
            logger.warning("No file is given during drag-and-drop");
            return;
        }

        // Just take the first file and find out its extension. If there is
        // none, then signal an error and return since we do not know who
        // handles this file type.
        final File file = fileList.get(0);
        final String extension = getFileExtension(file.getName());
        if (extension == null) {
            logger.warning("No file extension found for " + file.getAbsolutePath());
            return;
        }

        // Otherwise, ask the content manager for whom handles this kind of
        // file and dispatch there.
        ContentImportManager cim = ContentImportManager.getContentImportManager();
        final ContentImporterSPI importer = cim.getContentImporter(extension, true);
        if (importer == null) {
            logger.warning("No importer found for " + file.getAbsolutePath());
            return;
        }

        // Kick off a thread to upload the content. We put this in its own
        // thread to let this method complete (since we are running on the
        // AWT event queue.
        new UploadThread(file, extension, importer).start();
    }

    /**
     * A thread that uploads content and displays a message dialog indicating
     * the status of the upload
     */
    class UploadThread extends Thread {
        private File file = null;
        private String extension = null;
        private ContentImporterSPI importer;

        public UploadThread(File file, String extension, ContentImporterSPI importer) {
            this.file = file;
            this.extension = extension;
            this.importer = importer;
        }

        @Override
        public void run() {
            // Display a dialog showing a wait message while we import. We need
            // to do this in the SwingWorker thread so it gets drawn
            JOptionPane waitMsg = new JOptionPane("Please wait while " +
                    file.getName() + " is being uploaded");
            JFrame frame = JmeClientMain.getFrame().getFrame();
            final JDialog dialog = waitMsg.createDialog(frame, "Uploading Content");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dialog.setVisible(true);
                }
            });

            String uri = importer.importFile(file, extension);

            // Close down the dialog indicating success
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dialog.setVisible(false);
                }
            });
        }
    }

    /**
     * Returns the string extension name of the given file name. If none, return
     * null. This simply looks for the final period (.) in the name.
     *
     * @param fileName The name of the file
     * @return The file extension
     */
    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        return fileName.substring(index + 1);
    }
}
