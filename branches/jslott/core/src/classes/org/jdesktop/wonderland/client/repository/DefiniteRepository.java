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

/**
 * The DefiniteRepository class represents content that comes from an asset
 * server defined by the absolute uri of the asset.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@InternalAPI
public class DefiniteRepository implements Repository {

    /** Default constructor */
    public DefiniteRepository() {}
    
    /**
     * Factory method to return a new instance of DefiniteRepository
     * 
     * @return A new DefiniteRepository class
     */
    public static final DefiniteRepository getDefiniteRepository() {
        return new DefiniteRepository();
    }
    
    /**
     * Returns the base URL of the repository we prefer to download from. This
     * always return null.
     * 
     * @return The repository that is preferred to download from
     */
    public String getPreferedBaseURL() {
        return null;
    }
    
    /**
     * Returns the full URL used to download the asset from the repository
     * that we prefer to download from. This always returns the uri.
     * 
     * @param assetURI The uri of the asset to download
     * @return The full URL of the asset download
     */
    public String getPreferedURL(AssetURI assetURI) {
        try {
            return assetURI.getURI().toURL().toExternalForm();
        } catch (java.net.MalformedURLException excp) {
            // log and error, do something!
            return null;
        }
    }
    
    /**
     * Returns an array of base URLs of all known servers that host the content.
     * This always returns null.
     * 
     * @return An array of base repository URLs
     */
     public String[] getAllBaseURLs() {
         return null;
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
