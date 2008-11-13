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
package org.jdesktop.wonderland.web.help.resources;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The HelpURI class uniquely identifies an help resource within the sytem.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class HelpURI {
    
    /* The Help URI: wlh://<module name>/<path> */
    private URI uri = null;
    
    /**
     * Constructor which takes the string represents of the URI.
     * 
     * @param uri The string URI representation
     * @throw URISyntaxException If the URI is not well-formed
     */
    public HelpURI(String uri) throws URISyntaxException {
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
     * Returns the module name off this URI.
     * 
     * @return The module name
     */
    public String getModuleName() {
        return this.uri.getHost();
    }
    
    /**
     * Returns the relative path of the resource specified by the URI. The
     * relative path does not being with any forward "/".
     * 
     * @return The relative path within the URI
     */
    public String getRelativePath() {
        return "www" + this.uri.getPath();
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
}
