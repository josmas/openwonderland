/**
 * Project Wonderland
 *
 * $RCSfile: AssetType.java,v $
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
 * $Date: 2007/05/04 23:11:34 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.server.cell.loader.wfs;

import org.jdesktop.wonderland.common.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A WFSURI is a URI that uniquely identifies a WFS that is contained within
 * a module. It consists of the module name and the unique name of the WFS
 * within that module.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class WFSURI {
    /* The URI which is wrapped by this class */
    private URI uri = null;
    
    /* The protocol name for WFSs stored in Wonderland modules */
    public static final String WFS_PROTOCOL = "wfs";
    
    /**
     * Constructor which takes the string represents of the URI.
     * 
     * @param uri The string URI representation
     * @throw URISyntaxException If the URI is not well-formed
     */
    public WFSURI(String uri) throws URISyntaxException {
        this.uri = new URI(uri);
    }

    /**
     * Returns the URI object
     * 
     * @return The actual URI object
     */
    public URI getURI() {
        return this.uri;
    }
    
    /**
     * Returns the module name.
     * 
     * @return The module name, null if it doesn't refer to a module
     */
    public String getModuleName() {
        return this.uri.getHost();
    }
    
    /**
     * Returns the name of the WFS in the URI.
     * 
     * @return The name of the WFS in the URI
     */
    public String getRelativePath() {
        return this.stripLeadingSlash(this.uri.getPath());
    }
    
    /**
     * Returns the string representation of the URI
     * 
     * @return The string representation of the URI
     */
    @Override
    public String toString() {
        return this.uri.toString();
    }
    
    /**
     * Strips off the leading "/", if there is one and returns the new string
     */
    private String stripLeadingSlash(String path) {
        if (path.startsWith("/") == true) {
            return path.substring(1);
        }
        return path;
    }
}
