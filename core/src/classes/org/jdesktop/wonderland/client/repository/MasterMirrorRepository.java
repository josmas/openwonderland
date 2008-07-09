/**
 * Project Wonderland
 *
 * $RCSfile: AssetDB.java,v $
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
 * $Revision: 1.15 $
 * $Date: 2007/08/07 17:01:12 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.repository;

import org.jdesktop.wonderland.common.AssetURI;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.config.WonderlandConfig;

/**
 * The SystemDefaultRepository class represents content that comes from an asset
 * server defined by a system-wide property.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@InternalAPI
public class MasterMirrorRepository implements Repository {
    
    /* The base URL for the master repository, without the '/' */
    private String masterBaseURL = null;
    
    /* The base URLs for the mirror repositories, without the '/' */
    private String[] mirrorBaseURLs = null;
    
    /** Default constructor */
    public MasterMirrorRepository(String masterBaseURL, String[] mirrorBaseURLs) {
        this.masterBaseURL = new String(this.stripTrailingSlash(masterBaseURL));
        this.mirrorBaseURLs = new String[mirrorBaseURLs.length];
        for (int i = 0; i < mirrorBaseURLs.length; i++) {
            this.mirrorBaseURLs[i] = new String(this.stripTrailingSlash(mirrorBaseURLs[i]));
        }
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
    
    /**
     * Returns the base URL of the repository we prefer to download from. This
     * always returns the master repository.
     * 
     * @return The repository that is preferred to download from
     */
    public String getPreferedBaseURL() {
        return this.masterBaseURL;
    }
    
    /**
     * Returns the full URL used to download the asset from the repository
     * that we prefer to download from. This always returns the master URL
     * followed by the relative path in the asset uri.
     * 
     * @param assetURI The uri of the asset to download
     * @return The full URL of the asset download
     */
    public String getPreferedURL(AssetURI assetURI) {
        return this.masterBaseURL + "/" + assetURI.getRelativePath();
    }
    
    /**
     * Returns an array of base URLs of all known servers that host the content.
     * This returns the master in the first index, followed by the mirrors.
     * 
     * @return An array of base repository URLs
     */
     public String[] getAllBaseURLs() {
         String[] baseURLs = new String[this.mirrorBaseURLs.length + 1];
         baseURLs[0] = this.masterBaseURL;
         for (int i = 0; i < this.mirrorBaseURLs.length; i++) {
             baseURLs[i + 1] = this.mirrorBaseURLs[i];
         }
         return baseURLs;
     }
     
     /**
      * Returns an array of full URLs used to download the asset from the
      * repository that host the content for the given asset uri. The order of
      * the list is determined by some policy.
      * 
      * @param assetURI The uri of the asset to download
      * @return The full URLs of the asset download
      */
     public String[] getAllURLs(AssetURI assetURI) {
         String[] baseURLs = new String[this.mirrorBaseURLs.length + 1];
         baseURLs[0] = this.masterBaseURL + "/" + assetURI.getRelativePath();
         for (int i = 0; i < this.mirrorBaseURLs.length; i++) {
             baseURLs[i + 1] = this.mirrorBaseURLs[i] + "/" + assetURI.getRelativePath();
         }
         return baseURLs; 
     }
}
