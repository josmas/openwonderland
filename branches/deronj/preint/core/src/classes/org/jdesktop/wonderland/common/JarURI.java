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
package org.jdesktop.wonderland.common;

import java.io.File;
import java.net.URISyntaxException;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The JarURI class uniquely identifies a plugin jar resource within the sytem.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
@XmlJavaTypeAdapter(JarURIAdapter.class)
public class JarURI extends ResourceURI {
    
    /** Default constructor */
    public JarURI() {
    }
    
    /**
     * Constructor which takes the string represents of the URI.
     * 
     * @param uri The string URI representation
     * @throw URISyntaxException If the URI is not well-formed
     */
    public JarURI(String uri) throws URISyntaxException {
        super(uri);
    }

    /**
     * Constructor which takes the module name, relative path and server name/port
     */
    public JarURI(String moduleName, String path, String server) throws URISyntaxException {
        super("wlj://" + moduleName + "@" + server + "/" + path);
    }
    
    /**
     * Returns the relative path of the resource specified by the URI. The
     * relative path does not being with any forward "/".
     * 
     * @return The relative path within the URI
     */
    public String getRelativePath() {
        String relativePath = this.getURI().getPath();
        if (relativePath.startsWith("/") == true) {
            relativePath = relativePath.substring(1);
        }
        return relativePath;
    }
    
    /**
     * Returns a relative path of the asset so that it exists in a unique
     * location within a cache. The path does not have a leading "/".
     * 
     * @return A unique relative path for the URI
     */
    public String getRelativeCachePath() {
        /*
         * If the uri describes an asset within a module, prepend the "module"
         * directory followed by the name of the module (which must be
         * unique).
         */
        return "module" + File.separator + this.getModuleName() + File.separator + this.getRelativePath();
    }
}
