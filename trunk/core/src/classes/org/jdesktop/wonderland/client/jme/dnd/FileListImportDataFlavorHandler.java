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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.content.ContentImportManager;
import org.jdesktop.wonderland.client.content.spi.ContentImporterSPI;
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
public class FileListImportDataFlavorHandler implements DataFlavorHandlerSPI {

    private static Logger logger = Logger.getLogger(FileListImportDataFlavorHandler.class.getName());

    /**
     * @inheritDoc()
     */
    public DataFlavor[] getDataFlavors() {
        return new DataFlavor[] { DataFlavor.javaFileListFlavor };
    }
    
    /**
     * @inheritDoc()
     */
    public boolean accept(Transferable transferable, DataFlavor dataFlavor) {
        // Just accept everything sent our way
        return true;
    }

    /**
     * @inheritDoc()
     */
    public void handleDrop(Transferable transferable, DataFlavor dataFlavor, Point dropLocation) {

        // Fetch the list of files from the transferable using the flavor
        // provided (assuming it is the java file list flavor).
        List<File> fileList = null;
        try {
            fileList = (List<File>) transferable.getTransferData(dataFlavor);
        } catch (java.io.IOException excp) {
            logger.log(Level.WARNING, "Unable to complete drag and drop", excp);
        } catch (UnsupportedFlavorException excp) {
            logger.log(Level.WARNING, "Unable to complete drag and drop", excp);
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
        final String extension = DragAndDropManager.getFileExtension(file.getName());
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
        new Thread() {
            @Override
            public void run() {
                importer.importFile(file, extension);
            }
        }.start();
    }
}
