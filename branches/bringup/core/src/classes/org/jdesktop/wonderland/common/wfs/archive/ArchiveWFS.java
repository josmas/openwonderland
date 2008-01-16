/*
 * Project Looking Glass
 *
 * $RCSfile: ArchiveWFS.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.2 $
 * $Date: 2007/10/17 17:11:15 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.common.wfs.archive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import org.jdesktop.wonderland.common.wfs.*;
import org.jdesktop.wonderland.common.wfs.WFS;

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
 * @author jslott
 */
public class ArchiveWFS extends WFS {
    /* The location of the file system */
    private URI uri = null;
    
    /* The version of the file system, parsed from the top-level directory */
    private WFSVersion version = null;
    
    /* The mapping of URIs to locations within this file system */
    private WFSAliases aliases = null;
    
    /* The directory object associated with the root of the file system */
    private WFSCellDirectory directory = null;
    
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
        
        /*
         * Read the version.xml file from disk and instantiate a WFSVersion
         * class, if it exists
         */
        String      wfsversion = wfsdir + "/" + WFS.VERSION;
        InputStream vis        = this.manifest.getEntryInputStream(wfsversion);
        this.version           = WFSVersion.decode(vis);

        /*
         * Read the aliases.xml file from disk and instantiate a WFSAliases
         * class, if it exists
         */
        String      wfsaliases = wfsdir + "/" + WFS.ALIASES;
        InputStream ais        = this.manifest.getEntryInputStream(wfsaliases);
        this.aliases           = WFSAliases.decode(ais);
        
        /* Create the top level directory consisting of the base WFS directory */
        this.directory = new WFSArchiveCellDirectory(this.manifest, wfsdir, wfsdir);
    }
    
    /**
     * Closes any resources associated with the archive
     */
    @Override
    public void close() {
        this.manifest.close();
    }
    
    /**
     * Returns the version of the file system
     * @return The version of the WFS.
     */
    public WFSVersion getVersion() {
        return this.version;
    }
    
    /**
     * Returns the WFS URI aliases for this filesystem
     */
    public WFSAliases getAliases() {
        return this.aliases;
    }
    
    /**
     * Returns the Cell directory class representing this root
     */
    public WFSCellDirectory getCellDirectory() {
        return this.directory;
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
    
    private static void printDir(WFSCellDirectory dir) {
        System.out.println("Parent: " + dir.getCanonicalParent());
        WFSCell[] cells = dir.getCells();
        for (WFSCell cell : cells) {
            String name   = cell.getCellName();
            String path   = cell.getCanonicalName();
            
            System.out.println("Cell: " + name + " (" + path + ")");
            try {
                WFSCellDirectory newdir = dir.getCellDirectory(name, path);
                ArchiveWFS.printDir(newdir);
            } catch (Exception excp) {
                excp.toString();
                // nothing
            }
        }
    }
    
    public static final void main(String[] args) {
        try {
            ArchiveWFS wfs = new ArchiveWFS(new URL(args[0]));
            WFSCellDirectory dir = wfs.getCellDirectory();
            ArchiveWFS.printDir(dir);
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
        }
    }
}
