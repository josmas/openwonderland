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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
public class AssetURI {
    /* The URI which is wrapped by this class */
    private URI uri = null;
    
    /** Default constructor */
    public AssetURI() {
    }
    
    /**
     * Constructor which takes the string represents of the URI.
     * 
     * @param uri The string URI representation
     * @throw URISyntaxException If the URI is not well-formed
     */
    public AssetURI(String uri) throws URISyntaxException {
        this.uri = new URI(uri);
    }

    /**
     * Constructor which takes the module name, relative path and server name
     */
    public AssetURI(String moduleName, String path, String serverURL) throws URISyntaxException {
        this.uri = new URI("wla://" + moduleName + "/" + path + "?server=" + serverURL);
    }
    
    /**
     * Constructor which takes the module uri and server name
     */
    public AssetURI(String uri, String serverURL) throws URISyntaxException {
        this.uri = new URI(uri + "?server=" + serverURL);
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
     * Returns a URL from the URI.
     * 
     * @return A URL
     */
    public URL toURL() throws MalformedURLException {
        return this.uri.toURL();
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
     * Returns the name of the server associated with this URI. This is encoded
     * as a query string. Returns null if the server is not found.
     * 
     * @return The name of the server session
     */
    public String getServer() {
        return this.getQueryMap().get("server");
    }
    
    /**
     * Returns the relative path of the resource specified by the URI. The
     * relative path does not being with any forward "/".
     * 
     * @return The relative path within the URI
     */
    public String getRelativePath() {
       String path = this.getURI().getPath();
       if (path.startsWith("/") == true) {
           path = path.substring(1);
       }
       return path;
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
     * Returns the string representation of the URI
     * 
     * @return The string representation of the URI
     */
    @Override
    public String toString() {
        return this.uri.toString();
    }

    /**
     * Returns a map of the query parameters in key-value pair.
     */
    private Map<String, String> getQueryMap() {
        Map<String, String> map = new HashMap<String, String>();
        String query = this.getURI().getQuery();
        if (query == null) {
            return map;
        }
        
        String[] params = query.split("&");
        for (String param : params) {
            String[] pArray = param.split("=");
            map.put(pArray[0], pArray[1]);
        }
        return map;
    }
}
