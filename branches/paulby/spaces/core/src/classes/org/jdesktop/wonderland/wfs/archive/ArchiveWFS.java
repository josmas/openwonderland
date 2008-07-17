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
package org.jdesktop.wonderland.wfs.archive;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.jdesktop.wonderland.wfs.InvalidWFSException;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSAliases;
import org.jdesktop.wonderland.wfs.WFSRootDirectory;
import org.jdesktop.wonderland.wfs.WFSVersion;

/**
 * The ArchiveWFS class extends the WFS abstract class and represents a Wonderland
 * File System that resides as an archive file. The archive file, which may be
 * located over the net, is specified as a URL as follows:
 * <p>
 * jar:<url>!\
 * <p>
 * where <url> is the location of the jar file. For example:
 * <p>
 * jar:http://www.foo.com/bar.jar!/
 * <p>
 * specifies a JAR file named bar.jar located at http://www.foo.com.
 * <p>
 * The implementation for jar files do not yet supported writing.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ArchiveWFS extends WFS {
    /* The location of the file system */
    private URI uri = null;
    
    /* The directory object associated with the root of the file system */
    private WFSRootDirectory directory = null;
    
    /* The object managing the manifest and JAR file contents */
    private ArchiveManifest manifest = null;

    /**
     * Creates a new instance of WFS given the URL of the archive file.
     * <p>
     * @param uri The URI of the location of the file system
     * @throw SecurityException If a URI protocol type is not supported
     * @throw FileNotFoundException If the URI does not exist
     * @throw InvalidWFSException If the URI does not contain a valid WFS
     * @throw IOException If the URI cannot be opened and/or read.
     */
    public ArchiveWFS(URL url) throws SecurityException, IOException, InvalidWFSException {
        /*
         * Given the URL, parse into the constituent components: the protocol
         * (which must be JAR) and the WFS URI. Open a connection to the JAR
         * file.
         */
        String protocol = url.getProtocol();
        String body     = url.getPath();
        URI    wfsuri   = null;
        
        /* If the protocol is not JAR, then throw IOException */
        if (protocol.equals("jar") != true) {
            throw new IOException("Protocol of URL is not JAR: " + url.toString());
        }
        
        /* Try to parse of the WFS URI */
        try {
            wfsuri = this.getWfsUri(body);
        } catch (URISyntaxException excp) {
            throw new IOException("Invalid WFS URI: " + url.toString());
        }
        
        /* Open a connection to the JAR file and parse out its entries */
        this.manifest = new ArchiveManifest(url);
        
        /*
         * Find the base-level wfs directory. If there is more than one, then
         * simply take the first. If there are no file systems within the JAR
         * file, then throw an exception.
         */
        String[] fsystems = this.manifest.getFileSystems();
        if (fsystems.length == 0) {
            throw new InvalidWFSException("WFS URI has no valid filesystems: " +
                uri.toString());
        }
        String wfsdir = fsystems[0];
        
        /* If a WFS URI is given, find the root of the world XXX */
        
        /* Create the top level directory consisting of the base WFS directory */
        this.directory = new WFSArchiveRootDirectory(this.manifest, wfsdir);
        
        /*
         * Read the version.xml file from disk and instantiate a WFSVersion
         * class, if it exists
         */
        String      wfsversion = wfsdir + "/" + WFSRootDirectory.VERSION;
        InputStream vis        = this.manifest.getEntryInputStream(wfsversion);
        if (vis != null) {
            this.directory.setVersion(WFSVersion.decode(vis));
        }
        
        /*
         * Read the aliases.xml file from disk and instantiate a WFSAliases
         * class, if it exists
         */
        String      wfsaliases = wfsdir + "/" + WFSRootDirectory.ALIASES;
        InputStream ais        = this.manifest.getEntryInputStream(wfsaliases);
        if (ais != null) {
            this.directory.setAliases(WFSAliases.decode(ais));
        }
        
 
    }
    
    /**
     * Closes any resources associated with the archive
     */
    @Override
    public void close() {
        this.manifest.close();
    }

    /**
     * Returns the root cell directory class representing of the WFS.
     * 
     * @return The directory containing the children in the root of the WFS
     */
    public WFSRootDirectory getRootDirectory() {
        return this.directory;
    }
        
    /**
     * Writes the entire WFS to the underlying medium, including the meta-
     * information contains within the root directory, the cells containing
     * within the root directory, and any child directories.
     * <p>
     * @throw IOException Upon a general I/O error.
     */
    public void write() throws IOException {
        // Writing to archive not currently suppported
        throw new UnsupportedOperationException("Not yet supported.");
    }
    
    /*-----------------------------------------------------------------------*
     * Private Utility Routines                                              *
     *-----------------------------------------------------------------------*/
    
    /**
     * Given an URL of protocol type "jar" searches for a WFS URI, that is,
     * one that follows a "#" in the URL. If no such URL exists, then return
     * null, otherwise, return the URL object.
     * <p>
     * @param The body of the JAR URL
     * @return The WFS URI
     * @throw URISyntaxException Indicates a malformed WFS URI
     */
    public URI getWfsUri(String url) throws URISyntaxException {
        /* Find the '#' character and fetch the substring if it exists */
        int index = url.lastIndexOf("#");
        if (index == -1) {
            return null;
        }
        
        /* Otherwise, construct the WFS URI and return it */
        return new URI(url.substring(index));
    }
}
