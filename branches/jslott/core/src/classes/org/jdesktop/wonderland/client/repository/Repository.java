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
package org.jdesktop.wonderland.client.repository;

import org.jdesktop.wonderland.checksum.RepositoryChecksums;
import org.jdesktop.wonderland.common.AssetURI;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A Repository represents a collection of assets located and made available on
 * some asset server. Multiple repositories (on multiple servers) may serve the
 * same (or similar assets). Typically, the assets contained within a repository
 * come from the same module, although a repository make represent the assets
 * of multiple modules. An asset server may contain multiple repositories. (In
 * fact, the same asset server may host multiple repositories of the same
 * assets -- assets are uniquely identified by a URI and their checksum.)
 * <p>
 * The list of checksums may be null -- which indicates that it has not been
 * loaded. If an attempt was made to load the checksums and it failed or did
 * not exist, then the 'checksums' member variable contains a valid, but empty
 * RepositoryChecksums object.
 * <p>
 * A Repository is uniquely identified by a base URL. Within the base-level
 * directory specified by the unique URL is a checksum.xml file that provides
 * the checksums for the assets contained within the repository. If the checksum
 * file is missing, or assets do not have entries in the checksum file, it is
 * assumed that these assets are not to be cached.
 * 
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net?
 */
@ExperimentalAPI
public class Repository {
    
    /* The unique URL that serves the repository, with any trailing '/' */
    private String baseURL = null;
    
    /* The collection of checksums for each asset */
    private RepositoryChecksums checksums = null;

    /**
     * Constructor that takes the URL and checksums.
     * 
     * @param baseURL The base URL of the repository
     * @param checksums The checksum for the assets within the repository
     */    
    public Repository(String baseURL, RepositoryChecksums checksums) {
        this.baseURL = this.stripTrailingSlash(baseURL);
        this.checksums = checksums;
    }

    /**
     * Constructor that just takes the URL.
     * 
     * @param baseURL The base URL of the repository
     */
    public Repository(String baseURL) {
        this.baseURL = this.stripTrailingSlash(baseURL);
        this.checksums = null;
    }
    
    /**
     * Returns the unique, base URL for the repository.
     * 
     * @return The base URL for the repository
     */
    public String getBaseURL() {
        return this.baseURL;
    }

    /**
     * Returns the checksums for the repository assets.
     * 
     * @return The checksums for the repository assets
     */
    public RepositoryChecksums getChecksums() {
        return this.checksums;
    }
    
    /**
     * Returns the full URL associated with the Asset to download from this
     * repository.
     * 
     * @param assetURI The URI of the asset to download
     */
    public String getAssetURL(AssetURI assetURI) {
        return this.baseURL + "/" + assetURI.getRelativePath();
    }
    
    /**
     * Strips the trailing '/' if it exists on the string.
     */
    private String stripTrailingSlash(String str) {
        if (str.endsWith("/") == true) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }
}
