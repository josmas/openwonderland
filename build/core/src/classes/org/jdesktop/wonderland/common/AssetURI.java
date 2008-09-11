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
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The AssetURI class uniquely identifies a resource within the sytem that is
 * managed by the client-side asset management system. It is an extension of
 * the java.net.URI class with special accessor methods.
 * <p>
 * The AssetURI typically takes one of the following formats: a well-formed
 * URL, a relative URL, and a Wonderland module URI.
 * 
 * <h3>A well-formed URL<h3>
 * 
 * If the asset refers to a definite asset located over the internet, then the
 * AssetURI takes the form of a well-formed URL. That is, it has a well-known
 * scheme such as 'http', 'ftp', or 'file'. The isDefinite() method returns true
 * in this instance (equivalent to isAbsolute() == true && getScheme() != 'wla').
 * 
 * <h3>A relative URL</h3>
 * 
 * If the asset refers to an asset stored within the default repository, then
 * the AssetURI takes the form of a relative URL. The absolute location of this
 * resource is a concatentation of the "base URL" for the repository configured
 * in Wonderland plus the relative URL. The isRelative() == false is returned
 * in this instance.
 * 
 * <h3>A Wonderland module URI</h3>
 * 
 * If the asset refers to an asset found in a Wonderland module, then the
 * AssetURI takes the form of an absolute URI with a getScheme() == 'wla'. The
 * actual location of the asset is obtained by taking the name of the module
 * (returned by getModuleName() method) and the path (returned by getPath()).
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class AssetURI {
    /* The URI which is wrapped by this class */
    private URI uri = null;
    
    /* The protocol name for assets stored in Wonderland modules */
    public static final String WLM_PROTOCOL = "wla";
    
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
     * Returns the URI object
     * 
     * @return The actual URI object
     */
    public URI getURI() {
        return this.uri;
    }
    
    /**
     * Returns true if the URI is a well-formed URL, false if not. A URI is
     * a well-formed URL only if it is an absolute URI with a well-known scheme
     * such as 'http', 'ftp', or 'file'.
     * 
     * @return True if the URI is an absolute, well-formated URL.
     */
    public boolean isDefinite() {
        return this.uri.isAbsolute() && this.uri.getScheme().equals(WLM_PROTOCOL) == false;
    }
    
    /**
     * Returns true if the URI refers to an asset within a module, false if not.
     * A URI refers to an asset within a module if it is absolute and its scheme
     * is equal to 'wlm'm
     * 
     * @return True if the URI is a Wonderland module URI.
     */
    public boolean isModule() {
        return this.uri.isAbsolute() && this.uri.getScheme().equals(WLM_PROTOCOL) == true;
    }
    
    /**
     * Returns true if the URI is a relative URL.
     * 
     * @return True if the URI is a relative URL.
     */
    public boolean isRelative() {
        return this.uri.isAbsolute() == false;
    }
    
    /**
     * Returns the module name only if this URI refers to a Wonderland module.
     * Otherwise, returns null
     * 
     * @return The module name, null if it doesn't refer to a module
     */
    public String getModuleName() {
        if (this.isModule() == true) {
            return this.uri.getHost();
        }
        return null;
    }
    
    /**
     * Returns the relative path of the resource specified by the URI.
     * 
     * @return The relative path within the URI
     */
    public String getRelativePath() {
        return this.uri.getPath();
    }
    
    /**
     * Returns a relative path of the asset so that it exists in a unique
     * location within a cache. The path does not have a leading "/".
     * 
     * @return A unique relative path for the URI
     */
    public String getRelativeCachePath() {
 
        if (this.isRelative() == true) {
            /*
             * If the uri is relative (to some system-wide asset repository) simply
             * store it under the "system" directory.
             */
            return "system" + File.separator + this.getRelativePath();
        }
        else if (this.isDefinite() == true) {
            /*
             * If the uri refers to an asset described by a full URL, then simply
             * store it under the "definite" directory.
             */
            return "definite" + this.getRelativePath();
        }
        else if (this.isModule() == true) {
            /*
             * If the uri describes an asset within a module, prepend the "module"
             * directory followed by the name of the module (which must be
             * unique).
             */
            return "module" + File.separator + this.getModuleName() + this.getRelativePath();
        }
        return null;
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
