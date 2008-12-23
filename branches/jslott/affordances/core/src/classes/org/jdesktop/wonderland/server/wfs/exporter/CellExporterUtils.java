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

package org.jdesktop.wonderland.server.wfs.exporter;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.wfs.WorldRoot;


/**
 * The CellExporter contains a collection of static utility methods to export
 * WFS information from the WFS web service.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellExporterUtils {
    /* The prefix to add to URLs for the WFS web service */
    private static final String WFS_PREFIX = "wonderland-web-wfs/wfs/";

    /**
     * Creates a new snapshot, returns a WorldRoot object representing the
     * new WFS or null upon failure
     */
    public static WorldRoot createSnapshot() {
        try {
            URL url = new URL(getWebServerURL(), WFS_PREFIX + "create/snapshot");
            return WorldRoot.decode(new InputStreamReader(url.openStream()));
        } catch (java.lang.Exception excp) {
            Logger.getLogger(CellExporterUtils.class.getName()).log(Level.WARNING,
                    "[WFS] Error creating snapshot", excp);
            return null;
        }
    }
    
    /**
     * Creates a cell on disk given the description of the cell, which includes
     * the root of the wfs, the path of the parent, the child name, and the
     * cell's setup information 
     */
    public static void createCell(String descriptor) throws MalformedURLException, IOException, JAXBException {
        // Open an output connection to the URL, pass along any exceptions
        URL url = new URL(getWebServerURL(), WFS_PREFIX + "create/cell");
        Logger.getLogger(CellExporterUtils.class.getName()).warning("[WFS] URL " + url.toString());
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/xml");
        OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream());
        
        // Write out the class as an XML stream to the output connection
        w.write(descriptor);
        w.flush();
        w.close();
        
        // For some reason, we need to read in the input for the HTTP POST to
        // work
        InputStreamReader r = new InputStreamReader(connection.getInputStream());
        String str;
        while (r.read() != -1) {
            // Do nothing
        }
        r.close();
    }
    
    /**
     * Returns the base URL of the web server.
     */
    public static URL getWebServerURL() throws MalformedURLException {
        return new URL(System.getProperty("wonderland.web.server.url"));
    }
}
