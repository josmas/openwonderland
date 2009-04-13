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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.dnd.spi.DataFlavorHandlerSPI;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A handler to support drag-and-drop from a URI (perhaps from a web browser).
 * The data flavor supported has the mime type "application/x-java-url". This
 * simply looks for a Cell that can handle the data type and launches it.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class URIListDataFlavorHandler implements DataFlavorHandlerSPI {

    private static Logger logger = Logger.getLogger(URIListDataFlavorHandler.class.getName());

    /**
     * @inheritDoc()
     */
    public DataFlavor[] getDataFlavors() {
        try {
            return new DataFlavor[] {
                new DataFlavor("text/uri-list;class=java.lang.String")
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
        // Just accept everything sent our way
        return true;
    }

    /**
     * @inheritDoc()
     */
    public void handleDrop(Transferable transferable, DataFlavor dataFlavor, Point dropLocation) {
        // Fetch the uri from the transferable using the flavor it is provided
        // (assuming it is a URI data flavor). Convert into a list of URIs,
        // just take the first one.
        String data = null;
        try {
            data = (String) transferable.getTransferData(dataFlavor);
        } catch (UnsupportedFlavorException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        List<URI> uriList = uriStringToList(data);

        // Check to see if we have at least one URI, if not, log an error and
        // return
        if (uriList.isEmpty() == true) {
            logger.warning("No URIs found in transferable, data " + data);
            return;
        }
        URI uri = uriList.get(0);

        // Find the scheme. We'll need this to dispatch to other places. Make
        // sure it is not null (can it ever be null?)
        String scheme = uri.getScheme();
        if (scheme == null) {
            logger.warning("Scheme is null for dropped URI " + uri.toString());
            return;
        }

        // First check to see if the protocol is not "file". If so, then assume
        // the content is available over the network somewhere and launch a
        // Cell based upon it.
        if (scheme.equals("file") == false) {
            try {
                URLDataFlavorHandler.launchCellFromURL(uri.toURL());
            } catch (MalformedURLException excp) {
                logger.log(Level.WARNING, "Unable to form URL from URI " +
                        uri.toString(), excp);
                return;
            }
        }
        else {
            // The URI has a scheme of "file". On certain systems (e.g. Mac OSX),
            // the URI has the form "file://localhost/<path>". This form cannot
            // be converted into a File, so we must just take the <path> part
            // and create a File with it. If the URi does not have an "authority"
            // then we can directly create a file object from it.
            File file = null;
            if (uri.getAuthority() != null) {
                logger.warning("FILE PATH " + uri.getPath());
                file = new File(uri.getPath());
            }
            else {
                logger.warning("FILE PATH " + uri.toString());
                file = new File(uri);
            }

            // Launch a file based upon the File object we just created
            List<File> fileList = new LinkedList();
            fileList.add(file);
            FileListDataFlavorHandler.launchCellFromFileList(fileList);
        }
    }

    /**
     * Takes a string list of URIs returned from the "text/uri-list" DataFlavor
     * type and returns a List of URIs of the data.
     * <p>
     * This method is taken from the Java SE bug database at:
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4899516.
     */
    private List<URI> uriStringToList(String data) {
        java.util.List list = new ArrayList(1);
        for (StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens();) {
            String s = st.nextToken();
            if (s.startsWith("#") == true) {
                // the line is a comment (as per the RFC 2483)
                continue;
            }
            try {
                URI uri = new URI(s);
                list.add(uri);
            } catch (java.net.URISyntaxException e) {
                // malformed URI
                logger.log(Level.SEVERE, null, e);
            } catch (IllegalArgumentException e) {
                // the URI is not a valid 'file:' URI
                logger.log(Level.SEVERE, null, e);
            }
        }
        return list;
    }
}
