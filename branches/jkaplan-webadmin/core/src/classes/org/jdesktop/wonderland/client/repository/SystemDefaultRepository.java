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
public class SystemDefaultRepository implements Repository {
    
    /* The base url for the system-wide repository with any final '/' */
    private String baseURL = null;
    
    /** Default constructor */
    public SystemDefaultRepository() {
        /* Fetch the system-wide URL and remove any trailing '/' */
        this.baseURL = WonderlandConfig.getBaseURL();
        if (this.baseURL.endsWith("/") == true) {
            this.baseURL = this.baseURL.substring(0, this.baseURL.length() - 1);
        }
    }
    
    /**
     * Factory method to return a new instance of SystemDefaultRepository
     * 
     * @return A new SystemDefaultRepository class
     */
    public static final SystemDefaultRepository getSystemDefaultRepository() {
        return new SystemDefaultRepository();
    }
    
    /**
     * Returns the base URL of the repository we prefer to download from. This
     * always returns the system-wide repository.
     * 
     * @return The repository that is preferred to download from
     */
    public String getPreferedBaseURL() {
        return this.baseURL;
    }
    
    /**
     * Returns the full URL used to download the asset from the repository
     * that we prefer to download from. This always returns the base 
     * 
     * @param assetURI The uri of the asset to download
     * @return The full URL of the asset download
     */
    public String getPreferedURL(AssetURI assetURI) {
        return this.baseURL + "/" + assetURI.getRelativePath();
    }
    
    /**
     * Returns an array of base URLs of all known servers that host the content.
     * This always returns null.
     * 
     * @return An array of base repository URLs
     */
     public String[] getAllBaseURLs() {
         return new String[] {
             this.getPreferedBaseURL()
         };
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
         return new String[] {
             this.getPreferedURL(assetURI)
         };
     }
}
