/**
 * Project Looking Glass
 *
 * $RCSfile: WFS.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2007/10/23 21:34:33 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.server.utils.wfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.utils.wfs.archive.ArchiveWFS;
import org.jdesktop.wonderland.server.utils.wfs.file.FileWFS;

/**
 * The WFS class represents a Wonderland File System (WFS). A WFS is a series
 * of directories and files which describe a world or a portion of a world. A
 * WFS may also contain a series of resources and/or cells which are exported
 * for use.
 * <p>
 * The WFS class is instantiated with the URI describing the location of the
 * file system. Currently, the URI is restricted to of type 'file' for security
 * reasons and as the initial implementation step. Also, the URI must be a
 * directory on disk, and not an archive file in the current implementation.
 * <p>
 * @author jslott
 */
public abstract class WFS {
    
    /* The error logger */
    private static final Logger logger = Logger.getLogger("wonderland.wfs");
    
    /* Prefix names for WFS components */
    public static final String CELL_DIRECTORY_SUFFIX = "-wld";
    public static final String CELL_FILE_SUFFIX = "-wlc.xml";
    public static final String WFS_DIRECTORY_SUFFIX = "-wfs";
    
    /* The standard name of the aliases.xml and version.xml files */
    public static final String ALIASES = "aliases.xml";
    public static final String VERSION = "version.xml";
    
    /* Support URL protocols for file systems */
    private static final String FILE_PROTOCOL = "file";
    private static final String JAR_PROTOCOL  = "jar";
    private static final String WFS_PROTOCOL  = "wfs";
    
    /** Creates a new instance of WFS */
    public WFS() {
    }
    
    /**
     * Factory method for creating a new WFS object based upon a given URL
     */
    public static final WFS createWFS(URL url) throws FileNotFoundException, IOException, InvalidWFSException {
        String protocol = url.getProtocol();
        
        /* If the URL points to a disk directory */
        if (protocol.equals(WFS.FILE_PROTOCOL) == true) {
            //try {
                return new FileWFS(url.getPath());
            //} catch (URISyntaxException excp) {
            //    WFS.getLogger().log(Level.SEVERE, "Invalid File URL: " + url.toString(), excp);
            //    throw new FileNotFoundException();
            //}
        }
        else if (protocol.equals(WFS.JAR_PROTOCOL) == true) {
            return new ArchiveWFS(url);
        }
        else if (protocol.equals(WFS.WFS_PROTOCOL) == true) {
            return null;
        }
        else {
            throw new InvalidWFSException("Invalid Protocol for WFS Given: " + url.toString());
        }
    }
    
    /**
     * Close any open resource associated with the WFS
     */
    public void close() {
        // Do nothing, to be overridden perhaps
    }
    
    /**
     * Returns the WFS URI aliases for this filesystem
     */
    public abstract WFSAliases getAliases();
    
    /**
     * Returns the Cell directory class representing this root
     */
    public abstract WFSCellDirectory getCellDirectory();
    
    /**
     * Returns the logger for the WFS package
     */
    public static Logger getLogger() {
        return WFS.logger;
    }
    
    /**
     * Returns the version of the file system
     *
     * @return The version of the WFS.
     */
    public abstract WFSVersion getVersion();
}
