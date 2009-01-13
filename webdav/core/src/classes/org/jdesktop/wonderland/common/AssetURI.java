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
package org.jdesktop.wonderland.common;

import java.io.File;
import java.net.URISyntaxException;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The AssetURI class uniquely identifies a resource within the sytem that is
 * managed by the client-side asset management system. It is an extension of
 * the java.net.URI class with special accessor methods.
 * <p>
 * A AssetURI is typically a URI where certain of the field has specific
 * interpretations. Since these URIs refer to resources in modules, the host
 * part is interpreted as the module name and the path part as the relative
 * path of the resource within the module.
 * <p>
 * This class is meant to be subclasses by more specific kinds of protocols.
 * Two examples are AssetURI (protocol 'wla') that represents art assets in
 * modules, and JarURI (protocol 'wlj') that represents plugin jars in modules.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
@XmlJavaTypeAdapter(AssetURIAdapter.class)
public class AssetURI extends ResourceURI {
    
    /** Default constructor */
    public AssetURI() {
        super();
    }
    
    /**
     * Constructor which takes the string represents of the URI.
     * 
     * @param uri The string URI representation
     * @throw URISyntaxException If the URI is not well-formed
     */
    public AssetURI(String uri) throws URISyntaxException {
        super(uri);
    }
    
    /**
     * Constructor which takes the module name, asset path and host name and
     * host port.
     */
    public AssetURI(String moduleName, String hostName, int hostPort, String assetPath) {
        super("wla", moduleName, hostName, hostPort, assetPath);
    }
    
    /**
     * Constructor which takes the module name, host name/port, and asset path.
     * The host name/port is given as: <host name>:<port>
     */
    public AssetURI(String moduleName, String hostNameAndPort, String assetPath) {
        super("wla", moduleName, hostNameAndPort, assetPath);
    }
    
    /**
     * Returns the relative path of the resource specified by the URI. The
     * relative path does not being with any forward "/".
     * 
     * @return The relative path within the URI
     */
    public String getRelativePath() {
       return "art/" + this.getAssetPath();
    }
    
    /**
     * Returns a relative path of the asset so that it exists in a unique
     * location within a cache. The path does not have a leading "/".
     * 
     * @return A unique relative path for the URI
     */
    public String getRelativeCachePath() {
        return "module" + File.separator + this.getModuleName() + File.separator + this.getRelativePath();       
    }
    
    /**
     * Annotates this URI with a <server name>:<port>. Returns a new instance
     * of AssetURI with this annotation
     * 
     * @param hostNameAndPort The <server name>:<port>
     * @return A new AssetURI with annotated with the <server name>:<port>
     * @throw URISyntaxException If the URI is not properly formed
     */
    public AssetURI getAnnotatedURI(String hostNameAndPort) throws URISyntaxException {
        return new AssetURI(this.getModuleName(), hostNameAndPort, this.getAssetPath());
    }
}
